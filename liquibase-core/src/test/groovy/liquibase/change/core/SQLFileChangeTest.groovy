package liquibase.change.core

import liquibase.change.Change
import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet
import liquibase.exception.UnexpectedLiquibaseException
import liquibase.sdk.database.MockDatabase
import liquibase.sdk.resource.MockResourceAccessor
import liquibase.statement.SqlStatement
import spock.lang.Unroll

import static org.junit.Assert.assertEquals

public class SQLFileChangeTest extends StandardChangeTest {

    def "generateStatements throws Exception if file does not exist"() throws Exception {
        when:
        def change = new SQLFileChange();
        change.setPath("doesnotexist.sql");
        change.finishInitialization();

        change.generateStatements(new MockDatabase())

        then:
         thrown(UnexpectedLiquibaseException.class)
    }

    @Unroll
    def "lines from file parse into one or more statements correctly"() throws Exception {
        when:
        SQLFileChange change2 = new SQLFileChange();
        change2.setSql(fileContents);
        MockDatabase database = new MockDatabase();
        SqlStatement[] statements = change2.generateStatements(database);

        then:
        statements.length == expectedStatements.size();
        for (int i = 0; i < expectedStatements.size(); i++) {
            assert expectedStatements[i] == statements[i].sql
        }

        where:
        fileContents | expectedStatements
        "SELECT * FROM customer;"                                                | ["SELECT * FROM customer"]
        "SELECT * FROM customer;\nSELECT * from table;\nSELECT * from table2;\n" | ["SELECT * FROM customer", "SELECT * from table", "SELECT * from table2"]
        "SELECT * FROM customer\ngo"                                             | ["SELECT * FROM customer"]
        "goSELECT * FROM customer\ngo" | ["goSELECT * FROM customer"]
        "SELECT * FROM customer\ngo\nSELECT * FROM table\ngo" | ["SELECT * FROM customer", "SELECT * FROM table"]
        "SELECT * FROM go\ngo\nSELECT * from gogo\ngo\n" | ["SELECT * FROM go", "SELECT * from gogo"]
        "insert into table ( col ) values (' value with; semicolon ');" | ["insert into table ( col ) values (' value with; semicolon ')"]
        "--\n-- This is a comment\nUPDATE tablename SET column = 1;\nGO" | ["--\n-- This is a comment\nUPDATE tablename SET column = 1"]
        "ALTER INDEXTYPE position_indextype COMPILE;\n ALTER TABLESPACE tbs_01 BEGIN BACKUP;\n ALTER TABLESPACE tbs_01 END BACKUP;\n ALTER USER sidney IDENTIFIED BY second_2nd_pwd DEFAULT TABLESPACE example;" | ["ALTER INDEXTYPE position_indextype COMPILE", "ALTER TABLESPACE tbs_01 BEGIN BACKUP", "ALTER TABLESPACE tbs_01 END BACKUP", "ALTER USER sidney IDENTIFIED BY second_2nd_pwd DEFAULT TABLESPACE example"]
        "CREATE PROCEDURE AMY.NEW_SALES_ORDER2 ( IN CUSTID int, IN ITEMID int, IN QTY int, OUT SONUM2 bigint) LANGUAGE SQL BEGIN DECLARE CUSTVAR int; DECLARE ITEMVAR int; DECLARE QTYVAR int; DECLARE ITEMCOSTVAR dec(7,2); DECLARE AMTVAR dec(9,2); DECLARE STAMP timestamp; DECLARE cursor1 CURSOR FOR SELECT ITEMCOST FROM DB2ADMIN.ITEMS WHERE ITEMID = ITEMVAR; DECLARE cursor2 CURSOR FOR SELECT SONUM2 FROM DB2ADMIN.SALES_ORDERS WHERE DTEORD = STAMP; SET CUSTVAR = CUSTID; SET ITEMVAR = ITEMID; SET QTYVAR = QTY; SET STAMP = CURRENT TIMESTAMP; OPEN cursor1; FETCH FROM cursor1 INTO ITEMCOSTVAR; CLOSE cursor1; SET AMTVAR = QTY * ITEMCOSTVAR; INSERT INTO DB2ADMIN.SALES_ORDERS (CUSTID, ITEMID, QTY, AMT, DTEORD) VALUES (CUSTVAR, ITEMVAR, QTYVAR, AMTVAR, STAMP); OPEN cursor2; FETCH FROM cursor2 INTO SONUM2; CLOSE cursor2; END;" | ["CREATE PROCEDURE AMY.NEW_SALES_ORDER2 ( IN CUSTID int, IN ITEMID int, IN QTY int, OUT SONUM2 bigint) LANGUAGE SQL BEGIN DECLARE CUSTVAR int; DECLARE ITEMVAR int; DECLARE QTYVAR int; DECLARE ITEMCOSTVAR dec(7,2); DECLARE AMTVAR dec(9,2); DECLARE STAMP timestamp; DECLARE cursor1 CURSOR FOR SELECT ITEMCOST FROM DB2ADMIN.ITEMS WHERE ITEMID = ITEMVAR; DECLARE cursor2 CURSOR FOR SELECT SONUM2 FROM DB2ADMIN.SALES_ORDERS WHERE DTEORD = STAMP; SET CUSTVAR = CUSTID; SET ITEMVAR = ITEMID; SET QTYVAR = QTY; SET STAMP = CURRENT TIMESTAMP; OPEN cursor1; FETCH FROM cursor1 INTO ITEMCOSTVAR; CLOSE cursor1; SET AMTVAR = QTY * ITEMCOSTVAR; INSERT INTO DB2ADMIN.SALES_ORDERS (CUSTID, ITEMID, QTY, AMT, DTEORD) VALUES (CUSTVAR, ITEMVAR, QTYVAR, AMTVAR, STAMP); OPEN cursor2; FETCH FROM cursor2 INTO SONUM2; CLOSE cursor2; END"]
        "CREATE TRIGGER AMY.TRIGGER_02 AFTER INSERT ON TABLE_02 FOR EACH ROW BEGIN ATOMIC UPDATE TABLE_01 SET ID = ID + 1; END;" | ["CREATE TRIGGER AMY.TRIGGER_02 AFTER INSERT ON TABLE_02 FOR EACH ROW BEGIN ATOMIC UPDATE TABLE_01 SET ID = ID + 1; END"]
    }

