package mg.hrms.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import mg.hrms.models.Employee;
import mg.hrms.models.SalaryComponent;
import mg.hrms.models.SalarySlip;
import mg.hrms.models.User;
import mg.hrms.services.EmployeeService;
import mg.hrms.services.SalaryComponentService;
import mg.hrms.services.SalaryService;

@Controller
@RequestMapping("/salary")
public class SalaryController {

    private static final Logger logger = LoggerFactory.getLogger(SalaryController.class);
    
    private final EmployeeService employeeService;
    private final SalaryService salaryService;
    private final SalaryComponentService salaryComponentService;

    public SalaryController(SalaryComponentService salaryComponentService,EmployeeService employeeService, SalaryService salaryService){
        this.employeeService = employeeService;
        this.salaryService = salaryService;
        this.salaryComponentService = salaryComponentService;
    }

    @GetMapping("/generate")
    public String showSalaryGeneratorForm(Model model, HttpSession session){

        try {
            User user = validateUser(session);
            model.addAttribute("pageTitle", "Salary Generator");
            model.addAttribute("contentPage", "pages/salary/salary-generator.jsp");
            model.addAttribute("employees", employeeService.getAll(user, null));

            return "layout/main-layout";
        } catch (Exception e) {
            logger.error("Failed to show salary generation form: {}", e.getMessage());
            model.addAttribute("error", "Failed to show salary generation form: " + e.getMessage());
            return "layout/main-layout";
        }
        
    }

    @PostMapping("/generate")
    public String generateSalary(
            @RequestParam("employee") String employeeId,
            @RequestParam("startMonth") String startMonth,
            @RequestParam("endMonth") String endMonth,
            @RequestParam(value = "amount", required = false) Double amount,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            User user = validateUser(session);
            
            // Validate input parameters
            if (employeeId == null || employeeId.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select an employee");
                return "redirect:/salary/generate";
            }
            
            if (startMonth == null || startMonth.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a start month");
                return "redirect:/salary/generate";
            }
            
            if (endMonth == null || endMonth.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select an end month");
                return "redirect:/salary/generate";
            }
            
            // Get employee details
            Employee employee = employeeService.getById(user, employeeId);
            if (employee == null) {
                redirectAttributes.addFlashAttribute("error", "Employee not found with ID: " + employeeId);
                return "redirect:/salary/generate";
            }
            
            // Convert date format from yyyy-MM-dd (HTML date input) to dd/MM/yyyy (service format)
            String formattedStartDate = convertDateFormat(startMonth);
            String formattedEndDate = convertDateFormat(endMonth);
            
            // Validate date order
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE);
            LocalDate start = LocalDate.parse(formattedStartDate, formatter);
            LocalDate end = LocalDate.parse(formattedEndDate, formatter);

            if (start.isAfter(end)) {
                redirectAttributes.addFlashAttribute("error", "Start month cannot be after end month");
                return "redirect:/salary/generate";
            }
            
            // Set default amount if not provided
            double salaryAmount = (amount != null && amount > 0) ? amount : 0;
            
            logger.info("Generating salary slips for employee {} from {} to {} with amount {}", 
                       employeeId, formattedStartDate, formattedEndDate, salaryAmount);
            
            // Generate salary slips
            List<SalarySlip> generatedSlips = salaryService.generateSalarySlips(
                employee, 
                formattedStartDate, 
                formattedEndDate, 
                salaryAmount, 
                user
            );
            
            // Prepare success message
            String successMessage;
            if (generatedSlips.isEmpty()) {
                successMessage = "No new salary slips were generated. All salary slips for the selected period already exist.";
            } else {
                successMessage = String.format("Successfully generated %d salary slip(s) for employee %s (%s) from %s to %s", 
                    generatedSlips.size(), 
                    employee.getFullName(), 
                    employee.getEmployeeId(),
                    formatDisplayDate(formattedStartDate),
                    formatDisplayDate(formattedEndDate)
                );
            }
            
