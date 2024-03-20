package liquibase.database;

import lombok.Data;

import java.io.Closeable;

/**
 * This class maintains an object quoting strategy and will reset the database's strategy when the close method is called.
 */
@Data
public class TempObjectQuotingStrategy implements Closeable {

    private final Database database;
    private final ObjectQuotingStrategy originalObjectQuotingStrategy;

    @Override
    public void close() {
        database.setObjectQuotingStrategy(originalObjectQuotingStrategy);
    }
}
