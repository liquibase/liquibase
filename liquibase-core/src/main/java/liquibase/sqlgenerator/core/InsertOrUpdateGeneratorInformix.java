package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.InsertOrUpdateStatement;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

@SuppressWarnings("ALL")
public class InsertOrUpdateGeneratorInformix extends InsertOrUpdateGenerator {

  // Table Aliases for Merge references
  private static final String SOURCE_ALIAS = "src";
  private static final String DEST_ALIAS = "dst";

  @Override
  public boolean supports(InsertOrUpdateStatement statement, Database database) {
    return database instanceof InformixDatabase;
  }

  @Override
  protected String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause) {
    StringBuilder sql = new StringBuilder();
    String[] pkFields = insertOrUpdateStatement.getPrimaryKey().split(",");
    String tableReference = database.escapeTableName(insertOrUpdateStatement.getCatalogName(), insertOrUpdateStatement.getSchemaName(), insertOrUpdateStatement.getTableName());

    sql.append("MERGE INTO ").append(tableReference).append(" AS ").append(DEST_ALIAS).append("\n");
    sql.append("USING (\n");
    sql.append(getSelect(insertOrUpdateStatement, database));
    sql.append(") AS ").append(SOURCE_ALIAS).append("\n");
    sql.append("ON ");

    // Merge and match upon the Primary key fields
    for (int i = 0; i < pkFields.length; i++) {
      sql.append(DEST_ALIAS).append(".").append(pkFields[i]).append(" = ").append(SOURCE_ALIAS).append(".").append(pkFields[i]);

      if (i < pkFields.length - 1) {
        sql.append(" AND ");
      }
    }
    sql.append("\nWHEN NOT MATCHED THEN\n");

    return sql.toString();
  }

  @Override
  protected String getInsertStatement(InsertOrUpdateStatement insertOrUpdateStatement, Database database,
                                      SqlGeneratorChain sqlGeneratorChain) {
    StringBuilder columns = new StringBuilder();
    StringBuilder values = new StringBuilder();

    for (String columnKey : insertOrUpdateStatement.getColumnValues().keySet()) {
      columns.append(", ");
      columns.append(DEST_ALIAS).append(".").append(columnKey);
      values.append(SOURCE_ALIAS).append(".").append(columnKey).append(", ");
    }
    columns.delete(0, 2);

    int lastComma = values.lastIndexOf(",");
    if (lastComma > -1) {
      values.delete(lastComma, lastComma + 2);
    }

    return "INSERT (" + columns.toString() + ") VALUES (" + values.toString() + ")\n";
  }

  @Override
  protected String getElse(Database database) {
    return "";
  }

  @Override
  protected String getUpdateStatement(InsertOrUpdateStatement insertOrUpdateStatement, Database database,
                                      String whereClause, SqlGeneratorChain sqlGeneratorChain) {

    Map<String, Object> columnValues = insertOrUpdateStatement.getColumnValues();
    String[] pkFields = insertOrUpdateStatement.getPrimaryKey().split(",");

    // Only generate the update statement if non key columns exist
    if (pkFields.length != columnValues.size()) {
      StringBuilder sql = new StringBuilder("WHEN MATCHED THEN\n");
      sql.append("UPDATE SET ");

      HashSet<String> hashPkFields = new HashSet<String>(Arrays.asList(pkFields));

      for (String columnKey : columnValues.keySet()) {
        // Do not include Primary Key fields within the update
        if (!hashPkFields.contains(columnKey)) {
          sql.append(DEST_ALIAS).append(".").append(columnKey).append(" = ");
          sql.append(SOURCE_ALIAS).append(".").append(columnKey);
          sql.append(", ");
        }
      }

      int lastComma = sql.lastIndexOf(",");
      if (lastComma > -1) {
        sql.delete(lastComma, lastComma + 2);
      }
      return sql.toString();
    }
    else {
      return "";
    }
  }

  // Copied and modified from liquibase.sqlgenerator.core.InsertOrUpdateGeneratorMySQL
  private String convertToString(Object newValue, Database database) {
    String sqlString;
    if (newValue == null || newValue.toString().equals("") || newValue.toString().equalsIgnoreCase("NULL")) {
      sqlString = "NULL::INTEGER";
    } else if (newValue instanceof String && !looksLikeFunctionCall(((String) newValue), database)) {
      sqlString = "'" + database.escapeStringForDatabase(newValue.toString()) + "'";
    } else if (newValue instanceof Date) {
      sqlString = database.getDateLiteral(((Date) newValue));
    } else if (newValue instanceof Boolean) {
      if (((Boolean) newValue)) {
        sqlString = DataTypeFactory.getInstance().getTrueBooleanValue(database);
      } else {
        sqlString = DataTypeFactory.getInstance().getFalseBooleanValue(database);
      }
    } else {
      sqlString = newValue.toString();
    }
    return sqlString;
  }

  private String getSelect(InsertOrUpdateStatement insertOrUpdateStatement, Database database) {
    StringBuilder select = new StringBuilder();
    select.append("\tSELECT ");

    for (String columnKey : insertOrUpdateStatement.getColumnValues().keySet()) {
      select.append(convertToString(insertOrUpdateStatement.getColumnValue(columnKey), database));
      select.append(" AS ");
      select.append(columnKey);
      select.append(", ");
    }

    int lastComma = select.lastIndexOf(", ");
    if (lastComma > -1) {
      select.delete(lastComma, lastComma + 2);
    }

    select.append("\n\tFROM sysmaster:informix.sysdual\n");

    return select.toString();
  }

}