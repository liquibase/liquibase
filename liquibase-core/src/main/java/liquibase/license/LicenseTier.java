package liquibase.license;

public enum LicenseTier {
    PRO("Liquibase Pro"),
    EMPIRE("Liquibase Empire");

    private final String subject;

    LicenseTier(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }
}
