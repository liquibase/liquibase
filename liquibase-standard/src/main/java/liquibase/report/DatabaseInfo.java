package liquibase.report;

import lombok.Data;

@Data
public class DatabaseInfo {
    private String databaseType;
    private String version;
}
