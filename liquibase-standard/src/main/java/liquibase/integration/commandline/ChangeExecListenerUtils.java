package liquibase.integration.commandline;

import liquibase.Scope;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.util.Properties;

public final class ChangeExecListenerUtils {
    private ChangeExecListenerUtils() {
    }

    public static ChangeExecListener getChangeExecListener(
            Database database, ResourceAccessor resourceAccessor,
            String changeExecListenerClass, String changeExecListenerPropertiesFile) throws Exception {

        ChangeExecListener changeExecListener = null;

        if (changeExecListenerClass != null) {
            Logger logger = Scope.getCurrentScope().getLog(ChangeExecListenerUtils.class);

            logger.fine("Setting ChangeExecListener: " + changeExecListenerClass);

            ClassLoader classLoader = Scope.getCurrentScope().getClassLoader();
            Class<?> clazz = Class.forName(changeExecListenerClass, true, classLoader);

            Properties properties = loadProperties(changeExecListenerPropertiesFile);
            Constructor<?> cons = getConstructor(clazz, Database.class, Properties.class);
            if (cons != null) {
                logger.fine("Create " + clazz.getSimpleName() + "(Database, Properties)");
                changeExecListener = (ChangeExecListener) cons.newInstance(database, properties);
            } else {
                cons = getConstructor(clazz, Properties.class, Database.class);
                if (cons != null) {
                    logger.fine("Create " + clazz.getSimpleName() + "(Properties, Database)");
                    changeExecListener = (ChangeExecListener) cons.newInstance(properties, database);
                } else {
                    cons = getConstructor(clazz, Database.class);
                    if (cons != null) {
                        logger.fine("Create " + clazz.getSimpleName() + "(Database)");
                        changeExecListener = (ChangeExecListener) cons.newInstance(database);
                    } else {
                        cons = getConstructor(clazz, Properties.class);
                        if (cons != null) {
                            logger.fine("Create " + clazz.getSimpleName() + "(Properties)");
                            changeExecListener = (ChangeExecListener) cons.newInstance(properties);
                        } else {
                            logger.fine("Create " + clazz.getSimpleName() + "()");
                            changeExecListener = (ChangeExecListener) clazz.getConstructor().newInstance();
                        }
                    }
                }
            }
        }
        return changeExecListener;
    }

    private static Constructor<?> getConstructor(Class<?> clazz, Class<?> ... params) {
        try {
            return clazz.getConstructor(params);
        } catch (Exception e) {
            return null;
        }
    }

    private static Properties loadProperties(final String propertiesFile) throws IOException {
        if (propertiesFile != null) {
            final File file = new File(propertiesFile);
            if (file.exists()) {
                final Properties properties = new Properties();
                try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                    properties.load(inputStream);
                }
                return properties;
            } else {
                throw new FileNotFoundException(propertiesFile);
            }
        } else {
            return null;
        }
    }
}
