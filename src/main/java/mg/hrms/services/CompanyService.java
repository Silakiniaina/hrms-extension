package mg.hrms.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import mg.hrms.models.Company;
import mg.hrms.models.User;
import mg.hrms.utils.OperationUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CompanyService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyService.class);
    private final FrappeService frappeService;
    private final ObjectMapper objectMapper;

    public CompanyService(ObjectMapper objectMapper, FrappeService frappeService) {
        this.objectMapper = objectMapper;
        this.frappeService = frappeService;
    }

    /* -------------------------------------------------------------------------- */
    /*                         Method to get All companies                        */
    /* -------------------------------------------------------------------------- */
    public List<Company> getAll(User user) throws Exception {
        OperationUtils.logStep("Fetching all companies", logger);
        String[] fields = new String[]{"name"};
        List<Map<String, Object>> response = frappeService.searchFrappeDocuments("Company",fields, null, user);

        if (response == null) {
            logger.error("Companies data not found for user: {}", user.getFullName());
            throw new Exception("Companies data not found");
        }

        List<Company> companies = objectMapper.convertValue(
                response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Company.class)
        );
        OperationUtils.logStep("Retrieve "+companies.size()+" compnies", logger);
        return companies;
    }
}