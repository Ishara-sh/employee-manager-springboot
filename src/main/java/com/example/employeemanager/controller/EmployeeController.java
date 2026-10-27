package com.example.employeemanager.controller;

import com.example.employeemanager.model.Employee;
import com.example.employeemanager.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/employees";
    }

    @GetMapping("/employees")
    public String listEmployees(Model model) {
        model.addAttribute("employees", employeeService.getAllEmployees());

        if (!model.containsAttribute("employee")) {
            model.addAttribute("employee", new Employee());
        }

        return "employees";
    }

    @PostMapping("/employees")
    public String saveEmployee(
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (employeeService.emailExists(employee.getEmail())) {
            result.rejectValue("email", "duplicate",
                    "This email is already registered");
        }

        if (result.hasErrors()) {
            model.addAttribute("employees",
                    employeeService.getAllEmployees());
            return "employees";
        }

        employeeService.saveEmployee(employee);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Employee saved successfully!");

        return "redirect:/employees";
    }

    @GetMapping("/employees/{id}/edit")
    public String editEmployee(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        return employeeService.getEmployeeById(id)
                .map(employee -> {
                    model.addAttribute("employee", employee);
                    return "editemployee";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute(
                            "errorMessage", "Employee not found");
                    return "redirect:/employees";
                });
    }

    @PostMapping("/employees/{id}/update")
    public String updateEmployee(
            @PathVariable Long id,
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (employeeService.getEmployeeById(id).isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Employee not found");
            return "redirect:/employees";
        }

        if (employeeService.emailExistsForAnotherEmployee(
                employee.getEmail(), id)) {
            result.rejectValue("email", "duplicate",
                    "This email is already registered");
        }

        if (result.hasErrors()) {
            employee.setId(id);
            return "editemployee";
        }

        employeeService.updateEmployee(id, employee);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Employee updated successfully!");

        return "redirect:/employees";
    }

    @PostMapping("/employees/{id}/delete")
    public String deleteEmployee(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        if (employeeService.getEmployeeById(id).isPresent()) {
            employeeService.deleteEmployee(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Employee deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Employee not found");
        }

        return "redirect:/employees";
    }
}