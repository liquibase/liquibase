package liquibase.util;

import liquibase.Scope;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.ActionExecutor;
import liquibase.exception.ActionPerformException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectReference;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Properties;

public class LiquibaseUtil {
    public static String getBuildVersion() {
        String buildVersion = "UNKNOWN";
        Properties buildInfo = new Properties();
        ClassLoader classLoader = LiquibaseUtil.class.getClassLoader();

        URL buildInfoFile = classLoader.getResource("buildinfo.properties");
        InputStream in = null;
        try {
            if (buildInfoFile != null) {
            	URLConnection connection = buildInfoFile.openConnection();
            	connection.setUseCaches(false);
                in = connection.getInputStream();
                buildInfo.load(in);
                String o = (String) buildInfo.get("build.version");

                if (o != null) {
                    buildVersion = o;
                }
            }
        } catch (IOException e) {
            // This is not a fatal exception.
            // Build info will be returned as 'UNKNOWN'        }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // TODO Log this error and remove the RuntimeException.
                    throw new RuntimeException("Failed to close InputStream in LiquibaseUtil.", e);
                }
            }
        }

        return buildVersion;
    }

    /**
     * Convenience method for snapshotting a particular object. Returns null if none are found.
     */
    public static <T extends DatabaseObject> T snapshotObject(Class<T> type, DatabaseObject relatedTo, Scope scope) throws ActionPerformException {
        ActionExecutor actionExecutor = scope.getSingleton(ActionExecutor.class);

        return actionExecutor.query(new SnapshotDatabaseObjectsAction(type, relatedTo), scope).asObject(type);
    }

    /**
     * Convenience method for snapshotting multiple objects. Returns empty list if none are found.
     */
    public static <T extends DatabaseObject> List<T> snapshotAll(Class<T> type, DatabaseObject relatedTo, Scope scope) throws ActionPerformException {
        ActionExecutor actionExecutor = scope.getSingleton(ActionExecutor.class);

        return actionExecutor.query(new SnapshotDatabaseObjectsAction(type, relatedTo), scope).asList(type);
    }
}
