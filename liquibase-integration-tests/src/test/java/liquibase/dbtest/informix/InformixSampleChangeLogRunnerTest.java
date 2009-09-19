package liquibase.dbtest.informix;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;
import org.junit.Test;

public class InformixSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public InformixSampleChangeLogRunnerTest() throws Exception {
        super("informix", "jdbc:informix-sqli://"+DATABASE_SERVER_HOSTNAME+":9088/liquibase:informixserver=ol_ids_1150_1");
    }

    @Test
    @Override
    public void testRerunDiffChangeLogAltSchema() throws Exception {
    	/*
    	 * Informix handles schemas differently
    	 * It is allowed to have several schemas, but all the tables
    	 * have to have unique names, even though they are in
    	 * different schemas.
    	 * So this test is disabled for Informix. 
    	 */
    	
    }
    
}
