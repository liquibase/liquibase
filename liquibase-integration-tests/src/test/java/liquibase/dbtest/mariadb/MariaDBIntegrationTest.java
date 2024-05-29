package liquibase.dbtest.mariadb;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import org.junit.Test;

import java.sql.SQLSyntaxErrorException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

public class MariaDBIntegrationTest extends AbstractIntegrationTest {

    public MariaDBIntegrationTest() throws Exception {
        super("mariadb", DatabaseFactory.getInstance().getDatabase("mariadb"));
    }

    @Test
    public void dateDefaultValue() throws Exception {
        if (getDatabase() == null) {
            return;
        }
        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase()).execute(new RawParameterizedSqlStatement("DROP TABLE IF " +
             "EXISTS ad"));

        try {
            Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase()).execute(new RawParameterizedSqlStatement("CREATE TABLE ad (\n" +
                    "ad_id int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
                    "advertiser_id int(10) unsigned NOT NULL,\n" +
                    "ad_type_id int(10) unsigned NOT NULL,\n" +
                    "name varchar(155) NOT NULL DEFAULT '',\n" +
                    "label varchar(155)NOT NULL DEFAULT '',\n" +
                    "description text NOT NULL,\n" +
                    "active tinyint(1) NOT NULL DEFAULT '0',\n" +
                    "created datetime NOT NULL DEFAULT '0000-00-00 00:00:00',\n" +
                    "updated datetime DEFAULT '0000-00-00 00:00:00',\n" +
                    "PRIMARY KEY (ad_id),\n" +
                    "KEY active (active)\n" +
                    ")"));
        } catch (DatabaseException e) {
            if (e.getCause() instanceof SQLSyntaxErrorException) {
                Scope.getCurrentScope().getLog(getClass()).warning("MariaDB returned DatabaseException", e);
                assumeTrue("MariaDB seems to run in strict mode (no datetime literals with 0000-00-00 allowed). " + "Cannot run this test", false);

            } else {
                throw e;
            }
        }

        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(CatalogAndSchema.DEFAULT, getDatabase(), new SnapshotControl(getDatabase()));
        Column createdColumn = snapshot.get(new Column().setRelation(new Table().setName("ad").setSchema(new Schema())).setName("created"));

        Object defaultValue = createdColumn.getDefaultValue();
        assertNotNull(defaultValue);
        assertEquals("0000-00-00 00:00:00", defaultValue);
    }

}
