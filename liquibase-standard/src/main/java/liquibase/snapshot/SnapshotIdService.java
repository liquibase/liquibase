package liquibase.snapshot;

import liquibase.checksums.ComputeChecksumService;
import lombok.Getter;

import java.util.Date;

public class SnapshotIdService {
    @Getter
    private static final SnapshotIdService instance = new SnapshotIdService();
    private int nextId = 100;
    private final String base = ComputeChecksumService.compute(Long.toString(new Date().getTime())).substring(0, 4);

    private SnapshotIdService() {
    }

    public String generateId() {
        return base+ nextId++;
    }
}
