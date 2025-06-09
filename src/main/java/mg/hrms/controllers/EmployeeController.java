package mg.hrms.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mg.hrms.models.Employee;
import mg.hrms.models.SalarySlip;
import mg.hrms.models.User;
import mg.hrms.models.args.EmployeeFilterArgs;
import mg.hrms.services.CompanyService;
import mg.hrms.services.EmployeeService;
import mg.hrms.services.ExportPdfService;
import mg.hrms.services.GenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService employeeService;
    private final GenderService genderService;
    private final CompanyService companyService;
    private final ExportPdfService exportPdfService;

    public EmployeeController(EmployeeService employeeService, GenderService genderService, 
                              CompanyService companyService, ExportPdfService exportPdfService) {
        this.employeeService = employeeService;
        this.genderService = genderService;
        this.companyService = companyService;
        this.exportPdfService = exportPdfService;
    }

    @GetMapping
    public String getAllEmployees(Model model, HttpSession session,
                                  @RequestParam(required = false) String name,
                                  @RequestParam(required = false) Integer minAge,
                                  @RequestParam(required = false) Integer maxAge,
                                  @RequestParam(required = false) String gender,
                                  @RequestParam(required = false) String company,
                                  @RequestParam(required = false) String status) {
        logger.info("Fetching all employees with filters: name={}, minAge={}, maxAge={}, gender={}, company={}, status={}", 
                    name, minAge, maxAge, gender, company, status);
        try {
            User user = validateUser(session);
            model.addAttribute("pageTitle", "Employee Management");
            model.addAttribute("contentPage", "pages/employees/employee-list.jsp");
            model.addAttribute("companies", companyService.getAll(user));
            model.addAttribute("genders", genderService.getAll(user));

            EmployeeFilterArgs filter = new EmployeeFilterArgs(
                    name, minAge != null ? minAge : 0, maxAge != null ? maxAge : 0, company, gender, status);
            model.addAttribute("employees", employeeService.getAll(user, filter));
            logger.info("Successfully loaded employees list");
            return "layout/main-layout";
        } catch (Exception e) {
            logger.error("Failed to load employees: {}", e.getMessage());
            model.addAttribute("error", "Failed to load employees: " + e.getMessage());
            return "layout/main-layout";
        }
    }

    @PostMapping("/filter")
    public String filterEmployees(Model model, HttpSession session,
                                  @RequestParam(required = false) String name,
                                  @RequestParam(required = false) Integer minAge,
                                  @RequestParam(required = false) Integer maxAge,
                                  @RequestParam(required = false) String gender,
                                  @RequestParam(required = false) String company,
                                  @RequestParam(required = false) String status) {
        logger.info("Filtering employees with: name={}, minAge={}, maxAge={}, gender={}, company={}, status={}", 
                    name, minAge, maxAge, gender, company, status);
        try {
            model.addAttribute("pageTitle", "Employee Management - Filtered");
            model.addAttribute("contentPage", "pages/employees/employee-list.jsp");
            return getAllEmployees(model, session, name, minAge, maxAge, gender, company, status);
        } catch (Exception e) {
            logger.error("Failed to filter employees: {}", e.getMessage());
            model.addAttribute("error", "Failed to filter employees: " + e.getMessage());
            return "layout/main-layout";
        }
    }

    @GetMapping("/view")
    public String viewEmployee(Model model, HttpSession session, @RequestParam String employeeId) {
        logger.info("Viewing employee details for ID: {}", employeeId);
        try {
            User user = validateUser(session);
            Employee employee = employeeService.getById(user, employeeId);
            List<SalarySlip> salaries = employeeService.getEmployeeSalaries(user, employeeId);

            model.addAttribute("pageTitle", "Employee Details");
            model.addAttribute("contentPage", "pages/employees/employee-view.jsp");
            model.addAttribute("employee", employee);
            model.addAttribute("salaries", salaries);
            logger.info("Successfully loaded employee details for ID: {}", employeeId);
            return "layout/main-layout";
        } catch (Exception e) {
            logger.error("Failed to load employee details for ID: {} - Error: {}", employeeId, e.getMessage());
            model.addAttribute("error", "Failed to load employee details: " + e.getMessage());
            return "redirect:/employees";
        }
    }

    @GetMapping("/payslip")
    public String showPayslipForm(Model model, HttpSession session, @RequestParam String employeeId) {
        logger.info("Showing payslip form for employee ID: {}", employeeId);
        try {
            User user = validateUser(session);
            Employee employee = employeeService.getById(user, employeeId);
            List<SalarySlip> salaries = employeeService.getEmployeeSalaries(user, employeeId);

            model.addAttribute("pageTitle", "View Payslip");
            model.addAttribute("contentPage", "pages/employees/payslip/payslip-form.jsp");
            model.addAttribute("employee", employee);
            model.addAttribute("salaries", salaries);
            logger.info("Successfully loaded payslip form for employee ID: {}", employeeId);
            return "layout/main-layout";
        } catch (Exception e) {
            logger.error("Failed to load payslip form for employee ID: {} - Error: {}", employeeId, e.getMessage());
            model.addAttribute("error", "Failed to load payslip form: " + e.getMessage());
            return "redirect:/employees/view?employeeId=" + employeeId;
        }
    }

    @PostMapping("/payslip")
    public String viewPayslip(Model model, HttpSession session, 
                              @RequestParam String employeeId, @RequestParam String payslipId) {
        logger.info("Viewing payslip ID: {} for employee ID: {}", payslipId, employeeId);
        try {
            User user = validateUser(session);
            SalarySlip payslip = employeeService.getPayslipById(user, payslipId);
            Employee employee = employeeService.getById(user, employeeId);

            model.addAttribute("pageTitle", "Payslip - " + (payslip.getPostingDate() != null ? payslip.getPostingDate() : "N/A"));
            model.addAttribute("contentPage", "pages/employees/payslip/payslip-view.jsp");
            model.addAttribute("payslip", payslip);
            model.addAttribute("employee", employee);
            logger.info("Successfully loaded payslip ID: {} for employee ID: {}", payslipId, employeeId);
            return "layout/main-layout";
        } catch (Exception e) {
            logger.error("Failed to load payslip ID: {} for employee ID: {} - Error: {}", payslipId, employeeId, e.getMessage());
            model.addAttribute("error", "Failed to load payslip: " + e.getMessage());
            return "redirect:/employees/payslip?employeeId=" + employeeId;
        }
    }

    @GetMapping("/payslip/export")
    public void exportPayslipPdf(HttpServletResponse response, HttpSession session, 
                                 @RequestParam String employeeId, @RequestParam String payslipId) throws Exception {
        logger.info("Exporting payslip ID: {} for employee ID: {} as PDF", payslipId, employeeId);
        try {
            User user = validateUser(session);
            SalarySlip payslip = employeeService.getPayslipById(user, payslipId);
            Employee employee = employeeService.getById(user, employeeId);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=payslip_" + payslipId + ".pdf");
            exportPdfService.generatePayslipPdf(employee, payslip, response.getOutputStream());
            logger.info("Successfully exported payslip ID: {} for employee ID: {}", payslipId, employeeId);
        } catch (Exception e) {
            logger.error("Failed to export payslip ID: {} for employee ID: {} - Error: {}", payslipId, employeeId, e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate PDF: " + e.getMessage());
        }
    }

    private User validateUser(HttpSession session) throws Exception {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            logger.warn("Unauthenticated access attempt, redirecting to login");
            throw new Exception("User not authenticated");
        }
        return user;
    }
}