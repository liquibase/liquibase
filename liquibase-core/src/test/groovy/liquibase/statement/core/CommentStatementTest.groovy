package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class CommentStatementTest extends AbstractStatementTest<CommentStatement> {

    def "toString logic"() {
        when:
        def statement = new CommentStatement(comment)

        then:
        statement.toString() == expected

        where:
        comment                                                                                                                                    | expected
        null                                                                                                                                       | null
        ""                                                                                                                                         | ""
        "short text"                                                                                                                               | "short text"
        "a long message that has more than the max chars allowed of 80. I needed to add more to make sure to get there becauase 80 chars is a lot" | "a long message that has more than the max chars allowed of 80. I needed to ad..."
    }
}