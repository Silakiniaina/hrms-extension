package mg.hrms.services;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import mg.hrms.models.Gender;
import mg.hrms.models.SalaryComponent;
import mg.hrms.models.User;
import mg.hrms.utils.OperationUtils;

@Service
public class SalaryComponentService {

    private static final Logger logger = LoggerFactory.getLogger(SalaryComponentService.class);

    private final FrappeService frappeService; 
    private final ObjectMapper objectMapper;

    public SalaryComponentService(FrappeService frappeService, ObjectMapper objectMapper){
        this.frappeService = frappeService;
        this.objectMapper = objectMapper;
    }

    /* -------------------------------------------------------------------------- */
    /*                         Fetch all Salary Components                        */
    /* -------------------------------------------------------------------------- */
    public List<SalaryComponent> getAll(User user) throws Exception{
        OperationUtils.logStep("Fetching all companies", logger);
        String[] fields = new String[]{"name","type"};
        List<Map<String, Object>> response = frappeService.searchFrappeDocuments("Salary Component",fields, null, user);

        if (response == null) {
            logger.error("Salary Components data not found for user: {}", user.getFullName());
            throw new Exception("Salary Components data not found");
        }

        List<SalaryComponent> salaryComponents = objectMapper.convertValue(
                response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, SalaryComponent.class)
        );
        OperationUtils.logStep("Retrieve "+salaryComponents.size()+" salaryComponents", logger);
        return salaryComponents;
    }

}