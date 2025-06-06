package mg.hrms.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import mg.hrms.models.User;
import mg.hrms.models.SalarySummary;
import mg.hrms.services.SalarySummaryService;

import java.util.List;

@Controller
@RequestMapping("/salary-summary")
public class SalarySummaryController {

    private final SalarySummaryService salarySummaryService;

    @Autowired
    public SalarySummaryController(SalarySummaryService salarySummaryService) {
        this.salarySummaryService = salarySummaryService;
    }

    @GetMapping
    public String getMonthlySalarySummary(Model model, HttpSession session,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String year) {
        try {
            model.addAttribute("pageTitle", "Monthly Salary Summary");
            model.addAttribute("contentPage", "pages/employees/salary-summary.jsp");

            User connectedUser = (User) session.getAttribute("user");
            if (connectedUser == null) {
                throw new Exception("User not authenticated");
            }

            List<SalarySummary> summaries = salarySummaryService.getMonthlySalarySummary(connectedUser, month, year);
            model.addAttribute("summaries", summaries);
            model.addAttribute("selectedMonth", month);
            model.addAttribute("selectedYear", year);

            return "layout/main-layout";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to load salary summary: " + e.getMessage());
            return "layout/main-layout";
        }
    }
}
