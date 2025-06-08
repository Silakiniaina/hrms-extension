package mg.hrms.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.core.ParameterizedTypeReference;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import mg.hrms.models.*;
import mg.hrms.models.dataImport.EmployeeImport;
import mg.hrms.models.dataImport.ImportData;
import mg.hrms.models.dataImport.SalaryRecordImport;
import mg.hrms.models.dataImport.SalaryStructureImport;
import mg.hrms.payload.ImportResult; // Assurez-vous que cette importation est présente

@Service
public class ImportService {

    Logger logger = LoggerFactory.getLogger(ImportService.class);
    private final RestApiService restApiService;
    private final ObjectMapper objectMapper;

    public ImportService(RestApiService restApiService, ObjectMapper objectMapper) {
        this.restApiService = restApiService;
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    public ImportResult processImport(MultipartFile employeesFile,
                                    MultipartFile structuresFile,
                                    MultipartFile recordsFile,
                                    User user) throws IOException, Exception {

        ImportData importData = parseImportFiles(employeesFile, structuresFile, recordsFile);
        validateImportData(importData);

        // Convert to ERPNext compatible format
        Map<String, Object> erpNextPayload = new HashMap<>();
        if (importData.getEmployees() != null) {
            erpNextPayload.put("employees_file", convertEmployees(importData.getEmployees()));
        }
        if (importData.getSalaryStructures() != null) {
            erpNextPayload.put("salary_structures_file", convertStructures(importData.getSalaryStructures()));
        }
        if (importData.getSalaryRecords() != null) {
            erpNextPayload.put("salary_records_file", convertRecords(importData.getSalaryRecords()));
        }

        // Print the payload being sent to ERPNext
        logger.info("Sending to ERPNext API:");
        logger.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(erpNextPayload));

        String importUrl = restApiService.getServerHost() + "/api/method/hrms.api.data_import.import_hrms_data";
        String requestBody = objectMapper.writeValueAsString(erpNextPayload);

        ResponseEntity<Map> response = restApiService.executeApiCall(
            importUrl,
            HttpMethod.POST,
            requestBody,
            user,
            new ParameterizedTypeReference<Map>() {}
        );

        return processErpNextResponse(response.getBody());
    }

    private ImportData parseImportFiles(MultipartFile employeesFile,
                                      MultipartFile structuresFile,
                                      MultipartFile recordsFile) throws IOException {
        ImportData importData = new ImportData();

        if (employeesFile != null && !employeesFile.isEmpty()) {
            importData.setEmployees(parseEmployeeFile(employeesFile.getInputStream()));
        }

        if (structuresFile != null && !structuresFile.isEmpty()) {
            importData.setSalaryStructures(parseStructureFile(structuresFile.getInputStream()));
        }

        if (recordsFile != null && !recordsFile.isEmpty()) {
            importData.setSalaryRecords(parseRecordFile(recordsFile.getInputStream()));
        }

        return importData;
    }

