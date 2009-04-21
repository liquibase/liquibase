package liquibase.database.sql.generator;

import liquibase.database.sql.SqlStatement;
import liquibase.database.Database;

import java.util.*;

/**
 * SqlGeneratorFactory is a singleton registry of SqlGenerators.
 * Use the register(SqlGenerator) method to add custom SqlGenerators,
 * and the getBestGenerator() method to retrieve the SqlGenerator that should be used for a given SqlStatement.
 */
public class SqlGeneratorFactory {

    private static SqlGeneratorFactory instance = new SqlGeneratorFactory();

    private List<SqlGenerator> generators = new ArrayList<SqlGenerator>();

    private SqlGeneratorFactory() {
    }

    /**
     * Return singleton SqlGeneratorFactory
     */
    public static SqlGeneratorFactory getInstance() {
        return instance;
    }


    public void register(SqlGenerator generator) {
        generators.add(generator);
    }


    /**
     * Internal method for retrieving all generators.  Mainly exists for unit testing.
     */
    protected List<SqlGenerator> getGenerators() {
        return generators;
    }

    public SqlGenerator getBestGenerator(final SqlStatement statement, final Database database) {
        SortedSet<SqlGenerator> validGenerators = new TreeSet<SqlGenerator>(new Comparator<SqlGenerator>() {
            public int compare(SqlGenerator o1, SqlGenerator o2) {
                return -1 * new Integer(o1.getApplicability(statement, database)).compareTo(o2.getApplicability(statement, database));
            }
        });

        for (SqlGenerator generator : getGenerators()) {
            if (generator.getApplicability(statement, database) >= 0) {
                validGenerators.add(generator);
            }
        }

        if (validGenerators.size() == 0) {
            return new NotImplementedGenerator();
        } else {
            return validGenerators.first();
        }
    }
}
