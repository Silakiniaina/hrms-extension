package mg.hrms.controllers;

import jakarta.servlet.http.HttpSession;
import mg.hrms.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/home")
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @GetMapping
    public String showHomePage(Model model, HttpSession session, 
                               @RequestParam(value = "success", required = false) String success) {
        logger.debug("Displaying home page");
        User user = (User) session.getAttribute("user");
        if (user == null) {
            logger.warn("Unauthenticated access to home page, redirecting to login");
            return "redirect:/auth";
        }

        model.addAttribute("pageTitle", "Home");
        model.addAttribute("contentPage", "pages/home.jsp");
        if (success != null && !success.isBlank()) {
            model.addAttribute("success", success);
        }
        logger.info("Home page displayed for user: {}", user.getFullName());
        return "layout/main-layout";
    }
}