    def getConfirmationMessage() throws Exception {
        when:
        def change = new SQLFileChange();
        change.setPath("com/example/changelog.xml");

        then:
        "SQL in file com/example/changelog.xml executed" == change.getConfirmationMessage()
    }

    def replacementOfProperties() throws Exception {
        when:
        SQLFileChange change = new SQLFileChange();
        ChangeLogParameters changeLogParameters = new ChangeLogParameters();
        changeLogParameters.set("table.prefix", "prfx");
        changeLogParameters.set("some.other.prop", "nofx");
        ChangeSet changeSet = new ChangeSet("x", "y", true, true, null, null, null, null);
        changeSet.setChangeLogParameters(changeLogParameters);
        change.setChangeSet(changeSet);

        String fakeSql = "create \${table.prefix}_customer (\${some.other.prop} INTEGER NOT NULL, PRIMARY KEY (\${some.other.prop}));";

        change.setSql(fakeSql);

        then:
        assertEquals("create prfx_customer (nofx INTEGER NOT NULL, PRIMARY KEY (nofx));", change.getSql());
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def change = new RawSQLChange()

        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown
        assert change.checkStatus(database).message == "Cannot check raw sql status"
    }

    @Override
    protected boolean canUseStandardGenerateCheckSumTest() {
        return false;
    }

    def isValidForLoad(Change change) {
        return ((SQLFileChange) change).path != null;
    }

    def "openSqlStream throws exception if file does not exist"() {
        when:
        def change = new SQLFileChange()
        change.path = "non-existing.sql"
        change.resourceAccessor = new MockResourceAccessor()
        change.openSqlStream()

        then:
        def e = thrown(IOException)
        e.message == "File does not exist: 'non-existing.sql'"

    }

}
