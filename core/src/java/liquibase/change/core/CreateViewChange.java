package liquibase.change.core;

import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.CreateViewStatement;
import liquibase.statement.DropViewStatement;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;
import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.Change;

import java.util.ArrayList;
import java.util.List;

/**
 * Creats a new view.
 */
public class CreateViewChange extends AbstractChange {

    private String schemaName;
    private String viewName;
    private String selectQuery;
    private Boolean replaceIfExists;

    public CreateViewChange() {
        super("createView", "Create View", ChangeMetaData.PRIORITY_DEFAULT);
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public String getSelectQuery() {
        return selectQuery;
    }

    public void setSelectQuery(String selectQuery) {
        this.selectQuery = selectQuery;
    }

    public Boolean getReplaceIfExists() {
        return replaceIfExists;
    }

    public void setReplaceIfExists(Boolean replaceIfExists) {
        this.replaceIfExists = replaceIfExists;
    }

    public SqlStatement[] generateStatements(Database database) {
    	List<SqlStatement> statements = new ArrayList<SqlStatement>();
    
        boolean replaceIfExists = false;
        if (getReplaceIfExists() != null && getReplaceIfExists()) {
            replaceIfExists = true;
        }
        
        if (!supportsReplaceIfExistsOption(database) && replaceIfExists) {
        	statements.add(new DropViewStatement(
        			getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), 
        			getViewName()));
        	statements.add(new CreateViewStatement(
         			getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), 
         			getViewName(), getSelectQuery(), false));
        } else {
         	statements.add(new CreateViewStatement(
         			getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), 
         			getViewName(), getSelectQuery(), replaceIfExists));
        }
        
        return statements.toArray(new SqlStatement[statements.size()]);
    }

    public String getConfirmationMessage() {
        return "View "+getViewName()+" created";
    }

    @Override
    protected Change[] createInverses() {
        DropViewChange inverse = new DropViewChange();
        inverse.setViewName(getViewName());

        return new Change[]{
                inverse
        };
    }

    private boolean supportsReplaceIfExistsOption(Database database) {
    	return !(database instanceof SQLiteDatabase);
    }

}
