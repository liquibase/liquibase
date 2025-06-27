package liquibase.serializer.core.yaml;

import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.InsertDataChange;
import liquibase.changelog.ChangeSet;

import org.junit.Assert;
import org.junit.Test;

public class YamlChangeLogSerializerTest {

    @Test
    public void serialize__createTableChange() {
        ChangeSet changeSet = new ChangeSet("test1", "nvoxland", false, true, "/test/me.txt", null, null,false, null);
        changeSet.setIgnore(true);
        changeSet.setRunOrder("last");
        CreateTableChange change = new CreateTableChange();
        change.setTableName("testTable");
        change.addColumn(new ColumnConfig().setName("id").setType("int"));
        change.addColumn(new ColumnConfig().setName("name").setType("varchar(255)"));
        changeSet.addChange(change);

        String expectedOutput = "changeSet:\n" +
                "  id: test1\n" +
                "  author: nvoxland\n" +
                "  ignore: true\n" +
                "  objectQuotingStrategy: LEGACY\n" +
                "  runInTransaction: false\n" +
                "  runOnChange: true\n" +
                "  runOrder: last\n" +
                "  changes:\n" +
                "  - createTable:\n" +
                "      columns:\n" +
                "      - column:\n" +
                "          name: id\n" +
                "          type: int\n" +
                "      - column:\n" +
                "          name: name\n" +
                "          type: varchar(255)\n" +
                "      tableName: testTable\n";

        String actualOutput = new YamlChangeLogSerializer().serialize(changeSet, false);
        Assert.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void serialize__insertDataChange() {
        ChangeSet changeSet = new ChangeSet("test1", "nvoxland", false, true, "/test/me.txt", null, null,false, null);
        InsertDataChange change = new InsertDataChange();
        change.setTableName("testTable");
        change.addColumn(new ColumnConfig().setName("x").setValue(null));
        change.addColumn(new ColumnConfig().setName("y").setValue(null));
        change.addColumn(new ColumnConfig().setName("z").setValue("Geronimo!"));
        changeSet.addChange(change);

        String expectedOutput = "changeSet:\n" +
                "  id: test1\n" +
                "  author: nvoxland\n" +
                "  objectQuotingStrategy: LEGACY\n" +
                "  runInTransaction: false\n" +
                "  runOnChange: true\n" +
                "  changes:\n" +
                "  - insert:\n" +
                "      columns:\n" +
                "      - column:\n" +
                "          name: x\n" +
                "      - column:\n" +
                "          name: y\n" +
                "      - column:\n" +
                "          name: z\n" +
                "          value: Geronimo!\n" +
                "      tableName: testTable\n";

        String actualOutput = new YamlChangeLogSerializer().serialize(changeSet, false);
        Assert.assertEquals(expectedOutput, actualOutput);
    }

//    @Test
//    public void serialize_changelog() {
//        ChangeSet changeSet = new ChangeSet("test1", "nvoxland", false, true, "/test/me.txt", null, null);
//        CreateTableChange change = new CreateTableChange();
//        change.setTableName("testTable");
//        change.addColumn(new ColumnConfig().setName("id").setType("int"));
//        change.addColumn(new ColumnConfig().setName("name").setType("varchar(255)"));
//        changeSet.addChange(change);
//
//        DatabaseChangeLog changeLog = new DatabaseChangeLog("physical/path.txt");
//        changeLog.addChangeSet(changeSet);
//
//        String out = new YamlChangeLogSerializer().serialize(changeLog);
//
//        System.out.println(out);
//    }
}
