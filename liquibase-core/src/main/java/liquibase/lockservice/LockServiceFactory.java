package liquibase.lockservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import liquibase.database.Database;
import liquibase.servicelocator.ServiceLocator;

/**
 * @author John Sanda
 */
public class LockServiceFactory {

    private static LockServiceFactory instance;

    private Map<Database, LockService> lockServices = new ConcurrentHashMap<Database, LockService>();

    public static LockServiceFactory getInstance() {
        if (instance == null) {
            instance = new LockServiceFactory();
        }
        return instance;
    }

    private LockServiceFactory() {
//        Class[] classes;
//        try {
//            classes = ServiceLocator.getInstance().findClasses(LockService.class);
//
//            for (Class clazz : classes) {
//                register((LockService) clazz.getConstructor().newInstance());
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

    public LockService getLockService(Database database) {
//        LockService lockService = lockServices.get(0);
//        lockService.setDatabase(database);
//        return lockService;
        if (!lockServices.containsKey(database)) {
            LockService lockService = new LockServiceImpl();
            lockService.setDatabase(database);
            lockServices.put(database, lockService);
        }
        return lockServices.get(database);
    }

    public Collection<LockService> getLockServices() {
        return lockServices.values();
    }

//    public void register(LockService lockService) {
//        lockServices.add(lockService);
//    }

}
