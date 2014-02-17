package liquibase.sdk.supplier.database;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public abstract class ConnectionSupplier implements Cloneable {

    public static final String CONFIG_NAME_STANDARD = "standard";

    public String VAGRANT_BOX_NAME_WINDOWS_STANDARD = "liquibase.windows.2008r2.x64";
    public String VAGRANT_BOX_NAME_LINUX_STANDARD = "liquibase.linux.centos.x64";

    public String version;
    private String ipAddress = "10.10.100.100";

    public abstract String getDatabaseShortName();
    public abstract String getConfigurationName();

    public abstract String getJdbcUrl();

    public String getPrimaryCatalog() {
        return "liquibase";
    }

    public String getPrimarySchema() {
        return "liquibase";
    }

    public String getDatabaseUsername() {
        return "liquibase";
    }

    public String getDatabasePassword() {
        return "liquibase";
    }

    public String getAlternateUsername() {
        return "liquibaseb";
    }

    public String getAlternateUserPassword() {
        return "liquibase";
    }

    public String getAlternateCatalog() {
        return "liquibaseb";
    }

    public String getAlternateSchema() {
        return "liquibaseb";
    }

    public String getAlternateTablespace() {
        return "liquibase2";
    }

    public abstract String getAdminUsername();

    public String getAdminPassword() {
        return "liquibase";
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
        return VAGRANT_BOX_NAME_LINUX_STANDARD;
    }

    public Set<String> getRequiredPackages(String vagrantBoxName) {
        return new HashSet<String>();
    }

    public String getPuppetInit(String box) throws IOException {
        return null;
    }

    public String getVersion() {
        return version;
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

    public void writeConfigFiles(File configDir) throws IOException {

    }
}
