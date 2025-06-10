package mg.hrms.models;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalaryChartData {
    private List<String> labels;
    private List<ChartDataset> datasets;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChartDataset {
        private String label;
        private List<Double> data;
        private String borderColor;
        private String backgroundColor;
        private boolean fill;
        private double tension; // Changed from int to double
        
        public ChartDataset(String label, List<Double> data, String borderColor, String backgroundColor) {
            this.label = label;
            this.data = data;
            this.borderColor = borderColor;
            this.backgroundColor = backgroundColor;
            this.fill = false;
            this.tension = 0.4; // For smooth curves
        }
    }
}