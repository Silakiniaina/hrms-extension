package mg.hrms.models.dataImport;

import lombok.Data;

import java.util.List;

@Data
public class ImportData {
    private List<EmployeeImport> employees;
    private List<SalaryStructureImport> salaryStructures;
    private List<SalaryRecordImport> salaryRecords;
}
