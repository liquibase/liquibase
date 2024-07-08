package liquibase.report;

import lombok.Data;

import java.util.Map;

@Data
public class CustomData {
    private String customDataFile;
    private Map<String, Object> fileContents;
}
