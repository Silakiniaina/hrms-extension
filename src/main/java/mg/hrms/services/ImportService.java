package mg.hrms.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import mg.hrms.models.User;
import mg.hrms.models.dataImport.EmployeeImport;
import mg.hrms.models.dataImport.ImportData;
import mg.hrms.models.dataImport.SalaryRecordImport;
import mg.hrms.models.dataImport.SalaryStructureImport;
import mg.hrms.payload.ImportResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ImportService {

    private static final Logger logger = LoggerFactory.getLogger(ImportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final RestApiService restApiService;
    private final ObjectMapper objectMapper;

    public ImportService(RestApiService restApiService, ObjectMapper objectMapper) {
        this.restApiService = restApiService;
        this.objectMapper = objectMapper;
    }

    public ImportResult processImport(MultipartFile employeesFile, MultipartFile structuresFile, MultipartFile recordsFile, User user) throws Exception {
        logger.info("Processing import for user: {}", user.getFullName());
        try {
            ImportData importData = parseImportFiles(employeesFile, structuresFile, recordsFile);
            validateImportData(importData);

            Map<String, Object> erpNextPayload = new HashMap<>();
            if (importData.getEmployees() != null) erpNextPayload.put("employees_file", convertEmployees(importData.getEmployees()));
            if (importData.getSalaryStructures() != null) erpNextPayload.put("salary_structures_file", convertStructures(importData.getSalaryStructures()));
            if (importData.getSalaryRecords() != null) erpNextPayload.put("salary_records_file", convertRecords(importData.getSalaryRecords()));

            logger.debug("ERPNext payload: {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(erpNextPayload));

            String importUrl = restApiService.getServerHost() + "/api/method/hrms.api.data_import.import_hrms_data";
            String requestBody = objectMapper.writeValueAsString(erpNextPayload);

            ResponseEntity<Map<String, Object>> response = restApiService.executeApiCall(
                    importUrl,
                    HttpMethod.POST,
                    requestBody,
                    user,
                    new ParameterizedTypeReference<>() {}
            );

            return processErpNextResponse(response.getBody());
        } catch (Exception e) {
            logger.error("Failed to process import: {}", e.getMessage(), e);
            throw new Exception("Import operation failed: " + e.getMessage());
        }
    }

    private ImportData parseImportFiles(MultipartFile employeesFile, MultipartFile structuresFile, MultipartFile recordsFile) throws IOException {
        ImportData importData = new ImportData();
        if (employeesFile != null && !employeesFile.isEmpty()) importData.setEmployees(parseEmployeeFile(employeesFile.getInputStream()));
        if (structuresFile != null && !structuresFile.isEmpty()) importData.setSalaryStructures(parseStructureFile(structuresFile.getInputStream()));
        if (recordsFile != null && !recordsFile.isEmpty()) importData.setSalaryRecords(parseRecordFile(recordsFile.getInputStream()));
        return importData;
    }

    private List<EmployeeImport> parseEmployeeFile(InputStream is) throws IOException {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            return csvParser.getRecords().stream().map(record -> {
                EmployeeImport emp = new EmployeeImport();
                emp.setEmployeeRef(record.get("Ref"));
                emp.setLastName(record.get("Nom"));
                emp.setFirstName(record.get("Prenom"));
                emp.setGender(record.get("genre"));
                emp.setCompany(record.get("company"));
                try {
                    emp.setHireDate(Date.valueOf(LocalDate.parse(record.get("Date embauche"), DATE_FORMATTER)));
                    emp.setBirthDate(Date.valueOf(LocalDate.parse(record.get("date naissance"), DATE_FORMATTER)));
                } catch (DateTimeParseException e) {
                    throw new RuntimeException("Invalid date format in employee file. Expected DD/MM/YYYY", e);
                }
                return emp;
            }).collect(Collectors.toList());
        }
    }

    private List<SalaryStructureImport> parseStructureFile(InputStream is) throws IOException {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

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

    private List<SalaryRecordImport> parseRecordFile(InputStream is) throws IOException {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            return csvParser.getRecords().stream().map(record -> {
                SalaryRecordImport recordImport = new SalaryRecordImport();
                recordImport.setMonth(record.get("Mois"));
                recordImport.setEmployeeRef(record.get("Ref Employe"));
                try {
                    recordImport.setBaseSalary(Double.parseDouble(record.get("Salaire Base")));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid salary base format in salary records file", e);
                }
                recordImport.setSalaryStructure(record.get("Salaire"));
                return recordImport;
            }).collect(Collectors.toList());
        }
    }

    private void validateImportData(ImportData importData) {
        if (importData.getEmployees() != null && importData.getSalaryRecords() != null) {
            Set<String> employeeRefs = importData.getEmployees().stream()
                    .map(EmployeeImport::getEmployeeRef)
                    .collect(Collectors.toSet());
            importData.getSalaryRecords().forEach(record -> {
                if (!employeeRefs.contains(record.getEmployeeRef())) {
                    throw new IllegalArgumentException("Salary record references unknown employee: " + record.getEmployeeRef());
                }
            });
        }
        if (importData.getSalaryStructures() != null && importData.getSalaryRecords() != null) {
            Set<String> structureNames = importData.getSalaryStructures().stream()
                    .map(SalaryStructureImport::getSalaryStructure)
                    .collect(Collectors.toSet());
            importData.getSalaryRecords().forEach(record -> {
                if (!structureNames.contains(record.getSalaryStructure())) {
                    throw new IllegalArgumentException("Salary record references unknown structure: " + record.getSalaryStructure());
                }
            });
        }
    }

    private List<Map<String, String>> convertEmployees(List<EmployeeImport> employees) {
        return employees.stream().map(emp -> {
            Map<String, String> map = new HashMap<>();
            map.put("Ref", emp.getEmployeeRef());
            map.put("Nom", emp.getLastName());
            map.put("Prenom", emp.getFirstName());
            map.put("genre", emp.getGender());
            map.put("Date embauche", emp.getHireDate() != null ? DATE_FORMATTER.format(emp.getHireDate().toLocalDate()) : "");
            map.put("date naissance", emp.getBirthDate() != null ? DATE_FORMATTER.format(emp.getBirthDate().toLocalDate()) : "");
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
        return records.stream().map(record -> {
            Map<String, String> map = new HashMap<>();
            map.put("Mois", record.getMonth());
            map.put("Ref Employe", record.getEmployeeRef());
            map.put("Salaire Base", String.valueOf(record.getBaseSalary()));
            map.put("Salaire", record.getSalaryStructure());
            return map;
        }).collect(Collectors.toList());
    }

    private ImportResult processErpNextResponse(Map<String, Object> responseBody) {
        ImportResult result = new ImportResult();
        try {
            if (responseBody.containsKey("success")) {
                result.setSuccess((Boolean) responseBody.get("success"));
                result.setMessage((String) responseBody.get("message"));
                if (responseBody.get("counts") instanceof Map) {
                    result.setCounts((Map<String, Object>) responseBody.get("counts"));
                }
                if (responseBody.get("errors") instanceof Map) {
                    result.setErrors(processErrors((Map<String, Object>) responseBody.get("errors")));
                }
                if (responseBody.get("warnings") instanceof Map) {
                    result.setWarnings(processErrors((Map<String, Object>) responseBody.get("warnings")));
                }
                logger.info("Import result: success={}, totalRecords={}", result.isSuccess(), result.getTotalRecordsCreated());
            } else {
                boolean hasErrors = responseBody.values().stream().anyMatch(value -> value instanceof List && !((List<?>) value).isEmpty());
                result.setSuccess(!hasErrors);
                result.setMessage(hasErrors ? "Import failed with errors" : "Import completed successfully");
                result.setErrors(processErrors(responseBody));
            }
        } catch (Exception e) {
            logger.error("Error processing ERPNext response: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setMessage("Error processing import response: " + e.getMessage());
        }
        return result;
    }

    private Map<String, List<String>> processErrors(Map<String, Object> errors) {
        Map<String, List<String>> processedErrors = new HashMap<>();
        for (Map.Entry<String, Object> entry : errors.entrySet()) {
            if (entry.getValue() instanceof List) {
                processedErrors.put(entry.getKey(), ((List<?>) entry.getValue()).stream()
                        .map(Object::toString)
                        .collect(Collectors.toList()));
            }
        }
        return processedErrors;
    }
}
