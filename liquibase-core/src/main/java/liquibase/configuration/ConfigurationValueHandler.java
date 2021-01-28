package liquibase.configuration;

public interface ConfigurationValueHandler<DataType> {

    DataType convert(Object value) throws IllegalArgumentException;
}
