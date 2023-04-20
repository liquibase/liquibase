package liquibase.snapshot;

import liquibase.util.MD5Util;

import java.util.Date;

public class SnapshotIdService {
    private static final SnapshotIdService instance = new SnapshotIdService();
    private int nextId = 100;
    private final String base = MD5Util.computeMD5(Long.toString(new Date().getTime())).substring(0, 4);

    public static SnapshotIdService getInstance() {
        return instance;
    }

    private SnapshotIdService() {

    }

    public String generateId() {
        return base+ nextId++;
    }
}
