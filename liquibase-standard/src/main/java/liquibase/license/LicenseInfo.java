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
    private Date issuedDate;

    /**
     * This constructor is used by the Liquibase AWS License Service extension and should not be removed.
     */
    public LicenseInfo(String issuedTo, Date expirationDate) {
        this.issuedTo = issuedTo;
        this.expirationDate = expirationDate;
    }

    public String formatExpirationDate() {
        if (expirationDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            return dateFormat.format(expirationDate);
        } else {
            return null;
        }
    }
}
