package paymentTransaction.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import paymentTransaction.entity.Transaction;
import java.io.File;
import java.util.List;

public class TransactionRepository {
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Transaction> loadAll(String path) throws Exception {
        return mapper.readValue(new File(path), new TypeReference<List<Transaction>>() {});
    }

    public void save(String path, Object data) throws Exception {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path), data);
    }
}
