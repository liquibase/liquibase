package liquibase.statement.core

import liquibase.datatype.core.BigIntType
import liquibase.datatype.core.IntType
import liquibase.datatype.core.VarcharType
import liquibase.statement.*
import org.hamcrest.Matchers
import spock.lang.Unroll

import static org.hamcrest.Matchers.containsInAnyOrder
import static spock.util.matcher.HamcrestSupport.that

public class CreateTableStatementTest extends AbstractStatementTest {

    def "constructor"() {
        when:
        def statement = new CreateTableStatement("CAT_NAME", "SCHEMA_NAME", "TABLE_NAME")

        then:
        statement.catalogName == "CAT_NAME"
        statement.schemaName == "SCHEMA_NAME"
        statement.tableName == "TABLE_NAME"
    }

    @Unroll()
    def "all constructors initialize correctly"() {
        expect:
        statement.getColumnNames().size() == 0
        statement.getAutoIncrementConstraints().size() == 0
        statement.getPrimaryKeyConstraint() == null
        statement.getNotNullConstraints().size() == 0
        statement.getForeignKeyConstraints().size() == 0
        statement.getUniqueConstraints().size() == 0

        where:
        statement << [new CreateTableStatement(), new CreateTableStatement("CAT_NAME", "SCHEMA_NAME", "TABLE_NAME")]
    }

    def "general constraint methods"() {
        when:
        def statement = new CreateTableStatement()

        then:
        statement.getConstraints().size() == 0
        statement.getConstraints(ForeignKeyConstraint).size() == 0

        when:
        statement.addConstraint(new ForeignKeyConstraint("fk_1", "other_table1(id)"))
        statement.addConstraint(new ForeignKeyConstraint("fk_2", "other_table2(id)"))
        statement.addConstraint(new PrimaryKeyConstraint("pk_name"))

        then:
        statement.getConstraints().size() == 3
        statement.getConstraints(ForeignKeyConstraint).size() == 2
        statement.getConstraints(PrimaryKeyConstraint).size() == 1
        statement.getConstraints(UniqueConstraint).size() == 0
    }

    def "not null constraint logic"() {
        when:
        def statement = new CreateTableStatement()
        then:
        statement.getNotNullConstraint("id") == null

        when:
        statement.addConstraint(new NotNullConstraint("id"))
        statement.addConstraint(new NotNullConstraint("name"))

        then:
        statement.getNotNullConstraint("id").columnName == "id"
        statement.getNotNullConstraint("name").columnName == "name"
        statement.getNotNullConstraint("address") == null
        statement.getNotNullConstraint("ID") == null

        when:
        statement.addConstraint(new NotNullConstraint("address"))

        then:
        statement.getNotNullConstraint("address").columnName == "address"
    }

    def "primary key constraint logic"() {
        when:
        def statement = new CreateTableStatement()

        then:
        statement.getPrimaryKeyConstraint() == null

        when:
        statement.addConstraint(new PrimaryKeyConstraint("pk_1"))
        then:
        statement.getPrimaryKeyConstraint().constraintName == "pk_1"

        when:
        statement.addConstraint(new PrimaryKeyConstraint("pk_2"))
        then:
        statement.getPrimaryKeyConstraint().constraintName in ["pk_1", "pk_2"]
    }

    def "addPrimaryKeyColumn"() {
        when:
        def statement = new CreateTableStatement()
        statement.addPrimaryKeyColumn("id", new IntType(), null, null, null)
        then:
        statement.getPrimaryKeyConstraint().constraintName == null
        statement.getPrimaryKeyConstraint().columns == ["id"]
        statement.getNotNullConstraint("id") != null

        when:
        statement = new CreateTableStatement()
        statement.addPrimaryKeyColumn("id", new IntType(), null, "pk_name", null)
        then:
        statement.getPrimaryKeyConstraint().constraintName == "pk_name"
        statement.getPrimaryKeyConstraint().columns == ["id"]
        statement.getNotNullConstraint("id") != null


        when:
        statement = new CreateTableStatement()
        statement.addPrimaryKeyColumn("id", new IntType(), null, null, null, new UniqueConstraint())
        then:
        statement.getPrimaryKeyConstraint().columns == ["id"]
        statement.getUniqueConstraints().size() == 1
        statement.getNotNullConstraint("id") != null

        when:
        statement = new CreateTableStatement()
        statement.addPrimaryKeyColumn("id1", new IntType(), null, "pk_name", null)
        statement.addPrimaryKeyColumn("id2", new IntType(), null, "pk_name", null)
        then:
        statement.getConstraints(PrimaryKeyConstraint).size() == 1
        statement.getPrimaryKeyConstraint().constraintName == "pk_name"
        statement.getPrimaryKeyConstraint().columns == ["id1", "id2"]
    }

