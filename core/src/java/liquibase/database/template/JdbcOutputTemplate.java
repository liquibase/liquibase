package liquibase.database.template;

import liquibase.database.Database;
import liquibase.database.sql.SqlStatement;
import liquibase.exception.JDBCException;
import liquibase.util.StreamUtil;

import java.io.IOException;
import java.io.Writer;

public class JdbcOutputTemplate extends JdbcTemplate {

    private Writer output;

    public JdbcOutputTemplate(Writer output, Database database) {
        super(database);
        this.output = output;
    }

    public boolean executesStatements() {
        return false;
    }

    public void execute(SqlStatement sql) throws JDBCException {
        outputStatement(sql);
    }

    public int update(SqlStatement sql) throws JDBCException {
        outputStatement(sql);

        return 0;
    }

    public void comment(String message) throws JDBCException {
        try {
            output.write(database.getLineComment());
            output.write(" ");
            output.write(message);
            output.write(StreamUtil.getLineSeparator());
        } catch (IOException e) {
            throw new JDBCException(e);
        }
    }

    private void outputStatement(SqlStatement sql) throws JDBCException {
        try {
            output.write(sql.getSqlStatement(database));
            output.write(";");
            output.write(StreamUtil.getLineSeparator());
            output.write(StreamUtil.getLineSeparator());
        } catch (IOException e) {
            throw new JDBCException(e);
        }
    }
}
