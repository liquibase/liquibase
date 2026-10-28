package liquibase.snapshot.jvm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndexSnapshotGeneratorTest {

    @Test
    void unescapeMySqlExpression_singleQuotesAreUnescaped() {
        assertEquals("cast(json_extract(`payload`,_latin1'$.regionId') as unsigned)",
                IndexSnapshotGenerator.unescapeMySqlExpression("cast(json_extract(`payload`,_latin1\\'$.regionId\\') as unsigned)"));
    }

    @Test
    void unescapeMySqlExpression_quoteInsideLiteralKeepsItsOwnEscape() {
        // original expression: concat(`name`,_utf8mb4'it\'s')
        assertEquals("concat(`name`,_utf8mb4'it\\'s')",
                IndexSnapshotGenerator.unescapeMySqlExpression("concat(`name`,_utf8mb4\\'it\\\\\\'s\\')"));
    }

    @Test
    void unescapeMySqlExpression_backslashInsideLiteralIsUnescaped() {
        // original expression: concat(`name`,_utf8mb4'a\\b')
        assertEquals("concat(`name`,_utf8mb4'a\\\\b')",
                IndexSnapshotGenerator.unescapeMySqlExpression("concat(`name`,_utf8mb4\\'a\\\\\\\\b\\')"));
    }

    @Test
    void unescapeMySqlExpression_expressionWithoutEscapesIsUnchanged() {
        assertEquals("lower(`name`)", IndexSnapshotGenerator.unescapeMySqlExpression("lower(`name`)"));
    }
}
