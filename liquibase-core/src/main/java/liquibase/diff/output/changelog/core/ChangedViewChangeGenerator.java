package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.CreateViewChange;
import liquibase.change.core.SetTableRemarksChange;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ChangedViewChangeGenerator extends AbstractChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (View.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[]{
                Table.class
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, final Database comparisonDatabase, ChangeGeneratorChain chain) {
        View view = (View) changedObject;

        CreateViewChange change = createViewChange();
        change.setViewName(view.getName());
        change.setReplaceIfExists(true);
        if (control.getIncludeCatalog()) {
            change.setCatalogName(view.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(view.getSchema().getName());
        }
        String selectQuery = view.getDefinition();
        boolean fullDefinitionOverridden = false;
        if (selectQuery == null) {
            selectQuery = "COULD NOT DETERMINE VIEW QUERY";
        } else if ((comparisonDatabase instanceof OracleDatabase) && (view.getColumns() != null) && !view.getColumns
            ().isEmpty()) {
            String viewName;
            if ((change.getCatalogName() == null) && (change.getSchemaName() == null)) {
                viewName = comparisonDatabase.escapeObjectName(change.getViewName(), View.class);
            } else {
                viewName = comparisonDatabase.escapeViewName(change.getCatalogName(), change.getSchemaName(), change.getViewName());
            }
            selectQuery = "CREATE OR REPLACE FORCE VIEW " + viewName
                    + " (" + StringUtils.join(view.getColumns(), ", ", new StringUtils.StringUtilsFormatter() {
                @Override
                public String toString(Object obj) {
                    if ((((Column) obj).getComputed() != null) && ((Column) obj).getComputed()) {
                        return ((Column) obj).getName();
                    } else {
                        return comparisonDatabase.escapeColumnName(null, null, null, ((Column) obj).getName(), false);
                    }
                }
            }) + ") AS " + selectQuery;
            change.setFullDefinition(true);
            fullDefinitionOverridden = true;

        }
        change.setSelectQuery(selectQuery);
        if (!fullDefinitionOverridden) {
            change.setFullDefinition(view.getContainsFullDefinition());
        }

        List<Change> changes = new ArrayList<>();
        changes.add(change);

        Difference changedRemarks = differences.getDifference("remarks");
        if (changedRemarks != null) {
            SetTableRemarksChange setRemarksChange = new SetTableRemarksChange();
            if (control.getIncludeCatalog()) {
                setRemarksChange.setCatalogName(view.getSchema().getCatalogName());
            }
            if (control.getIncludeSchema()) {
                setRemarksChange.setSchemaName(view.getSchema().getName());
            }

            setRemarksChange.setTableName(view.getName());
            setRemarksChange.setRemarks(view.getRemarks());

            changes.add(setRemarksChange);
        }


        return changes.toArray(new Change[changes.size()]);
    }

    protected CreateViewChange createViewChange() {
        return new CreateViewChange();
    }

}
