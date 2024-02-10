package liquibase.sqlgenerator.core

import liquibase.change.ColumnConfig
import liquibase.database.core.CockroachDatabase
import liquibase.database.core.DB2Database
import liquibase.database.core.Db2zDatabase
import liquibase.database.core.DerbyDatabase
import liquibase.database.core.EnterpriseDBDatabase
import liquibase.database.core.FirebirdDatabase
import liquibase.database.core.H2Database
import liquibase.database.core.HsqlDatabase
import liquibase.database.core.InformixDatabase
import liquibase.database.core.Ingres9Database
import liquibase.database.core.MSSQLDatabase
import liquibase.database.core.MariaDBDatabase
import liquibase.database.core.MySQLDatabase
import liquibase.database.core.OracleDatabase
import liquibase.database.core.PostgresDatabase
import liquibase.database.core.SybaseASADatabase
import liquibase.database.core.SybaseDatabase
import liquibase.database.core.UnsupportedDatabase
import liquibase.sqlgenerator.SqlGenerator
import liquibase.statement.core.AddUniqueConstraintStatement
import spock.lang.Specification
import spock.lang.Unroll


class AddUniqueConstraintGeneratorAllVariantsTest extends Specification {

    protected static final String TABLE_NAME = "AddUQTest";
    protected static final String COLUMN_NAME = "colToMakeUQ";
    protected static final String COLUMN_NAME2 = "colToMakeUQ2";
    protected static final String CONSTRAINT_NAME = "UQ_TEST";
    private static final String INDEX_NAME = "uqIndex";

    @Unroll
    def  "test unique constraint using index with constraint name for #database database"() {
        when:
            SqlGenerator<AddUniqueConstraintStatement> generatorUnderTest = new AddUniqueConstraintGenerator()
            AddUniqueConstraintStatement statement = new AddUniqueConstraintStatement(null, null, TABLE_NAME, [new ColumnConfig().setName(COLUMN_NAME), new ColumnConfig().setName(COLUMN_NAME2)] as ColumnConfig[], CONSTRAINT_NAME);
            statement.setForIndexName(INDEX_NAME);
        then:
            expectedSql ==  generatorUnderTest.generateSql(statement, database, null)[0].toSql()
        where:
            database | expectedSql
            new SybaseDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new FirebirdDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new H2Database() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE (colToMakeUQ, colToMakeUQ2)"
            new UnsupportedDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new MSSQLDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new OracleDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new SybaseASADatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new Db2zDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new Ingres9Database() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new DerbyDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new CockroachDatabase() || "ALTER TABLE \"AddUQTest\" ADD CONSTRAINT UQ_TEST UNIQUE (\"colToMakeUQ\", \"colToMakeUQ2\") USING INDEX \"uqIndex\""
            new EnterpriseDBDatabase() || "ALTER TABLE \"AddUQTest\" ADD CONSTRAINT UQ_TEST UNIQUE (\"colToMakeUQ\", \"colToMakeUQ2\") USING INDEX \"uqIndex\""
            new MariaDBDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new HsqlDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new DB2Database() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new MySQLDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new PostgresDatabase() || "ALTER TABLE \"AddUQTest\" ADD CONSTRAINT UQ_TEST UNIQUE (\"colToMakeUQ\", \"colToMakeUQ2\") USING INDEX \"uqIndex\""
            new InformixDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
    }

