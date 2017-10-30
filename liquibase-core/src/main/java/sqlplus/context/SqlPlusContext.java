package sqlplus.context;

import liquibase.database.sqlplus.SqlPlusConnection;

import java.lang.reflect.Field;

/**
 * @author gette
 */
public class SqlPlusContext {
    private boolean sqlplus = false;
    //manual mode unavailable in Linux OS
    private boolean manual = false;
    private SqlPlusConnection sqlPlusConnection;
    private static SqlPlusContext INSTANCE = new SqlPlusContext();

    public static SqlPlusContext getInstance() {
        return INSTANCE;
    }

    public void setBooleanFieldValue(String fieldName, Boolean value) throws NoSuchFieldException, IllegalAccessException {
        Field field = getClass().getDeclaredField(fieldName);
        field.setBoolean(this, value);
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
