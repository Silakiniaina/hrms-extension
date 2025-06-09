package mg.hrms.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import mg.hrms.models.SalaryComponent;
import java.util.List;

@Service
public class SalaryComponentService {
    private final Logger logger = LoggerFactory.getLogger(SalaryComponentService.class);

    public List<SalaryComponent> getComponentsForStructure(String structureId) {
        // Implémentez la logique de récupération depuis l'API
        // ou une base de données locale si vous cachez ces données
        return List.of();
    }
}