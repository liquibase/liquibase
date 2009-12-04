package liquibase.sqlgenerator.core;

import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.core.InsertOrUpdateStatement;

/**
 * Created by IntelliJ IDEA.
 * User: bassettt
 * Date: Dec 2, 2009
 * Time: 12:15:34 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class InsertOrUpdateGenerator implements SqlGenerator<InsertOrUpdateStatement> {

    public int getPriority() {
        return PRIORITY_DEFAULT;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
