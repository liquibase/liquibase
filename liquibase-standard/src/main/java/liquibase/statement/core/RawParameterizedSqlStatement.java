package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import liquibase.util.StringUtil;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RawParameterizedSqlStatement extends AbstractSqlStatement {

    private final String sql;
    private final List<Object> parameters = new ArrayList<>();
    @Setter
    private String endDelimiter  = ";";

    public RawParameterizedSqlStatement(String sql) {
        this.sql = sql;
    }

    public RawParameterizedSqlStatement(String sql, Object... parameters) {
        this(sql);
        if (parameters != null) {
            this.parameters.addAll(Arrays.asList(parameters));
        }
    }

    public String getSql() {
        return sql;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public RawParameterizedSqlStatement addParameter(Object parameter) {
        this.parameters.add(parameter);

        return this;
    }

    @Override
    public String toString() {
        return !parameters.isEmpty() ? sql + " with " + StringUtil.join(parameters, ",", new StringUtil.ToStringFormatter()) : sql ;
    }

    public String getEndDelimiter() {
        if(this.endDelimiter != null) {
            return this.endDelimiter.replace("\\r","\r").replace("\\n","\n");
        }
        return ";";

    }
}
