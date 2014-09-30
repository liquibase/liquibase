package liquibase.sdk.supplier.database;

import liquibase.servicelocator.ServiceLocator;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionSupplier {

    private static final Object existingConnectionsLock = new Object();
    private static Set<TestConnection> existingConnections = null;

    public ConnectionSupplier() {
        synchronized (existingConnectionsLock) {
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            List<Callable<Object>> todo = new ArrayList<Callable<Object>>();

            existingConnections = new HashSet<TestConnection>();
            for (final Class testConnectionClasses : ServiceLocator.getInstance().findClasses(TestConnection.class)) {
                todo.add(Executors.callable(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TestConnection testConnection = (TestConnection) testConnectionClasses.newInstance();
                            long startTime = System.currentTimeMillis();
                            testConnection.init();
                            if (testConnection.getConnectionUnavailableReason() == null) {
                                System.out.println("Connected to " + testConnection + " in " + ((System.currentTimeMillis() - startTime) / 1000d) + "s");
                            } else {
                                System.out.println("CANNOT CONNECT TO " + testConnection + " after " + ((System.currentTimeMillis() - startTime) / 1000d)+"s: "+testConnection.getConnectionUnavailableReason());

                            }
                            existingConnections.add(testConnection);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }

                    }
                }));
            }

            try {
                System.out.println("Opening connections...");
                executorService.invokeAll(todo);
            } catch (InterruptedException ignored) {
            }
            System.out.println("Opening connections done");

        }
    }

    /**
     * Return all TestConnections.
     */
    public Set<TestConnection> getConnections() throws Exception {
        synchronized (existingConnectionsLock) {
            if (existingConnections == null) {
            }

            return Collections.unmodifiableSet(existingConnections);
        }
    }

}
