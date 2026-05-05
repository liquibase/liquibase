package liquibase.diff.output.changelog.core

import spock.lang.Specification
import spock.lang.Unroll

class MissingDataExternalFileChangeGeneratorTest extends Specification {

    @Unroll
    def "sanitizeTableName blocks path traversal: '#input' -> '#expected'"() {
        expect:
        MissingDataExternalFileChangeGenerator.sanitizeTableName(input) == expected

        where:
        input                      | expected
        "../escaped/export_target" | "___escaped_export_target"
        "../../etc/passwd"         | "______etc_passwd"
        ".."                       | "__"
        "../sibling"               | "___sibling"
    }

    @Unroll
    def "sanitizeTableName preserves safe characters: '#input' -> '#expected'"() {
        expect:
        MissingDataExternalFileChangeGenerator.sanitizeTableName(input) == expected

        where:
        input              | expected
        "my_table"         | "my_table"
        "MyTable"          | "mytable"
        "table-name"       | "table-name"
        "table123"         | "table123"
        "UPPER_CASE_TABLE" | "upper_case_table"
    }

    @Unroll
    def "sanitizeTableName replaces special characters: '#input' -> '#expected'"() {
        expect:
        MissingDataExternalFileChangeGenerator.sanitizeTableName(input) == expected

        where:
        input           | expected
        "table name"    | "table_name"
        "table.name"    | "table_name"
        "table/name"    | "table_name"
        "table\\name"   | "table_name"
        "table!@#name"  | "table___name"
        "schéma_table"  | "sch_ma_table"
    }
}
