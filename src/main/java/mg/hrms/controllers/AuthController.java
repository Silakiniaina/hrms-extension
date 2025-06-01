package mg.hrms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {
    
    /* -------------------------------------------------------------------------- */
    /*      Display the login page when the user accesses the /auth endpoint      */
    /* -------------------------------------------------------------------------- */
    @GetMapping
    public String showLoginPage() {
        return "pages/login";
    }
}
