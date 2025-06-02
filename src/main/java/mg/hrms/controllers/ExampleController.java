package mg.hrms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/")
public class ExampleController {

    @GetMapping
    public String dashboard(Model model) {
        // Set page title
        
        model.addAttribute("pageTitle", "Dashboard");
        
        // Set the content page to include
        model.addAttribute("contentPage", "pages/dashboard.jsp");
        
        // Add any additional data for the dashboard
        model.addAttribute("totalUsers", 150);
        model.addAttribute("totalOrders", 1250);
        model.addAttribute("revenue", "$12,500");
        
        return "layout/main-layout";
    }
    
    @GetMapping("/users")
    public String usersList(Model model) {
        model.addAttribute("pageTitle", "User Management");
        model.addAttribute("contentPage", "pages/users/list.jsp");

        // Add success message example
        model.addAttribute("successMessage", "Users loaded successfully!");
        
        return "layout/main-layout";
    }
    
    @GetMapping("/users/create")
    public String createUser(Model model) {
        model.addAttribute("pageTitle", "Add New User");
        model.addAttribute("contentPage", "pages/users/create.jsp");
        
        return "layout/main-layout";
    }
    
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("pageTitle", "Settings");
        model.addAttribute("contentPage", "pages/settings.jsp");
        
        // Example with warning message
        model.addAttribute("warningMessage", "Please backup your data before changing settings.");
        
        // Optional: Add additional CSS
        model.addAttribute("additionalCSS", 
            "<link rel=\"stylesheet\" href=\"/css/custom-settings.css\">");
            
        // Optional: Add additional JS
        model.addAttribute("additionalJS", 
            "<script src=\"/js/settings.js\"></script>");
        
        return "layout/main-layout";
    }
}