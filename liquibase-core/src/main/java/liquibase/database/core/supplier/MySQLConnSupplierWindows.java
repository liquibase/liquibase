package liquibase.database.core.supplier;

public class MySQLConnSupplierWindows extends MySQLConnSupplier {

    @Override
    public String getConfigurationName() {
        return "liquibase.windows.2008r2.x64";
    }
}
