package liquibase.license;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
public class LicenseInfo {
    private String issuedTo;
    private Date expirationDate;
    private String info;

    public String formatExpirationDate() {
        if (expirationDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            return dateFormat.format(expirationDate);
        } else {
            return null;
        }
    }
}
