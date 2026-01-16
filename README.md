**Note:** This project was developed with the assistance of **Gemini Pro**.
# Payment Transaction Processor
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
