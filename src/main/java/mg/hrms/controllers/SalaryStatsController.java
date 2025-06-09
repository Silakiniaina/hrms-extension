package mg.hrms.controllers;

import jakarta.servlet.http.HttpSession;
import mg.hrms.models.SalaryStats;
import mg.hrms.models.User;
import mg.hrms.services.SalaryStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/salary-stats")
public class SalaryStatsController {

    private static final Logger logger = LoggerFactory.getLogger(SalaryStatsController.class);
    private final SalaryStatsService salaryStatsService;

    public SalaryStatsController(SalaryStatsService salaryStatsService) {
        this.salaryStatsService = salaryStatsService;
    }

    @GetMapping
    public String getMonthlySalaryStats(Model model, HttpSession session,
                                       @RequestParam(required = false) String year) {
        logger.info("Fetching salary statistics for year: {}", year);
        try {
            User user = validateUser(session);

            // If no year is provided, use current year
            if (year == null || year.isEmpty()) {
                year = String.valueOf(LocalDate.now().getYear());
            }

            List<SalaryStats> stats = salaryStatsService.getMonthlySalaryStats(user, year);
            List<String> availableYears = salaryStatsService.getAvailableYears(user);

            model.addAttribute("pageTitle", "Salary Statistics");
            model.addAttribute("contentPage", "pages/stats/salary-stats.jsp");
            model.addAttribute("stats", stats);
            model.addAttribute("availableYears", availableYears);
            model.addAttribute("selectedYear", year);

            // Calculate yearly totals
            double yearlyGrossPay = stats.stream().mapToDouble(s -> s.getTotalGrossPay() != null ? s.getTotalGrossPay() : 0.0).sum();
            double yearlyNetPay = stats.stream().mapToDouble(s -> s.getTotalNetPay() != null ? s.getTotalNetPay() : 0.0).sum();
            double yearlyDeductions = stats.stream().mapToDouble(s -> s.getTotalDeductions() != null ? s.getTotalDeductions() : 0.0).sum();

            model.addAttribute("yearlyGrossPay", yearlyGrossPay);
            model.addAttribute("yearlyNetPay", yearlyNetPay);
            model.addAttribute("yearlyDeductions", yearlyDeductions);

            logger.info("Successfully loaded salary statistics for year: {} with {} months", year, stats.size());
            return "layout/main-layout";
        } catch (Exception e) {
            logger.error("Failed to load salary statistics: {}", e.getMessage());
            model.addAttribute("pageTitle", "Salary Statistics");
            model.addAttribute("contentPage", "pages/stats/salary-stats.jsp");
            model.addAttribute("error", "Failed to load salary statistics: " + e.getMessage());
            return "layout/main-layout";
        }
    }

    private User validateUser(HttpSession session) throws Exception {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            logger.warn("Unauthenticated access attempt to salary statistics, redirecting to login");
            throw new Exception("User not authenticated");
        }
        return user;
    }
}
