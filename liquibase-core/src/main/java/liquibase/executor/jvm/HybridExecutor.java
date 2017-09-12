package liquibase.executor.jvm;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;

import java.util.ArrayList;

/**
 * Extending JdbcExecutor to allow us to use SQLPlus instead of JDBC
 * @author gette j
 */
public class HybridExecutor extends JdbcExecutor {
    private Logger log = LogFactory.getLogger();
    @Override
    public void execute(final SqlStatement sql) throws DatabaseException {
        if (database instanceof AbstractJdbcDatabase)
            execute(sql, new ArrayList<SqlVisitor>());
        else {
            log.debug("Using SQLPlus. Please, stand by!");
        }
    }


}
