package liquibase.integration.commandline;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

@Getter
@Setter
public class Banner {
    private String version;
    private String build;
    private String built;
    private String path;
    private String licensee;
    private String licenseEndDate;

    public Banner() {
        version = LiquibaseUtil.getBuildVersionInfo();
        built = LiquibaseUtil.getBuildTime();
        build = LiquibaseUtil.getBuildNumber();
    }

    @Override
    public String toString() {
        StringBuilder banner = new StringBuilder();
        if (GlobalConfiguration.SHOW_BANNER.getCurrentValue()) {

            // Banner is stored in liquibase/banner.txt in resources.
            Class<CommandLineUtils> commandLinUtilsClass = CommandLineUtils.class;
            InputStream inputStream = commandLinUtilsClass.getResourceAsStream("/liquibase/banner.txt");
            try {
                banner.append(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
            } catch (IOException e) {
                Scope.getCurrentScope().getLog(commandLinUtilsClass).fine("Unable to locate banner file.");
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");
        banner.append(String.format(
                coreBundle.getString("starting.liquibase.at.timestamp"), dateFormat.format(calendar.getTime())
        ));

        if (StringUtil.isNotEmpty(version) && StringUtil.isNotEmpty(built)) {
            version = version + " #" + build;
            banner.append(String.format(coreBundle.getString("liquibase.version.builddate"), version, built));
        }

        return banner.toString();
    }
}
