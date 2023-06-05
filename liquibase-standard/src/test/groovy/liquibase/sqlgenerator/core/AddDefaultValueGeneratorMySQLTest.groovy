package liquibase.sqlgenerator.core

import liquibase.database.core.MySQLDatabase
import liquibase.exception.DatabaseException
import liquibase.sqlgenerator.SqlGeneratorChain
import liquibase.statement.DatabaseFunction
import liquibase.statement.core.AddDefaultValueStatement
import spock.lang.Specification
import spock.lang.Unroll

class AddDefaultValueGeneratorMySQLTest extends Specification {

    @Unroll
    def "set default value #finalDefaultValue for MySQL database and validate expected SQL is #expectedSQL"() throws DatabaseException{
        when:
        def addDefaultValueStatement = new AddDefaultValueStatement("lbcat", "public", "testTable", "testColumn", columnDataType, finalDefaultValue);
        AddDefaultValueGeneratorMySQL defaultValueGenerator = new AddDefaultValueGeneratorMySQL();
        def unparseSQL = defaultValueGenerator.generateSql(addDefaultValueStatement, new MySQLDatabase(), new SqlGeneratorChain(null));

        then:
        unparseSQL*.toSql() == expectedSQL;

        where:
        columnDataType | finalDefaultValue                           | expectedSQL
        "timestamp"    | new DatabaseFunction("CURRENT_TIMESTAMP()") | ["ALTER TABLE lbcat.testTable ALTER testColumn SET DEFAULT (CURRENT_TIMESTAMP())"]
        "timestamp"    | "2002-01-1"                                 | ["ALTER TABLE lbcat.testTable ALTER testColumn SET DEFAULT '2002-01-1'"]
        "timestamp"    | new DatabaseFunction(null)                  | ["ALTER TABLE lbcat.testTable ALTER testColumn SET DEFAULT (null)"]
        "int"          | 1                                           | ["ALTER TABLE lbcat.testTable ALTER testColumn SET DEFAULT 1"]
        "int"          | "CEILING(4)"                                | ["ALTER TABLE lbcat.testTable ALTER testColumn SET DEFAULT 'CEILING(4)'"]
        "int"          | new DatabaseFunction("CEILING(4)")          | ["ALTER TABLE lbcat.testTable ALTER testColumn SET DEFAULT (CEILING(4))"]
    }

    @Unroll
    def "validate generated errors message when setting up Default Value for MySQL version #majorVersion "() {
        when:
        def addDefaultValueStatement = new AddDefaultValueStatement("lbcat", "public", "testTable", "testColumn", columnDataType, finalDefaultValue);
        AddDefaultValueGeneratorMySQL defaultValueGenerator = new AddDefaultValueGeneratorMySQL();

        int majorDBVersion =  majorVersion;
        def errors = defaultValueGenerator.validate(addDefaultValueStatement, new MySQLDatabase() {
            @Override
            int getDatabaseMajorVersion() throws DatabaseException {
                return majorDBVersion
            }
        }, null)

        then:
        errors.getErrorMessages() == expectedErrors

        where:
        majorVersion | expectedErrors                                                         | columnDataType | finalDefaultValue
        5            | ["This version of mysql does not support non-literal default values"]  | "timestamp"    | new DatabaseFunction("CURRENT_TIMESTAMP()")
        8            | []                                                                     | "timestamp"    | "2002-01-1"
    }
}
