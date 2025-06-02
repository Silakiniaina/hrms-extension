package mg.hrms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/home")
public class HomeController{

    /* -------------------------------------------------------------------------- */
    /*                               Show home page                               */
    /* -------------------------------------------------------------------------- */
    @GetMapping
    public String showHomePage(Model model, @RequestParam("success") String success){
        model.addAttribute("pageTitle", "Home");
        model.addAttribute("contentPage", "pages/home.jsp");
        model.addAttribute(success, success);
        return "layout/main-layout";
    }
}