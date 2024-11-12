package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.servicelocator.LiquibaseService;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.TagDatabaseStatement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

public class ChainableAbstractSqlGeneratorTest {

    @Test
    public void generateSqlInChainableGenerator_returnAllSqlsFromAllGenerators() {
        // given
        SortedSet<SqlGenerator> generators = new TreeSet<>((o1, o2) -> -1 * Integer.compare(o1.getPriority(), o2.getPriority()));
        generators.add(new ChainableGeneratorTest(5, "SELECT * FROM TABLE1", "SELECT * FROM TABLE2"));
        generators.add(new ChainableGeneratorTest(1,"SELECT * FROM TABLE3"));
        SqlGeneratorChain sqlGeneratorChain = new SqlGeneratorChain(generators);

        // when
        Sql[] sqls = sqlGeneratorChain.generateSql(new TagDatabaseStatement("tag1"), new PostgresDatabase());

        // then
        assertEquals("SELECT * FROM TABLE1", sqls[0].toSql());
        assertEquals("SELECT * FROM TABLE2", sqls[1].toSql());
        assertEquals("SELECT * FROM TABLE3", sqls[2].toSql());

    }

    @LiquibaseService(skip = true)
    private static class ChainableGeneratorTest extends ChainableAbstractSqlGenerator<TagDatabaseStatement> {

        private String[] returnSql;
        private int priority;

        public ChainableGeneratorTest(int priority,String... returnSql) {
            this.priority = priority;
            this.returnSql = returnSql;
        }

        @Override
        public int getPriority() {
            return this.priority;
        }

        @Override
        public ValidationErrors validate(final TagDatabaseStatement statement, final Database database, final SqlGeneratorChain<TagDatabaseStatement> sqlGeneratorChain) {
            return null;
        }

        @Override
        public Sql[] generateSql(final TagDatabaseStatement statement, final Database database) {
            List<Sql> sql = new ArrayList<Sql>();
            for (String returnSql  : this.returnSql) {
                sql.add(new UnparsedSql(returnSql));
            }
            return sql.toArray(EMPTY_SQL);
        }
    }

}