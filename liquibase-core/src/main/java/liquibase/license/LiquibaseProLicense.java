package liquibase.license;

public class LiquibaseProLicense {
  public static final String LIQUIBASE_PRO_LICENSE = "LIQUIBASE_PRO_LICENSE";
  public enum LiquibaseLicenseStatus {
    VALID, INVALID, NONE
  }
  public enum LiquibaseLicenseType {
    PRO
  }
  private LiquibaseLicenseStatus licenseStatus;
  private int daysToExpiration;
  private LiquibaseLicenseType licenseType;
  private String key;

  public LiquibaseProLicense(LiquibaseLicenseType licenseType) {
    this.licenseType = licenseType;
    String licenseValue = System.getenv(LIQUIBASE_PRO_LICENSE);
  }

  public int getDaysToExpiration() {
    return daysToExpiration;
  }

  public LiquibaseLicenseType getLicenseType() {
    return licenseType;
  }

  public LiquibaseLicenseStatus getLicenseStatus() {
    return licenseStatus;
  }
}
