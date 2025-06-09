package mg.hrms.controllers;

import jakarta.servlet.http.HttpSession;
import mg.hrms.models.SalarySummary;
import mg.hrms.models.User;
import mg.hrms.services.SalarySummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/salary-summary")
public class SalarySummaryController {

    private static final Logger logger = LoggerFactory.getLogger(SalarySummaryController.class);
    private final SalarySummaryService salarySummaryService;

    public SalarySummaryController(SalarySummaryService salarySummaryService) {
        this.salarySummaryService = salarySummaryService;
    }

    @GetMapping
    public String getMonthlySalarySummary(Model model, HttpSession session,
                                          @RequestParam(required = false) String month,
                                          @RequestParam(required = false) String year) {
        logger.info("Fetching salary summary for month: {}, year: {}", month, year);
        try {
            User user = validateUser(session);
            List<SalarySummary> summaries = salarySummaryService.getMonthlySalarySummary(user, month, year);

            model.addAttribute("pageTitle", "Monthly Salary Summary");
            model.addAttribute("contentPage", "pages/employees/salary-summary.jsp");
            model.addAttribute("summaries", summaries);
            model.addAttribute("selectedMonth", month);
            model.addAttribute("selectedYear", year);
            logger.info("Successfully loaded salary summary for user: {}", user.getFullName());
            return "layout/main-layout";
        } catch (Exception e) {
            logger.error("Failed to load salary summary: {}", e.getMessage());
            model.addAttribute("error", "Failed to load salary summary: " + e.getMessage());
            return "layout/main-layout";
        }
    }

    private User validateUser(HttpSession session) throws Exception {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            logger.warn("Unauthenticated access attempt to salary summary, redirecting to login");
            throw new Exception("User not authenticated");
        }
        return user;
    }
}