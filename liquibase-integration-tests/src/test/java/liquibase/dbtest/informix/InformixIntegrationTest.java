package liquibase.dbtest.informix;

import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;

/*
 * To run the tablespace tests you have to
 * create the dbspace "liquibase2" on the informix server
 * e.g. with the following command:
 *     onspaces -c -d liquibase2 -p PATH_TO_A_FILE -o 0 -s 1024
 */
public class InformixIntegrationTest extends AbstractIntegrationTest {

    public InformixIntegrationTest() throws Exception {
        super("informix", DatabaseFactory.getInstance().getDatabase("informix"));
    }

    @Override
    protected boolean isDatabaseProvidedByTravisCI() {
        // Seems unlikely to ever be provided by Travis, as it's not free
        return false;
    }
}
