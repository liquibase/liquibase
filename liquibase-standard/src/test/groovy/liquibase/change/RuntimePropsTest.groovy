package liquibase.change

import liquibase.Scope
import liquibase.change.core.InsertDataChange
import liquibase.change.core.RawSQLChange
import liquibase.change.core.SQLFileChange
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.core.MockDatabase
import liquibase.exception.DatabaseException
import liquibase.exception.LiquibaseException
import liquibase.executor.Executor
import liquibase.executor.ExecutorService
import liquibase.executor.jvm.JdbcExecutor
import liquibase.parser.core.ParsedNode
import liquibase.precondition.core.PreconditionContainer
import liquibase.resource.ResourceAccessor
import liquibase.sdk.executor.MockExecutor
import liquibase.sdk.supplier.resource.ResourceSupplier
import liquibase.sql.visitor.InjectRuntimeVariablesVisitor
import liquibase.sql.visitor.InjectRuntimeVariablesVisitorTest
import liquibase.sql.visitor.SqlVisitor
import liquibase.statement.ReturningSqlStatement
import liquibase.statement.SqlStatement
import spock.lang.Shared
import spock.lang.Specification

import static liquibase.util.TestUtil.*

class RuntimePropsTest extends Specification {
	@Shared resourceSupplier = new ResourceSupplier()
	def "sqlCheck" () {
		final String rtPropName = 'rtProp'
		final String rtPropValue = 'rtValue'
		final MockExecutor mockExecutor = new MockExecutor([updatesDatabase:true,
																			 queryForObject: rtPropValue])
		final Database db =  mockExecutor.database
		DatabaseChangeLog chLog = databaseChangeLog('dummy')
		ChangeSet changeSet = new ChangeSet(chLog)
		RawSQLChange sql = new RawSQLChange("SELECT")
		sql.setSetProperty(rtPropName)
		changeSet.addChange sql
		SqlStatement[] stmts = sql.generateStatements(db)
		assert stmts.size() == 1
		changeSet.preconditions = new PreconditionContainer()
		ParsedNode pn = parsedNode "preConditions", sqlCheck:[sql:'SELECT ${rtProp}',
																				 		expectedResult: rtPropValue]
		changeSet.preconditions.load(pn, null)
		InjectRuntimeVariablesVisitorTest.clear() // Test executed in mass with mvn uses same static
		(stmts[0] as ReturningSqlStatement).setResult(rtPropValue)
		when:
		Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor( db, mockExecutor);
		changeSet.execute( db )
		then:
		chLog.changeLogParameters.getValue(rtPropName, chLog) == rtPropValue
		mockExecutor.getRanSql().trim() == "SELECT ${rtPropValue};"
	}

	static class FakeJdbcExecutor extends JdbcExecutor {
		String queryForObjectResult
		FakeJdbcExecutor(Database db, String queryForObjectResult = null) {
			database = db
			this.queryForObjectResult = queryForObjectResult
		}
		@Override
		<T> T queryForObject(SqlStatement sql, Class<T> requiredType, List<SqlVisitor> sqlVisitors)
			throws DatabaseException {
			queryForObjectResult
		}
	}

	static class FakeDatabase extends MockDatabase {
		@Override
		void executeStatements(final Change change, final DatabaseChangeLog changeLog,
									  final List<SqlVisitor> sqlVisitors) throws LiquibaseException {
			Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor( this)
			executor.execute(change, sqlVisitors)
		}
	}

	def "sql set global runtime property read "() {
		final String rtPropName = 'rtProp'
		final String rtPropValue = 'rtValue'
		final Database db = new FakeDatabase()
		final FakeJdbcExecutor mockExecutor = new FakeJdbcExecutor(db, rtPropValue)
		DatabaseChangeLog chLog = databaseChangeLog('dummy')
		ChangeSet changeSet = new ChangeSet(chLog)
		RawSQLChange sql = load (new RawSQLChange(), changeSet, setProperty: rtPropName,
																			 	  sql: "SELECT '$rtPropValue'" )
		InjectRuntimeVariablesVisitorTest.clear() // Test executes in mass with mvn uses same static
		when:
		Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor( db, mockExecutor)
		changeSet.execute( db)
		then:
		chLog.changeLogParameters.getValue(rtPropName, null) == rtPropValue
	}

