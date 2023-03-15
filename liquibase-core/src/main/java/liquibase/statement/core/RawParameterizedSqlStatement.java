package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RawParameterizedSqlStatement extends AbstractSqlStatement {

    private final String sql;
    private final List<Object> parameters = new ArrayList<>();

    public RawParameterizedSqlStatement(String sql, Object... parameters) {
        this.sql = sql;
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
        return sql + " with " + StringUtil.join(parameters, ",", new StringUtil.ToStringFormatter());
    }
}
