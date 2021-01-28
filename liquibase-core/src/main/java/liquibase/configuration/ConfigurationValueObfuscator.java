package liquibase.configuration;

public interface ConfigurationValueObfuscator<DataType> {

    DataType obfuscate(DataType value);
}
