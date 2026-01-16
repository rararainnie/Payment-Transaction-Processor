package paymentTransaction.service;

import paymentTransaction.entity.*;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionService {
    public Report process(List<Transaction> transactions) {
        Report report = new Report();
        report.setTotalRecords(transactions.size());

        // Initialize status counts map
        report.getStatusCounts().put("SUCCESS", 0);
        report.getStatusCounts().put("FAILED", 0);
        report.getStatusCounts().put("PENDING", 0);

        List<Transaction> validTxs = new ArrayList<>();

        // 1. Validation: Check all rules (Date, Amount, Missing fields)
        for (Transaction tx : transactions) {
            String error = validate(tx);
            if (error == null) {
                validTxs.add(tx);
            } else {
                report.setInvalidRecords(report.getInvalidRecords() + 1);
                report.getInvalidBreakdown().put(error, report.getInvalidBreakdown().getOrDefault(error, 0) + 1);
            }
        }

        // 2. Duplicate Detection: Check both Rule 1 and Rule 2 on valid transactions
        detectDuplicates(validTxs, report);

        // 3. Idempotency: Keep only one unique ID per transaction
        Map<String, Transaction> uniqueMap = validTxs.stream()
                .collect(Collectors.toMap(Transaction::getId, t -> t, (t1, t2) -> t1));

        report.setValidRecords(uniqueMap.size());

        // 4. Summary: Calculate counts and metrics based on unique transactions
        for (Transaction t : uniqueMap.values()) {
            report.getStatusCounts().put(t.getStatus(), report.getStatusCounts().get(t.getStatus()) + 1);
        }

        calculateSuccessMetrics(new ArrayList<>(uniqueMap.values()), report);

        return report;
    }

    // ---------------- VALIDATION ----------------
    private String validate(Transaction t) {
        if (t.getId() == null || t.getRef() == null || t.getCurrency() == null ||
                t.getStatus() == null || t.getDate() == null) return "MISSING_FIELDS";

        if (t.getAmount() <= 0) return "INVALID_AMOUNT";
        if (!t.getCurrency().matches("[A-Z]{3}")) return "INVALID_CURRENCY";
        if (!Set.of("SUCCESS", "FAILED", "PENDING").contains(t.getStatus())) return "INVALID_STATUS";

        try {
            // Ensure date is valid UTC ISO-8601
            ZonedDateTime zdt = ZonedDateTime.parse(t.getDate());
            if (!zdt.getOffset().equals(ZoneOffset.UTC)) return "INVALID_DATE";
        } catch (Exception e) {
            return "INVALID_DATE";
        }
        return null;
    }

    // ---------------- DUPLICATES ----------------
    private void detectDuplicates(List<Transaction> txs, Report report) {
        // Rule 1: Same Transaction ID
        txs.stream().collect(Collectors.groupingBy(Transaction::getId))
                .forEach((id, list) -> { if (list.size() > 1) addDup(report, "TXID", list); });

        // Rule 2: Same Merchant + Amount + Day
        txs.stream().collect(Collectors.groupingBy(t ->
                        t.getRef() + "|" + t.getAmount() + "|" + t.getCurrency() + "|" + t.getDate().substring(0, 10)))
                .forEach((k, list) -> { if (list.size() > 1) addDup(report, "MERCHANT_AMOUNT_DAY", list); });

        report.setDuplicateCount(report.getDuplicateGroups().size());
    }

    private void addDup(Report r, String rule, List<Transaction> list) {
        // Add duplicate group details (include all involved IDs)
        r.getDuplicateGroups().add(Map.of("rule", rule, "ids", list.stream().map(Transaction::getId).toList()));
    }

    // ---------------- SUMMARY ----------------
    private void calculateSuccessMetrics(List<Transaction> txs, Report report) {
        DoubleSummaryStatistics stats = txs.stream()
                .filter(t -> "SUCCESS".equals(t.getStatus()))
                .mapToDouble(Transaction::getAmount).summaryStatistics();

        if (stats.getCount() > 0) {
            report.getMetricsSuccess().put("min", stats.getMin());
            report.getMetricsSuccess().put("max", stats.getMax());
            report.getMetricsSuccess().put("avg", stats.getAverage());
        }
    }
}