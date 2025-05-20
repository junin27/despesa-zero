package junin.com.controle_despesas.controller;

import junin.com.controle_despesas.repository.UserJsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private UserJsonRepository userRepository;

    @GetMapping({"/", "/home"})
    public String home(Model model,
                       @AuthenticationPrincipal UserDetails principal) {

        if (principal != null) {

            String email = principal.getUsername();
            userRepository.findByEmail(email)
                    .ifPresent(u -> model.addAttribute("user", u));

        }

        return "home";

    }
}
