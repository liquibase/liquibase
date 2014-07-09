package liquibase.sdk.supplier.database;

import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

public abstract class JdbcTestConnection extends AbstractTestConnection {

    private Connection connection;
    private Object unavailableConnectionProxy;

    protected JdbcTestConnection() {
    }

    protected String getDriverClassName() throws Exception {
        return getCorrectDatabase().getDefaultDriver(getUrl());
    }

    @Override
    public boolean connectionIsAvailable() {
        return unavailableConnectionProxy == null;
    }

    public void init() throws Exception {
        if (connection == null) {
            try {
                connection = openConnection();
            } catch (Throwable e) {
                unavailableConnectionProxy = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Connection.class, DatabaseMetaData.class}, new UnavailableConnection(e));
                connection = (Connection) unavailableConnectionProxy;
            }
        }

    }

    @Override
    public DatabaseConnection getConnection() {
        return new JdbcConnection(connection);
    }

    protected Connection openConnection() throws Exception {
        File baseDir = new File("/liquibase-bin/lib");
        File[] jarFiles = baseDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jar") && !name.toLowerCase().startsWith("snakeyaml");
            }
        });

        URL[] urls = new URL[jarFiles.length];
        for (int i = 0; i < jarFiles.length; i++) {
            urls[i] = jarFiles[i].toURL();
        }
        URLClassLoader classLoader = new URLClassLoader(urls, this.getClass().getClassLoader());
        String driverClassName = getDriverClassName();
        Enumeration<URL> resources = classLoader.getResources(driverClassName.replace(".", "/") + ".class");
        int count = 0;
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            count++;
            if (count > 1) {
                System.out.println("Found multiple versions of "+ driverClassName);
            }
        }

        Driver driver = (Driver) Class.forName(driverClassName, true, classLoader).newInstance();

        Properties properties = getConnectionProperties();

        return driver.connect(this.getUrl(), properties);
    }

    protected Properties getConnectionProperties() {
        Properties properties = new Properties();
        properties.setProperty("user", this.getDatabaseUsername());
        properties.setProperty("password", this.getDatabasePassword());
        properties.setProperty("loginTimeout", "1");
        return properties;
    }

    protected String getDatabaseUsername() {
        return "lbuser";
    }

    protected String getDatabasePassword() {
        return "lbuser";
    }


    protected abstract String getUrl();

    protected String getIpAddress() {
        return "10.10.100.100";
    }

    public String getPrimaryCatalog() {
        return "lbcat";
    }

    public String getPrimarySchema() {
        return "lbschema";
    }

    @Override
    public Class<? extends DatabaseConnection> getConnectionClass() {
        return JdbcConnection.class;
    }

    private class UnavailableConnection implements InvocationHandler {
        private final Throwable openException;

        public UnavailableConnection(Throwable openException) {
            this.openException = openException;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass().equals(Connection.class)) {
                if (method.getName().equals("getMetaData")) {
                    return unavailableConnectionProxy;
                } else if (method.getName().equals("getAutoCommit")) {
                    return true;
                } else if (method.getName().equals("setAutoCommit")) {
                    return null;
                } else if (method.getName().equals("getCatalog")) {
                    return getPrimaryCatalog();
                }
            } else if (method.getDeclaringClass().equals(DatabaseMetaData.class)) {
                if (method.getName().equals("getUserName")) {
                    return getDatabaseUsername();
                } else if (method.getName().equals("getURL")) {
                    return getUrl();
                } else if (method.getName().equals("getSQLKeywords")) {
                    return "";
                } else if (method.getName().equals("supportsMixedCaseIdentifiers")) {
                    return true;
                }
            } else if (method.getDeclaringClass().equals(Object.class)) {
                if (method.getName().equals("hashCode")) {
                    return JdbcTestConnection.this.describe().hashCode();
                }
            }
            throw new SQLException("Cannot call '" + method.toString() + "' because the connection is unavailable: " + openException.getMessage(), openException);
        }
    }
}
