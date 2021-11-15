package liquibase.integration.spring;

public interface Registrable<T> {
    void register(T register);
}
