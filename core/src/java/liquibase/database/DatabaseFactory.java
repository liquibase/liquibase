package liquibase.database;

import liquibase.exception.DatabaseException;
import liquibase.util.log.LogFactory;
import liquibase.util.plugin.ClassPathScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class DatabaseFactory {
    private static DatabaseFactory instance;
    protected static final Logger log = LogFactory.getLogger();
    private List<Database> implementedDatabases = new ArrayList<Database>();

    static {
        try {
            instance = (DatabaseFactory) ClassPathScanner.getInstance().getClasses(DatabaseFactory.class)[0].newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected DatabaseFactory() {
        try {
            Class[] classes = ClassPathScanner.getInstance().getClasses(Database.class);

            for (Class<? extends Database> clazz : classes) {
                register(clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static DatabaseFactory getInstance() {
        return instance;
    }

    /**
     * Returns instances of all implemented database types.
     */
    public List<Database> getImplementedDatabases() {
        return implementedDatabases;
    }

    public void register(Database database) {
        implementedDatabases.add(0, database);
    }

    public abstract Database findCorrectDatabaseImplementation(DatabaseConnection connection) throws DatabaseException;

    public String findDefaultDriver(String url) {
        for (Database database : this.getImplementedDatabases()) {
            String defaultDriver = database.getDefaultDriver(url);
            if (defaultDriver != null) {
                return defaultDriver;
            }
        }

        return null;
    }
}
