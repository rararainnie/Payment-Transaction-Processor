**Note:** This project was developed with the assistance of **Gemini Pro**.
# Part A coding task: Payment Transaction Processor
A Java CLI tool to validate transactions, detect duplicates, and generate a summary report.

---

## Features
- Transaction validation
- Duplicate detection (TXID & business rules)
- Idempotent processing
- Summary report generation
- Unit-tested core logic

---

## 1. Build the Project
Compile the source code and run all unit tests:
```
mvn clean package
```

## 2. Run the Application
You can execute the processor using the helper script or directly via Java.
Option A: Using Batch Script (Recommended for Windows)
```
app.bat --input transactions.json --output report.json
```
Option B: Using Java Command
```
java -cp "target/classes;target/dependency/*" paymentTransaction.PaymentTransactionApplication --input transactions.json --output report.json
```

## 3. Run Tests To verify the logic (Validation, Duplicates, Idempotency), run: 
```
mvn test
```
--- 

## Assumptions
- Idempotency (Bonus): If a TXID appears multiple times, only the first valid record counts towards the summary metrics. Others are flagged as duplicates.
- Date Format: Input must be strict ISO-8601 UTC (e.g., 2023-10-01T10:00:00Z).
- Duplicate Rules:
  - TXID: Identical Transaction ID.
  - MERCHANT_AMOUNT_DAY: Same Ref + Amount + Currency + Date (YYYY-MM-DD).
 
## Trade-offs
- In-Memory Processing: I used a List to load all transactions.
  - Pro: Simple logic for grouping and sorting.
  - Con: High memory usage for very large files (Stream/DB would be better for production).
- Validation Strategy: The app skips invalid records and reports them (Fail-Safe) rather than stopping execution immediately.

--- 

# Part B Applied Scenario: Payment flow
<img width="672" height="675" alt="image" src="https://github.com/user-attachments/assets/7d7aff97-bca1-4c27-a5ad-68edbeb4d24b" />

## 1. Key Steps
- Create Order: Save order in memory with PENDING status to reserve stock.
- Payment Intent: Backend requests an Intent from the Gateway (locking amount server-side) to get a payment URL.
- Redirect: Send user to the Gateway’s secure hosted page to pay.
- Webhook Callback: Receive async notification from Gateway (the source of truth).
- Update State: Verify webhook and update order to PAID or FAILED.

## 2. Handling Failures & Reliability
- Webhook Retries: Respond with 200 OK immediately upon receipt to prevent Gateway timeouts.
- Idempotency: Track event_id. If a duplicate ID arrives, ignore it to prevent double processing.
- Timeout: Background job checks PENDING orders >30 mins. If unpaid, mark as EXPIRED.
- Reconciliation: Nightly job compares Gateway reports with our records to ensure data consistency.

## 3. Security Considerations
- No Sensitive Data: Never touch PAN/CVV. Use hosted pages to reduce PCI scope.
- Secure Storage: API keys are loaded from Environment Variables, never hardcoded.
- Signature Verification: Validate HMAC-SHA256 signatures on all webhooks to prevent spoofing

## 4. Monitoring (Bonus)
Key Metrics: Track Payment Success Rate, Webhook Error Rate, and End-to-End Latency.
