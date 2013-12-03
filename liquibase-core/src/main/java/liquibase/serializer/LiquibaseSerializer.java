package liquibase.serializer;

public interface LiquibaseSerializer {

    String[] getValidFileExtensions();

    String serialize(LiquibaseSerializable object, boolean pretty);


}
