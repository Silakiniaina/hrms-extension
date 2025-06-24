package mg.hrms.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import mg.hrms.models.SalarySlip;
import mg.hrms.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SalarySlipService {

    private static final Logger logger = LoggerFactory.getLogger(SalarySlipService.class);
    private final FrappeService frappeService;
    private final ObjectMapper objectMapper;

    public SalarySlipService(FrappeService frappeService, ObjectMapper objectMapper) {
        this.frappeService = frappeService;
        this.objectMapper = objectMapper;
    }

    /* -------------------------------------------------------------------------- */
    /*                    Fetch a salary slip by its name (ID)                    */
    /* -------------------------------------------------------------------------- */
    public SalarySlip getById(User user, String slipId) throws Exception {
        logger.info("Fetching salary slip by ID: {}", slipId);
        String[] fields = {"name", "employee","employee_name", "posting_date", "salary_structure", "gross_pay", "net_pay", "status","bank_name","bank_account_no","earnings","deductions"};

        Map<String, Object> response = frappeService.getFrappeDocument("Salary Slip", fields, slipId, user);

        if (response == null) {
            logger.error("Salary slip not found: {}", slipId);
            throw new Exception("Salary slip not found");
        }

        SalarySlip slip = objectMapper.convertValue(response, SalarySlip.class);
        logger.info("Retrieved salary slip: {}", slipId);
        return slip;
    }

    /* -------------------------------------------------------------------------- */
    /*                    Get all salary slips for an employee                    */
    /* -------------------------------------------------------------------------- */
    public List<SalarySlip> getAllForEmployee(User user, String employeeId) throws Exception {
        logger.info("Fetching salary slips for employee: {}", employeeId);
        String[] fields = {"name", "employee","employee_name", "posting_date", "salary_structure", "gross_pay", "net_pay", "status","bank_name","bank_account_no"};
        List<String[]> filters = new ArrayList<>();
        if(employeeId != null){
            filters.add(new String[]{"employee", "=", employeeId});
        }

        filters.add(new String[]{"docstatus", "=", "1"});

        List<Map<String, Object>> response = frappeService.searchFrappeDocuments("Salary Slip", fields, filters, user);
        if (response == null) {
            logger.warn("No salary slips found for employee: {}", employeeId);
            return new ArrayList<>();
        }

        List<SalarySlip> slips = objectMapper.convertValue(
                response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, SalarySlip.class)
        );
        logger.info("Retrieved {} salary slips for employee: {}", slips.size(), employeeId);
        return slips;
    }
}
