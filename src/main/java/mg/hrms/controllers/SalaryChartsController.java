package mg.hrms.controllers;

import jakarta.servlet.http.HttpSession;
import mg.hrms.models.SalaryStats;
import mg.hrms.models.SalaryChartData;
import mg.hrms.models.User;
import mg.hrms.services.SalaryChartsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/salary-charts")
public class SalaryChartsController {

    private static final Logger logger = LoggerFactory.getLogger(SalaryChartsController.class);
    private final SalaryChartsService salaryChartsService;

    public SalaryChartsController(SalaryChartsService salaryChartsService) {
        this.salaryChartsService = salaryChartsService;
    }

    @GetMapping
    public String getSalaryCharts(Model model, HttpSession session,
                                 @RequestParam(required = false) String startYear,
                                 @RequestParam(required = false) String endYear) {
        logger.info("Fetching salary charts for period: {} to {}", startYear, endYear);
        try {
            User user = validateUser(session);

            // Set default date range if not provided
            if (startYear == null || startYear.isEmpty()) {
                startYear = String.valueOf(LocalDate.now().getYear() - 2);
            }
            if (endYear == null || endYear.isEmpty()) {
                endYear = String.valueOf(LocalDate.now().getYear());
            }

            List<SalaryStats> stats = salaryChartsService.getSalaryStatsForPeriod(user, startYear, endYear);
            List<String> availableYears = salaryChartsService.getAvailableYears(user);

            model.addAttribute("pageTitle", "Salary Evolution Charts");
            model.addAttribute("contentPage", "pages/stats/salary-charts.jsp");
            model.addAttribute("stats", stats);
            model.addAttribute("availableYears", availableYears);
            model.addAttribute("startYear", startYear);
            model.addAttribute("endYear", endYear);

            logger.info("Successfully loaded salary charts for period: {} to {} with {} data points", 
                       startYear, endYear, stats.size());
            return "layout/main-layout";
        } catch (Exception e) {
            logger.error("Failed to load salary charts: {}", e.getMessage());
            model.addAttribute("pageTitle", "Salary Evolution Charts");
            model.addAttribute("contentPage", "pages/stats/salary-charts.jsp");
            model.addAttribute("error", "Failed to load salary charts: " + e.getMessage());
            return "layout/main-layout";
        }
    }

    @GetMapping("/data")
    @ResponseBody
    public SalaryChartData getSalaryChartData(HttpSession session,
                                             @RequestParam(required = false) String startYear,
                                             @RequestParam(required = false) String endYear) {
        logger.info("Fetching salary chart data API for period: {} to {}", startYear, endYear);
        try {
            User user = validateUser(session);

            // Set default date range if not provided
            if (startYear == null || startYear.isEmpty()) {
                startYear = String.valueOf(LocalDate.now().getYear() - 2);
            }
            if (endYear == null || endYear.isEmpty()) {
                endYear = String.valueOf(LocalDate.now().getYear());
            }

            SalaryChartData chartData = salaryChartsService.getSalaryChartData(user, startYear, endYear);
            logger.info("Successfully generated chart data with {} data points", 
                       chartData.getLabels().size());
            return chartData;
        } catch (Exception e) {
            logger.error("Failed to generate salary chart data: {}", e.getMessage());
            return new SalaryChartData(); // Return empty data
        }
    }

    @GetMapping("/components-data")
    @ResponseBody
    public SalaryChartData getSalaryComponentsChartData(HttpSession session,
                                                       @RequestParam(required = false) String startYear,
                                                       @RequestParam(required = false) String endYear) {
        logger.info("Fetching salary components chart data API for period: {} to {}", startYear, endYear);
        try {
            User user = validateUser(session);

            // Set default date range if not provided
            if (startYear == null || startYear.isEmpty()) {
                startYear = String.valueOf(LocalDate.now().getYear() - 2);
            }
            if (endYear == null || endYear.isEmpty()) {
                endYear = String.valueOf(LocalDate.now().getYear());
            }

            SalaryChartData chartData = salaryChartsService.getSalaryComponentsChartData(user, startYear, endYear);
            logger.info("Successfully generated components chart data with {} datasets", 
                       chartData.getDatasets().size());
            return chartData;
        } catch (Exception e) {
            logger.error("Failed to generate salary components chart data: {}", e.getMessage());
            return new SalaryChartData(); // Return empty data
        }
    }

    private User validateUser(HttpSession session) throws Exception {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            logger.warn("Unauthenticated access attempt to salary charts, redirecting to login");
            throw new Exception("User not authenticated");
        }
        return user;
    }
}
