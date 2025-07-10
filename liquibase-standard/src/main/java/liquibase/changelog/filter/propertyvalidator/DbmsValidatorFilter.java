package liquibase.changelog.filter.propertyvalidator;

import liquibase.GlobalConfiguration;
import liquibase.changelog.RawChangeSet;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DbmsValidatorFilter implements ValidatorFilter {

    private final List<String> validDbmsValues;

    public DbmsValidatorFilter() {
        this.validDbmsValues = fetchSupportedDatabases();
    }

    private List<String> fetchSupportedDatabases() {
        return DatabaseFactory.getInstance()
                .getImplementedDatabases()
                .stream()
                .map(Database::getShortName)
                .collect(Collectors.toList());
    }

    private boolean isValidDbms(String dbms) {
        return this.validDbmsValues.stream().anyMatch(validDbms -> validDbms.equalsIgnoreCase(dbms));
    }

    @Override
    public ChangeSetFilterResult accepts(RawChangeSet changeSet) {
        boolean strict = GlobalConfiguration.STRICT.getCurrentValue();
        StringBuilder errors = new StringBuilder();

        if(strict) {
            if (changeSet.getDbms() == null) {
                return new ChangeSetFilterResult(true, "", ChangeSetFilter.class ,"validDBMS", "dbms");
            }

            String[] dbmsList = changeSet.getDbms() != null ? changeSet.getDbms().split(",") : null;

            for (String dbms : dbmsList) {
                String cleanDbms = dbms.startsWith("!") ? dbms.substring(1) : dbms.trim();
                if((cleanDbms.isEmpty())) {
                    errors.append(String.format("%n\t- dbms value cannot be empty while on Strict mode"));
                } else {
                    if( (!(Objects.equals(cleanDbms, "all") || Objects.equals(cleanDbms, "none") || isValidDbms(cleanDbms)))) {
                        errors.append(String.format("%n\t- %s is not a valid dbms value", cleanDbms));
                    }
                }

            }
        }
        if(errors.length() == 0) {
            return new ChangeSetFilterResult(true, "Valid dbms values: " + String.join(", ", validDbmsValues), ChangeSetFilter.class, "validDBMS", "dbms");
        } else {
            return new ChangeSetFilterResult(false, errors.toString(), ChangeSetFilter.class, "invalidDBMS", "dbms");
        }
    }
}
