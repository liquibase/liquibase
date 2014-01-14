package liquibase.sdk.supplier.database;

import java.util.HashSet;
import java.util.Set;

public abstract class ConnectionConfiguration implements Cloneable {

    public static final String NAME_STANDARD = "standard";

    public String version;
    private String hostname = "10.10.100.100";

    public abstract String getDatabaseShortName();
    public abstract String getConfigurationName();

    public abstract String getUrl();

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

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Set<String> getPuppetModules() {
        return new HashSet<String>();
    }

    public Set<String> getPuppetForges(String boxName) {
        HashSet<String> forges = new HashSet<String>();
        forges.add("http://forge.puppetlabs.com");

        return forges;
    }

    public String getVagrantBoxName() {
        return "linux";
    }

    public Set<String> getRequiredPackages(String vagrantBoxName) {
        return new HashSet<String>();
    }

    public String getPuppetInit(String box) {
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
        return getDatabaseShortName()+"["+getConfigurationName()+"]";
    }

    public String getDescription() {
        String version = getVersion();
        if (version == null) {
            version = "LATEST";
        }

        return "JDBC Url: "+getUrl()+"\n"+
                "Version: "+ version +"\n"+
                "Standard User: "+ getDatabaseUsername()+"\n"+
                "         Password: "+ getDatabasePassword()+"\n"+
                "Alternate User: "+ getAlternateUsername()+"\n"+
                "          Password: "+ getAlternateUserPassword()+"\n"+
                "Alternate Schema: "+ getAlternateSchema()+"\n"+
                "Alternate Tablespace: "+ getAlternateTablespace()+"\n";
    }
}
