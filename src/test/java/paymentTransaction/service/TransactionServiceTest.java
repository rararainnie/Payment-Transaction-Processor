package paymentTransaction.service;

import org.junit.jupiter.api.Test;
import paymentTransaction.entity.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest {
    private final TransactionService service = new TransactionService();

    @Test
    void testValidationAndDuplicates() {
        // Test Case 1: Validation failures and Rule 1 Duplicate (Same TXID)
        Transaction t1 = new Transaction("TX01", "M01", 100.0, "THB", "SUCCESS", "2023-10-01T10:00:00Z");
        Transaction t2 = new Transaction("TX01", "M01", 100.0, "THB", "SUCCESS", "2023-10-01T10:00:00Z");
        Transaction t3 = new Transaction("TX03", "M02", -50.0, "THB", "SUCCESS", "2023-10-01T10:00:00Z");

        Report report = service.process(List.of(t1, t2, t3));

        assertEquals(1, report.getInvalidRecords()); // Should catch t3
        assertTrue(report.getDuplicateCount() >= 1); // Should catch t1/t2 duplicate
        assertEquals("TXID", report.getDuplicateGroups().get(0).get("rule"));
    }

    @Test
    void testDuplicateRule2() {
        // Test Case 2: Rule 2 Duplicate (Merchant + Amount + Day)
        // Different IDs, but same details on the same day
        Transaction t1 = new Transaction("TX_A", "REF_X", 500.0, "THB", "SUCCESS", "2023-11-01T09:00:00Z");
        Transaction t2 = new Transaction("TX_B", "REF_X", 500.0, "THB", "SUCCESS", "2023-11-01T14:00:00Z");

        Report report = service.process(List.of(t1, t2));

        assertEquals(1, report.getDuplicateCount());
        assertEquals("MERCHANT_AMOUNT_DAY", report.getDuplicateGroups().get(0).get("rule"));

        // Verify involved IDs are listed
        List<?> ids = (List<?>) report.getDuplicateGroups().get(0).get("ids");
        assertTrue(ids.contains("TX_A"));
        assertTrue(ids.contains("TX_B"));
    }

    @Test
    void testSummaryAndIdempotency() {
        // Test Case 3: Idempotency (Count SUCCESS only once per ID)
        Transaction t1 = new Transaction("TX01", "M01", 100.0, "THB", "SUCCESS", "2023-10-01T10:00:00Z");
        Transaction t2 = new Transaction("TX01", "M01", 100.0, "THB", "SUCCESS", "2023-10-01T10:00:00Z");
        Transaction t3 = new Transaction("TX03", "M02", 500.0, "THB", "SUCCESS", "2023-10-01T10:00:00Z");

        Report report = service.process(List.of(t1, t2, t3));

        // Expect 2 SUCCESS records (TX01 counted once, TX03 counted once)
        assertEquals(2, report.getStatusCounts().get("SUCCESS"));
        assertEquals(300.0, report.getMetricsSuccess().get("avg")); // (100 + 500) / 2
    }
}