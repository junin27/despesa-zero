package junin.com.controle_despesas.controller;

import junin.com.controle_despesas.model.Category;
import junin.com.controle_despesas.model.User;
import junin.com.controle_despesas.repository.UserJsonRepository;
import junin.com.controle_despesas.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService service;

    @Autowired
    private UserJsonRepository userRepo;

    @GetMapping({"", "/"})
    public String list(Model model, Authentication auth) {

        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        model.addAttribute("categories", service.listByUser(user.getId()));

        return "category-list";

    }

    @GetMapping("/new")
    public String newCategory(Model model) {

        model.addAttribute("category", new Category());
        model.addAttribute("isNewForm", true);

        return "category-form";

    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, Authentication auth, RedirectAttributes redirectAttrs) {

        try {

            User user = userRepo.findByEmail(auth.getName()).orElseThrow();
            Category cat = service.buscarPorId(user.getId(), id)
                    .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada: " + id));
            model.addAttribute("category", cat);
            model.addAttribute("isNewForm", false);

            return "category-form";

        }

        catch (IllegalArgumentException e) {

            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/categories";

        }
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Category category, Authentication auth, RedirectAttributes redirectAttrs) {

        try {

            User user = userRepo.findByEmail(auth.getName()).orElseThrow();

            if (category.getId() == null) {

                service.create(category, user.getId());
                redirectAttrs.addFlashAttribute("successMessage", "Categoria criada com sucesso!");

            }

            else {

                service.update(user.getId(), category.getId(), category);
                redirectAttrs.addFlashAttribute("successMessage", "Categoria atualizada com sucesso!");

            }

            return "redirect:/categories";

        }

        catch (Exception e) {

            redirectAttrs.addFlashAttribute("errorMessage", "Erro ao salvar categoria: " + e.getMessage());

            return "redirect:/categories";

        }
    }

    @GetMapping("/remove/{id}")
    public String remove(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttrs) {

        try {

            User user = userRepo.findByEmail(auth.getName()).orElseThrow();
            service.remove(user.getId(), id);
            redirectAttrs.addFlashAttribute("successMessage", "Categoria removida com sucesso!");

        }
        catch (IllegalArgumentException e) {

            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());

        }

        return "redirect:/categories";

    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, RedirectAttributes redirectAttrs) {

        redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());

        return "redirect:/categories";

    }
}