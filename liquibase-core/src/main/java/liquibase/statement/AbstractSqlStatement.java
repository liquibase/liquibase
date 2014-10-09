package liquibase.statement;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public abstract class AbstractSqlStatement implements SqlStatement {

    @Override
    public boolean skipOnUnsupported() {
        return false;
    }

    @Override
    public String toString() {
      return ReflectionToStringBuilder.toString(this);
    }
    
    
}
