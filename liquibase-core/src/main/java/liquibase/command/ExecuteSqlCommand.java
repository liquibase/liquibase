package liquibase.command;

import liquibase.database.Database;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.Row;
import liquibase.statement.core.RawSqlStatement;
import liquibase.util.FileUtil;
import liquibase.util.StringUtils;

import java.io.File;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class ExecuteSqlCommand extends AbstractCommand {

    private Database database;
    private String sql;
    private String sqlFile;

    @Override
    public String getName() {
        return "execute";
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

    @Override
    public CommandValidationErrors validate() {
        CommandValidationErrors commandValidationErrors = new CommandValidationErrors(this);
        return commandValidationErrors;
    }

    @Override
    protected Object run() throws Exception {
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        String sqlText;
        if (sqlFile == null) {
            sqlText = sql;
        } else {
             sqlText = FileUtil.getContents(new File(sqlFile));
        }

        String out = "";
        String[] sqlStrings = StringUtils.processMutliLineSQL(sqlText, true, true, ";");
        for (String sql : sqlStrings) {
            if (sql.toLowerCase().matches("\\s*select .*")) {
                List<Row> rows = executor.query(new RawSqlStatement(sql)).toList();
                out += "Output of "+sql+":\n";
                if (rows.size() == 0) {
                    out += "-- Empty Resultset --\n";
                } else {
                    SortedSet<String> keys = new TreeSet<String>();
                    for (Row row : rows) {
                        keys.addAll(row.getColumns());
                    }
                    out += StringUtils.join(keys, " | ")+" |\n";

                    for (Row row : rows) {
                        for (String key : keys) {
                            out += row.get(key, String.class)+" | ";
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
        return out.trim();
    }
}
