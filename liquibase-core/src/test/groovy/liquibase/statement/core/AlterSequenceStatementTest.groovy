package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class AlterSequenceStatementTest extends AbstractStatementTest<AlterSequenceStatement> {

    def "constructor"() {
        when:
        def obj = new AlterSequenceStatement("CAT_NAME", "SCHEMA_NAME", "SEQ_NAME")

        then:
        obj.getCatalogName() == "CAT_NAME"
        obj.getSchemaName() == "SCHEMA_NAME"
        obj.getSequenceName() == "SEQ_NAME"
    }

}
