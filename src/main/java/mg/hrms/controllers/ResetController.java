// ResetController.java
package mg.hrms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import mg.hrms.models.User;
import mg.hrms.payload.ResetResult;
import mg.hrms.services.ResetService;

@Controller
@RequestMapping("/reset")
public class ResetController {

    private final ResetService resetService;

    public ResetController(ResetService resetService) {
        this.resetService = resetService;
    }

    @GetMapping
    public String showResetForm(Model model) {
        model.addAttribute("pageTitle", "HRMS Data Reset");
        model.addAttribute("contentPage", "pages/data/reset-form.jsp");
        return "layout/main-layout";
    }

    @PostMapping
    public String resetData(
            @RequestParam(value = "company", required = false) String company,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                throw new Exception("User not authenticated");
            }

            ResetResult result = resetService.processReset(company, user);

            if (result.isSuccess()) {
                redirectAttributes.addFlashAttribute("success", "Data reset successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", result.getMessage());
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Reset failed: " + e.getMessage());
        }

        return "redirect:/reset";
    }
}
