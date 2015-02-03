package liquibase.datatype.core

import spock.lang.Specification

class VarcharTypeTest extends Specification {
//    @Unroll
//    def "toDatabaseType"() {
//        when:
//        def type = new VarcharType()
//        for (param in params) {
//            type.addParameter(param)
//        }
//
//        then:
//        type.toDatabaseDataType(database).toString() == expected
//
//        where:
//        params       | database               | expected
//        [13]         | new DerbyDatabase()    | "VARCHAR(13)"
//        [13]         | new HsqlDatabase()     | "VARCHAR(13)"
//        [13]         | new PostgresDatabase() | "VARCHAR(13)"
//        [13]         | new OracleDatabase()   | "VARCHAR2(13)"
//        [13]         | new MSSQLDatabase()    | "VARCHAR(13)"
//        [2147483647] | new MSSQLDatabase()    | "VARCHAR(MAX)"
//        [13]         | new MySQLDatabase()    | "VARCHAR(13)"
//    }

    //    @Test
//    public void varchar2ForHsqldbInOracleSyntaxMode() {
//        VarcharType type = new VarcharType();
//        HsqlDatabase hsqlDatabase = mock(HsqlDatabase.class);
//        when(hsqlDatabase.isUsingOracleSyntax()).thenReturn(true);
//        DatabaseDataType databaseDataType = type.toDatabaseDataType(hsqlDatabase);
//        assertEquals("VARCHAR2", databaseDataType.getType().toUpperCase());
//    }
//
//    @Test
//    public void varcharForHsqldbNotInOracleSyntaxMode() {
//        VarcharType type = new VarcharType();
//        HsqlDatabase hsqlDatabase = mock(HsqlDatabase.class);
//        when(hsqlDatabase.isUsingOracleSyntax()).thenReturn(false);
//        DatabaseDataType databaseDataType = type.toDatabaseDataType(hsqlDatabase);
//        assertEquals("VARCHAR", databaseDataType.getType().toUpperCase());
//    }

}