    @SuppressWarnings("deprecation")
    private List<EmployeeImport> parseEmployeeFile(InputStream is) throws IOException {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (var fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            var csvParser = new CSVParser(fileReader,
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            return csvParser.getRecords().stream().map(record -> {
                EmployeeImport emp = new EmployeeImport();
                emp.setEmployeeRef(record.get("Ref"));
                emp.setLastName(record.get("Nom"));
                emp.setFirstName(record.get("Prenom"));
                emp.setGender(record.get("genre"));
                emp.setCompany(record.get("company"));

                try {
                    // Parse hire date
                    LocalDate hireLocalDate = LocalDate.parse(record.get("Date embauche"), dateFormatter);
                    emp.setHireDate(Date.valueOf(hireLocalDate));

                    // Parse birth date
                    LocalDate birthLocalDate = LocalDate.parse(record.get("date naissance"), dateFormatter);
                    emp.setBirthDate(Date.valueOf(birthLocalDate));
                } catch (DateTimeParseException e) {
                    throw new RuntimeException("Invalid date format. Expected DD/MM/YYYY", e);
                }

                return emp;
            }).collect(Collectors.toList());
        }
    }

    @SuppressWarnings("deprecation")
    private List<SalaryStructureImport> parseStructureFile(InputStream is) throws IOException {
        try (var fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             var csvParser = new CSVParser(fileReader,
                 CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            return csvParser.getRecords().stream().map(record -> {
                SalaryStructureImport structure = new SalaryStructureImport();
                structure.setSalaryStructure(record.get("salary structure"));
                structure.setName(record.get("name"));
                structure.setAbbreviation(record.get("Abbr"));
                structure.setType(record.get("type"));
                structure.setFormula(record.get("valeur"));
                structure.setCompany(record.get("company"));
                return structure;
            }).collect(Collectors.toList());
        }
    }

    @SuppressWarnings("deprecation")
    private List<SalaryRecordImport> parseRecordFile(InputStream is) throws IOException {
        try (var fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             var csvParser = new CSVParser(fileReader,
                 CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            return csvParser.getRecords().stream().map(record -> {
                SalaryRecordImport recordImport = new SalaryRecordImport();
                recordImport.setMonth(record.get("Mois"));
                recordImport.setEmployeeRef(record.get("Ref Employe"));
                recordImport.setBaseSalary(Double.parseDouble(record.get("Salaire Base")));
                recordImport.setSalaryStructure(record.get("Salaire"));
                return recordImport;
            }).collect(Collectors.toList());
        }
    }

    private void validateImportData(ImportData importData) {
        // Validate employee references in salary records
        if (importData.getEmployees() != null && importData.getSalaryRecords() != null) {
            Set<String> employeeRefs = importData.getEmployees().stream()
                .map(EmployeeImport::getEmployeeRef)
                .collect(Collectors.toSet());

            importData.getSalaryRecords().forEach(record -> {
                if (!employeeRefs.contains(record.getEmployeeRef())) {
                    throw new IllegalArgumentException(
                        "Salary record references unknown employee: " + record.getEmployeeRef());
                }
            });
        }

        // Validate structure references in salary records
        if (importData.getSalaryStructures() != null && importData.getSalaryRecords() != null) {
            Set<String> structureNames = importData.getSalaryStructures().stream()
                .map(SalaryStructureImport::getSalaryStructure)
                .collect(Collectors.toSet());

            importData.getSalaryRecords().forEach(record -> {
                if (!structureNames.contains(record.getSalaryStructure())) {
                    throw new IllegalArgumentException(
                        "Salary record references unknown structure: " + record.getSalaryStructure());
                }
            });
        }
    }

    private List<Map<String, String>> convertEmployees(List<EmployeeImport> employees) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return employees.stream().map(emp -> {
            Map<String, String> map = new HashMap<>();
            map.put("Ref", emp.getEmployeeRef());
            map.put("Nom", emp.getLastName());
            map.put("Prenom", emp.getFirstName());
            map.put("genre", emp.getGender());
            map.put("Date embauche", formatter.format(emp.getHireDate().toLocalDate()));
            map.put("date naissance", formatter.format(emp.getBirthDate().toLocalDate()));
            map.put("company", emp.getCompany());
            return map;
        }).collect(Collectors.toList());
    }

    private List<Map<String, String>> convertStructures(List<SalaryStructureImport> structures) {
        return structures.stream().map(struct -> {
            Map<String, String> map = new HashMap<>();
            map.put("salary structure", struct.getSalaryStructure());
            map.put("name", struct.getName());
            map.put("Abbr", struct.getAbbreviation());
            map.put("type", struct.getType());
            map.put("valeur", struct.getFormula());
            map.put("company", struct.getCompany());
            return map;
        }).collect(Collectors.toList());
    }

    private List<Map<String, String>> convertRecords(List<SalaryRecordImport> records) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return records.stream().map(record -> {
            Map<String, String> map = new HashMap<>();
            map.put("Mois", record.getMonth());
            map.put("Ref Employe", record.getEmployeeRef());
            map.put("Salaire Base", String.valueOf(record.getBaseSalary()));
            map.put("Salaire", record.getSalaryStructure());
            return map;
        }).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private ImportResult processErpNextResponse(Map<String, Object> responseBody) {
        ImportResult result = new ImportResult();

        // Check if the new response structure exists
        if (responseBody.containsKey("success")) {
            // New response structure
            Boolean success = (Boolean) responseBody.get("success");
            String message = (String) responseBody.get("message");
            Map<String, Object> counts = (Map<String, Object>) responseBody.get("counts");
            Map<String, Object> errors = (Map<String, Object>) responseBody.get("errors");
            Map<String, Object> warnings = (Map<String, Object>) responseBody.get("warnings"); // NOUVEAU

            result.setSuccess(success);
            result.setMessage(message != null ? message : (success ? "Import completed successfully" : "Import failed"));

            // Set counts if available
            if (counts != null) {
                result.setCounts(counts);
            }

            // Process errors
            if (errors != null) {
                result.setErrors(processErrors(errors));
            }

            // Process warnings (NOUVEAU)
            if (warnings != null) {
                result.setWarnings(processErrors(warnings)); // Réutilise processErrors pour la structure Map<String, List<String>>
            }

            // Log the actual result
            if (success) {
                int totalRecords = counts != null ? counts.values().stream()
                    .mapToInt(v -> v instanceof Number ? ((Number) v).intValue() : 0)
                    .sum() : 0;
                logger.info("Import successful: {} records created", totalRecords);
            } else {
                logger.warn("Import failed: {}", message);
            }

        } else {
            // Legacy response structure (backward compatibility)
            boolean hasErrors = responseBody.values().stream()
                .anyMatch(value -> value instanceof List && !((List<?>) value).isEmpty());

            result.setSuccess(!hasErrors);
            result.setMessage(hasErrors ? "Import failed with errors" : "Import completed successfully");
            result.setErrors(processErrors(responseBody));
        }

        return result;
    }

    private Map<String, List<String>> processErrors(Map<String, Object> errors) {
        Map<String, List<String>> processedErrors = new HashMap<>();

        for (Map.Entry<String, Object> entry : errors.entrySet()) {
            if (entry.getValue() instanceof List) {
                List<String> errorList = new ArrayList<>();
                for (Object error : (List<?>) entry.getValue()) {
                    errorList.add(error.toString());
                }
                processedErrors.put(entry.getKey(), errorList);
            }
        }

        return processedErrors;
    }
}
