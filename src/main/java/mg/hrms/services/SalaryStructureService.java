package mg.hrms.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import mg.hrms.models.SalaryStructure;
import mg.hrms.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SalaryStructureService {

    private static final Logger logger = LoggerFactory.getLogger(SalaryStructureService.class);
    private final FrappeService frappeService;
    private final ObjectMapper objectMapper;

    public SalaryStructureService(FrappeService frappeService, ObjectMapper objectMapper) {
        this.frappeService = frappeService;
        this.objectMapper = objectMapper;
    }

    /* -------------------------------------------------------------------------- */
    /*                   Get a salary structure by its name (ID)                  */
    /* -------------------------------------------------------------------------- */
    public SalaryStructure getByName(User user, String name) throws Exception {
        logger.info("Fetching salary structure by name: {}", name);
        String[] fields = {"name", "company", "components"};
        
        Map<String, Object> response = frappeService.getFrappeDocument("Salary Structure", fields, name, user);

        if (response == null) {
            logger.error("Salary structure not found: {}", name);
            throw new Exception("Salary structure not found");
        }

        SalaryStructure structure = objectMapper.convertValue(response, SalaryStructure.class);
        logger.info("Retrieved salary structure: {}", name);
        return structure;
    }

    /* -------------------------------------------------------------------------- */
    /*                         Fetch all salary structures                        */
    /* -------------------------------------------------------------------------- */
    public List<SalaryStructure> getAll(User user) throws Exception {
        logger.info("Fetching all salary structures for user: {}", user.getFullName());
        String[] fields = {"name", "company", "components"};
        
        List<Map<String, Object>> response = frappeService.searchFrappeDocuments("Salary Structure", fields, null, user);

        if (response == null) {
            logger.error("Salary structures data not found for user: {}", user.getFullName());
            throw new Exception("Salary structures data not found");
        }

        List<SalaryStructure> structures = objectMapper.convertValue(
                response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, SalaryStructure.class)
        );
        logger.info("Retrieved {} salary structures for user: {}", structures.size(), user.getFullName());
        return structures;
    }
}