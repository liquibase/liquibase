package liquibase.dbtest.mysql;

import liquibase.CatalogAndSchema;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.*;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class MySQLIntegrationTest extends AbstractIntegrationTest {

    public MySQLIntegrationTest() throws Exception {
        super("mysql", "jdbc:mysql://"+ getDatabaseServerHostname("MySQL") +":3306/lbcat");
    }

    @Test
    @Override
    public void testRunChangeLog() throws Exception {
        super.testRunChangeLog();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Test
    public void snapshot() throws Exception {
        if (getDatabase() == null) {
            return;
        }


        runCompleteChangeLog();
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(getDatabase().getDefaultSchema(), getDatabase(), new SnapshotControl(getDatabase()));
        System.out.println(snapshot);
    }

    @Test
    public void dateDefaultValue() throws Exception {
        if (getDatabase() == null) {
            return;
        }

        ExecutorService.getInstance().getExecutor(getDatabase()).execute(new RawSqlStatement("CREATE TABLE ad (\n" +
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

        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(CatalogAndSchema.DEFAULT, getDatabase(), new SnapshotControl(getDatabase()));
        Column createdColumn = snapshot.get(new Column().setRelation(new Table().setName("ad").setSchema(new Schema())).setName("created"));

        Object defaultValue = createdColumn.getDefaultValue();
        assertNotNull(defaultValue);
        assertTrue(defaultValue instanceof DatabaseFunction);
        assertEquals("0000-00-00 00:00:00", ((DatabaseFunction) defaultValue).getValue());
    }

}
