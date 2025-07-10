package liquibase.changelog.filter.propertyvalidator;

import liquibase.GlobalConfiguration;
import liquibase.changelog.RawChangeSet;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;

public class RequiredFieldsValidatorFilter implements ValidatorFilter {

    @Override
    public ChangeSetFilterResult accepts(RawChangeSet changeSet) {
        String id = changeSet.getId();
        String author = changeSet.getAuthor();
        boolean strict = GlobalConfiguration.STRICT.getCurrentValue();

        if (id == null || author == null) {
            return new ChangeSetFilterResult(false, "id and author are required fields and cannot be null", ChangeSetFilter.class, "requiredFieldsCannotBeNull", "idAuthor");
        } else {
            if(strict) {
                if(id.trim().isEmpty() || author.trim().isEmpty()) {
                    return new ChangeSetFilterResult(false, "id and author cannot be empty while on Strict mode", ChangeSetFilter.class, "requiredFieldsCannotBeEmptyOnStrictMode", "idAuthor");
                }
            }
        }

        return new ChangeSetFilterResult(true, "id and author values have been provided", ChangeSetFilter.class, "validRequiredFields", "idAuthor");
    }
}
