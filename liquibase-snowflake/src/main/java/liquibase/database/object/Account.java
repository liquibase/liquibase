package liquibase.database.object;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtil;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Snowflake account - the top-level container for account-level objects.
 * Account objects exist as peers to Catalog objects in the DatabaseSnapshot hierarchy.
 * 
 * Account-level objects include:
 * - Warehouses (virtual compute clusters)
 * - Resource Monitors (usage/cost controls) 
 * - Network Policies (IP/network access controls)
 * - Integrations (external service connections)
 */
public class Account extends AbstractDatabaseObject {
    
    // Account identifier (typically account name or URL)
    private String name;
    
    // Account metadata
    private String region;
    private String cloud;
    private String accountUrl;
    
    // Container for account-level objects
    private List<DatabaseObject> accountObjects;
    
    public Account() {
        this.accountObjects = new ArrayList<>();
    }

    @Override
    public String getSnapshotId() {
        return getName();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Account setName(String name) {
        if (StringUtil.isEmpty(name) || (name != null && name.trim().isEmpty())) {
            throw new IllegalArgumentException("Account name cannot be null or empty");
        }
        this.name = name.toUpperCase(); // Snowflake stores identifiers in uppercase
        return this;
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        // Accounts exist at the top level, peer to Catalogs
        return new DatabaseObject[0];
    }

    @Override
    public Schema getSchema() {
        // Accounts don't belong to schemas in Snowflake
        return null;
    }

    // Account Properties
    
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCloud() {
        return cloud;
    }

    public void setCloud(String cloud) {
        this.cloud = cloud;
    }

    public String getAccountUrl() {
        return accountUrl;
    }

    public void setAccountUrl(String accountUrl) {
        this.accountUrl = accountUrl;
    }

    // Container methods

    public void addDatabaseObject(DatabaseObject object) {
        if (object != null) {
            this.accountObjects.add(object);
        }
    }

    public List<DatabaseObject> getDatabaseObjects() {
        return new ArrayList<>(accountObjects);
    }

    public <T extends DatabaseObject> List<T> getDatabaseObjects(Class<T> type) {
        List<T> result = new ArrayList<>();
        for (DatabaseObject obj : accountObjects) {
            if (type.isAssignableFrom(obj.getClass())) {
                result.add(type.cast(obj));
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Account account = (Account) o;
        return Objects.equals(name, account.name) &&
               Objects.equals(region, account.region) &&
               Objects.equals(cloud, account.cloud) &&
               Objects.equals(accountUrl, account.accountUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, region, cloud, accountUrl);
    }

    @Override
    public String toString() {
        return "Account{" +
                "name='" + name + '\'' +
                ", region='" + region + '\'' +
                ", cloud='" + cloud + '\'' +
                ", accountUrl='" + accountUrl + '\'' +
                '}';
    }
}