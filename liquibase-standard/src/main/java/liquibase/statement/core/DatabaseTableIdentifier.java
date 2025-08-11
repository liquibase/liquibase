package liquibase.statement.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DatabaseTableIdentifier{
    private String catalogName;
    private String schemaName;
    private String tableName;

}

