package liquibase.integration.spring;

public interface MountRegistrable<T extends Registrable> extends Registrable<T> {

    Class<T> registerPoint();
}
