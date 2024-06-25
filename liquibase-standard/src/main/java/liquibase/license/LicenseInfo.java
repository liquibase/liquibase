package liquibase.license;

import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

@Setter
@Getter
public class LicenseInfo {
    private String issuedTo;
    private Date expirationDate;

    public LicenseInfo(String issuedTo, Date expirationDate) {
        this.issuedTo = issuedTo;
        this.expirationDate = expirationDate;
    }

    public String formatExpirationDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        return dateFormat.format(expirationDate);
    }
}
