package liquibase.sqlgenerator.core

import liquibase.database.Database
import liquibase.database.core.MSSQLDatabase
import liquibase.sql.Sql
import liquibase.sqlgenerator.SqlGeneratorChain
import liquibase.statement.core.CreateViewStatement
import spock.lang.Specification
import spock.lang.Unroll

class CreateViewGeneratorSpockTest extends Specification {

    private static final String DEFAULT_SET_ANSI_STATEMENT = "SET ANSI_NULLS ON"
    private static final String DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT = "SET QUOTED_IDENTIFIER ON"

    @Unroll
    def "Verify CreateViewGenerator logic correctly generates create view sql statement based on given procedureText and replaceIfExists attributes for MSSQL"() {
        given:
        Database database = new MSSQLDatabase()
        SqlGeneratorChain chain = new SqlGeneratorChain(new TreeSet<>())
        CreateViewGenerator generator = new CreateViewGenerator()
        CreateViewStatement statement = new CreateViewStatement(null, schemaName, viewName, viewText, reaplceIfExists)
        statement.setFullDefinition(isFullDefinition)

        when:
        Sql[] generatedSql = generator.generateSql(statement, database, chain)

        then:
        generatedSql.size() == expectedSql.size()
        generatedSql.collect{it.toSql()} == expectedSql

        where:
        viewText                                                                        | schemaName | viewName | reaplceIfExists | isFullDefinition | expectedSql
        "CREATE VIEW [dbo].[view_1]"                                                    | "dbo"      | "view_1" | true            | true             | ["IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[DBO].[view_1]'))\n    EXEC sp_executesql N'CREATE VIEW [DBO].[view_1] AS SELECT ''This is a code stub which will be replaced by an Alter Statement'' as [code_stub]'", "ALTER VIEW [dbo].[view_1]", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
        "CREATE VIEW [dbo].[view_1] AS SELECT CREATE FROM EMPLOYEE"                     | "dbo"      | "view_1" | true            | true             | ["IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[DBO].[view_1]'))\n    EXEC sp_executesql N'CREATE VIEW [DBO].[view_1] AS SELECT ''This is a code stub which will be replaced by an Alter Statement'' as [code_stub]'", "ALTER VIEW [dbo].[view_1] AS SELECT CREATE FROM EMPLOYEE", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
        "CREATE VIEW [dbo].[view_1]"                                                    | "dbo"      | "view_1" | false           | true             | ["CREATE VIEW [dbo].[view_1]", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
        "CREATE OR ALTER VIEW [dbo].[view_1]"                                           | "dbo"      | "view_1" | true            | true             | ["IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[DBO].[view_1]'))\n    EXEC sp_executesql N'CREATE VIEW [DBO].[view_1] AS SELECT ''This is a code stub which will be replaced by an Alter Statement'' as [code_stub]'", "CREATE OR ALTER VIEW [dbo].[view_1]", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
        "CREATE OR ALTER VIEW [dbo].[view_1] AS SELECT CREATE, OR, ALTER FROM EMPLOYEE" | "dbo"      | "view_1" | true            | true             | ["IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[DBO].[view_1]'))\n    EXEC sp_executesql N'CREATE VIEW [DBO].[view_1] AS SELECT ''This is a code stub which will be replaced by an Alter Statement'' as [code_stub]'", "CREATE OR ALTER VIEW [dbo].[view_1] AS SELECT CREATE, OR, ALTER FROM EMPLOYEE", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
        "CREATE OR ALTER VIEW [dbo].[view_1] AS SELECT create, or, alter FROM employee" | "dbo"      | "view_1" | true            | true             | ["IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[DBO].[view_1]'))\n    EXEC sp_executesql N'CREATE VIEW [DBO].[view_1] AS SELECT ''This is a code stub which will be replaced by an Alter Statement'' as [code_stub]'", "CREATE OR ALTER VIEW [dbo].[view_1] AS SELECT create, or, alter FROM employee", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
        "CREATE OR ALTER VIEW [dbo].[view_1]"                                           | "dbo"      | "view_1" | false           | true             | ["CREATE OR ALTER VIEW [dbo].[view_1]", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
        "SELECT LastName FROM [dbo].view_1;"                                            | "dbo"      | "view_1" | false           | false            | ["CREATE VIEW [dbo].[view_1] AS SELECT LastName FROM [dbo].view_1;", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
        "SELECT LastName FROM [dbo].view_1;"                                            | "dbo"      | "view_1" | true            | false            | ["IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[DBO].[view_1]'))\n    EXEC sp_executesql N'CREATE VIEW [DBO].[view_1] AS SELECT ''This is a code stub which will be replaced by an Alter Statement'' as [code_stub]'", "ALTER VIEW [dbo].[view_1] AS SELECT LastName FROM [dbo].view_1;", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
    }

}
