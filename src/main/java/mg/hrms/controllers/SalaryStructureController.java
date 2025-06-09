package mg.hrms.controllers;

import jakarta.servlet.http.HttpSession;
import mg.hrms.models.SalaryStructure;
import mg.hrms.models.User;
import mg.hrms.services.SalaryStructureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/salary-structures")
public class SalaryStructureController {

    private static final Logger logger = LoggerFactory.getLogger(SalaryStructureController.class);
    private final SalaryStructureService salaryStructureService;

    public SalaryStructureController(SalaryStructureService salaryStructureService) {
        this.salaryStructureService = salaryStructureService;
    }

    @GetMapping
    public String getAllSalaryStructures(Model model, HttpSession session) {
        logger.info("Fetching all salary structures");
        try {
            User user = validateUser(session);
            List<SalaryStructure> structures = salaryStructureService.getAll(user);

            model.addAttribute("pageTitle", "Salary Structures");
            model.addAttribute("contentPage", "pages/salary-structures/structure-list.jsp");
            model.addAttribute("structures", structures);
            logger.info("Successfully loaded {} salary structures", structures.size());
            return "layout/main-layout";
        } catch (Exception e) {
            logger.error("Failed to load salary structures: {}", e.getMessage());
            model.addAttribute("error", "Failed to load salary structures: " + e.getMessage());
            return "layout/main-layout";
        }
    }

    @GetMapping("/view")
    public String viewSalaryStructure(Model model, HttpSession session, @RequestParam String name) {
        logger.info("Viewing salary structure: {}", name);
        try {
            User user = validateUser(session);
            SalaryStructure structure = salaryStructureService.getByName(user, name);

            model.addAttribute("pageTitle", "Salary Structure - " + name);
            model.addAttribute("contentPage", "pages/salary-structures/structure-view.jsp");
            model.addAttribute("structure", structure);
            logger.info("Successfully loaded salary structure: {}", name);
            return "layout/main-layout";
        } catch (Exception e) {
            logger.error("Failed to load salary structure: {} - Error: {}", name, e.getMessage());
            model.addAttribute("error", "Failed to load salary structure: " + e.getMessage());
            return "redirect:/salary-structures";
        }
    }

    private User validateUser(HttpSession session) throws Exception {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            logger.warn("Unauthenticated access attempt to salary structures, redirecting to login");
            throw new Exception("User not authenticated");
        }
        return user;
    }
}