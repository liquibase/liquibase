package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawCallStatement;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates Tibero database support.
 */
public class TiberoDatabase extends AbstractJdbcDatabase {

  public static final String PRODUCT_NAME = "tibero";

  private final Set<String> reservedWords = new HashSet<>();


  /**
   * Default constructor for an object that represents the Tibero Database DBMS.
   */
  public TiberoDatabase() {
    super.unquotedObjectsAreUppercased = true;
    //noinspection HardCodedStringLiteral
    super.setCurrentDateTimeFunction("SYSTIMESTAMP");
    // Setting list of Tibero's native functions
    //noinspection HardCodedStringLiteral
    dateFunctions.add(new DatabaseFunction("SYSDATE"));
    //noinspection HardCodedStringLiteral
    dateFunctions.add(new DatabaseFunction("SYSTIMESTAMP"));
    //noinspection HardCodedStringLiteral
    dateFunctions.add(new DatabaseFunction("CURRENT_TIMESTAMP"));
    //noinspection HardCodedStringLiteral
    super.sequenceNextValueFunction = "%s.nextval";
    //noinspection HardCodedStringLiteral
    super.sequenceCurrentValueFunction = "%s.currval";
    reservedWords.addAll(Arrays.asList(
        "ACCESS", "ADD", "ALL", "ALTER", "AND", "ANY", "AS", "ASC", "AUDIT",
        "BETWEEN", "BY",
        "CHAR", "CHECK", "CLUSTER", "COLUMN", "COMMENT", "COMPRESS", "CREATE", "CURRENT",
        "DATE", "DECIMAL", "DEFAULT", "DELETE", "DESC", "DISTINCT", "DROP",
        "ELSE", "EXCEPT", "EXCLUSIVE", "EXISTS",
        "FILE", "FLOAT", "FOR", "FOREIGN", "FROM",
        "GRANT", "GROUP", "HAVING",
        "IDENTIFIED", "IMMEDIATE", "IN", "INDEX", "INDEXES", "INITIAL", "INSERT", "INTEGER", "INTERSECT", "INTO", "INVALIDATE", "IS",
        "LEVEL", "LESS", "LIKE", "LOCK", "LONG",
        "MAXEXTENTS", "MINUS", "MODE", "MODIFY",
        "NOAUDIT", "NOCOMPRESS", "NOT", "NOWAIT", "NULL", "NUMBER",
        "OF", "OFFLINE", "ON", "ONLINE", "OPTION", "OR", "ORDER",
        "PRIMARY", "PRIOR", "PRIVILEGES", "PUBLIC",
        "RAW", "RENAME", "REVOKE", "ROW", "ROWID", "ROWNUM", "ROWS",
        "SELECT", "SESSION", "SET", "SHARE", "SIZE", "SMALLINT", "START", "SUCCESSFUL", "SYNONYM", "SYSDATE",
        "TABLE", "THAN", "THEN", "TO", "TRIGGER",
        "UID", "UNION", "UNIQUE", "UPDATE", "USER",
        "VALIDATE", "VALUES", "VARCHAR", "VARCHAR2", "VIEW",
        "WHENEVER", "WHERE", "WITH"
        ));
  }

  @Override
  public int getPriority() {
    return PRIORITY_DEFAULT;
  }

  @Override
  public String getShortName() {
    //noinspection HardCodedStringLiteral
    return "tibero";
  }

  @Override
  protected String getDefaultDatabaseProductName() {
    //noinspection HardCodedStringLiteral
    return "Tibero";
  }

  @Override
  public Integer getDefaultPort() {
    return 8629;
  }


  @Override
  public boolean supportsInitiallyDeferrableColumns() {
    return true;
  }

  @Override
  public boolean isReservedWord(String objectName) {
    return reservedWords.contains(objectName.toUpperCase());
  }

  @Override
  protected SqlStatement getConnectionSchemaNameCallStatement() {
    return new RawCallStatement("select sys_context( 'userenv', 'current_schema' ) from dual");
  }

  @Override
  public boolean supportsSequences() {
    return true;
  }

  @Override
  public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
    return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
  }

  @Override
  public String getDefaultDriver(String url) {
    //noinspection HardCodedStringLiteral
    if (url.startsWith("jdbc:tibero")) {
      return "com.tmax.tibero.jdbc.TbDriver";
    }
    return null;
  }


  @Override
  public boolean supportsTablespaces() {
    return true;
  }

  @Override
  public boolean supportsAutoIncrement() {
    return false;
  }


  @Override
  public boolean supportsRestrictForeignKeys() {
    return false;
  }

  @Override
  public boolean supportsDatabaseChangeLogHistory() {
    return false;
  }
  
  

}
