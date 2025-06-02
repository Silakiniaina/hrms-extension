package mg.hrms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import mg.hrms.models.User;
import mg.hrms.services.AuthService;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    /* -------------------------------------------------------------------------- */
    /*                                 Constructor                                */
    /* -------------------------------------------------------------------------- */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /* -------------------------------------------------------------------------- */
    /*      Display the login page when the user accesses the /auth endpoint      */
    /* -------------------------------------------------------------------------- */
    @GetMapping
    public String showLoginPage() {
        return "pages/login";
    }

    /* -------------------------------------------------------------------------- */
    /*         Handle login requests when the user submits the login form         */
    /* -------------------------------------------------------------------------- */
    @PostMapping
    public String handleLogin(@RequestParam String username, @RequestParam String password, HttpSession session,
            Model model) {
        try {
            if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
                model.addAttribute("error", "Username and password are required");
                return "pages/login";
            }

            User user = authService.login(username, password);
            session.setAttribute("sid", user.getSid());
            session.setAttribute("user", user);
            String success =  "User : "+user.getFullName() +" successfully connected";
            
            return "redirect:/home?success="+success;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "pages/login";
        }
    }
}
