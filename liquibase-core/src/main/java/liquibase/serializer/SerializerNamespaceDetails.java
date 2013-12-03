package liquibase.serializer;

import liquibase.parser.LiquibaseParser;
import liquibase.servicelocator.PrioritizedService;

public interface SerializerNamespaceDetails extends PrioritizedService{

    public static int PRIORITY_EXTENSION = PRIORITY_DATABASE;

    boolean supports(LiquibaseSerializer serializer, String namespace);

    boolean supports(LiquibaseParser parser, String namespace);

    String getShortName(String namespace);

    String getSchemaUrl(String namespace);

    String getLocalPath(String namespace);

}
