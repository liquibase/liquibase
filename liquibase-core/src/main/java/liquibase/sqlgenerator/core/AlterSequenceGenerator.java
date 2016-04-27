package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AlterSequenceStatement;
import liquibase.structure.core.Index;
import liquibase.structure.core.Sequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class AlterSequenceGenerator extends AbstractSqlGenerator<AlterSequenceStatement> {

    private static final int MIN_H2_MAJOR_VERSION = 1;
    private static final int MIN_H2_MINOR_VERSION = 3;
    private static final int MIN_H2_BUILD_VERSION = 175;

    @Override
    public boolean supports(AlterSequenceStatement statement, Database database) {
        return database.supportsSequences();
    }

    @Override
    public ValidationErrors validate(AlterSequenceStatement alterSequenceStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        if (databaseIsUnsupportedH2Database(database)) {
            validationErrors.checkDisallowedField("maxValue", alterSequenceStatement.getMaxValue(), database, HsqlDatabase.class, H2Database.class);
            validationErrors.checkDisallowedField("minValue", alterSequenceStatement.getMinValue(), database, H2Database.class);
        }
        else {
            validationErrors.checkDisallowedField("maxValue", alterSequenceStatement.getMaxValue(), database, HsqlDatabase.class);
        }

        validationErrors.checkDisallowedField("incrementBy", alterSequenceStatement.getIncrementBy(), database, HsqlDatabase.class, H2Database.class);
        validationErrors.checkDisallowedField("ordered", alterSequenceStatement.getOrdered(), database, HsqlDatabase.class, DB2Database.class);

        validationErrors.checkRequiredField("sequenceName", alterSequenceStatement.getSequenceName());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AlterSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ALTER SEQUENCE ");
        buffer.append(database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName()));

        if (statement.getIncrementBy() != null) {
                buffer.append(" INCREMENT BY ").append(statement.getIncrementBy());
        }

        if (statement.getMinValue() != null) {
            if (database instanceof FirebirdDatabase || database instanceof HsqlDatabase) {
                buffer.append(" RESTART WITH ").append(statement.getMinValue());
            } else {
                buffer.append(" MINVALUE ").append(statement.getMinValue());
            }
        }

        if (statement.getMaxValue() != null) {
            buffer.append(" MAXVALUE ").append(statement.getMaxValue());
        }

        if (statement.getOrdered() != null) {
            if (statement.getOrdered()) {
                buffer.append(" ORDER");
            }
        }

        return new Sql[]{
                new UnparsedSql(buffer.toString(), getAffectedSequence(statement))
        };
    }

    protected Sequence getAffectedSequence(AlterSequenceStatement statement) {
        return new Sequence().setName(statement.getSequenceName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }

    private boolean databaseIsUnsupportedH2Database(Database database) {
        return H2Database.class.isAssignableFrom(database.getClass()) && !h2DatabaseVersionSupportsMinMaxSequenceValue(database);
    }

    private boolean h2DatabaseVersionSupportsMinMaxSequenceValue(Database database) {

        try {
            if (database.getDatabaseMajorVersion() != MIN_H2_MAJOR_VERSION) {
                return database.getDatabaseMajorVersion() > MIN_H2_MAJOR_VERSION;
            }
            if (database.getDatabaseMinorVersion() != MIN_H2_MINOR_VERSION) {
                return database.getDatabaseMinorVersion() > MIN_H2_MINOR_VERSION;
            }
            return h2BuildVersionFromH2ProductionVersion(database.getDatabaseProductVersion()) >= MIN_H2_BUILD_VERSION;

        } catch (DatabaseException e) {
            LogFactory.getInstance().getLog().warning("Failed to determine database version, reported error: " + e.getMessage());
        }
        return false;
    }

    private Integer h2BuildVersionFromH2ProductionVersion(String h2ProductVersion) {

        Pattern patchVersionPattern = Pattern.compile("^(?:\\d+\\.)(?:\\d+\\.)(\\d+).*$");
        Matcher matcher = patchVersionPattern.matcher(h2ProductVersion);

        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        else {
            LogFactory.getInstance().getLog().warning("Failed to determine H2 build number from product version: " + h2ProductVersion);
            return -1;
        }

    }
}
