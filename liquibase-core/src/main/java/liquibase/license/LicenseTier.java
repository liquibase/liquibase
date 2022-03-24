package liquibase.license;

import liquibase.util.StringUtil;

import java.util.Collections;
import java.util.List;

/**
 * The different tiers of licenses that are sold for Liquibase.
 */
public enum LicenseTier {
    EMPIRE("Liquibase Empire", 200),
    PRO("Liquibase Pro", Collections.singletonList(LicenseTier.EMPIRE), 100);

    private final String subject;

    private final List<LicenseTier> supersetLicenses;

    private final int value;

    LicenseTier(String subject, int value) {
        this(subject, null, value);
    }

    LicenseTier(String subject, List<LicenseTier> supersetLicenses, int value) {
        this.subject = subject;
        this.supersetLicenses = supersetLicenses;
        this.value = value;
    }

    public String getSubject() {
        return subject;
    }

    /**
     * The licenses which are a superset of the specified license. In other words, the licenses that a user could own
     * instead of the specified license and still have access to the features provided by the specified license. For
     * example, owning an Empire license permits access to Pro features.
     */
    public List<LicenseTier> getSupersetLicenses() {
        return supersetLicenses;
    }

    /**
     * The perceived value of a license. If multiple licenses are installed, we would only care about the license
     * with the highest value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Given a subject, find the matching LicenseTier.
     * @return the matching LicenseTier if found, null if not found
     */
    public static LicenseTier fromSubject(String subject) {
        if (!StringUtil.isEmpty(subject)) {
            for (LicenseTier value : LicenseTier.values()) {
                if (value.getSubject().equals(subject)) {
                    return value;
                }
            }
        }
        return null;
    }

}
