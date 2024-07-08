package liquibase.change.core;

import static liquibase.change.ChangeParameterMetaData.ALL;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropViewStatement;
import liquibase.structure.core.View;
import lombok.Setter;

/**
 * Drops an existing view.
 */
@DatabaseChange(name = "dropView", description = "Drops an existing view", priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "view")
@Setter
public class DropViewChange extends AbstractChange {
    private String catalogName;
    private String schemaName;
    private String viewName;
    private Boolean ifExists;

    @DatabaseChangeProperty(mustEqualExisting ="view.catalog", since = "3.0", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="view.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "view", description = "Name of the view to drop")
    public String getViewName() {
        return viewName;
    }

    @DatabaseChangeProperty(since = "4.19.0", supportsDatabase = ALL,
        description = "Appends IF EXISTS to the DROP VIEW statement. If ifExists=true, the view is only dropped if it already exists, but the migration continues even if the view does not exist. If ifExists=false and the view does not exist, the database returns an error. Default: false.")
    public Boolean isIfExists() {
        return ifExists;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
                new DropViewStatement(getCatalogName(), getSchemaName(), getViewName(), isIfExists()),
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new View(getCatalogName(), getSchemaName(), getViewName()), database), "View exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }


    @Override
    public String getConfirmationMessage() {
        return "View "+getViewName()+" dropped";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
