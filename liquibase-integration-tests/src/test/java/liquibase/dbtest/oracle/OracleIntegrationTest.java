package liquibase.dbtest.oracle;

import liquibase.dbtest.AbstractIntegrationTest;

/**
 * create tablespace liquibase2 datafile 'C:\ORACLEXE\ORADATA\XE\LIQUIBASE2.DBF' SIZE 5M autoextend on next 5M
 */
public class OracleIntegrationTest extends AbstractIntegrationTest {

    public OracleIntegrationTest() throws Exception {
        super("oracle", "jdbc:oracle:thin:@"+ getDatabaseServerHostname() +"/XE");
    }

}
