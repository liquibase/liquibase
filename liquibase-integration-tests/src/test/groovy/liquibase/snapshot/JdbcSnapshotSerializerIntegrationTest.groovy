package liquibase.snapshot

import com.example.liquibase.change.CreateTableExampleChange
import com.example.liquibase.change.KeyColumnConfig
import com.example.liquibase.change.PrimaryKeyConfig
import liquibase.CatalogAndSchema
import liquibase.Scope
import liquibase.change.ColumnConfig
import liquibase.change.ConstraintsConfig
import liquibase.change.core.CreateTableChange
import liquibase.changelog.ChangeSet
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.DatabaseException
import liquibase.executor.Executor
import liquibase.executor.ExecutorService
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.serializer.core.json.JsonSnapshotSerializer
import liquibase.structure.DatabaseObject
import liquibase.structure.core.Column
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

class JdbcSnapshotSerializerIntegrationTest extends Specification {
    @Rule
    public DatabaseTestSystem h2 = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("h2")

    @Unroll
    def "setup tables and snapshot"() {
        when:
        def connection = h2.getConnection()
        def db = DatabaseFactory.instance.findCorrectDatabaseImplementation(new JdbcConnection(connection))

        ChangeSet changeSet =
                new ChangeSet("1", "mock-author", false, false, "test/changelog.xml",
                        (String)null, (String)null, "sqlplus", null, false, null, null)

        CreateTableChange exampleChange = new CreateTableChange()
        exampleChange.setTableName("first")
        ColumnConfig config = (ColumnConfig) ColumnConfig.fromName("first")
        config.setType("VARCHAR (255)")
        ConstraintsConfig constraintsConfig = new ConstraintsConfig()
        constraintsConfig.setPrimaryKey(true)
        constraintsConfig.setNotNullConstraintName("PK_FIRST")
        config.setConstraints(constraintsConfig)
        exampleChange.getColumns().add(config)
        changeSet.addChange(exampleChange)

        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", db)
        changeSet.getChanges().each {
            try {
                executor.execute(it)
                db.commit()
            } catch (DatabaseException dbe) {
                throw new RuntimeException(dbe)
            }
        }

        CatalogAndSchema schema = new CatalogAndSchema(null, "public")
        CatalogAndSchema[] schemas = new CatalogAndSchema[1]
        schemas[0] = schema
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(schemas, db, new SnapshotControl(db))
        String snapshotResult = new JsonSnapshotSerializer().serialize(snapshot, true)

        then:
        assert snapshotResult != null
    }
}