    def "addColumn: simple"() {
        when:
        def statement = new CreateTableStatement()
        statement.addColumn("id", new IntType())
        statement.addColumn("ID", new VarcharType())
        then:
        statement.getColumnNames() == ["id", "ID"]
        statement.getColumnType("id").class == IntType
        statement.getColumnType("ID").class == VarcharType
        statement.getConstraints().size() == 0
    }

    def "addColumn: with default value"() {
        when:
        def statement = new CreateTableStatement()
        statement.addColumn("id", new IntType(), 55)
        statement.addColumn("name", new VarcharType(), "fred")

        then:
        statement.getDefaultValue("id") == 55
        statement.getDefaultValue("name") == "fred"
    }

    def "addColumn: with constraint(s)"() {
        when:
        def statement = new CreateTableStatement()
        statement.addColumn("id", new IntType(), new NotNullConstraint())
        statement.addColumn("name", new VarcharType(), [new NotNullConstraint(), new ForeignKeyConstraint("fk_test", "other_table(id)")] as Constraint[])

        then:
        that statement.getConstraints(NotNullConstraint).collect {it.columnName}, Matchers.containsInAnyOrder(["id", "name"] as Object[])
        statement.getConstraints(ForeignKeyConstraint).collect({it.foreignKeyName}) == ["fk_test"]
    }

    def "addColumn: with constraints and default value"() {
        when:
        def statement = new CreateTableStatement()
        statement.addColumn("id", new IntType(), new NotNullConstraint())
        statement.addColumn("name", new VarcharType(), "fred", [new NotNullConstraint(), new ForeignKeyConstraint("fk_test", "other_table(id)")] as Constraint[])

        then:
        that statement.getConstraints(NotNullConstraint).collect {it.columnName}, Matchers.containsInAnyOrder(["id", "name"] as Object[])
        statement.getConstraints(ForeignKeyConstraint).collect({it.foreignKeyName}) == ["fk_test"]
        statement.getDefaultValue("name") == "fred"
        statement.getDefaultValue("id") == null
    }

    def "addColumn: full version"() {
        when:
        def statement = new CreateTableStatement()
        statement.addColumn("id", new IntType(), null, [new NotNullConstraint(), new PrimaryKeyConstraint()] as Constraint[])
        statement.addColumn("name", new VarcharType(), "fred", "some remarks", [new NotNullConstraint(), new ForeignKeyConstraint("fk_test", "other_table(id)")] as Constraint[])
        statement.addColumn("address", new VarcharType(), null, null, null)

        then:
        that statement.getConstraints(NotNullConstraint).collect {it.columnName}, Matchers.containsInAnyOrder(["id", "name"] as Object[])
        statement.getConstraints(ForeignKeyConstraint).collect({it.foreignKeyName}) == ["fk_test"]
        statement.getPrimaryKeyConstraint() != null
        statement.getPrimaryKeyConstraint().columns == ["id"]

        statement.getColumnType("id").class == IntType
        statement.getColumnType("name").class == VarcharType
        statement.getColumnType("address").class == VarcharType

        statement.getDefaultValue("name") == "fred"
        statement.getDefaultValue("id") == null
        statement.getDefaultValue("address") == null

        statement.getColumnRemarks("name") == "some remarks"
        statement.getColumnRemarks("id") == null
        statement.getColumnRemarks("address") == null

        that statement.getConstraints(NotNullConstraint).collect({it.columnName}), Matchers.containsInAnyOrder(["id", "name"] as Object[])
    }

