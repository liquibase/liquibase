package liquibase.database.core.supplier;

public class MySQLSupplierWindows extends MySQLSupplier {

    @Override
    public String getConfigurationName() {
        return "windows";
    }
}
