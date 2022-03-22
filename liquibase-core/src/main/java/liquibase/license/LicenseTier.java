package liquibase.license;

import java.util.Collections;
import java.util.List;

public enum LicenseTier {
    EMPIRE("Liquibase Empire"),
    PRO("Liquibase Pro", Collections.singletonList(LicenseTier.EMPIRE));

    private final String subject;
    /**
     * The licenses which are a superset of the specified license. In other words, the licenses that a user could own
     * instead of the specified license and still have access to the features provided by the specified license. For
     * example, owning an Empire license permits access to Pro features.
     */
    private final List<LicenseTier> supersetLicenses;

    LicenseTier(String subject) {
        this(subject, null);
    }

    LicenseTier(String subject, List<LicenseTier> supersetLicenses) {
        this.subject = subject;
        this.supersetLicenses = supersetLicenses;
    }

    public String getSubject() {
        return subject;
    }

    public List<LicenseTier> getSupersetLicenses() {
        return supersetLicenses;
    }
}
