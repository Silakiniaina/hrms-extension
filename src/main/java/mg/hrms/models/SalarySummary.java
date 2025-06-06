package mg.hrms.models;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SalarySummary {
    @JsonProperty("month")
    private String month;

    @JsonProperty("year")
    private String year;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("gross_pay")
    private double grossPay;

    @JsonProperty("total_deduction")
    private double totalDeduction;

    @JsonProperty("net_pay")
    private double netPay;

    // Additional details for modal
    @JsonProperty("employee_id")
    private String employeeId;

    @JsonProperty("posting_date")
    private String postingDate;

    @JsonProperty("status")
    private String status;

    // Add helper method for month name
    public String getMonthName() {
        switch(month) {
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
