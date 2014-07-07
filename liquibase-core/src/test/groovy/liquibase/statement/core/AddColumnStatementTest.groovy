package liquibase.statement.core

import liquibase.statement.AbstractStatementTest
import liquibase.statement.AutoIncrementConstraint
import liquibase.statement.NotNullConstraint
import liquibase.statement.PrimaryKeyConstraint
import liquibase.statement.UniqueConstraint

public class AddColumnStatementTest extends AbstractStatementTest {

    def "constructor without remarks but with constraints"() {
        when:
        def obj = new AddColumnStatement("CAT_NAME", "SCHEMA_NAME", "TABLE_NAME", "COLUMN_NAME", "DATA TYPE", 55, new UniqueConstraint("UQ_CONST"), new PrimaryKeyConstraint("PK_NAME"))

        then:
        obj.getCatalogName() == "CAT_NAME"
        obj.getSchemaName() == "SCHEMA_NAME"
        obj.getTableName() == "TABLE_NAME"
        obj.getColumnName() == "COLUMN_NAME"
        obj.getColumnType() == "DATA TYPE"
        obj.getDefaultValue() == 55
        obj.getConstraints().size() == 2
    }

    def "constructor without remarks and no constraints"() {
        when:
        def obj = new AddColumnStatement("CAT_NAME", "SCHEMA_NAME", "TABLE_NAME", "COLUMN_NAME", "DATA TYPE", 55)

        then:
        obj.getCatalogName() == "CAT_NAME"
        obj.getSchemaName() == "SCHEMA_NAME"
        obj.getTableName() == "TABLE_NAME"
        obj.getColumnName() == "COLUMN_NAME"
        obj.getColumnType() == "DATA TYPE"
        obj.getDefaultValue() == 55
        obj.getConstraints().size() == 0
    }

    def "getAutoIncrement and isAutoIncrement"() {
        when:
        def objWithoutConstraint = new AddColumnStatement()
        def objWithConstraint = new AddColumnStatement().addConstraint(new AutoIncrementConstraint("ID"))

        then:
        assert !objWithoutConstraint.isAutoIncrement()
        objWithoutConstraint.getAutoIncrementConstraint() == null

        assert objWithConstraint.isAutoIncrement()
        objWithConstraint.getAutoIncrementConstraint().columnName == "ID"
    }


    def "isPrimaryKey"() {
        when:
        def objWithoutConstraint = new AddColumnStatement()
        def objWithConstraint = new AddColumnStatement().addConstraint(new PrimaryKeyConstraint("ID"))

        then:
        assert !objWithoutConstraint.isPrimaryKey()
        assert objWithConstraint.isPrimaryKey()
    }

    def "isNullable"() {
        when:
        def objWithoutConstraint = new AddColumnStatement()
        def objWithConstraint = new AddColumnStatement().addConstraint(new NotNullConstraint("ID"))

        then:
        assert objWithoutConstraint.isNullable()
        assert !objWithConstraint.isNullable()
    }

    def "isUnique and getUniqueConstraintName"() {
        when:
        def objWithoutConstraint = new AddColumnStatement()
        def objWithConstraint = new AddColumnStatement().addConstraint(new UniqueConstraint("UQ_CONST"))

        then:
        assert !objWithoutConstraint.isUnique()
        assert objWithoutConstraint.getUniqueConstraintName() == null

        assert objWithConstraint.isUnique()
        assert objWithConstraint.getUniqueConstraintName() == "UQ_CONST"
    }

    @Override
    protected List<String> getStandardProperties() {
        def properties = super.getStandardProperties()
        properties.remove("primaryKey")
        properties.remove("nullable")
        properties.remove("autoIncrement")
        properties.remove("autoIncrementConstraint")
        properties.remove("constraints")
        properties.remove("uniqueConstraintName")
        properties.remove("unique")


        return properties
    }
}