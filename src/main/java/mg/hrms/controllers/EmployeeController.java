package mg.hrms.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import mg.hrms.models.User;
import mg.hrms.models.args.EmployeeFilterArgs;
import mg.hrms.services.CompanyService;
import mg.hrms.services.EmployeeService;
import mg.hrms.services.GenderService;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final GenderService genderService;
    private final CompanyService companyService;
    private final ObjectMapper objectMapper;

    @Autowired
    public EmployeeController(EmployeeService employeeService, GenderService genderService, CompanyService companyService, ObjectMapper objectMapper) {
        this.employeeService = employeeService;
        this.genderService = genderService;
        this.companyService = companyService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String getAllEmployees(Model model, HttpSession session,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String status) {
        try {
            model.addAttribute("pageTitle", "Employee Management");
            model.addAttribute("contentPage", "pages/employees/employee-list.jsp");

            User connectedUser = (User) session.getAttribute("user");
            if (connectedUser == null) {
                throw new Exception("User not authenticated");
            }

            // Add companies and genders to the model
            model.addAttribute("companies", companyService.getAll(connectedUser));
            model.addAttribute("genders", genderService.getAll(connectedUser));

            EmployeeFilterArgs filter = new EmployeeFilterArgs();
            filter.setName(name);
            filter.setMinAge(minAge != null ? minAge : 0);
            filter.setMaxAge(maxAge != null ? maxAge : 0);
            filter.setGender(gender);
            filter.setCompany(company);
            filter.setStatus(status);

            System.out.println("Filter : "+filter);

            model.addAttribute("employees", employeeService.getAll(connectedUser, filter));
            return "layout/main-layout";
        } catch (Exception e) {
            e.printStackTrace();
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
        try {
            model.addAttribute("pageTitle", "Employee Management - Filtered");
            model.addAttribute("contentPage", "pages/employees/employee-list.jsp");
            return getAllEmployees(model, session, name, minAge, maxAge, gender, company, status);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to filter employees: " + e.getMessage());
            return "layout/main-layout";
        }
    }
}
