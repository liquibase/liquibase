package liquibase.report;

import liquibase.util.LiquibaseUtil;
import liquibase.util.NetUtil;
import lombok.Data;

@Data
public class RuntimeInfo {
    private final String systemUsername = System.getProperty("user.name");
    private final String hostname = NetUtil.getLocalHostName();
    private final String os = System.getProperty("os.name");
    private String interfaceType;
    private String startTime;
    private String updateDuration;
    private final String liquibaseVersion = LiquibaseUtil.getBuildVersionInfo();
    private String javaVersion = System.getProperty("java.version");
}
