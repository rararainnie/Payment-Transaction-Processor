package paymentTransaction;

import paymentTransaction.controller.TransactionController;
public class PaymentTransactionApplication {
    public static void main(String[] args) {
        String input = "transactions.json";
        String output = "report.json";

        for (int i = 0; i < args.length; i++) {
            if ("--input".equals(args[i])) input = args[++i];
            if ("--output".equals(args[i])) output = args[++i];
        }

        new TransactionController().execute(input, output);
    }
}
