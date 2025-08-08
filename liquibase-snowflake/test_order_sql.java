import liquibase.change.core.CreateSequenceChange;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.statement.SqlStatement;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sql.Sql;

public class test_order_sql {
    public static void main(String[] args) {
        try {
            SnowflakeDatabase database = new SnowflakeDatabase();
            
            // Test ORDER sequence
            CreateSequenceChange orderedChange = new CreateSequenceChange();
            orderedChange.setSequenceName("TEST_ORDERED");
            orderedChange.setOrdered(true);
            orderedChange.setComment("Test ordered sequence");
            
            SqlStatement[] statements = orderedChange.generateStatements(database);
            for (SqlStatement statement : statements) {
                Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
                for (Sql sql : sqls) {
                    System.out.println("ORDERED SQL: " + sql.toSql());
                }
            }
            
            // Test NOORDER sequence
            CreateSequenceChange noOrderChange = new CreateSequenceChange();
            noOrderChange.setSequenceName("TEST_NOORDER");
            noOrderChange.setOrdered(false);
            noOrderChange.setComment("Test noorder sequence");
            
            SqlStatement[] statements2 = noOrderChange.generateStatements(database);
            for (SqlStatement statement : statements2) {
                Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
                for (Sql sql : sqls) {
                    System.out.println("NOORDER SQL: " + sql.toSql());
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}