package liquibase.sqlgenerator.core

import liquibase.database.core.MariaDBDatabase
import liquibase.database.core.MySQLDatabase
import liquibase.exception.DatabaseException
import liquibase.sqlgenerator.SqlGeneratorChain
import liquibase.statement.core.RenameColumnStatement
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Tests for {@link RenameColumnGenerator}, specifically focusing on MySQL and MariaDB
 * behavior for the renameColumn change type.
 * <p>
 * This test class verifies that:
 * <ul>
 *   <li>Modern MySQL (8.0+) and MariaDB (10.5+) use RENAME COLUMN syntax when no columnDataType is specified</li>
 *   <li>When columnDataType IS specified, the CHANGE syntax is used to allow type modifications</li>
 *   <li>Older MySQL/MariaDB versions always use CHANGE syntax</li>
 * </ul>
 * </p>
 *
 * @see <a href="https://github.com/liquibase/liquibase/issues/7339">GitHub Issue #7339</a>
 */
class RenameColumnGeneratorTest extends Specification {

    @Unroll
    def "MySQL #majorVersion.#minorVersion without columnDataType should use RENAME COLUMN syntax"() {
        given:
        def statement = new RenameColumnStatement(null, null, "test_table", "old_column", "new_column", null, null)
        def generator = new RenameColumnGenerator()
        def database = createMySQLDatabase(majorVersion, minorVersion)

        when:
        def sql = generator.generateSql(statement, database, new SqlGeneratorChain(null))

        then:
        sql[0].toSql() == expectedSql

        where:
        majorVersion | minorVersion | expectedSql
        8            | 0            | "ALTER TABLE test_table RENAME COLUMN old_column TO new_column"
        8            | 4            | "ALTER TABLE test_table RENAME COLUMN old_column TO new_column"
    }

    @Unroll
    def "MySQL #majorVersion.#minorVersion requires columnDataType for CHANGE syntax"() {
        given:
        def statement = new RenameColumnStatement(null, null, "test_table", "old_column", "new_column", "VARCHAR(255)", null)
        def generator = new RenameColumnGenerator()
        def database = createMySQLDatabase(majorVersion, minorVersion)

        when:
        def sql = generator.generateSql(statement, database, new SqlGeneratorChain(null))

        then:
        sql[0].toSql() == expectedSql

        where:
        majorVersion | minorVersion | expectedSql
        5            | 7            | "ALTER TABLE test_table CHANGE old_column new_column VARCHAR(255)"
        5            | 6            | "ALTER TABLE test_table CHANGE old_column new_column VARCHAR(255)"
    }

    @Unroll
    def "MySQL #majorVersion.#minorVersion with columnDataType should use CHANGE syntax"() {
        given:
        def statement = new RenameColumnStatement(null, null, "test_table", "old_column", "new_column", "VARCHAR(64)", null)
        def generator = new RenameColumnGenerator()
        def database = createMySQLDatabase(majorVersion, minorVersion)

        when:
        def sql = generator.generateSql(statement, database, new SqlGeneratorChain(null))

        then:
        sql[0].toSql() == expectedSql

        where:
        majorVersion | minorVersion | expectedSql
        8            | 0            | "ALTER TABLE test_table CHANGE old_column new_column VARCHAR(64)"
        8            | 4            | "ALTER TABLE test_table CHANGE old_column new_column VARCHAR(64)"
        5            | 7            | "ALTER TABLE test_table CHANGE old_column new_column VARCHAR(64)"
    }

    @Unroll
    def "MariaDB #majorVersion.#minorVersion without columnDataType should use RENAME COLUMN syntax"() {
        given:
        def statement = new RenameColumnStatement(null, null, "test_table", "old_column", "new_column", null, null)
        def generator = new RenameColumnGenerator()
        def database = createMariaDBDatabase(majorVersion, minorVersion)

        when:
        def sql = generator.generateSql(statement, database, new SqlGeneratorChain(null))

        then:
        sql[0].toSql() == expectedSql

        where:
        majorVersion | minorVersion | expectedSql
        10           | 5            | "ALTER TABLE test_table RENAME COLUMN old_column TO new_column"
        10           | 6            | "ALTER TABLE test_table RENAME COLUMN old_column TO new_column"
        11           | 0            | "ALTER TABLE test_table RENAME COLUMN old_column TO new_column"
        11           | 4            | "ALTER TABLE test_table RENAME COLUMN old_column TO new_column"
    }

    @Unroll
    def "MariaDB #majorVersion.#minorVersion requires columnDataType for CHANGE syntax"() {
        given:
        def statement = new RenameColumnStatement(null, null, "test_table", "old_column", "new_column", "VARCHAR(255)", null)
        def generator = new RenameColumnGenerator()
        def database = createMariaDBDatabase(majorVersion, minorVersion)

        when:
        def sql = generator.generateSql(statement, database, new SqlGeneratorChain(null))

        then:
        sql[0].toSql() == expectedSql

        where:
        majorVersion | minorVersion | expectedSql
        10           | 4            | "ALTER TABLE test_table CHANGE old_column new_column VARCHAR(255)"
        10           | 3            | "ALTER TABLE test_table CHANGE old_column new_column VARCHAR(255)"
    }