    @Unroll
    def  "test unique constraint using index without constraint name for #database database"() {
        when:
            SqlGenerator<AddUniqueConstraintStatement> generatorUnderTest = new AddUniqueConstraintGenerator()
            AddUniqueConstraintStatement statement = new AddUniqueConstraintStatement(null, null, TABLE_NAME, [new ColumnConfig().setName(COLUMN_NAME), new ColumnConfig().setName(COLUMN_NAME2)] as ColumnConfig[], null);
            statement.setForIndexName(INDEX_NAME);
        then:
            expectedSql == generatorUnderTest.generateSql(statement, database, null)[0].toSql()
        where:
            database | expectedSql
            new SybaseDatabase() || "ALTER TABLE AddUQTest ADD UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new FirebirdDatabase() || "ALTER TABLE AddUQTest ADD UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new H2Database() || "ALTER TABLE AddUQTest ADD UNIQUE (colToMakeUQ, colToMakeUQ2)"
            new UnsupportedDatabase() || "ALTER TABLE AddUQTest ADD UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new MSSQLDatabase() || "ALTER TABLE AddUQTest ADD UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new OracleDatabase() || "ALTER TABLE AddUQTest ADD UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new SybaseASADatabase() || "ALTER TABLE AddUQTest ADD UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new Db2zDatabase() || "ALTER TABLE AddUQTest ADD UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new Ingres9Database() || "ALTER TABLE AddUQTest ADD UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new DerbyDatabase() || "ALTER TABLE AddUQTest ADD UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new CockroachDatabase() || "ALTER TABLE \"AddUQTest\" ADD UNIQUE (\"colToMakeUQ\", \"colToMakeUQ2\") USING INDEX \"uqIndex\""
            new EnterpriseDBDatabase() || "ALTER TABLE \"AddUQTest\" ADD UNIQUE (\"colToMakeUQ\", \"colToMakeUQ2\") USING INDEX \"uqIndex\""
            new MariaDBDatabase() || "ALTER TABLE AddUQTest ADD UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new HsqlDatabase() || "ALTER TABLE AddUQTest ADD UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new DB2Database() || "ALTER TABLE AddUQTest ADD UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new MySQLDatabase() || "ALTER TABLE AddUQTest ADD UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
            new PostgresDatabase() || "ALTER TABLE \"AddUQTest\" ADD UNIQUE (\"colToMakeUQ\", \"colToMakeUQ2\") USING INDEX \"uqIndex\""
            new InformixDatabase() || "ALTER TABLE AddUQTest ADD UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex"
    }

    @Unroll
    def  "test unique constraint set clustered with constraint name for #database database"() {
        when:
            SqlGenerator<AddUniqueConstraintStatement> generatorUnderTest = new AddUniqueConstraintGenerator()
            AddUniqueConstraintStatement statement = new AddUniqueConstraintStatement(null, null, TABLE_NAME, [new ColumnConfig().setName(COLUMN_NAME), new ColumnConfig().setName(COLUMN_NAME2)] as ColumnConfig[], CONSTRAINT_NAME);
            statement.setClustered(true);
        then:
            expectedSql == generatorUnderTest.generateSql(statement, database, null)[0].toSql()
        where:
            database | expectedSql
            new SybaseDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (colToMakeUQ, colToMakeUQ2)"
            new FirebirdDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (colToMakeUQ, colToMakeUQ2)"
            new H2Database() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (colToMakeUQ, colToMakeUQ2)"
            new UnsupportedDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (colToMakeUQ, colToMakeUQ2)"
            new MSSQLDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (colToMakeUQ, colToMakeUQ2)"
            new OracleDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (colToMakeUQ, colToMakeUQ2)"
            new SybaseASADatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (colToMakeUQ, colToMakeUQ2)"
            new Db2zDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (colToMakeUQ, colToMakeUQ2)"
            new Ingres9Database() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (colToMakeUQ, colToMakeUQ2)"
            new DerbyDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (colToMakeUQ, colToMakeUQ2)"
            new CockroachDatabase() || "ALTER TABLE \"AddUQTest\" ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (\"colToMakeUQ\", \"colToMakeUQ2\")"
            new EnterpriseDBDatabase() || "ALTER TABLE \"AddUQTest\" ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (\"colToMakeUQ\", \"colToMakeUQ2\")"
            new MariaDBDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (colToMakeUQ, colToMakeUQ2)"
            new HsqlDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (colToMakeUQ, colToMakeUQ2)"
            new DB2Database() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (colToMakeUQ, colToMakeUQ2)"
            new MySQLDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (colToMakeUQ, colToMakeUQ2)"
            new PostgresDatabase() || "ALTER TABLE \"AddUQTest\" ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (\"colToMakeUQ\", \"colToMakeUQ2\")"
            new InformixDatabase() || "ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (colToMakeUQ, colToMakeUQ2)"
    }

}