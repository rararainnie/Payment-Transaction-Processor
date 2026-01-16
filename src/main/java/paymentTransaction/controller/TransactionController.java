package paymentTransaction.controller;
import paymentTransaction.repository.TransactionRepository;
import paymentTransaction.service.TransactionService;

public class TransactionController {
    private final TransactionRepository repo = new TransactionRepository();
    private final TransactionService service = new TransactionService();

    public void execute(String input, String output) {
        try {
            var data = repo.loadAll(input);
            var report = service.process(data);
            repo.save(output, report);
            System.out.println("Success! Report saved to " + output);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