    @Unroll
    def "MariaDB #majorVersion.#minorVersion with columnDataType should use CHANGE syntax (issue #7339)"() {
        given:
        def statement = new RenameColumnStatement(null, null, "test_table", "old_column", "new_column", "VARCHAR(64)", null)
        def generator = new RenameColumnGenerator()
        def database = createMariaDBDatabase(majorVersion, minorVersion)

        when:
        def sql = generator.generateSql(statement, database, new SqlGeneratorChain(null))

        then:
        sql[0].toSql() == expectedSql

        where:
        majorVersion | minorVersion | expectedSql
        10           | 5            | "ALTER TABLE test_table CHANGE old_column new_column VARCHAR(64)"
        10           | 11           | "ALTER TABLE test_table CHANGE old_column new_column VARCHAR(64)"
        11           | 0            | "ALTER TABLE test_table CHANGE old_column new_column VARCHAR(64)"
        11           | 4            | "ALTER TABLE test_table CHANGE old_column new_column VARCHAR(64)"
        10           | 4            | "ALTER TABLE test_table CHANGE old_column new_column VARCHAR(64)"
    }

    @Unroll
    def "validation requires columnDataType for MySQL #majorVersion.#minorVersion when RENAME COLUMN not supported"() {
        given:
        def statement = new RenameColumnStatement(null, null, "test_table", "old_column", "new_column", null, null)
        def generator = new RenameColumnGenerator()
        def database = createMySQLDatabase(majorVersion, minorVersion)

        when:
        def errors = generator.validate(statement, database, null)

        then:
        errors.hasErrors() == expectError
        if (expectError) {
            errors.getErrorMessages().any { it.contains("columnDataType") }
        }

        where:
        majorVersion | minorVersion | expectError
        5            | 7            | true   // MySQL 5.7 requires columnDataType
        5            | 6            | true   // MySQL 5.6 requires columnDataType
        8            | 0            | false  // MySQL 8.0+ doesn't require columnDataType
        8            | 4            | false  // MySQL 8.4 doesn't require columnDataType
    }

    @Unroll
    def "validation requires columnDataType for MariaDB #majorVersion.#minorVersion when RENAME COLUMN not supported"() {
        given:
        def statement = new RenameColumnStatement(null, null, "test_table", "old_column", "new_column", null, null)
        def generator = new RenameColumnGenerator()
        def database = createMariaDBDatabase(majorVersion, minorVersion)

        when:
        def errors = generator.validate(statement, database, null)

        then:
        errors.hasErrors() == expectError
        if (expectError) {
            errors.getErrorMessages().any { it.contains("columnDataType") }
        }

        where:
        majorVersion | minorVersion | expectError
        10           | 4            | true   // MariaDB 10.4 requires columnDataType
        10           | 3            | true   // MariaDB 10.3 requires columnDataType
        10           | 5            | false  // MariaDB 10.5+ doesn't require columnDataType
        10           | 11           | false  // MariaDB 10.11 doesn't require columnDataType
        11           | 0            | false  // MariaDB 11.0+ doesn't require columnDataType
    }

    def "columnDataType with whitespace only is treated as null (uses RENAME COLUMN)"() {
        given:
        def statement = new RenameColumnStatement(null, null, "test_table", "old_column", "new_column", "   ", null)
        def generator = new RenameColumnGenerator()
        def database = createMySQLDatabase(8, 0)

        when:
        def sql = generator.generateSql(statement, database, new SqlGeneratorChain(null))

        then:
        sql[0].toSql() == "ALTER TABLE test_table RENAME COLUMN old_column TO new_column"
    }

    def "empty string columnDataType is treated as null (uses RENAME COLUMN)"() {
        given:
        def statement = new RenameColumnStatement(null, null, "test_table", "old_column", "new_column", "", null)
        def generator = new RenameColumnGenerator()
        def database = createMariaDBDatabase(10, 5)

        when:
        def sql = generator.generateSql(statement, database, new SqlGeneratorChain(null))

        then:
        sql[0].toSql() == "ALTER TABLE test_table RENAME COLUMN old_column TO new_column"
    }

    /**
     * Creates a mock MySQLDatabase with the specified version numbers.
     */
    private MySQLDatabase createMySQLDatabase(int majorVersion, int minorVersion) {
        return new MySQLDatabase() {
            @Override
            int getDatabaseMajorVersion() throws DatabaseException {
                return majorVersion
            }

            @Override
            int getDatabaseMinorVersion() throws DatabaseException {
                return minorVersion
            }
        }
    }

    /**
     * Creates a mock MariaDBDatabase with the specified version numbers.
     */
    private MariaDBDatabase createMariaDBDatabase(int majorVersion, int minorVersion) {
        return new MariaDBDatabase() {
            @Override
            int getDatabaseMajorVersion() throws DatabaseException {
                return majorVersion
            }

            @Override
            int getDatabaseMinorVersion() throws DatabaseException {
                return minorVersion
            }
        }
    }
}
