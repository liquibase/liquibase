package liquibase.validator.propertyvalidator;

import liquibase.GlobalConfiguration;
import liquibase.validator.RawChangeSet;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;

/**
 * This filter will make sure that validate command reports an error if the id or author (Required) fields are not provided in a {@link RawChangeSet}.
 * It checks if the id and author are not null or are not empty strings when strict mode is enabled.
 * If any of these values are empty or null, it won't accept the provided changeSet and will provide a validation error message.
 */
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
