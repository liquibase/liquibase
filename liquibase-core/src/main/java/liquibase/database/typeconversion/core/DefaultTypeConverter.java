package liquibase.database.typeconversion.core;

import liquibase.database.Database;
import liquibase.database.structure.type.*;
import liquibase.database.typeconversion.*;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.DateParseException;
import liquibase.statement.ComputedDateValue;
import liquibase.statement.ComputedNumericValue;
import liquibase.change.ColumnConfig;
import liquibase.util.StringUtils;
import liquibase.logging.LogFactory;

import java.text.ParseException;
import java.sql.Types;
import java.math.BigInteger;

public class DefaultTypeConverter extends AbstractTypeConverter {

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(Database database) {
        return true;
    }
}
