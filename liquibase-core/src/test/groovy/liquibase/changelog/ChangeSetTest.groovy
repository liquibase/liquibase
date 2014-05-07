package liquibase.changelog

import liquibase.ContextExpression;
import liquibase.change.CheckSum;
import liquibase.change.core.AddDefaultValueChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.InsertDataChange
import liquibase.parser.core.ParsedNode
import liquibase.util.CollectionUtil
import spock.lang.Specification
import spock.lang.Unroll;

import static org.junit.Assert.*;
import org.junit.Test;

public class ChangeSetTest extends Specification {

    def getDescriptions() {
        when:
        def insertDescription = "insert";
        def changeSet = new ChangeSet("testId", "testAuthor", false, false,null, null, null, null);
        then:
        changeSet.getDescription() == "Empty"

        when:
        changeSet.addChange(new InsertDataChange());
        then:
        changeSet.getDescription() == insertDescription

        when:
        changeSet.addChange(new InsertDataChange());
        then:
        changeSet.getDescription() == insertDescription + " (x2)"

        when:
        changeSet.addChange(new CreateTableChange());
        then:
        changeSet.getDescription() == insertDescription + " (x2), createTable"
    }
    
    def generateCheckSum() {
        when:
        def changeSet1 = new ChangeSet("testId", "testAuthor", false, false,null, null, null, null);
        def changeSet2 = new ChangeSet("testId", "testAuthor", false, false,null, null, null, null);

        def change = new AddDefaultValueChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setDefaultValue("DEF STRING");
        change.setDefaultValueNumeric("42");
        change.setDefaultValueBoolean(true);
        change.setDefaultValueDate("2007-01-02");

        changeSet1.addChange(change);
        changeSet2.addChange(change);

        CheckSum md5Sum1 = changeSet1.generateCheckSum();

        change.setSchemaName("SCHEMA_NAME2");
        CheckSum md5Sum2 = changeSet2.generateCheckSum();

        then:
        assert !md5Sum1.equals(md5Sum2);
    }

    def isCheckSumValid_validCheckSum() {
        when:
        def changeSet = new ChangeSet("1", "2",false, false, "/test.xml",null, null, null);

        then:
        assertTrue(changeSet.isCheckSumValid(changeSet.generateCheckSum()));
    }

    def isCheckSumValid_invalidCheckSum() {
        when:
        def checkSum = CheckSum.parse("2:asdf");
        def changeSet = new ChangeSet("1", "2",false, false, "/test.xml",null, null, null);

        then:
        assert !changeSet.isCheckSumValid(checkSum)
    }

    def isCheckSumValid_differentButValidCheckSum() {
        when:
        CheckSum checkSum = CheckSum.parse("2:asdf");

        ChangeSet changeSet = new ChangeSet("1", "2",false, false, "/test.xml",null, null, null);
        changeSet.addValidCheckSum(changeSet.generateCheckSum().toString());

        then:
        assert changeSet.isCheckSumValid(checkSum)
    }

    def "load empty node"() {
        when:
        def changeSet = new ChangeSet(new DatabaseChangeLog("com/example/test.xml"))
        def node = new ParsedNode(null, "changeSet").addChildren([id: "1", author:"nvoxland"])
        changeSet.load(node)
        then:
        changeSet.toString(false) == "com/example/test.xml::1::nvoxland"
        changeSet.changes.size() == 0
    }

    def "load node with changeSet properties"() {
        when:
        def changeSet = new ChangeSet(new DatabaseChangeLog("com/example/test.xml"))
        def node = new ParsedNode(null, "changeSet")
        def fields = new ChangeSet(new DatabaseChangeLog()).getSerializableFields()
        def testValue = new HashMap()
        for (param in fields) {
            if (param in ["runAlways", "runOnChange", "failOnError"]) {
                testValue[param] = "true"
                node.addChild(null, param, testValue[param])
            } else if (param == "context") {
                testValue[param] = "test or value"
            } else if (param == "rollback" || param == "changes") {
                continue
            } else {
                testValue[param] = "value for ${param}"
            }
            node.addChild(null, param, testValue[param])
        }
        changeSet.load(node)

        then:

        for (param in fields) {
            if (param == "context") {
                assert changeSet.getSerializableFieldValue(param).toString() == "(${testValue[param]})"
            } else if (param in testValue.keySet()) {
                assert changeSet.getSerializableFieldValue(param).toString() == testValue[param]
            }
        }

    }

    def "load node with changes as direct children"() {
        when:
        def changeSet = new ChangeSet(new DatabaseChangeLog("com/example/test.xml"))
        def node = new ParsedNode(null, "changeSet")
                .addChildren([id: "1", author:"nvoxland"])
                .addChild(new ParsedNode(null, "createTable").addChild(null, "tableName", "table_1"))
                .addChild(new ParsedNode(null, "createTable").addChild(null, "tableName", "table_2"))
        changeSet.load(node)
        then:
        changeSet.toString(false) == "com/example/test.xml::1::nvoxland"
        changeSet.changes.size() == 2
        changeSet.changes[0].tableName == "table_1"
        changeSet.changes[1].tableName == "table_2"
    }
}