package mg.hrms.models.dataImport;
import java.util.List;

import lombok.Data;

@Data
public class ImportData {
    private List<EmployeeImport> employees;
    private List<SalaryStructureImport> salaryStructures;
    private List<SalaryRecordImport> salaryRecords;
}
