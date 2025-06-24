package mg.hrms.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import mg.hrms.models.Gender;
import mg.hrms.models.User;
import mg.hrms.utils.OperationUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GenderService {

    private static final Logger logger = LoggerFactory.getLogger(GenderService.class);
    private final FrappeService frappeService;
    private final ObjectMapper objectMapper;

    public GenderService(ObjectMapper objectMapper, FrappeService frappeService) {
        this.objectMapper = objectMapper;
        this.frappeService = frappeService;
    }

    /* -------------------------------------------------------------------------- */
    /*                              Fetch all genders                             */
    /* -------------------------------------------------------------------------- */
    public List<Gender> getAll(User user) throws Exception {
        OperationUtils.logStep("Fetching all companies", logger);
        String[] fields = new String[]{"name"};
        List<Map<String, Object>> response = frappeService.searchFrappeDocuments("Gender",fields, null, user);

        if (response == null) {
            logger.error("Genders data not found for user: {}", user.getFullName());
            throw new Exception("Genders data not found");
        }

        List<Gender> genders = objectMapper.convertValue(
                response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Gender.class)
        );
        OperationUtils.logStep("Retrieve "+genders.size()+" genders", logger);
        return genders;
    }
}