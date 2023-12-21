package liquibase.util;

import liquibase.Beta;
import liquibase.logging.mdc.customobjects.UpdateSummary;
import lombok.Data;
import lombok.ToString;

/**
 * Container to handle sharing update summary message between different services
 */
@Data
@Beta
@ToString
public class UpdateSummaryDetails {
    private UpdateSummary summary;
    private String output;
}
