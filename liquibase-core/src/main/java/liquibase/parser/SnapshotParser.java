package liquibase.parser;

import liquibase.exception.LiquibaseParseException;
import liquibase.resource.ResourceAccessor;
import liquibase.snapshot.DatabaseSnapshot;

public interface SnapshotParser extends LiquibaseParser {

    public DatabaseSnapshot parse(String path, ResourceAccessor resourceAccessor) throws LiquibaseParseException;

    boolean supports(String path, ResourceAccessor resourceAccessor);

}
