package liquibase.command.core;

import liquibase.command.AbstractCommand;
import liquibase.command.CommandResult;
import liquibase.command.CommandValidationErrors;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;
import liquibase.util.FileUtil;
import liquibase.util.StringUtil;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class ExecuteSqlCommand extends AbstractCommand {

    private Database database;
    private String sql;
    private String sqlFile;
    private String delimiter = ";";

    @Override
    public String getName() {
        return "executeSql";
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSqlFile() {
        return sqlFile;
    }

    public void setSqlFile(String sqlFile) {
        this.sqlFile = sqlFile;
    }

    public void setDelimiter(String delimiter) {
      this.delimiter = delimiter;
    }
    
    @Override
    public CommandValidationErrors validate() {
        CommandValidationErrors commandValidationErrors = new CommandValidationErrors(this);
        return commandValidationErrors;
    }

    @Override
    protected CommandResult run() throws Exception {
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        String sqlText;
        if (sqlFile == null) {
            sqlText = sql;
        } else {
            File file = new File(sqlFile);
            if (! file.exists()){
              throw new LiquibaseException(String.format("The file '%s' does not exist", file.getCanonicalPath()));
            }
            sqlText = FileUtil.getContents(file);
        }

        String out = "";
        String[] sqlStrings = StringUtil.processMutliLineSQL(sqlText, true, true, delimiter);
        for (String sql : sqlStrings) {
            if (sql.toLowerCase().matches("\\s*select .*")) {
                List<Map<String, ?>> rows = executor.queryForList(new RawSqlStatement(sql));
                out += "Output of "+sql+":\n";
                if (rows.isEmpty()) {
                    out += "-- Empty Resultset --\n";
                } else {
                    SortedSet<String> keys = new TreeSet<>();
                    for (Map<String, ?> row : rows) {
                        keys.addAll(row.keySet());
                    }
                    out += StringUtil.join(keys, " | ")+" |\n";

                    for (Map<String, ?> row : rows) {
                        for (String key : keys) {
                            out += row.get(key)+" | ";
                        }
                        out += "\n";
                    }
                }
            } else {
                executor.execute(new RawSqlStatement(sql));
                out += "Successfully Executed: "+ sql+"\n";
            }
            out += "\n";
        }
        database.commit();
        return new CommandResult(out.trim());
    }

}
