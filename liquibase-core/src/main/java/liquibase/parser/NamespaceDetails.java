package liquibase.parser;

import liquibase.serializer.LiquibaseSerializer;
import liquibase.servicelocator.PrioritizedService;

public interface NamespaceDetails extends PrioritizedService{

    public static int PRIORITY_EXTENSION = PRIORITY_DATABASE;

    boolean supports(LiquibaseSerializer serializer, String namespaceOrUrl);

    boolean supports(LiquibaseParser parser, String namespaceOrUrl);

    String getShortName(String namespaceOrUrl);

    String getSchemaUrl(String namespaceOrUrl);

    String[] getNamespaces();

    String getLocalPath(String namespaceOrUrl);

}