    def "addColumn: full version with compound primary key ends up with just one primary key constraint"() {
        when:
        def statement = new CreateTableStatement()
        statement.addColumn("id1", new IntType(), [new NotNullConstraint(), new PrimaryKeyConstraint("pk_name")] as Constraint[])
        statement.addColumn("id2", new IntType(), [new NotNullConstraint(), new PrimaryKeyConstraint("pk_name")] as Constraint[])
        statement.addColumn("name", new VarcharType(), "fred", "some remarks", [new NotNullConstraint(), new ForeignKeyConstraint("fk_test", "other_table(id)")] as Constraint[])

        then:
        that statement.getConstraints(NotNullConstraint).collect {it.columnName}, containsInAnyOrder(["id1", "id2", "name"] as Object[])
        statement.getPrimaryKeyConstraint() != null
        statement.getPrimaryKeyConstraint().columns == ["id1", "id2"]
    }

    def "removeColumn"() {
        given:
        def statement = new CreateTableStatement()
        statement.addColumn("id1", new IntType(), [new NotNullConstraint(), new PrimaryKeyConstraint("pk_name")] as Constraint[])
        statement.addColumn("id2", new IntType(), 33, "id with default value", [new NotNullConstraint(), new PrimaryKeyConstraint("pk_name")] as Constraint[])
        statement.addColumn("name", new VarcharType(), [new NotNullConstraint(), new ForeignKeyConstraint("fk_name", "other_table(id)")] as Constraint[])
        statement.addColumn("address", new VarcharType())

        when: "column is removed and ColumnConstraints (which does not include FK)"
        statement.removeColumn("name")

        then:
        assert !statement.getColumnNames().contains("name")
        statement.getColumnType("name") == null
        that statement.getConstraints(NotNullConstraint).collect {it.columnName}, containsInAnyOrder(["id1", "id2"] as Object[])
        statement.getConstraints(ForeignKeyConstraint).collect {it.foreignKeyName} == ["fk_name"]

        when: "a primary key column is removed, the PK is not modified"
        statement.removeColumn("id2")

        then:
        assert !statement.getColumnNames().contains("id2")
        statement.getColumnType("id2") == null
        that statement.getConstraints(NotNullConstraint).collect {it.columnName}, containsInAnyOrder(["id1"] as Object[])
        statement.getPrimaryKeyConstraint().columns == ["id1", "id2"]
    }

    def "get and set defaultValue"() {
        when:
        def statement = new CreateTableStatement()
        statement.addColumn("id", new IntType())
        statement.addColumn("name", new VarcharType())

        then:
        statement.getDefaultValue("id") == null
        statement.getDefaultValue("name") == null

        when:
        statement.setDefaultValue("id", 12)
        then:
        statement.getDefaultValue("id") == 12
        statement.getDefaultValue("name") == null

        when:
        statement.setDefaultValue("id", 15)
        then:
        statement.getDefaultValue("id") == 15
        statement.getDefaultValue("name") == null
    }

    def "get and set remarks"() {
        when:
        def statement = new CreateTableStatement()
        statement.addColumn("id", new IntType())
        statement.addColumn("name", new VarcharType())

        then:
        statement.getColumnRemarks("id") == null
        statement.getColumnRemarks("name") == null

        when:
        statement.setColumnRemarks("id", "id remarks")
        then:
        statement.getColumnRemarks("id") == "id remarks"
        statement.getColumnRemarks("name") == null

        when:
        statement.setColumnRemarks("id", "other remarks")
        then:
        statement.getColumnRemarks("id") == "other remarks"
        statement.getColumnRemarks("name") == null
    }

    def "get and set column type"() {
        when:
        def statement = new CreateTableStatement()
        statement.addColumn("id", new IntType())
        statement.addColumn("name", new VarcharType())

        then:
        statement.getColumnType("id").class == IntType
        statement.getColumnType("name").class == VarcharType

        when:
        statement.setColumnType("id", new BigIntType())
        then:
        statement.getColumnType("id").class == BigIntType
        statement.getColumnType("name").class == VarcharType
    }

    @Override
    protected List<String> getStandardProperties() {
        def properties = super.getStandardProperties()
        properties.remove("autoIncrementConstraints")
        properties.remove("defaultValues")
        properties.remove("columnTypes")
        properties.remove("foreignKeyConstraints")
        properties.remove("uniqueConstraints")
        properties.remove("notNullConstraints")
        properties.remove("columnNames")
        properties.remove("primaryKeyConstraint")
        properties.remove("columnRemarks")
        properties.remove("constraints")

        return properties
    }
}
