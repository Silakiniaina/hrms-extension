package mg.hrms.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import mg.hrms.models.SalarySlip;
import mg.hrms.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SalarySlipService {

    private static final Logger logger = LoggerFactory.getLogger(SalarySlipService.class);
    private final RestApiService restApiService;
    private final ObjectMapper objectMapper;

    public SalarySlipService(RestApiService restApiService, ObjectMapper objectMapper) {
        this.restApiService = restApiService;
        this.objectMapper = objectMapper;
    }

    public SalarySlip getById(User user, String slipId) throws Exception {
        logger.info("Fetching salary slip by ID: {}", slipId);
        String[] fields = {"name", "employee","employee_name", "posting_date", "salary_structure", "gross_pay", "net_pay", "status","bank_name","bank_account_no","earnings", "deductions"};
        String apiUrl = restApiService.buildResourceUrl("Salary Slip", slipId, fields);

        var response = restApiService.executeApiCall(
                apiUrl, HttpMethod.GET, null, user, new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response.getBody() == null || response.getBody().get("data") == null) {
            logger.error("Salary slip not found: {}", slipId);
            throw new Exception("Salary slip not found");
        }

        SalarySlip slip = objectMapper.convertValue(response.getBody().get("data"), SalarySlip.class);
        logger.info("Retrieved salary slip: {}", slipId);
        return slip;
    }

    public List<SalarySlip> getAllForEmployee(User user, String employeeId) throws Exception {
        logger.info("Fetching salary slips for employee: {}", employeeId);
        String[] fields = {"name", "employee","employee_name", "posting_date", "salary_structure", "gross_pay", "net_pay", "status","bank_name","bank_account_no"};
        List<String[]> filters = new ArrayList<>();
        if(employeeId != null){
            filters.add(new String[]{"employee", "=", employeeId});
        }

        filters.add(new String[]{"docstatus", "=", "1"});

        String apiUrl = restApiService.buildUrl("Salary Slip", fields, filters);
        var response = restApiService.executeApiCall(
                apiUrl, HttpMethod.GET, null, user, new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response.getBody() == null || response.getBody().get("data") == null) {
            logger.warn("No salary slips found for employee: {}", employeeId);
            return new ArrayList<>();
        }

        List<SalarySlip> slips = objectMapper.convertValue(
                response.getBody().get("data"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, SalarySlip.class)
        );
        logger.info("Retrieved {} salary slips for employee: {}", slips.size(), employeeId);
        return slips;
    }
}