            redirectAttributes.addFlashAttribute("success", successMessage);
            logger.info("Salary generation completed successfully for employee {}. Generated {} slips", 
                       employeeId, generatedSlips.size());
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input for salary generation: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Invalid input: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to generate salary for employee {}: {}", employeeId, e.getMessage());
            
            String errorMessage = e.getMessage();
            if (errorMessage.contains("No previous salary slip found") && errorMessage.contains("no amount provided")) {
                errorMessage = "No previous salary slip found for this employee and no amount was provided. Please enter an amount or ensure the employee has at least one salary slip before the start date.";
            }
            redirectAttributes.addFlashAttribute("error", "Failed to generate salary: " + errorMessage);
        }
        
        return "redirect:/salary/generate";
    }


    @GetMapping("/update")
    public String showSalaryUpdateForm(Model model, HttpSession session){

        try {
            User user = validateUser(session);
            model.addAttribute("pageTitle", "Salary Update");
            model.addAttribute("contentPage", "pages/salary/salary-update.jsp");
            model.addAttribute("salaryComponents", salaryComponentService.getAll(user));

            return "layout/main-layout";
        } catch (Exception e) {
            logger.error("Failed to show salary update form: {}", e.getMessage());
            model.addAttribute("error", "Failed to show salary update form: " + e.getMessage());
            return "layout/main-layout";
        }
        
    }

    @PostMapping("/update")
    public String updateSalaries(
            @RequestParam("salaryComponent") String salaryComponent,
            @RequestParam("operator") String operator,
            @RequestParam("amount") double amount,
            @RequestParam("action") String action,
            @RequestParam("percentage") double percentage,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            User user = validateUser(session);
            
            // Validate input parameters
            if (salaryComponent == null || salaryComponent.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a salary component");
                return "redirect:/salary/update";
            }
            
            if (amount <= 0) {
                redirectAttributes.addFlashAttribute("error", "Amount must be positive");
                return "redirect:/salary/update";
            }
            
            if (percentage <= 0 || percentage > 100) {
                redirectAttributes.addFlashAttribute("error", "Percentage must be between 0 and 100");
                return "redirect:/salary/update";
            }
            
            // Process the update
            List<SalarySlip> updatedSlips = salaryService.updateSalarySlipsByCondition(
                    salaryComponent, 
                    operator, 
                    amount, 
                    action, 
                    percentage, 
                    user);
            
            // Prepare success message
            String successMessage;
            if (updatedSlips.isEmpty()) {
                successMessage = "No salary slips matched the criteria. No updates were made.";
            } else {
                successMessage = String.format("Successfully updated %d salary slip(s) with component %s %s %.2f. " +
                        "Applied %s of %.2f%% to base salary.", 
                        updatedSlips.size(), 
                        salaryComponent, 
                        operator, 
                        amount,
                        action,
                        percentage);
            }
            
            redirectAttributes.addFlashAttribute("success", successMessage);
            logger.info("Salary update completed successfully. {} slips updated.", updatedSlips.size());
            
        } catch (Exception e) {
            logger.error("Failed to update salaries: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to update salaries: " + e.getMessage());
        }
        
        return "redirect:/salary/update";
    }

    private User validateUser(HttpSession session) throws Exception {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            logger.warn("Unauthenticated access attempt, redirecting to login");
            throw new Exception("User not authenticated");
        }
        return user;
    }
    
    /**
     * Convert date from yyyy-MM-dd format (HTML date input) to dd/MM/yyyy format (service format)
     */
    private String convertDateFormat(String htmlDate) {
        logger.info("Attempt to convert date format of : "+htmlDate);
        String realDate = htmlDate+"-01";
        logger.info("Date format converted to : "+realDate);
        LocalDate date = LocalDate.parse(realDate);
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
    /**
     * Format date for display purposes
     */
    private String formatDisplayDate(String serviceDate) {
        LocalDate date = LocalDate.parse(serviceDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return date.format(DateTimeFormatter.ofPattern("MMM yyyy")); // e.g., "Jan 2025"
    }
}