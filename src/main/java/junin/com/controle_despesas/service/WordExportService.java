package junin.com.controle_despesas.service;

import junin.com.controle_despesas.model.Category;
import junin.com.controle_despesas.model.Expense;
import junin.com.controle_despesas.model.User;
import junin.com.controle_despesas.repository.CategoryJsonRepository;
import junin.com.controle_despesas.repository.UserJsonRepository;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WordExportService {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserJsonRepository userRepo;

    @Autowired
    private CategoryJsonRepository catRepo;

    public ByteArrayInputStream gerarWord(Authentication auth) throws Exception {

        XWPFDocument document = new XWPFDocument();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        List<Expense> expenses = expenseService.listByUser(user.getId());

        List<Category> categories = catRepo.findByUserId(user.getId());
        Map<Long, Category> categoriaMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        Map<Category, BigDecimal> totaisPorCategoria = expenses.stream()
                .filter(d -> categoriaMap.containsKey(d.getCategoryId()))
                .collect(Collectors.groupingBy(
                        d -> categoriaMap.get(d.getCategoryId()),
                        Collectors.mapping(Expense::getValue,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        BigDecimal valorTotal = totaisPorCategoria.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorMedio = expenses.isEmpty() ? BigDecimal.ZERO :
                valorTotal.divide(BigDecimal.valueOf(expenses.size()), 2, BigDecimal.ROUND_HALF_UP);

        int quantidadeTotal = expenses.size();
        long categoriasComDespesas = totaisPorCategoria.size();
        double mediaPorCategoria = categoriasComDespesas > 0
                ? (double) quantidadeTotal / categoriasComDespesas
                : 0;

        Map.Entry<Category, BigDecimal> maior = totaisPorCategoria.entrySet()
                .stream().max(Map.Entry.comparingByValue()).orElse(null);

        Map.Entry<Category, BigDecimal> menor = totaisPorCategoria.entrySet()
                .stream().min(Map.Entry.comparingByValue()).orElse(null);

        Map<Category, Double> porcentagens = totaisPorCategoria.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().divide(valorTotal, 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100)).doubleValue()
                ));

        YearMonth ym = YearMonth.now();
        String mesAtual = ym.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR")).toUpperCase() + " " + ym.getYear();

        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun titleRun = title.createRun();
        titleRun.setText("Relatório de Despesas - " + mesAtual);
        titleRun.setBold(true);
        titleRun.setFontSize(20);
        titleRun.addBreak(); // separa do conteúdo abaixo

        XWPFParagraph infoTitulo = document.createParagraph();
        XWPFRun tituloRun = infoTitulo.createRun();
        tituloRun.setText("\nInformações Pessoais do Usuário:");
        tituloRun.setBold(true); // Aplica negrito somente nessa linha

        document.createParagraph().createRun().setText("• Nome: " + user.getName());
        document.createParagraph().createRun().setText("• Email: " + user.getEmail());

        XWPFParagraph catTitle = document.createParagraph();
        XWPFRun catRun = catTitle.createRun();
        catRun.addBreak(); // quebra de linha visual antes do título
        catRun.setText("Valores de Despesas Totais por Categoria:");
        catRun.setBold(true); // aplica negrito somente ao título

        for (Map.Entry<Category, BigDecimal> entry : totaisPorCategoria.entrySet()) {

            double pct = porcentagens.get(entry.getKey());
            document.createParagraph().createRun()
                    .setText("• " + entry.getKey().getName() +
                            ": R$ " + entry.getValue() +
                            " (" + String.format("%.1f", pct) + "%)");

        }

        XWPFParagraph resumo = document.createParagraph();
        resumo.setSpacingBefore(150); // ou 100 para um espaçamento visual leve
        XWPFRun resumoRun = resumo.createRun();
        resumoRun.setText("\nResumo Estatístico:");
        resumoRun.setBold(true); // se quiser destacar

        document.createParagraph().createRun().setText("• Valor Total de Despesas: R$ " + valorTotal);
        document.createParagraph().createRun().setText("• Valor Médio de Despesas: R$ " + valorMedio);
        document.createParagraph().createRun().setText("• Quantidade Total de Despesas: " + quantidadeTotal);
        document.createParagraph().createRun().setText("• Quantidade Média de Despesas: " + String.format("%.2f", mediaPorCategoria));

        if (maior != null)

            document.createParagraph().createRun().setText("• Maior Valor de Despesa por Categoria: " + maior.getKey().getName() + " – R$ " + maior.getValue());

        if (menor != null)

            document.createParagraph().createRun().setText("• Menor Valor de Despesa por Categoria: " + menor.getKey().getName() + " – R$ " + menor.getValue());

        document.write(out);

        return new ByteArrayInputStream(out.toByteArray());

    }
}
