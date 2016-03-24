package liquibase.serializer;

import liquibase.servicelocator.PrioritizedService;

public interface LiquibaseSerializer extends PrioritizedService {

    String[] getValidFileExtensions();

    String serialize(LiquibaseSerializable object, boolean pretty);


}
