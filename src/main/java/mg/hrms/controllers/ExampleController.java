package mg.hrms.controllers;

import mg.hrms.models.Breadcrumb;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/")
public class ExampleController {

    @GetMapping("/")
    public String dashboard(Model model) {
        // Set page title
        
        model.addAttribute("pageTitle", "Dashboard");
        
        // Set the content page to include
        model.addAttribute("contentPage", "pages/dashboard.jsp");
        
        // Optional: Add custom breadcrumbs (if not provided, default will be used)
        List<Breadcrumb> breadcrumbs = Breadcrumb.create("Dashboard");
        model.addAttribute("breadcrumbs", breadcrumbs);
        
        // Add any additional data for the dashboard
        model.addAttribute("totalUsers", 150);
        model.addAttribute("totalOrders", 1250);
        model.addAttribute("revenue", "$12,500");
        
        return "layout";
    }
    
    @GetMapping("/users")
    public String usersList(Model model) {
        model.addAttribute("pageTitle", "User Management");
        model.addAttribute("contentPage", "pages/users/list.jsp");
        
        // Create breadcrumbs with URLs
        List<Breadcrumb> breadcrumbs = Breadcrumb.createWithUrls(
            "User Management", "/users",
            "All Users"  // Last item is active (no URL)
        );
        model.addAttribute("breadcrumbs", breadcrumbs);
        
        // Add success message example
        model.addAttribute("successMessage", "Users loaded successfully!");
        
        return "layout";
    }
    
    @GetMapping("/users/create")
    public String createUser(Model model) {
        model.addAttribute("pageTitle", "Add New User");
        model.addAttribute("contentPage", "pages/users/create.jsp");
        
        List<Breadcrumb> breadcrumbs = Breadcrumb.createWithUrls(
            "User Management", "/users",
            "All Users", "/users",
            "Add New User"
        );
        model.addAttribute("breadcrumbs", breadcrumbs);
        
        return "layout";
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
        
        return "layout";
    }
}