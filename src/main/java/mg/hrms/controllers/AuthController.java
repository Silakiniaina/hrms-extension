package mg.hrms.controllers;

import jakarta.servlet.http.HttpSession;
import mg.hrms.models.User;
import mg.hrms.services.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public String showLoginPage(Model model) {
        logger.debug("Displaying login page");
        model.addAttribute("pageTitle", "Login");
        return "pages/login";
    }

    @PostMapping
    public String handleLogin(@RequestParam String username, @RequestParam String password, 
                              HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        logger.info("Processing login attempt for user: {}", username);
        try {
            if (username == null || password == null || username.isBlank() || password.isBlank()) {
                logger.warn("Invalid login attempt: username or password is empty");
                model.addAttribute("error", "Username and password are required");
                model.addAttribute("pageTitle", "Login");
                return "pages/login";
            }

            User user = authService.login(username, password);
            session.setAttribute("user", user);
            redirectAttributes.addAttribute("success", "User: " + user.getFullName() + " successfully connected");
            logger.info("User {} logged in successfully", user.getFullName());
            return "redirect:/home";
        } catch (Exception e) {
            logger.error("Login failed for user: {} - Error: {}", username, e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pageTitle", "Login");
            return "pages/login";
        }
    }
}
