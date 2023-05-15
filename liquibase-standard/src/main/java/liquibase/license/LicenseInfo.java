package liquibase.license;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LicenseInfo {
    private String issuedTo;
    private Date expirationDate;

    public LicenseInfo(String issuedTo, Date expirationDate) {
        this.issuedTo = issuedTo;
        this.expirationDate = expirationDate;
    }

    public String getIssuedTo() {
        return issuedTo;
    }

    public void setIssuedTo(String issuedTo) {
        this.issuedTo = issuedTo;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String formatExpirationDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        return dateFormat.format(expirationDate);
    }
}
