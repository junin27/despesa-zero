package junin.com.controle_despesas.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import junin.com.controle_despesas.model.Category;
import junin.com.controle_despesas.model.Expense;
import junin.com.controle_despesas.model.User;
import junin.com.controle_despesas.repository.CategoryJsonRepository;
import junin.com.controle_despesas.repository.UserJsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfExportService {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserJsonRepository userRepo;

    @Autowired
    private CategoryJsonRepository catRepo;

    public ByteArrayInputStream gerarPdf(Authentication auth) throws Exception {

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

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


        Font bold = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font title = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);

        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();

        Paragraph titulo = new Paragraph("Relatório de Despesas - " + mesAtual, title);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(new Paragraph(" "));

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Informações Pessoais do Usuário:", bold));
        document.add(new Paragraph("• Nome: " + user.getName()));
        document.add(new Paragraph("• Email: " + user.getEmail()));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Valores de Despesas Totais por Categoria:", bold));

        for (Map.Entry<Category, BigDecimal> entry : totaisPorCategoria.entrySet()) {

            double pct = porcentagens.get(entry.getKey());
            document.add(new Paragraph("• " + entry.getKey().getName() +
                    ": R$ " + entry.getValue() + " (" + String.format("%.1f", pct) + "%)"));

        }

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Resumo Estatístico:", bold));
        document.add(new Paragraph("• Valor Total de Despesas: R$ " + valorTotal));
        document.add(new Paragraph("• Valor Médio de Despesas: R$ " + valorMedio));
        document.add(new Paragraph("• Quantidade Total de Despesas: " + quantidadeTotal));
        document.add(new Paragraph("• Quantidade Média de Despesas: " + String.format("%.2f", mediaPorCategoria)));

        if (maior != null)

            document.add(new Paragraph("• Maior Valor de Despesa por Categoria: " + maior.getKey().getName() + " – R$ " + maior.getValue()));

        if (menor != null)

            document.add(new Paragraph("• Menor Valor de Despesa por Categoria: " + menor.getKey().getName() + " – R$ " + menor.getValue()));

        document.close();

        return new ByteArrayInputStream(out.toByteArray());

    }
}
