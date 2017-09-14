package sqlplus.context;

import liquibase.database.sqlplus.SqlPlusConnection;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by sbt-gette-is on 13.09.2017.
 */
public class SqlPlusContext {
    private boolean sqlplus = false;
    private boolean manual = false;
    private SqlPlusConnection sqlPlusConnection;
    private static SqlPlusContext INSTANCE = new SqlPlusContext();

    public static SqlPlusContext getInstance() {
        return INSTANCE;
    }

    public void setBooleanFieldValue(String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = getClass().getDeclaredField(fieldName);
        field.setBoolean(this, true);
    }

    public Boolean isSqlplus(){
        return sqlplus;
    }

    public Boolean isManual(){
        return manual;
    }

    public void initSqlPlusConnection(SqlPlusConnection sqlPlusConnection) {
        this.sqlPlusConnection = sqlPlusConnection;
    }

    public SqlPlusConnection getConnection() {
        return sqlPlusConnection;
    }


}
