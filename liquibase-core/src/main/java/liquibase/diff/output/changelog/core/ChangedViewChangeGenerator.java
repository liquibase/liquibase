package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.CreateViewChange;
import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;
import liquibase.util.StringUtils;

public class ChangedViewChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (View.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[] {
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

        CreateViewChange change = new CreateViewChange();
        change.setViewName(view.getName());
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
//todo: action refactoring        } else if (comparisonDatabase instanceof OracleDatabase && view.getColumns() != null && view.getColumns().size() > 0) {
//            String viewName;
//            if (change.getCatalogName() == null && change.getSchemaName() == null) {
//                viewName = comparisonDatabase.escapeObjectName(change.getViewName(), View.class);
//            } else {
//                viewName = comparisonDatabase.escapeViewName(change.getCatalogName(), change.getSchemaName(), change.getViewName());
//        }
//            selectQuery = "CREATE OR REPLACE FORCE VIEW "+ viewName
//                    + " (" + StringUtils.join(view.getColumns(), ", ", new StringUtils.StringUtilsFormatter() {
//                @Override
//                public String toString(Object obj) {
//                    if (((Column) obj).getComputed() != null && ((Column) obj).getComputed()) {
//                        return ((Column) obj).getName();
//                    } else {
//                        return comparisonDatabase.escapeColumnName(null, null, null, ((Column) obj).getName(), false);
//                    }
//                }
//            }) + ") AS "+selectQuery;
//            change.setFullDefinition(true);
//            fullDefinitionOverridden = true;

        }
        change.setSelectQuery(selectQuery);
        if (!fullDefinitionOverridden) {
            change.setFullDefinition(view.getContainsFullDefinition());
        }

        return new Change[] { change };
    }
}
