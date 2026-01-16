package paymentTransaction.entity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Transaction {
    @JsonProperty("ID")
    private String id;

    @JsonProperty("Ref")
    private String ref;

    @JsonProperty("Amount")
    private double amount;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Date")
    private String date;

    public String getLocalDateString() {
        try {
            if (date == null) return "";
            return ZonedDateTime.parse(date).toLocalDate().toString();
        } catch (Exception e) {
            return "";
        }
    }
}
