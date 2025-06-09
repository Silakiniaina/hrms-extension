package mg.hrms.controllers;

import jakarta.servlet.http.HttpSession;
import mg.hrms.models.User;
import mg.hrms.payload.ResetResult;
import mg.hrms.services.ResetService;
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
@RequestMapping("/reset")
public class ResetController {

    private static final Logger logger = LoggerFactory.getLogger(ResetController.class);
    private final ResetService resetService;

    public ResetController(ResetService resetService) {
        this.resetService = resetService;
    }

    @GetMapping
    public String showResetForm(Model model, HttpSession session) {
        logger.debug("Displaying reset form");
        try {
            validateUser(session);
            model.addAttribute("pageTitle", "HRMS Data Reset");
            model.addAttribute("contentPage", "pages/data/reset-form.jsp");
            return "layout/main-layout";
        } catch (Exception e) {
            logger.error("Failed to display reset form: {}", e.getMessage());
            model.addAttribute("error", "Access denied: " + e.getMessage());
            return "redirect:/auth";
        }
    }

    @PostMapping
    public String resetData(
            @RequestParam(value = "company", required = false) String company,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        logger.info("Initiating data reset for company: {}", company != null ? company : "All");
        try {
            User user = validateUser(session);
            ResetResult result = resetService.processReset(company, user);

            if (result.isSuccess()) {
                redirectAttributes.addFlashAttribute("success", result.getMessage());
                logger.info("Data reset successful for company: {}", company != null ? company : "All");
            } else {
                redirectAttributes.addFlashAttribute("error", result.getMessage());
                logger.warn("Data reset failed: {}", result.getMessage());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Reset failed: " + e.getMessage());
            logger.error("Data reset failed: {}", e.getMessage(), e);
        }
        return "redirect:/reset";
    }

    private User validateUser(HttpSession session) throws Exception {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            logger.warn("Unauthenticated access attempt to reset data");
            throw new Exception("User not authenticated");
        }
        return user;
    }
}
