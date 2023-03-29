package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.CreateViewChange;
import liquibase.change.core.SetColumnRemarksChange;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;
import liquibase.util.ColumnParentType;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class MissingViewChangeGenerator extends AbstractChangeGenerator implements MissingObjectChangeGenerator {
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
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, final Database comparisonDatabase, ChangeGeneratorChain chain) {
        View view = (View) missingObject;

        CreateViewChange createViewChange = createViewChange();
        createViewChange.setViewName(view.getName());
        if (control.getIncludeCatalog()) {
            createViewChange.setCatalogName(view.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            createViewChange.setSchemaName(view.getSchema().getName());
        }
        if (view.getRemarks() != null) {
            createViewChange.setRemarks(view.getRemarks());
        }
        String selectQuery = view.getDefinition();
        boolean fullDefinitionOverridden = false;
        if (selectQuery == null) {
            selectQuery = "COULD NOT DETERMINE VIEW QUERY";
        } else if ((comparisonDatabase instanceof OracleDatabase) && (view.getColumns() != null) && !view.getColumns
            ().isEmpty()) {
            String viewName;
            if ((createViewChange.getCatalogName() == null) && (createViewChange.getSchemaName() == null)) {
                viewName = comparisonDatabase.escapeObjectName(createViewChange.getViewName(), View.class);
            } else {
                viewName = comparisonDatabase.escapeViewName(createViewChange.getCatalogName(), createViewChange.getSchemaName(), createViewChange.getViewName());
            }
            selectQuery = "CREATE OR REPLACE FORCE VIEW "+ viewName
                    + " (" + StringUtil.join(view.getColumns(), ", ", obj -> {
                        if ((((Column) obj).getComputed() != null) && ((Column) obj).getComputed()) {
                            return ((Column) obj).getName();
                        } else {
                            return comparisonDatabase.escapeColumnName(null, null, null, ((Column) obj).getName(), false);
                        }
                    }) + ") AS "+selectQuery;
            createViewChange.setFullDefinition(true);
            fullDefinitionOverridden = true;

        }
        createViewChange.setSelectQuery(selectQuery);
        if (!fullDefinitionOverridden) {
            createViewChange.setFullDefinition(view.getContainsFullDefinition());
        }

        List<SetColumnRemarksChange> columnRemarksList = new ArrayList<>();
        view.getColumns()
                .stream()
                .filter(column -> Objects.nonNull(column.getRemarks()))
                .forEach(column -> {
                            SetColumnRemarksChange columnRemarks = new SetColumnRemarksChange();
                            columnRemarks.setColumnName(column.getName());
                            columnRemarks.setColumnDataType(column.getType().getTypeName());
                            columnRemarks.setRemarks(column.getRemarks());
                            columnRemarks.setCatalogName(control.getIncludeCatalog() ? view.getSchema().getCatalogName() : null);
                            columnRemarks.setSchemaName(control.getIncludeSchema() ? view.getSchema().getName() : null);
                            columnRemarks.setTableName(column.getRelation().getName());
                            columnRemarks.setColumnParentType(ColumnParentType.VIEW.name());
                            columnRemarksList.add(columnRemarks);
                        }
                );

        Change[] viewChange = new Change[] { createViewChange };
        return Stream.concat(Arrays.stream(viewChange), columnRemarksList.stream())
                .toArray(Change[]::new);
    }

    protected CreateViewChange createViewChange() {
        return new CreateViewChange();
    }
}
