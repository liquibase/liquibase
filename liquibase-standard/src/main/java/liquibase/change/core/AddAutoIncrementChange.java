package liquibase.change.core;

import java.math.BigInteger;
import java.util.Locale;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeNote;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddAutoIncrementStatement;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.statement.core.SetNullableStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import lombok.Setter;

/**
 * Makes an existing column into an auto-increment column.
 * This change is only valid for databases with auto-increment/identity columns.
 * The current version does not support MS-SQL.
 */
@DatabaseChange(name = "addAutoIncrement",
    description = "Converts an existing column to be an auto-increment (a.k.a 'identity') column",
    priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column",
    databaseNotes = {@DatabaseChangeNote(
        database = "sqlite", notes = "If the column type is not INTEGER it is converted to INTEGER"
    )}
)
@Setter
public class AddAutoIncrementChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;
    private BigInteger startWith;
    private BigInteger incrementBy;
    private Boolean defaultOnNull;
    private String generationType;

    @DatabaseChangeProperty(since = "3.0", mustEqualExisting = "column.relation.catalog", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation", description = "Name of the table")
    public String getTableName() {
        return tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column", description = "Name of the column")
    public String getColumnName() {
        return columnName;
    }

    @DatabaseChangeProperty(exampleValue = "int", description = "Current data type of the column to make auto-increment")
    public String getColumnDataType() {
        return columnDataType;
    }

    @DatabaseChangeProperty(exampleValue = "100", description = "Initial value of the increment")
    public BigInteger getStartWith() {
        return startWith;
    }

    @DatabaseChangeProperty(exampleValue = "1", description = "Amount to increment by at each call")
    public BigInteger getIncrementBy() {
        return incrementBy;
    }

    @DatabaseChangeProperty(exampleValue = "true", since = "3.6",
        description = "When using generationType of BY DEFAULT then defaultOnNull=true allows the identity to be used " +
            "if the identity column is referenced, but a value of NULL is specified.")
    public Boolean getDefaultOnNull() {
        return defaultOnNull;
    }

    @DatabaseChangeProperty(exampleValue = "ALWAYS", since = "3.6",
        description = "Type of the generation in \"GENERATED %s AS IDENTITY\". Default: \"|\".")
    public String getGenerationType() {
        return generationType;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        if (database instanceof PostgresDatabase) {
            String sequenceName = (getTableName() + "_" + getColumnName() + "_seq");

            String escapedTableName = database.escapeObjectName(getTableName(), Table.class);
            String escapedColumnName = database.escapeObjectName(getColumnName(), Table.class);
            if ((escapedTableName != null) && (escapedColumnName != null) && !escapedTableName.startsWith("\"") &&
                !escapedColumnName.startsWith("\"")
            ) {
                sequenceName = sequenceName.toLowerCase(Locale.US);
            }

            String schemaPrefix;
            if (this.schemaName == null) {
                schemaPrefix = database.getDefaultSchemaName();
            } else {
                schemaPrefix = this.schemaName;
            }

            SequenceNextValueFunction nvf = new SequenceNextValueFunction(schemaPrefix, sequenceName);

            final CreateSequenceStatement createSequenceStatement = new CreateSequenceStatement(catalogName, this.schemaName, sequenceName);
            createSequenceStatement.setIncrementBy(this.getIncrementBy());
            createSequenceStatement.setStartValue(this.getStartWith());

            return new SqlStatement[]{
                    createSequenceStatement,
                    new SetNullableStatement(catalogName, this.schemaName, getTableName(), getColumnName(), null, false),
                    new AddDefaultValueStatement(catalogName, this.schemaName, getTableName(), getColumnName(), getColumnDataType(), nvf)
            };
        }

        return new SqlStatement[]{new AddAutoIncrementStatement(getCatalogName(), getSchemaName(), getTableName(),
            getColumnName(), getColumnDataType(), getStartWith(), getIncrementBy(), getDefaultOnNull(), getGenerationType())};
    }

    @Override
    public String getConfirmationMessage() {
        return "Auto-increment added to " + getTableName() + "." + getColumnName();
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        Column example = new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), getColumnName());
        try {
            Column column = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
            if (column == null) {
                return result.unknown("Column does not exist");
            }

            result.assertComplete(column.isAutoIncrement(), "Column is not auto-increment");
            if ((getStartWith() != null) && (column.getAutoIncrementInformation().getStartWith() != null)) {
                result.assertCorrect(getStartWith().equals(column.getAutoIncrementInformation().getStartWith()),
                     "startsWith incorrect");
            }

            if ((getIncrementBy() != null) && (column.getAutoIncrementInformation().getIncrementBy() != null)) {
                result.assertCorrect(getIncrementBy().equals(column.getAutoIncrementInformation().getIncrementBy()),
                     "Increment by incorrect");
            }

            if (getGenerationType() != null && column.getAutoIncrementInformation().getGenerationType() != null) {
                result.assertCorrect(getGenerationType().equals(column.getAutoIncrementInformation().getGenerationType()),
                     "Generation type is incorrect");
            }

            if (getDefaultOnNull() != null && column.getAutoIncrementInformation().getDefaultOnNull() != null) {
                result.assertCorrect(getDefaultOnNull().equals(column.getAutoIncrementInformation().getDefaultOnNull()),
                     "Default on null is incorrect");
            }

            return result;
        } catch (DatabaseException|InvalidExampleException e) {
            return result.unknown(e);
        }
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
