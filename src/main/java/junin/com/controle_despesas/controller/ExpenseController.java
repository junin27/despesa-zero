package junin.com.controle_despesas.controller;

import junin.com.controle_despesas.model.Category;
import junin.com.controle_despesas.model.Expense;
import junin.com.controle_despesas.model.User;
import junin.com.controle_despesas.repository.CategoryJsonRepository;
import junin.com.controle_despesas.repository.UserJsonRepository;
import junin.com.controle_despesas.service.ExpenseService;
import junin.com.controle_despesas.service.PdfExportService;
import junin.com.controle_despesas.service.WordExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/expense")
public class ExpenseController {

    @Autowired
    private ExpenseService service;

    @Autowired
    private UserJsonRepository userRepo;

    @Autowired
    private CategoryJsonRepository catRepo;

    @Autowired
    private PdfExportService pdfExportService;

    @Autowired
    private WordExportService wordExportService;

    @GetMapping({"", "/", "/list"})
    public String list(Model model, Authentication auth) {

        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        List<Expense> my = service.listByUser(user.getId());
        model.addAttribute("expenses", my);

        List<Category> categories = catRepo.findByUserId(user.getId());
        Map<Long, Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));
        model.addAttribute("categoryMap", categoryMap);

        YearMonth ym = YearMonth.now();
        String currentMonth = ym.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"))
                + " " + ym.getYear();
        model.addAttribute("currentMonth", currentMonth);

        Map<Category, BigDecimal> total = my.stream()
                .filter(d -> categoryMap.containsKey(d.getCategoryId()))
                .collect(Collectors.groupingBy(
                        d -> categoryMap.get(d.getCategoryId()),
                        Collectors.mapping(Expense::getValue,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        model.addAttribute("totalsByCategory", total);

        return "expense-list";

    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, Authentication auth) {

        Expense expense = service.searchById(id);
        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        model.addAttribute("expense", expense);
        model.addAttribute("categories", catRepo.findByUserId(user.getId()));

        return "expense-form";

    }

    @GetMapping("/new")
    public String newExpense(Model model, Authentication auth) {

        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        model.addAttribute("expense", new Expense());
        model.addAttribute("categories", catRepo.findByUserId(user.getId()));

        return "expense-form";

    }

    @PostMapping("/save")
    public String save(@ModelAttribute Expense expense, Authentication auth) {

        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        expense.setUserId(user.getId());
        service.save(expense);

        return "redirect:/expense";

    }

    @GetMapping("/remove/{id}")
    public String remove(@PathVariable Long id) {

        service.delete(id);

        return "redirect:/expense";

    }

    @GetMapping("/report")
    public String report(Model model, Authentication auth) {

        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        List<Expense> my = service.listByUser(user.getId());
        model.addAttribute("expenses", my);

        List<Category> categories = catRepo.findByUserId(user.getId());
        Map<Long, Category> categoriaMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        Map<Category, BigDecimal> totalsByCategory = my.stream()
                .filter(d -> categoriaMap.containsKey(d.getCategoryId()))
                .collect(Collectors.groupingBy(
                        d -> categoriaMap.get(d.getCategoryId()),
                        Collectors.mapping(Expense::getValue,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        model.addAttribute("totaisPorCategoria", totalsByCategory);

        YearMonth ym = YearMonth.now();
        String currentMonth = ym.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"))
                + " " + ym.getYear();
        model.addAttribute("currentMonth", currentMonth);

        BigDecimal totalValue = totalsByCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageValue = my.isEmpty() ? BigDecimal.ZERO :
                totalValue.divide(BigDecimal.valueOf(my.size()), 2, BigDecimal.ROUND_HALF_UP);

        int quantityTotal = my.size();

        long categoriesWithExpenses = totalsByCategory.size();
        double averageByCategory = categoriesWithExpenses > 0
                ? (double) quantityTotal / categoriesWithExpenses
                : 0;

        Map.Entry<Category, BigDecimal> bigger = totalsByCategory.entrySet()
                .stream().max(Map.Entry.comparingByValue()).orElse(null);

        Map.Entry<Category, BigDecimal> smaller = totalsByCategory.entrySet()
                .stream().min(Map.Entry.comparingByValue()).orElse(null);

        Map<Category, Double> percentages = totalsByCategory.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().divide(totalValue, 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100)).doubleValue()
                ));

        model.addAttribute("totalValue", totalValue);
        model.addAttribute("averageValue", averageValue);
        model.addAttribute("quantityTotal", quantityTotal);
        model.addAttribute("averageByCategory", averageByCategory);
        model.addAttribute("bigger", bigger);
        model.addAttribute("smaller", smaller);
        model.addAttribute("percentages", percentages);

        return "expense-report";

    }

    @PostMapping("/report/pdf")
    public ResponseEntity<InputStreamResource> exportPdf(Authentication auth) throws Exception {

        ByteArrayInputStream pdfStream = pdfExportService.gerarPdf(auth);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));

    }

    @PostMapping("/report/word")
    public ResponseEntity<InputStreamResource> exportWord(Authentication auth) throws Exception {

        ByteArrayInputStream wordStream = wordExportService.gerarWord(auth);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio.docx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(new InputStreamResource(wordStream));

    }
}
