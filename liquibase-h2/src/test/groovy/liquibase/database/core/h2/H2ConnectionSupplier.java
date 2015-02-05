package liquibase.database.core.h2;

import liquibase.JUnitScope;
import liquibase.Scope;
import liquibase.action.ExecuteSqlAction;
import liquibase.actionlogic.ActionExecutor;
import liquibase.database.ConnectionSupplier;
import liquibase.database.DatabaseConnection;
import liquibase.exception.ActionPerformException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.structure.ObjectName;
import testmd.logic.SetupResult;

import java.util.ArrayList;
import java.util.List;

public class H2ConnectionSupplier extends ConnectionSupplier {

    @Override
    public String getDatabaseShortName() {
        return "h2";
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:h2:mem:liquibase";
    }

    @Override
    public String getPrimaryCatalog() {
        return "LIQUIBASE";
        }

    @Override
    public String getPrimarySchema() {
        return "PUBLIC";
        }

    List<ObjectName> containers = new ArrayList<>();

    int maxDepth = getDatabase().getMaxContainerDepth();

    @Override
    protected List<ObjectName> getContainers(boolean includeParials) {
        List<ObjectName> containers = new ArrayList<>();

        containers.add(new ObjectName(getPrimarySchema()));
        containers.add(new ObjectName(getAlternateSchema()));
        if (includeParials) {
            containers.add(new ObjectName());
        }
        return containers;
    }

    protected void initConnection(Scope scope) {
        try {
            new ActionExecutor().execute(new ExecuteSqlAction("CREATE SCHEMA "+getAlternateSchema()), scope);
        } catch (ActionPerformException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}
