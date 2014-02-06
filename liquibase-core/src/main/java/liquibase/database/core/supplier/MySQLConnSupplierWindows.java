package liquibase.database.core.supplier;

public class MySQLConnSupplierWindows extends MySQLConnSupplier {

    @Override
    public String getConfigurationName() {
        return "windows";
    }
}
