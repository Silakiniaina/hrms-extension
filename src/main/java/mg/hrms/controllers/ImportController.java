package mg.hrms.controllers;

import jakarta.servlet.http.HttpSession;
import mg.hrms.models.User;
import java.util.Map;
import mg.hrms.payload.ImportResult;
import mg.hrms.services.ImportService;
import mg.hrms.services.ResetService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/import")
public class ImportController {

    private static final Logger logger = LoggerFactory.getLogger(ImportController.class);
    private final ImportService importService;
    private final ResetService resetService;

    public ImportController(ImportService importService, ResetService resetService) {
        this.importService = importService;
        this.resetService = resetService;
    }

    @GetMapping
    public String showImportForm(Model model, HttpSession session) {
        logger.debug("Displaying import form");
        try {
            validateUser(session);
            model.addAttribute("pageTitle", "HRMS Data Import");
            model.addAttribute("contentPage", "pages/data/import-form.jsp");
            return "layout/main-layout";
        } catch (Exception e) {
            logger.error("Failed to display import form: {}", e.getMessage());
            model.addAttribute("error", "Access denied: " + e.getMessage());
            return "redirect:/auth";
        }
    }

    @PostMapping
    public String importData(
            @RequestParam(value = "employeesFile", required = false) MultipartFile employeesFile,
            @RequestParam(value = "structuresFile", required = false) MultipartFile structuresFile,
            @RequestParam(value = "recordsFile", required = false) MultipartFile recordsFile,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        logger.info("Initiating data import");
        try {
            User user = validateUser(session);

            if (isEmptyFile(employeesFile) && isEmptyFile(structuresFile) && isEmptyFile(recordsFile)) {
                throw new IllegalArgumentException("At least one import file must be provided");
            }

            ImportResult result = importService.processImport(employeesFile, structuresFile, recordsFile, user);

            if (result.isSuccess()) {
                String successMessage = buildSuccessMessage(result);
                redirectAttributes.addFlashAttribute("success", successMessage);
                if (result.hasWarnings()) {
                    redirectAttributes.addFlashAttribute("warnings", result.getWarnings());
                }
                logger.info("Data import successful: {}", successMessage);
            } else {
                String errorMessage = result.getMessage() != null ? result.getMessage() : "Import failed - no records were created";
                redirectAttributes.addFlashAttribute("error", errorMessage);
                if (result.hasErrors()) {
                    redirectAttributes.addFlashAttribute("errors", result.getErrors());
                }
                if (result.hasWarnings()) {
                    redirectAttributes.addFlashAttribute("warnings", result.getWarnings());
                }
                logger.warn("Data import failed: {}", errorMessage);
                resetService.processReset(null, user);
            }
            redirectAttributes.addFlashAttribute("importResult", result);

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Validation error: " + e.getMessage());
            logger.error("Import validation failed: {}", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Import failed: " + e.getMessage());
            logger.error("Data import failed: {}", e.getMessage(), e);
        }
        return "redirect:/import";
    }

    private boolean isEmptyFile(MultipartFile file) {
        return file == null || file.isEmpty();
    }

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
                        message.append(count).append(" ").append(formatEntityType(entry.getKey())).append(", ");
                    }
                }
            }
            if (message.toString().endsWith(", ")) {
                message.setLength(message.length() - 2);
            }
            if (result.getCounts().size() > 1) {
                message.append(" (Total: ").append(totalRecords).append(")");
            }
        }
        return message.toString();
    }

    private String formatEntityType(String entityType) {
        return switch (entityType.toLowerCase()) {
            case "employees" -> "employees";
            case "salary_structures" -> "salary structures";
            case "salary_records" -> "salary records";
            default -> entityType.replace("_", " ");
        };
    }

    private User validateUser(HttpSession session) throws Exception {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            logger.warn("Unauthenticated access attempt to import data");
            throw new Exception("User not authenticated");
        }
        return user;
    }
}
