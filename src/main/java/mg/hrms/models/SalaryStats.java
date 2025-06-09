package mg.hrms.models;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalaryStats {
    @JsonProperty("year")
    private String year;
    @JsonProperty("month")
    private String month;
    @JsonProperty("total_gross_pay")
    private Double totalGrossPay;
    @JsonProperty("total_deductions")
    private Double totalDeductions;
    @JsonProperty("total_net_pay")
    private Double totalNetPay;

    private List<SalaryComponent> earningsDetails;
    private List<SalaryComponent> deductionsDetails;

    public String getMonthName() {
        if (month == null) return "N/A";
        switch (month) {
            case "01": return "January";
            case "02": return "February";
            case "03": return "March";
            case "04": return "April";
            case "05": return "May";
            case "06": return "June";
            case "07": return "July";
            case "08": return "August";
            case "09": return "September";
            case "10": return "October";
            case "11": return "November";
            case "12": return "December";
            default: return month;
        }
    }
}
