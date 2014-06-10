package liquibase.change.core;

import java.util.ArrayList;
import java.util.List;
import liquibase.change.AbstractChange;
import liquibase.change.DatabaseChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.ColumnConfig;
import liquibase.change.genericConfig;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;

@DatabaseChange(name="modifySql", description = "empty", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class modifySqlChange extends AbstractChange {
    private String dbms;
    private String context;
    private boolean applyToRollback = false;
    private List<genericConfig> append;

    public modifySqlChange() {
        super();
        append = new ArrayList<genericConfig>();
    }
    
    public String getContext(){
        return this.context;
    }
    
    public void setContext( String context ){
        this.context = context;
    }
    
    public void setdbms( String dbms ){
        this.dbms = dbms;
    }
    
    public String getdbms(){
        return this.dbms;
    }
    
    public void setappend( List<genericConfig> append ){
        this.append = append;
    }
    
    public void addappend( genericConfig append ){
        this.append .add(append);
    }
    
    public List<genericConfig> getappend(){
        return this.append;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[0];
    }

    @Override
    public String getConfirmationMessage() {
        return "Empty change did nothing";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
    
}
