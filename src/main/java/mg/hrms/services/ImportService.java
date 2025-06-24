package mg.hrms.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import mg.hrms.exception.DateFormatException;
import mg.hrms.models.User;
import mg.hrms.models.dataImport.EmployeeImport;
import mg.hrms.models.dataImport.SalaryStructureImport;
import mg.hrms.models.dataImport.SalaryRecordImport;
import mg.hrms.payload.ImportResult;
import  mg.hrms.utils.OperationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
 
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ImportService {

    private static final Logger logger = LoggerFactory.getLogger(ImportService.class);
    private final FrappeService frappeService;
    private final ObjectMapper objectMapper;
    private Map<String, String> employeeRefMap = new HashMap<>();
    private Map<String, Integer> logCounts = new HashMap<>();
    private Map<String, List<String>> createdDocs = new HashMap<>();
    private Map<String, List<String>> errorMap = new HashMap<>();
    private boolean forceUpdate = false;
    
    public ImportService(ObjectMapper objectMapper, FrappeService frappeService) {
        this.objectMapper = objectMapper;
        this.frappeService = frappeService;
        initializeCounters();
    }

    /* -------------------------------------------------------------------------- */
    /*                           Main method for import                           */
    /* -------------------------------------------------------------------------- */
    public ImportResult processImport(MultipartFile employeesFile, MultipartFile structuresFile, MultipartFile recordsFile, User user) {
        logger.info("Starting HRMS data import for user: {}", user.getFullName());

        // Reset state for new import
        initializeCounters();
        employeeRefMap.clear();

        ImportResult result = new ImportResult();

        try {
            // Parse input files
            List<EmployeeImport> employees = parseFile(employeesFile, new TypeReference<List<EmployeeImport>>() {
            });
            List<SalaryStructureImport> salaryStructures = parseFile(structuresFile,
                    new TypeReference<List<SalaryStructureImport>>() {
                    });
            List<SalaryRecordImport> salaryRecords = parseFile(recordsFile,
                    new TypeReference<List<SalaryRecordImport>>() {
                    });

            if (employees == null && salaryStructures == null && salaryRecords == null) {
                result.setSuccess(false);
                result.setMessage("No valid import data provided");
                result.addError("system", "No valid files uploaded");
                return result;
            }

            // Process in coordinated flow (like Python process_all)
            processAllData(employees, salaryStructures, salaryRecords, result, user);

            // Set final result status
            int totalCreated = getTotalCreatedRecords();
            if (!hasErrors() && totalCreated > 0) {
                result.setSuccess(true);
                result.setMessage("Successfully imported " + totalCreated + " records");
            } else if (hasErrors()) {
                result.setSuccess(false);
                result.setMessage("Import completed with errors");
                // Copy errors to result
                copyErrorsToResult(result);
            } else {
                result.setSuccess(false);
                result.setMessage("No records were created");
            }

            // Copy counts to result
            result.setCounts(new HashMap<>(logCounts));

            logger.info("Import completed: success={}, created={}, errors={}",
                    result.isSuccess(), totalCreated, errorMap.values().stream().mapToInt(List::size).sum());

            return result;

        } catch (Exception e) {
            logger.error("Import failed: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setMessage("Import failed: " + e.getMessage());
            result.addError("system", e.getMessage());
            return result;
        }
    }

    /* -------------------------------------------------------------------------- */
    /*                             Processing methods                             */
    /* -------------------------------------------------------------------------- */
    private void processAllData(List<EmployeeImport> employees, List<SalaryStructureImport> salaryStructures,
            List<SalaryRecordImport> salaryRecords, ImportResult result, User user) {

        // 1. Process Employees first (they are dependencies for salary records)
        if (employees != null && !employees.isEmpty()) {
            logger.info("--- Processing Employees (via processAll) ---");
            processEmployeesCoordinated(employees, user);
            logger.info("Processed {} employees, created {}",
                    logCounts.get("employees_processed"), logCounts.get("employees_created"));
            logger.info("Employee Ref Map: {}", employeeRefMap);
        }

        // 2. Process Salary Structures
        if (salaryStructures != null && !salaryStructures.isEmpty()) {
            logger.info("--- Processing Salary Structures (via processAll) ---");
            processSalaryStructuresCoordinated(salaryStructures, user);
            logger.info("Processed {} structures, created {}",
                    logCounts.get("salary_structures_processed"), logCounts.get("salary_structures_created"));
        }

        // 3. Process Salary Records (using the employee_ref_map built in step 1)
        if (salaryRecords != null && !salaryRecords.isEmpty()) {
            logger.info("--- Processing Salary Records (via processAll) ---");
            processSalaryRecordsCoordinated(salaryRecords, user);
            logger.info("Processed {} records, created {}",
                    logCounts.get("salary_records_processed"), logCounts.get("salary_records_created"));
        }
    }

    private void processEmployeesCoordinated(List<EmployeeImport> employees, User user) {
        logger.info("=== PROCESSING EMPLOYEES ===");

        try {
            for (int idx = 0; idx < employees.size(); idx++) {
                EmployeeImport emp = employees.get(idx);
                int lineNumber = idx + 1;
                logCounts.put("employees_processed", logCounts.get("employees_processed") + 1);

                try {
                    String employeeRef = emp.getEmployeeRef();
                    logStep("Creating employee: " + employeeRef);

                    // Validate employee data (equivalent to check_integrity)
                    if (!validateEmployeeIntegrity(emp, lineNumber)) {
                        logStep("Validation failed for employee: " + employeeRef);
                        continue;
                    }

                    // Create employee
                    String docName = insertEmployeeData(emp, lineNumber, user);
                    if (docName != null) {
                        createdDocs.get("employees").add(docName);
                        logCounts.put("employees_created", logCounts.get("employees_created") + 1);
                        employeeRefMap.put(employeeRef, docName); // THIS LINE IS CRUCIAL
                        logStep("Created employee: " + docName);
                    } else {
                        logStep("Failed to create employee: " + employeeRef);
                    }

                } catch (Exception e) {
                    String errorMsg = "Line " + lineNumber + ": " + e.getMessage();
                    logStep("!!! " + errorMsg);
                    errorMap.get("employees").add(errorMsg);
                }
            }

            logger.info("Successfully created {} employees", logCounts.get("employees_created"));

        } catch (Exception e) {
            logStep("!!! Error processing employees: " + e.getMessage());
            errorMap.get("employees").add(e.getMessage());
        }
    }

    private void processSalaryStructuresCoordinated(List<SalaryStructureImport> salaryStructures, User user) {
        logger.info("=== PROCESSING SALARY STRUCTURES ===");

        try {
            for (int idx = 0; idx < salaryStructures.size(); idx++) {
                SalaryStructureImport struct = salaryStructures.get(idx);
                int lineNumber = idx + 1;
                logCounts.put("salary_structures_processed", logCounts.get("salary_structures_processed") + 1);

                try {
                    String structureName = struct.getSalaryStructure();
                    logStep("Processing salary structure line " + lineNumber + ": " + structureName);

                    // Validate structure data (equivalent to check_integrity)
                    if (!validateSalaryStructureIntegrity(struct, lineNumber)) {
                        logStep("Validation failed for salary structure component: " + struct.getName());
                        continue;
                    }

                    // Create/update salary structure
                    String docName = insertSalaryStructureData(struct, lineNumber, user);
                    if (docName != null) {
                        if (!createdDocs.get("salary_structures").contains(docName)) {
                            createdDocs.get("salary_structures").add(docName);
                            logCounts.put("salary_structures_created", logCounts.get("salary_structures_created") + 1);
                        }
                        logStep("Processed salary structure component: " + struct.getName());
                    } else {
                        logStep("Failed to process salary structure component: " + struct.getName());
                    }

                } catch (Exception e) {
                    String errorMsg = "Line " + lineNumber + ": " + e.getMessage();
                    logStep("!!! " + errorMsg);
                    errorMap.get("salary_structures").add(errorMsg);
                }
            }

            logger.info("Successfully created {} salary structures", logCounts.get("salary_structures_created"));

        } catch (Exception e) {
            logStep("!!! Error processing salary structures: " + e.getMessage());
            errorMap.get("salary_structures").add(e.getMessage());
        }
    }

    private void processSalaryRecordsCoordinated(List<SalaryRecordImport> salaryRecords, User user) {
        logger.info("=== PROCESSING SALARY RECORDS ===");

        try {
            for (int idx = 0; idx < salaryRecords.size(); idx++) {
                SalaryRecordImport record = salaryRecords.get(idx);
                int lineNumber = idx + 1;
                logCounts.put("salary_records_processed", logCounts.get("salary_records_processed") + 1);

                try {
                    String employeeRef = record.getEmployeeRef();
                    String month = record.getMonth();
                    logStep("Processing salary record line " + lineNumber + ": " + employeeRef + " - " + month);

                    // Validate record data and use employee_ref_map (THIS IS CRUCIAL)
                    if (!validateSalaryRecordIntegrity(record, lineNumber)) {
                        logStep("Validation failed for salary record for employee: " + employeeRef);
                        continue;
                    }

                    // Create salary record
                    String docName = insertSalaryRecordData(record, lineNumber, user);
                    if (docName != null) {
                        createdDocs.get("salary_records").add(docName);
                        logCounts.put("salary_records_created", logCounts.get("salary_records_created") + 1);
                        logStep("Created salary record: " + docName);
                    } else {
                        logStep("Failed to create salary record for employee: " + employeeRef);
                    }

                } catch (Exception e) {
                    String errorMsg = "Line " + lineNumber + ": " + e.getMessage();
                    logStep("!!! " + errorMsg);
                    errorMap.get("salary_records").add(errorMsg);
                }
            }

            logger.info("Successfully created {} salary records", logCounts.get("salary_records_created"));

        } catch (Exception e) {
            logStep("!!! Error processing salary records: " + e.getMessage());
            errorMap.get("salary_records").add(e.getMessage());
        }
    }

    /* -------------------------------------------------------------------------- */
    /*                             Validation methods                             */
    /* -------------------------------------------------------------------------- */
    private boolean validateEmployeeIntegrity(EmployeeImport emp, int lineNumber) {
        List<String> errors = new ArrayList<>();

        if (emp.getEmployeeRef() == null || emp.getEmployeeRef().trim().isEmpty()) {
            errors.add("Employee Reference is required");
        }
        if (emp.getLastName() == null || emp.getLastName().trim().isEmpty()) {
            errors.add("Last Name (Nom) is required");
        }
        if (emp.getFirstName() == null || emp.getFirstName().trim().isEmpty()) {
            errors.add("First Name (Prenom) is required");
        }
        if (emp.getBirthDate() == null) {
            errors.add("Date Naissance is required");
        }

        if (emp.getHireDate() == null) {
            errors.add("Date Embauche is required");
        }

        if(emp.getBirthDate() != null){
            try {
                String formatedBd = OperationUtils.formatDate(emp.getBirthDate());
                emp.setBirthDate(formatedBd);
            } catch (DateFormatException e) {
                errors.add("BirthDate "+emp.getBirthDate()+" : "+e.getMessage());
            }
        }

        if(emp.getHireDate() != null){
            try {
                String formatedHd = OperationUtils.formatDate(emp.getHireDate());
                emp.setHireDate(formatedHd);
            } catch (DateFormatException e) {
                errors.add("HireDate "+emp.getHireDate()+" : "+e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            for (String error : errors) {
                errorMap.get("employees").add("Line " + lineNumber + ": " + error);
            }
            return false;
        }

        return true;
    }

    private boolean validateSalaryStructureIntegrity(SalaryStructureImport struct, int lineNumber) {
        List<String> errors = new ArrayList<>();

        if (struct.getSalaryStructure() == null || struct.getSalaryStructure().trim().isEmpty()) {
            errors.add("Salary Structure Name is required");
        }
        if (struct.getName() == null || struct.getName().trim().isEmpty()) {
            errors.add("Component Name is required");
        }
        if (struct.getAbbreviation() == null || struct.getAbbreviation().trim().isEmpty()) {
            errors.add("Abbreviation is required");
        }

        if (struct.getName() != null && struct.getName().length() > 140) {
            errors.add("Component name exceeds 140 characters");
        }
        if (struct.getAbbreviation() != null && struct.getAbbreviation().length() > 100) {
            errors.add("Abbreviation exceeds 100 characters");
        }

        String type = struct.getType() != null ? struct.getType().toLowerCase() : "earning";
        if (!Arrays.asList("earning", "deduction").contains(type)) {
            errors.add("Type must be 'earning' or 'deduction'");
        }

        if (!errors.isEmpty()) {
            for (String error : errors) {
                errorMap.get("salary_structures").add("Line " + lineNumber + ": " + error);
            }
            return false;
        }

        return true;
    }

    private boolean validateSalaryRecordIntegrity(SalaryRecordImport record, int lineNumber) {
        List<String> errors = new ArrayList<>();
    
        if (record.getEmployeeRef() == null || record.getEmployeeRef().trim().isEmpty()) {
            errors.add("Employee Reference is required");
        }
        if (record.getMonth() == null || record.getMonth().trim().isEmpty()) {
            errors.add("Month is required");
        } else {
            try {
                String formatedMonth = OperationUtils.formatDate(record.getMonth());
                record.setMonth(formatedMonth);
            } catch (DateFormatException e) {
                errors.add("BirthDate "+record.getMonth()+" : "+e.getMessage());
            }
        }

        if (record.getBaseSalary() == null || record.getBaseSalary() < 0) {
            errors.add("Base Salary is required and must be non-negative");
        }
        if (record.getSalaryStructure() == null || record.getSalaryStructure().trim().isEmpty()) {
            errors.add("Salary Structure is required");
        }
        if (record.getEmployeeRef() != null && !employeeRefMap.containsKey(record.getEmployeeRef())) {
            errors.add("Employee with Ref '" + record.getEmployeeRef() + "' not found in processed employees");
        }
    
        if (!errors.isEmpty()) {
            for (String error : errors) {
                errorMap.get("salary_records").add("Line " + lineNumber + ": " + error);
            }
            return false;
        }
    
        return true;
    }

    /* -------------------------------------------------------------------------- */
    /*                           Data insertion methods                           */
    /* -------------------------------------------------------------------------- */
    private String insertEmployeeData(EmployeeImport emp, int lineNumber, User user) {
        try {
            logStep("Creating employee: " + emp.getEmployeeRef());

            // Check if employee already exists
            String existingEmployee = checkExistingEmployee(emp.getEmployeeRef(), user);
            if (existingEmployee != null && !forceUpdate) {
                logStep("Employee " + emp.getEmployeeRef() + " already exists. Skipping.");
                return existingEmployee;
            }

            // Prepare employee data
            Map<String, Object> employeeData = new HashMap<>();
            employeeData.put("ref", emp.getEmployeeRef());
            employeeData.put("first_name", emp.getFirstName());
            employeeData.put("last_name", emp.getLastName());
            employeeData.put("employee_name", emp.getFirstName() + " " + emp.getLastName());
            employeeData.put("gender", mapGender(emp.getGender()));
            employeeData.put("date_of_birth", OperationUtils.formatIntoFrappeDate(emp.getBirthDate()));
            employeeData.put("date_of_joining", OperationUtils.formatIntoFrappeDate(emp.getHireDate()));
            employeeData.put("company",
                    getOrCreateCompany(emp.getCompany() != null ? emp.getCompany() : "Default Company", user));
            employeeData.put("status", "Active");
            employeeData.put("department",
                    getOrCreateDepartment("Human Resources", (String) employeeData.get("company"), user));
            employeeData.put("designation", getOrCreateDesignation("Employee", user));
            employeeData.put("branch", getOrCreateBranch("Main Branch", (String) employeeData.get("company"), user));
            employeeData.put("employment_type", "Full-time");

            String employeeName;
            if (existingEmployee != null && forceUpdate) {
                employeeName = frappeService.updateFrappeDocument("Employee", existingEmployee, employeeData, user);
                logStep("Updated employee: " + employeeName);
            } else {
                employeeName = frappeService.createFrappeDocument("Employee", employeeData, user, true);
                logStep("Created employee: " + employeeName);
            }

            return employeeName;

        } catch (Exception e) {
            logStep("Failed to insert employee: " + e.getMessage());
            errorMap.get("employees").add("Line " + lineNumber + ": " + e.getMessage());
            return null;
        }
    }

    private String insertSalaryStructureData(SalaryStructureImport struct, int lineNumber, User user) {
        try {
            logStep("Processing component '" + struct.getName() + "' for structure '" + struct.getSalaryStructure()
                    + "'");

            // Get or create company
            String company = getOrCreateCompany(struct.getCompany() != null ? struct.getCompany() : "Default Company",
                    user);

            // Get or create salary structure
            String structureDoc = getOrCreateSalaryStructure(struct, company, user);
            if (structureDoc == null) {
                return null;
            }

            // Ensure salary component exists
            if (!ensureSalaryComponentExists(struct, company, user)) {
                return null;
            }

            // Update structure with component
            return updateStructureComponent(structureDoc, struct, user);

        } catch (Exception e) {
            logStep("Error: " + e.getMessage());
            errorMap.get("salary_structures").add("Line " + lineNumber + ": " + e.getMessage());
            return null;
        }
    }

    private String insertSalaryRecordData(SalaryRecordImport record, int lineNumber, User user) {
        try {
            // Get employee name from our reference map
            String employeeName = employeeRefMap.get(record.getEmployeeRef());
            logStep("Processing SR for employee : "+employeeName);
            if (employeeName == null) {
                throw new RuntimeException("Employee with Ref '" + record.getEmployeeRef() + "' not found");
            }

            // Validate salary structure exists
            logStep("Start check existing salary Strucutre : "+record.getSalaryStructure());
            String salaryStructure = checkExistingSalaryStructure(record.getSalaryStructure(), user);
            if (salaryStructure == null) {
                throw new RuntimeException("Salary Structure '" + record.getSalaryStructure() + "' not found");
            }
            logStep("Found Salary Structure : "+salaryStructure);

            logStep("Parse month date " + record.getMonth() + " into Date");
            SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
            java.util.Date utilDate = null; // Use java.util.Date as an intermediate for parsing

            try {
                utilDate = parser.parse(record.getMonth());
            } catch (java.text.ParseException e) {
                logStep("Error parsing month date '" + record.getMonth() + "' into a java.util.Date object: " + e.getMessage());
                throw new RuntimeException("Failed to parse month date: " + record.getMonth(), e);
            }
            Date monthDate = new Date(utilDate.getTime()); // This is how you convert to java.sql.Date
            logStep("Create calendar instance");
            Calendar cal = Calendar.getInstance();
            logStep("Setting time for monthdate " + monthDate);
            cal.setTime(monthDate);
            logStep("Format start date");
            String startDate = OperationUtils.formatDate(record.getMonth()); 
            logStep("Find the last day of month");
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            logStep("Format enddate");
            java.util.Date lastDayOfMonthUtilDate = cal.getTime();
            String endDate = OperationUtils.formatDate(new SimpleDateFormat("dd/MM/yyyy").format(lastDayOfMonthUtilDate));


            // Ensure fiscal year
            logStep("Ensure fiscal year for "+monthDate);
            String fiscalYear = ensureFiscalYear(monthDate, user);
            if (fiscalYear == null) {
                throw new RuntimeException("Failed to create/find fiscal year");
            }
            logStep("Found fiscal year : "+fiscalYear);

            String company = getCompanyForEmployee(employeeName, user);

            // Ensure salary structure assignment
            logStep("Ensure salary structure assignment : "+salaryStructure+" for employee : "+employeeName+" on "+startDate);
            if (!ensureSalaryStructureAssignment(employeeName, salaryStructure, startDate, record.getBaseSalary(),
                    company, user)) {
                throw new RuntimeException("Failed to create salary structure assignment");
            }

            // Create salary slip
            Map<String, Object> slipData = new HashMap<>();
            slipData.put("employee", employeeName);
            slipData.put("start_date", OperationUtils.formatIntoFrappeDate(startDate));
            slipData.put("end_date", OperationUtils.formatIntoFrappeDate(String.valueOf(endDate)));
            slipData.put("salary_structure", salaryStructure);
            slipData.put("posting_date", OperationUtils.formatIntoFrappeDate(String.valueOf(endDate)));
            slipData.put("payroll_frequency", "Monthly");
            slipData.put("base_salary", record.getBaseSalary());
            slipData.put("company", company);

            logStep("Attempt to create Salary Slip");
            return frappeService.createFrappeDocument("Salary Slip", slipData, user, true);

        } catch (Exception e) {
            logStep("Failed to insert salary record: " + e.getMessage());
            errorMap.get("salary_records").add("Line " + lineNumber + ": " + e.getMessage());
            return null;
        }
    }

    
    /* -------------------------------------------------------------------------- */
    /*                    Helper methods for Salary Strucuture                    */
    /* -------------------------------------------------------------------------- */
    private String getOrCreateSalaryStructure(SalaryStructureImport struct, String company, User user) {
        try {
            // Check if structure exists
            String existingStructure = checkExistingSalaryStructure(struct.getSalaryStructure(), user);
            if (existingStructure != null) {
                logStep("Found existing Salary Structure: " + struct.getSalaryStructure());
                return existingStructure;
            }

            logStep("Creating new Salary Structure: " + struct.getSalaryStructure());
            Map<String, Object> structureData = new HashMap<>();
            structureData.put("salary_structure_name", struct.getSalaryStructure());
            structureData.put("name", struct.getSalaryStructure());
            structureData.put("is_active", "Yes");
            structureData.put("currency", getCurrencyForCompany(company, user));
            structureData.put("company", company);
            structureData.put("payroll_frequency", "Monthly");

            String docName = frappeService.createFrappeDocument("Salary Structure", structureData, user, true);
            if (docName != null) {
                // Submit the Salary Structure to make it active
                boolean submitted = frappeService.submitFrappeDocument("Salary Structure", docName, user);
                if (submitted) {
                    logStep("Submitted Salary Structure: " + docName);
                } else {
                    logStep("Failed to submit Salary Structure: " + docName);
                    errorMap.get("salary_structures").add("Failed to submit Salary Structure: " + docName);
                    return null;
                }
            }
            logStep("Created new structure: " + docName);
            return docName;

        } catch (Exception e) {
            errorMap.get("salary_structures").add("Failed to get/create structure: " + e.getMessage());
            return null;
        }
    }

    private boolean ensureSalaryComponentExists(SalaryStructureImport struct, String company, User user) {
        try {
            String existingComponent = checkExistingSalaryComponent(struct.getName(), user);
            if (existingComponent == null) {
                logStep("Creating Salary Component: " + struct.getName());
                Map<String, Object> componentData = new HashMap<>();
                componentData.put("salary_component", struct.getName());
                componentData.put("type",
                        struct.getType().substring(0, 1).toUpperCase() + struct.getType().substring(1).toLowerCase());
                componentData.put("description", "Auto-created for structure '" + struct.getSalaryStructure() + "'");
                componentData.put("is_deductible_from_income_tax", 0);
                componentData.put("company", company);
                componentData.put("salary_component_abbr", struct.getAbbreviation());
                componentData.put("amount_based_on_formula", 1);
                componentData.put("depends_on_payment_days", 0);
                if (struct.getValeur() != null) {
                    componentData.put("formula", struct.getValeur().trim());
                }

                String docName = frappeService.createFrappeDocument("Salary Component", componentData, user, true);
                if (docName != null) {
                    // Submit the component
                    frappeService.submitFrappeDocument("Salary Component", docName, user);
                    logStep("Component created and submitted: " + docName);
                } else {
                    return false;
                }
            } else {
                // Update existing component
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("salary_component_abbr", struct.getAbbreviation());
                updateData.put("amount_based_on_formula", 1);
                updateData.put("depends_on_payment_days", 0);
                if (struct.getValeur() != null) {
                    updateData.put("formula", struct.getValeur().trim());
                }

                String updated = frappeService.updateFrappeDocument("Salary Component", existingComponent, updateData, user);
                if (updated != null) {
                    frappeService.submitFrappeDocument("Salary Component", updated, user);
                    logStep("Component updated and resubmitted: " + updated);
                }
            }
            return true;

        } catch (Exception e) {
            errorMap.get("salary_structures").add("Failed to ensure component: " + e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private String updateStructureComponent(String structureDocName, SalaryStructureImport struct, User user) {
        try {
            // Get the existing structure document
            String[] fields = {"name", "company", "components"};
            Map<String, Object> structureDoc = frappeService.getFrappeDocument("Salary Structure",fields, structureDocName, user);
            if (structureDoc == null) {
                throw new RuntimeException("Structure document not found: " + structureDocName);
            }

            String field = struct.getType().equalsIgnoreCase("earning") ? "earnings" : "deductions";

            // Get existing components list

            List<Map<String, Object>> components = (List<Map<String, Object>>) structureDoc.get(field);
            if (components == null) {
                components = new ArrayList<>();
            }

            // Check if component already exists
            Map<String, Object> existingComp = null;
            for (Map<String, Object> comp : components) {
                if (struct.getName().equals(comp.get("salary_component"))) {
                    existingComp = comp;
                    break;
                }
            }

            Map<String, Object> comp;
            if (existingComp == null) {
                logStep("Appending " + struct.getType() + " component '" + struct.getName() + "'");
                comp = new HashMap<>();
                comp.put("salary_component", struct.getName());
                components.add(comp);
            } else {
                logStep("Updating existing " + struct.getType() + " component '" + struct.getName() + "'");
                comp = existingComp;
            }

            // Set component properties
            comp.put("abbr", struct.getAbbreviation());
            if (struct.getValeur() != null) {
                comp.put("formula", struct.getValeur().trim());
                comp.put("amount_based_on_formula", 1);
                comp.put("depends_on_payment_days", 0);
            } else {
                comp.put("amount_based_on_formula", 1);
                comp.put("depends_on_payment_days", 0);
            }

            // Update the structure document
            Map<String, Object> updateData = new HashMap<>();
            updateData.put(field, components);

            String updated = frappeService.updateFrappeDocument("Salary Structure", structureDocName, updateData, user);
            logStep("Component '" + struct.getName() + "' processed for structure '" + structureDocName + "'");
            return updated;

        } catch (Exception e) {
            throw new RuntimeException("Failed to update structure component: " + e.getMessage());
        }
    }


    /* -------------------------------------------------------------------------- */
    /*                             Additional methods                             */
    /* -------------------------------------------------------------------------- */
    private String getCurrencyForCompany(String company, User user) {
        try {
            String[] fields = new String[]{"name","default_currency"};
            Map<String, Object> companyDoc = frappeService.getFrappeDocument("Company",fields, company, user);
            return companyDoc != null ? (String) companyDoc.get("default_currency") : "USD";
        } catch (Exception e) {
            return "USD";
        }
    }

    private String checkExistingSalaryComponent(String name, User user) {
        String[] fields = new String[]{"name"};
        Map<String, Object> response = frappeService.getFrappeDocument("Salary Component",fields, name, user);
        return response != null ? name : null;
    }

    private String checkExistingSalaryStructure(String name, User user) {
        String[] fields = new String[]{"name"};
        Map<String, Object> response = frappeService.getFrappeDocument("Salary Structure",fields, name, user);
        return response != null ? name : null;
    }

    private String checkExistingEmployee(String ref, User user) {
        try {
            String[] fields = {"name", "last_name", "first_name", "gender", "date_of_birth", "date_of_joining", "company", "status"};
            List<String[]> filters = new ArrayList<>();
            filters.add(new String[]{"ref","=",ref});


            List<Map<String, Object>> employees = frappeService.searchFrappeDocuments("Employee",fields, filters, user);
            if (employees != null && !employees.isEmpty()) {
                return (String) employees.get(0).get("name");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getCompanyForEmployee(String employeeName, User user) {
        try {
            String[] fields = {"name", "last_name", "first_name", "gender", "date_of_birth", "date_of_joining", "company", "status"};
            Map<String, Object> emp = frappeService.getFrappeDocument("Employee",fields, employeeName, user);
            return emp != null ? (String) emp.get("company") : "Default Company";
        } catch (Exception e) {
            return "Default Company";
        }
    }

    private boolean ensureSalaryStructureAssignment(String employee, String salaryStructure,
            String fromDate, Double baseSalary, String company, User user) {
        try {
            String[] fields = {"employee", "salary_structure", "from_date"};

            List<String[]> filters = new ArrayList<>();
            filters.add(new String[]{"employee","=",employee});
            filters.add(new String[]{"salary_structure","=",salaryStructure});
            filters.add(new String[]{"from_date","=",OperationUtils.formatIntoFrappeDate(fromDate)});

            List<Map<String, Object>> assignments = frappeService.searchFrappeDocuments("Salary Structure Assignment",fields, filters, user);
            if (assignments != null && !assignments.isEmpty()) {
                logStep("Salary structure assignment already exists");
                return true;
            }

            // Create new assignment
            Map<String, Object> assignmentData = new HashMap<>();
            assignmentData.put("employee", employee);
            assignmentData.put("salary_structure", salaryStructure);
            assignmentData.put("from_date", OperationUtils.formatIntoFrappeDate(fromDate));
            // assignmentData.put("to_date", OperationUtils.formatIntoFrappeDate(toDate.toString()));
            assignmentData.put("base", baseSalary);
            assignmentData.put("company", company);

            String docName = frappeService.createFrappeDocument("Salary Structure Assignment", assignmentData, user, true);
            if (docName != null) {
                frappeService.submitFrappeDocument("Salary Structure Assignment", docName, user);
                logStep("Created salary structure assignment: " + docName);
                return true;
            }
            return false;

        } catch (Exception e) {
            logStep("Failed to ensure salary structure assignment: " + e.getMessage());
            return false;
        }
    }

    private String ensureFiscalYear(Date date, User user) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int year = cal.get(Calendar.YEAR);

            String fiscalYearName = String.valueOf(year);

            // Check if fiscal year exists
            String[] fields = new String[]{"name"};
            Map<String, Object> existing = frappeService.getFrappeDocument("Fiscal Year",fields, fiscalYearName, user);
            if (existing != null) {
                return fiscalYearName;
            }

            // Create fiscal year
            Map<String, Object> fiscalData = new HashMap<>();
            fiscalData.put("year", fiscalYearName);
            fiscalData.put("year_start_date", year + "-01-01");
            fiscalData.put("year_end_date", year + "-12-31");
            fiscalData.put("is_short_year", 0);

            String docName = frappeService.createFrappeDocument("Fiscal Year", fiscalData, user, true);
            logStep("Created fiscal year: " + docName);
            return docName;

        } catch (Exception e) {
            logStep("Failed to ensure fiscal year: " + e.getMessage());
            return null;
        }
    }

    /* -------------------------------------------------------------------------- */
    /*                        Master data creation methods                        */
    /* -------------------------------------------------------------------------- */
    private String getOrCreateCompany(String companyName, User user) {
        try {
            String[] fields = new String[]{"name"};
            Map<String, Object> existing = frappeService.getFrappeDocument("Company",fields, companyName, user);
            if (existing != null) {
                return companyName;
            }

            Map<String, Object> companyData = new HashMap<>();
            companyData.put("company_name", companyName);
            companyData.put("abbr", companyName.substring(0, Math.min(5, companyName.length())).toUpperCase());
            companyData.put("default_currency", "USD");
            companyData.put("country", "Madagascar");
            companyData.put("default_holiday_list", "Default Holiday List 2025");

            String docName = frappeService.createFrappeDocument("Company", companyData, user, true);
            logStep("Created company: " + docName);
            return docName;

        } catch (Exception e) {
            logStep("Failed to create company: " + e.getMessage());
            return "Default Company";
        }
    }

    private String getOrCreateDepartment(String deptName, String company, User user) {
        try {
            String[] fields = new String[]{"name"};
            Map<String, Object> existing = frappeService.getFrappeDocument("Department",fields, deptName, user);
            if (existing != null) {
                return deptName;
            }

            // Create department
            Map<String, Object> deptData = new HashMap<>();
            deptData.put("department_name", deptName);
            deptData.put("company", company);

            String docName = frappeService.createFrappeDocument("Department", deptData, user, true);
            logStep("Created department: " + docName);
            return docName;

        } catch (Exception e) {
            logStep("Failed to create department: " + e.getMessage());
            return "Human Resources";
        }
    }

    private String getOrCreateDesignation(String designation, User user) {
        try {
            String[] fields = new String[]{"name"};
            Map<String, Object> existing = frappeService.getFrappeDocument("Designation",fields, designation, user);
            if (existing != null) {
                return designation;
            }

            // Create designation
            Map<String, Object> designationData = new HashMap<>();
            designationData.put("designation_name", designation);

            String docName = frappeService.createFrappeDocument("Designation", designationData, user, true);
            logStep("Created designation: " + docName);
            return docName;

        } catch (Exception e) {
            logStep("Failed to create designation: " + e.getMessage());
            return "Employee";
        }
    }

    private String getOrCreateBranch(String branchName, String company, User user) {
        try {
            String[] fields = new String[]{"name"};
            Map<String, Object> existing = frappeService.getFrappeDocument("Branch", fields, branchName, user);
            if (existing != null) {
                return branchName;
            }

            // Create branch
            Map<String, Object> branchData = new HashMap<>();
            branchData.put("branch", branchName);
            branchData.put("company", company);

            String docName = frappeService.createFrappeDocument("Branch", branchData, user, true);
            logStep("Created branch: " + docName);
            return docName;

        } catch (Exception e) {
            logStep("Failed to create branch: " + e.getMessage());
            return "Main Branch";
        }
    }    

    /* -------------------------------------------------------------------------- */
    /*                            File parsing Methods                            */
    /* -------------------------------------------------------------------------- */
    private <T> List<T> parseFile(MultipartFile file, TypeReference<List<T>> typeRef) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String filename = file.getOriginalFilename();
            if (filename == null) {
                return null;
            }

            if (filename.toLowerCase().endsWith(".csv")) {
                return parseCsvFile(file, typeRef);
            } else if (filename.toLowerCase().endsWith(".json")) {
                return parseJsonFile(file, typeRef);
            } else {
                logStep("Unsupported file format: " + filename);
                return null;
            }
        } catch (Exception e) {
            logStep("Failed to parse file: " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> parseCsvFile(MultipartFile file, TypeReference<List<T>> typeRef) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            Class<?> targetClass = getClassFromTypeReference(typeRef);
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
                    .withType((Class<T>) targetClass)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withThrowExceptions(false) // Ne pas lever d'exceptions immédiatement
                    .build();

            List<T> beans = new ArrayList<>();
            String category = getCategoryFromTypeReference(typeRef);

            // Parcourir les résultats pour capturer les exceptions
            Iterator<T> iterator = csvToBean.iterator();
            int lineNumber = 1;
            while (iterator.hasNext()) {
                try {
                    T bean = iterator.next();
                    beans.add(bean);
                } catch (Exception exception) {
                    String errorMessage = String.format("Line %d: Failed to parse record: %s", lineNumber, exception.getMessage());
                    errorMap.get(category).add(errorMessage);
                    logStep(errorMessage);
                }
                lineNumber++;
            }

            // Collecter les beans valides
            csvToBean.iterator().forEachRemaining(beans::add);

            if (!beans.isEmpty()) {
                logStep("Parsed " + beans.size() + " records from CSV file");
            } else if (!errorMap.get(category).isEmpty()) {
                logStep("No records parsed due to errors in CSV file");
            }

            return beans.isEmpty() && !errorMap.get(category).isEmpty() ? null : beans;
        }
    }

    private <T> List<T> parseJsonFile(MultipartFile file, TypeReference<List<T>> typeRef) throws IOException {
        List<T> data = objectMapper.readValue(file.getInputStream(), typeRef);
        logStep("Parsed " + data.size() + " records from JSON file");
        return data;
    }

    private Class<?> getClassFromTypeReference(TypeReference<?> typeRef) {
        if (typeRef.getType().getTypeName().contains("EmployeeImport")) {
            return EmployeeImport.class;
        } else if (typeRef.getType().getTypeName().contains("SalaryStructureImport")) {
            return SalaryStructureImport.class;
        } else if (typeRef.getType().getTypeName().contains("SalaryRecordImport")) {
            return SalaryRecordImport.class;
        }
        throw new IllegalArgumentException("Unknown type reference: " + typeRef.getType());
    }


    /* -------------------------------------------------------------------------- */
    /*                               Utility Methods                              */
    /* -------------------------------------------------------------------------- */
    private String mapGender(String gender) {
        if (gender == null)
            return "Other";

        switch (gender.toLowerCase()) {
            case "m":
            case "male":
            case "masculin":
                return "Male";
            case "f":
            case "female":
            case "feminin":
                return "Female";
            default:
                return "Other";
        }
    }

    private void logStep(String message) {
        logger.info(message);
    }

    private boolean hasErrors() {
        return errorMap.values().stream().anyMatch(list -> !list.isEmpty());
    }

    private int getTotalCreatedRecords() {
        return logCounts.get("employees_created") +
                logCounts.get("salary_structures_created") +
                logCounts.get("salary_records_created");
    }

    private void copyErrorsToResult(ImportResult result) {
        for (Map.Entry<String, List<String>> entry : errorMap.entrySet()) {
            String category = entry.getKey();
            for (String error : entry.getValue()) {
                result.addError(category, error);
            }
        }
    }

    private String getCategoryFromTypeReference(TypeReference<?> typeRef) {
        if (typeRef.getType().getTypeName().contains("EmployeeImport")) {
            return "employees";
        } else if (typeRef.getType().getTypeName().contains("SalaryStructureImport")) {
            return "salary_structures";
        } else if (typeRef.getType().getTypeName().contains("SalaryRecordImport")) {
            return "salary_records";
        }
        return "unknown";
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public Map<String, Integer> getLogCounts() {
        return new HashMap<>(logCounts);
    }

    public Map<String, List<String>> getCreatedDocs() {
        return new HashMap<>(createdDocs);
    }

    public Map<String, List<String>> getErrorMap() {
        return new HashMap<>(errorMap);
    }

    public Map<String, String> getEmployeeRefMap() {
        return new HashMap<>(employeeRefMap);
    }

    private void initializeCounters() {
        logCounts.put("employees_processed", 0);
        logCounts.put("salary_structures_processed", 0);
        logCounts.put("salary_records_processed", 0);
        logCounts.put("employees_created", 0);
        logCounts.put("salary_structures_created", 0);
        logCounts.put("salary_records_created", 0);

        createdDocs.put("employees", new ArrayList<>());
        createdDocs.put("salary_structures", new ArrayList<>());
        createdDocs.put("salary_records", new ArrayList<>());

        errorMap.put("employees", new ArrayList<>());
        errorMap.put("salary_structures", new ArrayList<>());
        errorMap.put("salary_records", new ArrayList<>());
    }

    public static void main(String[] args) throws DateFormatException {
        System.out.println(OperationUtils.formatDate("03/04/2024"));  // should print: 03/04/2024
    }
}