package liquibase.integration.commandline;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

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
                banner.append(readFromInputStream(inputStream));
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

    private static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                resultStringBuilder.append(line + "\n");

            }
        }
        return resultStringBuilder.toString();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getBuilt() {
        return built;
    }

    public void setBuilt(String built) {
        this.built = built;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLicensee() {
        return licensee;
    }

    public void setLicensee(String licensee) {
        this.licensee = licensee;
    }

    public String getLicenseEndDate() {
        return licenseEndDate;
    }

    public void setLicenseEndDate(String licenseEndDate) {
        this.licenseEndDate = licenseEndDate;
    }
}
