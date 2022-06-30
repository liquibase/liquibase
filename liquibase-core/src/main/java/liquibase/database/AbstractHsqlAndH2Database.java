package liquibase.database;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractHsqlAndH2Database extends AbstractJdbcDatabase {

    private final Pattern pattern = Pattern.compile("(\\d+)(,).*(\\d+)");

    @Override
    public String getAutoIncrementClause(BigInteger startWith, BigInteger incrementBy, String generationType, Boolean defaultOnNull) {
        StringBuilder autoIncrement = new StringBuilder(super.getAutoIncrementClause(startWith, incrementBy, generationType, defaultOnNull));
        if(startWith != null && incrementBy != null) {
            Matcher m = pattern.matcher(autoIncrement.toString());
            if(m.find()) {
                String[] splittedClause = autoIncrement.toString().split("\\(");
                autoIncrement = new StringBuilder();
                autoIncrement.append(splittedClause[0]).append("(START WITH "+startWith.longValue()).append(" INCREMENT BY "+incrementBy.longValue()+")");
            }
        }
        return autoIncrement.toString();
    }
}
