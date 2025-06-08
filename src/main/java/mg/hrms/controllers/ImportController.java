package mg.hrms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import mg.hrms.models.User;
import mg.hrms.payload.ImportResult;
import mg.hrms.services.ImportService;
import java.util.Map;
import java.util.List; 

@Controller
@RequestMapping("/import")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @GetMapping
    public String showImportForm(Model model) {
        model.addAttribute("pageTitle", "HRMS Data Import");
        model.addAttribute("contentPage", "pages/data/import-form.jsp");
        return "layout/main-layout";
    }

    @PostMapping
    public String importData(
            @RequestParam(value = "employeesFile", required = false) MultipartFile employeesFile,
            @RequestParam(value = "structuresFile", required = false) MultipartFile structuresFile,
            @RequestParam(value = "recordsFile", required = false) MultipartFile recordsFile,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                throw new Exception("User not authenticated");
            }

            // Check at least one file is provided
            if ((employeesFile == null || employeesFile.isEmpty()) &&
                (structuresFile == null || structuresFile.isEmpty()) &&
                (recordsFile == null || recordsFile.isEmpty())) {
                throw new IllegalArgumentException("At least one import file must be provided");
            }

            ImportResult result = importService.processImport(
                employeesFile, structuresFile, recordsFile, user);

            // Handle the new response structure
            if (result.isSuccess()) {
                // Import was actually successful - records were created
                String successMessage = buildSuccessMessage(result);
                redirectAttributes.addFlashAttribute("success", successMessage);

                // Add warnings if any (NOUVEAU)
                if (result.hasWarnings()) {
                    redirectAttributes.addFlashAttribute("warnings", result.getWarnings());
                }

                redirectAttributes.addFlashAttribute("importResult", result); // Garde le résultat complet

                // Log success for debugging
                System.out.println("Import successful: " + result.getMessage());
                if (result.getCounts() != null) {
                    System.out.println("Records created: " + result.getCounts());
                }

            } else {
                // Import failed or no records were created
                String errorMessage = result.getMessage() != null ?
                    result.getMessage() : "Import failed - no records were created";

                if (result.hasErrors()) {
                    // There were actual errors
                    redirectAttributes.addFlashAttribute("error", errorMessage); // Message général
                    redirectAttributes.addFlashAttribute("errors", result.getErrors()); // Erreurs détaillées
                } else {
                    // No errors but no records created
                    redirectAttributes.addFlashAttribute("warning",
                        "Import completed but no records were created. Please check your data format and content.");
                }

                // Add warnings if any (NOUVEAU - même en cas d'échec si des avertissements sont présents)
                if (result.hasWarnings()) {
                    redirectAttributes.addFlashAttribute("warnings", result.getWarnings());
                }

                redirectAttributes.addFlashAttribute("importResult", result); // Garde le résultat complet

                // Enhanced logging for debugging
                System.out.println("=== IMPORT FAILED DEBUG INFO ===");
                System.out.println("Import failed: " + errorMessage);
                System.out.println("Success flag: " + result.isSuccess());
                System.out.println("Message: " + result.getMessage());
                System.out.println("Counts: " + result.getCounts());
                System.out.println("Has errors: " + result.hasErrors());
                if (result.getErrors() != null && !result.getErrors().isEmpty()) {
                    System.out.println("Errors: " + result.getErrors());
                }
                System.out.println("================================");
            }

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Validation error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Import failed: " + e.getMessage());
        }

        return "redirect:/import";
    }

    /**
     * Build a detailed success message including record counts
     */
    private String buildSuccessMessage(ImportResult result) {
        StringBuilder message = new StringBuilder("Import completed successfully!");

        if (result.getCounts() != null && !result.getCounts().isEmpty()) {
            message.append(" Records created: ");

            int totalRecords = 0;
            for (Map.Entry<String, Object> entry : result.getCounts().entrySet()) {
                if (entry.getValue() instanceof Number) {
                    int count = ((Number) entry.getValue()).intValue();
                    totalRecords += count;

                    if (count > 0) {
                        String entityType = formatEntityType(entry.getKey());
                        message.append(count).append(" ").append(entityType).append(", ");
                    }
                }
            }

            // Remove trailing comma and space
            if (message.toString().endsWith(", ")) {
                message.setLength(message.length() - 2);
            }

            // Add total if multiple types
            if (result.getCounts().size() > 1) {
                message.append(" (Total: ").append(totalRecords).append(")");
            }
        }

        return message.toString();
    }

    /**
     * Format entity type names for display
     */
    private String formatEntityType(String entityType) {
        switch (entityType.toLowerCase()) {
            case "employees":
                return "employees";
            case "salary_structures":
                return "salary structures";
            case "salary_records":
                return "salary records";
            default:
                return entityType.replace("_", " ");
        }
    }
}
