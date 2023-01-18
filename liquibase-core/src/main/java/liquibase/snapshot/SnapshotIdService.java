package liquibase.snapshot;

import liquibase.util.MD5Util;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class SnapshotIdService {
    private static final SnapshotIdService instance = new SnapshotIdService();
    private final AtomicInteger nextId = new AtomicInteger(100);
    private final String base = MD5Util.computeMD5(Long.toString(new Date().getTime())).substring(0, 4);

    public static SnapshotIdService getInstance() {
        return instance;
    }

    private SnapshotIdService() {

    }

    public String generateId() {
        return base + nextId.getAndIncrement();
    }
}
