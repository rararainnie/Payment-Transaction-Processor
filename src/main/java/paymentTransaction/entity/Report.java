package paymentTransaction.entity;

import lombok.*;
import java.util.*;

@Data
@Getter
@Setter
public class Report {
    private int totalRecords;
    private int validRecords;
    private int invalidRecords;
    private Map<String, Integer> invalidBreakdown = new HashMap<>();
    private Map<String, Integer> statusCounts = new HashMap<>();
    private Map<String, Double> metricsSuccess = new HashMap<>();
    private int duplicateCount;
    private List<Map<String, Object>> duplicateGroups = new ArrayList<>();
}