	def "sql set local runtime property read "() {
		final String rtPropName = 'rtProp'
		final String rtPropValue = 'rtValue'
		final Database db = new FakeDatabase()
		final FakeJdbcExecutor mockExecutor = new FakeJdbcExecutor(db, rtPropValue)
		DatabaseChangeLog chLog = databaseChangeLog('dummy')
		ChangeSet changeSet = new ChangeSet(chLog)
		RawSQLChange sql = load (new RawSQLChange(), changeSet, setProperty: 'local:'+rtPropName,
																					sql: "SELECT '$rtPropValue'" )
		InjectRuntimeVariablesVisitorTest.clear() // Test executes in mass with mvn uses same static
		when:
		Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor( db, mockExecutor)
		changeSet.execute( db)
		then:
		chLog.changeLogParameters.getValue(rtPropName, null) == null // Not found as global
		chLog.changeLogParameters.getValue(rtPropName, chLog) == rtPropValue
	}

	def "sqlFile runtime property read"() {
		final String rtPropName = 'rtProp'
		final String rtPropValue = 'rtValue'
		final Database db = new FakeDatabase()
		final FakeJdbcExecutor mockExecutor = new FakeJdbcExecutor(db, rtPropValue)
		DatabaseChangeLog chLog = databaseChangeLog('dummy')
		ChangeSet changeSet = new ChangeSet(chLog)
		SQLFileChange sql = load (new SQLFileChange(), changeSet, setProperty: rtPropName, path:'dummy.sql' )
		sql.sql = "SELECT '$rtPropValue'"

		InjectRuntimeVariablesVisitorTest.clear() // Test executes in mass with mvn uses same static
		Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor( db, mockExecutor)
		when:
		changeSet.execute( db )
		then:
		chLog.changeLogParameters.getValue(rtPropName, chLog) == rtPropValue
	}

	def "insert use runtime props" () {
		final String rtPropName = 'rtProp'
		final String rtPropValue = 'rtValue'

		final Database db = DatabaseFactory.getInstance().openDatabase("jdbc:h2:mem:", null, null, null, null as ResourceAccessor)
		//final FakeJdbcExecutor mockExecutor = new FakeJdbcExecutor(db)
		final MockExecutor mockExecutor = new MockExecutor()
		DatabaseChangeLog chLog = databaseChangeLog('dummy')
		ChangeSet changeSet = new ChangeSet(chLog)
		InjectRuntimeVariablesVisitorTest.clear() // Test executed in mass with mvn uses same static
		InjectRuntimeVariablesVisitor.get().params().set(rtPropName, rtPropValue)
		InsertDataChange ins = load new InsertDataChange(), changeSet, tableName: 'test',
													columns:[[name: "\${$rtPropName}", value:"\${$rtPropName}"]]

		Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor( db, mockExecutor);
		when:
		changeSet.execute(db)
		then:
		mockExecutor.getRanSql().trim() == "INSERT INTO test (rtValue) VALUES ('rtValue');"
	}

// TODO
//	def "loadData use runtime-props" () {
//		String fileName = 'dummy'
//		MockResourceAccessor ra = new MockResourceAccessor([(fileName): '''id,name
//1,
//2,${rtProp}
//'''])
//		final String rtPropName = 'rtProp'
//		final String rtPropValue = 'rtValue'
//		final MockExecutor mockExecutor = new MockExecutor([updatesDatabase:true,
//																			 queryForObject : rtPropValue])
//		final Database db =  mockExecutor.database
//		DatabaseChangeLog chLog = databaseChangeLog('dummy')
//		ChangeSet changeSet = new ChangeSet(chLog)
//		InjectRuntimeVariablesVisitorTest.clear() // Test executed in mass with mvn uses same static
//		InjectRuntimeVariablesVisitor.get().params().set(rtPropName, rtPropValue)
//		LoadDataChange change = load (new LoadDataChange(), changeSet, ra,
//			file: fileName,
//			table: 'a',
//			columns: [name: 'name',
//						 defaultValue: rtPropName]
//			)
//
//		when:
//		Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor( db, mockExecutor);
//		Scope.child(Scope.Attr.resourceAccessor, ra, {
//			changeSet.execute(db)
//		})
//		then:
//		mockExecutor.getRanSql().trim() == ''
//	}
}
