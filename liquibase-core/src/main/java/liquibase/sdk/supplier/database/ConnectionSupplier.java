package liquibase.sdk.supplier.database;

import liquibase.database.DatabaseFactory;
import liquibase.sdk.TemplateService;
import org.apache.velocity.Template;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ConnectionSupplier implements Cloneable {

    public static final String CONFIG_NAME_STANDARD = "standard";
    public static final String OS_LINUX = "linux";
    public static final String OS_WINDOWS = "windows";

    public String VAGRANT_BOX_NAME_WINDOWS_STANDARD = "liquibase.windows.2008r2.x64";
    public String VAGRANT_BOX_NAME_LINUX_STANDARD = "liquibase.linux.centos.x64";

    private String version;
    private String ipAddress = "10.10.100.100";
    private String os = OS_LINUX;

    public abstract String getDatabaseShortName();

    public String getConfigurationName() {
        return CONFIG_NAME_STANDARD;
    }

    public abstract String getJdbcUrl();

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getPrimaryCatalog() {
        return "lbcat";
    }

    public String getPrimarySchema() {
        return "lbschema";
    }

    public String getDatabaseUsername() {
        return "lbuser";
    }

    public String getDatabasePassword() {
        return "lbuser";
    }

    public String getAlternateUsername() {
        return "lbuser2";
    }

    public String getAlternateUserPassword() {
        return "lbuser2";
    }

    public String getAlternateCatalog() {
        return "lbcat2";
    }

    public String getAlternateSchema() {
        return "lbschema2";
    }

    public String getAlternateTablespace() {
        return "lbtbsp2";
    }

    public abstract String getAdminUsername();

    public String getAdminPassword() {
        return "lbadmin";
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Set<String> getPuppetModules() {
        return new HashSet<String>();
    }

    public Set<String> getPuppetForges(String boxName) {
        HashSet<String> forges = new HashSet<String>();
        forges.add("http://forge.puppetlabs.com");

        return forges;
    }

    public String getVagrantBaseBoxName() {
        if (getOs().equals(OS_WINDOWS)) {
            return VAGRANT_BOX_NAME_WINDOWS_STANDARD;
        }
        return VAGRANT_BOX_NAME_LINUX_STANDARD;
    }

    public Set<String> getRequiredPackages(String vagrantBoxName) {
        return new HashSet<String>();
    }

    public abstract ConfigTemplate getPuppetTemplate(Map<String, Object> context);

    public String getVersion() {
        return version;
    }

    public String getShortVersion() {
        if (getVersion() == null) {
            return "LATEST";
        }
        String[] split = getVersion().split("\\.");
        if (split.length == 1) {
            return split[0];
        } else {
            return split[0]+"."+split[1];
        }
    }



    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return getDatabaseShortName()+"[config:"+getConfigurationName()+"]";
    }

    public String getDescription() {
        String version = getVersion();
        if (version == null) {
            version = "LATEST";
        }

        return "JDBC Url: "+ getJdbcUrl()+"\n"+
                "Version: "+ version +"\n"+
                "Standard User: "+ getDatabaseUsername()+"\n"+
                "         Password: "+ getDatabasePassword()+"\n"+
                "Primary Catalog: "+ getPrimaryCatalog()+"\n"+
                "Primary Schema: "+ getPrimarySchema()+" (if applicable)\n"+
                "\n"+
                "Alternate User: "+ getAlternateUsername()+"\n"+
                "          Password: "+ getAlternateUserPassword()+"\n"+
                "Alternate Catalog: "+ getAlternateCatalog()+"\n"+
                "Alternate Schema: "+ getAlternateSchema()+" (if applicable)\n"+
                "Alternate Tablespace: "+ getAlternateTablespace()+"\n";
    }

    public Set<ConfigTemplate> generateConfigFiles(Map<String, Object> context) throws IOException {
        Set<ConfigTemplate> set = new HashSet<ConfigTemplate>();
        return set;
    }

    protected boolean isWindows() {
        return getOs().equalsIgnoreCase(OS_WINDOWS);
    }

    protected boolean isLinux() {
        return getOs().equalsIgnoreCase(OS_LINUX);
    }

    public String getFileSeparator() {
        if (isWindows()) {
            return "\\";
        } else {
            return "/";
        }
    }

    public Connection openConnection() throws SQLException {
        return DriverManager.getConnection(this.getJdbcUrl(), this.getDatabaseUsername(), this.getDatabasePassword());
    }

        public static class ConfigTemplate {

        private final String templatePath;
        private final Map<String, Object> context;
        private final String outputFileName;

        public ConfigTemplate(String templatePath, Map<String, Object> context) {
            this.templatePath = templatePath;
            this.context = context;
            this.outputFileName = templatePath.replaceFirst(".*/", "").replaceFirst("\\.vm$", "");
        }

        public String getTemplatePath() {
            return templatePath;
        }

        public Map<String, Object> getContext() {
            return context;
        }

        public String getOutputFileName() {
            return outputFileName;
        }

        public void write(File outputFile) throws IOException {
            TemplateService.getInstance().write(templatePath, outputFile, context);
        }

        public String output() throws IOException {
            return TemplateService.getInstance().output(templatePath, context);
        }
    }
}
