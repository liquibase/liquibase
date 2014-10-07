package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.logging.LogFactory;
import liquibase.precondition.AbstractPrecondition;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Schema;
import liquibase.exception.*;
import liquibase.precondition.Precondition;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

public class IndexExistsPrecondition extends AbstractPrecondition {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnNames;
    private String indexName;

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (getIndexName() == null && getTableName() == null && getColumnNames() == null) {
            validationErrors.addError("indexName OR tableName and columnNames is required");
        }
        return validationErrors;
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
    	try {
            Schema schema = new Schema(getCatalogName(), getSchemaName());
            Index example = new Index();
            String tableName = StringUtils.trimToNull(getTableName());
            if (tableName != null) {
                example.setTable((Table) new Table()
                        .setName(database.correctObjectName(getTableName(), Table.class))
                        .setSchema(schema));
            }
            example.setName(database.correctObjectName(getIndexName(), Index.class));
            if (StringUtils.trimToNull(getColumnNames()) != null) {
                for (String column : getColumnNames().split("\\s*,\\s*")) {
                    example.addColumn(new Column(database.correctObjectName(column, Column.class)));
                }
            }
            if (!SnapshotGeneratorFactory.getInstance().has(example, database)) {
                String name = "";

                if (getIndexName() != null) {
                    name += database.escapeObjectName(getIndexName(), Index.class);
                }

                if (tableName != null) {
                    name += " on "+database.escapeObjectName(getTableName(), Table.class);

                    if (StringUtils.trimToNull(getColumnNames()) != null) {
                        name += " columns "+getColumnNames();
                    }
                }
                throw new PreconditionFailedException("Index "+ name +" does not exist", changeLog, this);
            }
        } catch (Exception e) {
            if (e instanceof PreconditionFailedException) {
                throw (((PreconditionFailedException) e));
            }
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    @Override
    public String getName() {
        return "indexExists";
    }

    @Override
    public String toString() {
        String string = "Index Exists Precondition: ";

        if (getIndexName() != null) {
            string += getIndexName();
        }

        if (tableName != null) {
            string += " on "+getTableName();

            if (StringUtils.trimToNull(getColumnNames()) != null) {
                string += " columns "+getColumnNames();
            }
        }

        return string;
    }
}