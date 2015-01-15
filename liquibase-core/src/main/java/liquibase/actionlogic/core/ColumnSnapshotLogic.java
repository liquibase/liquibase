package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.QueryColumnsMetaDataAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RowBasedQueryResult;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ActionPerformException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.JdbcUtils;
import liquibase.util.SmartMap;
import liquibase.util.Validate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

public class ColumnSnapshotLogic extends AbstractJdbcMetaDataLogic {

    @Override
    protected Class<? extends DatabaseObject> getTypeToSnapshot() {
        return Column.class;
    }

    @Override
    protected Class<? extends DatabaseObject>[] getSupportedBaseObject() {
        return new Class[] {
                Column.class,
                Relation.class,
                Schema.class,
                Catalog.class
        };
    }

    @Override
    protected List<SmartMap> readRawMetaData(DatabaseObject relatedTo, Class<? extends DatabaseObject> typeToSnapshot, DatabaseMetaData metaData, Scope scope) throws SQLException {
        String catalogName = null;
        String schemaName = null;
        String relationName = null;
        String columnName = null;

        if (relatedTo instanceof Catalog) {
            catalogName = relatedTo.getName();
        } else if (relatedTo instanceof Schema) {
            catalogName = ((Schema) relatedTo).getCatalogName();
            schemaName = relatedTo.getName();
        } else if (relatedTo instanceof Relation) {
            relationName = relatedTo.getName();

            Schema schema = relatedTo.getSchema();
            if (schema != null) {
                catalogName = schema.getCatalogName();
                schemaName = schema.getName();
            }
        } else if (relatedTo instanceof Column) {
            columnName = relatedTo.getName();

            Relation relation = ((Column) relatedTo).getRelation();
            relationName = relation.getName();

            Schema schema = relation.getSchema();
            if (schema != null) {
                catalogName = schema.getCatalogName();
                schemaName = schema.getName();
            }
        } else {
            throw Validate.failure("Unexpected type: "+relatedTo.getClass().getName());
        }

        return JdbcUtils.extract(metaData.getColumns(catalogName, schemaName, relationName, columnName));
    }

    @Override
    protected DatabaseObject convertToObject(SmartMap row, Class outputType, Scope scope) {
        return null;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        AbstractJdbcDatabase database = scope.get(Scope.Attr.database, AbstractJdbcDatabase.class);
        Connection underlyingConnection = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();

        QueryColumnsMetaDataAction queryAction = (QueryColumnsMetaDataAction) action;
        try {
            return new RowBasedQueryResult(JdbcUtils.extract(underlyingConnection.getMetaData().getColumns(
                    queryAction.getAttribute(QueryColumnsMetaDataAction.Attr.catalogName, String.class),
                    queryAction.getAttribute(QueryColumnsMetaDataAction.Attr.schemaName, String.class),
                    queryAction.getAttribute(QueryColumnsMetaDataAction.Attr.tableName, String.class),
                    queryAction.getAttribute(QueryColumnsMetaDataAction.Attr.columnName, String.class))));
        } catch (SQLException e) {
            throw new ActionPerformException(e);
        }
    }
}
