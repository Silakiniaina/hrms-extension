package mg.hrms.models;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSalaryDetail {
    private String employeeId;
    private String employeeName;
    private String year;
    private String month;
    private Double totalGrossPay;
    private Double totalNetPay;
    private Double totalDeductions;
    private List<SalaryComponent> earningsDetails;
    private List<SalaryComponent> deductionsDetails;
    private List<SalarySlip> salarySlips; // Liste des bulletins de salaire pour ce mois

    public String getMonthName() {
        if (month == null) return "";

        String[] monthNames = {
            "", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };

        try {
            int monthNum = Integer.parseInt(month);
            if (monthNum >= 1 && monthNum <= 12) {
                return monthNames[monthNum];
            }
        } catch (NumberFormatException e) {
            // Ignore
        }

        return month;
    }
}
