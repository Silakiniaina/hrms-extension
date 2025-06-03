package mg.hrms.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import mg.hrms.models.User;
import mg.hrms.services.EmployeeService;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public String getAllEmployees(Model model, HttpSession session) {
        try {
            model.addAttribute("pageTitle", "Employee Management");
            model.addAttribute("contentPage", "pages/employees/employee-list.jsp");

            User connectedUser = (User) session.getAttribute("user");
            if (connectedUser == null) {
                throw new Exception("User not authenticated");
            }

            model.addAttribute("employees", employeeService.getAll(connectedUser));
            return "layout/main-layout";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load employees: " + e.getMessage());
            return "layout/main-layout";
        }
    }
}