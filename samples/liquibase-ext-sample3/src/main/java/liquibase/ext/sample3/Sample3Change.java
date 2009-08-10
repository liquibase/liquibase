package liquibase.ext.sample3;

import liquibase.change.AbstractChange;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateTableStatement;
import liquibase.database.Database;

public class Sample3Change extends AbstractChange {
    private String name;
    private Sample3Child child;
    private Sample3Child child2;

    public Sample3Change() {
        super("sample3", "Sample 3", 15);
    }

    public String getConfirmationMessage() {
        return "Sample 3 executed";
    }

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
            new CreateTableStatement(null, "sample3").addColumn("id", "int").addColumn("name", "varchar(5)")
        };
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Sample3Child getChild() {
        return child;
    }

    public Sample3Child getChild2() {
        return child2;
    }

    public Sample3Child createSample3Sub() {
        child = new Sample3Child();
        return child;
    }

    public Sample3Child createSample3Sub2() {
        child2 = new Sample3Child();
        return child2;
    }
}
