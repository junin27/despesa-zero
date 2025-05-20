package junin.com.controle_despesas.controller;

import junin.com.controle_despesas.model.User;
import junin.com.controle_despesas.service.JsonUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private JsonUserService userService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {

        model.addAttribute("user", new User());

        return "register";
        
    }

    @PostMapping("/register")
    public String doRegister(

            @ModelAttribute User user,
            RedirectAttributes redirectAttrs
    )
    {
        try {

            userService.register(user);
            redirectAttrs.addFlashAttribute("success", "Conta criada com sucesso! Faça login.");

            return "redirect:/login";

        }

        catch (IllegalArgumentException e) {

            redirectAttrs.addFlashAttribute("error", e.getMessage());

            return "redirect:/register";

        }

    }

    @GetMapping("/login")
    public String showLoginForm() {

        return "login";

    }

}
