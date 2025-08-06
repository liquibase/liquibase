package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.object.Account;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Snowflake-specific account snapshot generator.
 * Creates Account container objects that hold account-level objects like warehouses.
 * Account objects are added directly to DatabaseSnapshot as peers to Catalog objects.
 */
public class AccountSnapshotGeneratorSnowflake extends JdbcSnapshotGenerator {

    public AccountSnapshotGeneratorSnowflake() {
        super(Account.class, new Class[]{});
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        System.out.println("🔍 AccountSnapshotGenerator.getPriority() called for " + objectType.getSimpleName() + 
                         " on " + database.getClass().getSimpleName());
        
        if (Account.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            System.out.println("✅ AccountSnapshotGenerator returning PRIORITY_DATABASE for Account on Snowflake");
            return PRIORITY_DATABASE;
        }
        System.out.println("❌ AccountSnapshotGenerator returning PRIORITY_NONE for " + objectType.getSimpleName());
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] addsTo() {
        // Account objects are root-level objects, peers to Catalog (not children)
        return new Class[0];
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) 
            throws DatabaseException, InvalidExampleException {
        
        System.out.println("🔍 AccountSnapshotGenerator.snapshotObject() called with example: " + 
                         (example != null ? example.getClass().getSimpleName() + " name=" + 
                          (example instanceof Account ? ((Account)example).getName() : "N/A") : "null"));
        
        if (example == null) {
            System.out.println("❌ AccountSnapshotGenerator: example is null");
            return null;
        }
        
        if (!(example instanceof Account)) {
            System.out.println("❌ AccountSnapshotGenerator: example is not Account (" + example.getClass().getSimpleName() + ")");
            return null;
        }
        
        Account exampleAccount = (Account) example;
        String accountName = exampleAccount.getName();
        
        System.out.println("🔍 AccountSnapshotGenerator: Account example name = '" + accountName + "'");
        
        if (snapshot == null) {
            System.out.println("❌ AccountSnapshotGenerator: snapshot is null");
            return null;
        }
        
        Database database = snapshot.getDatabase();
        if (!(database instanceof SnowflakeDatabase)) {
            System.out.println("❌ AccountSnapshotGenerator: database is not Snowflake (" + database.getClass().getSimpleName() + ")");
            return null;
        }
        
        // CRITICAL FIX: If no account name provided, create account from connection
        if (accountName == null) {
            System.out.println("🔧 AccountSnapshotGenerator: No account name provided, creating from connection");
            try {
                Account account = createAccountContainer(database);
                System.out.println("✅ AccountSnapshotGenerator: Created account from connection: " + (account != null ? account.getName() : "null"));
                return account;
            } catch (SQLException e) {
                System.out.println("❌ AccountSnapshotGenerator: Failed to create account from connection: " + e.getMessage());
                throw new DatabaseException("Error creating account from connection: " + e.getMessage(), e);
            }
        }
        
        try {
            Account account = snapshotSingleAccount(accountName, database);
            System.out.println("✅ AccountSnapshotGenerator: Successfully snapshotted account: " + (account != null ? account.getName() : "null"));
            return account;
        } catch (SQLException e) {
            System.out.println("❌ AccountSnapshotGenerator: Error querying account " + accountName + ": " + e.getMessage());
            throw new DatabaseException("Error querying account information for " + accountName + ": " + e.getMessage(), e);
        }
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) 
            throws DatabaseException, InvalidExampleException {
        
        System.out.println("🔍 AccountSnapshotGenerator.addTo() called with foundObject: " + 
                         (foundObject != null ? foundObject.getClass().getSimpleName() : "null"));
        
        // Account objects are root-level objects, peers to Catalog
        // They don't get added TO other objects - the framework handles discovery through snapshotObject()
        // Leave empty since root-level objects are handled by the new framework discovery mechanism
    }

    /**
     * Snapshots a single account with available metadata.
     */
    private Account snapshotSingleAccount(String accountName, Database database) 
            throws SQLException, DatabaseException {
        
        Account result = new Account();
        result.setName(accountName);
        
        // Try to enrich with account metadata if available
        enrichWithAccountMetadata(result, database);
        
        return result;
    }

    /**
     * Creates the account container for this Snowflake connection.
     */
    private Account createAccountContainer(Database database) throws SQLException, DatabaseException {
        // Extract account information from the connection URL or context
        String accountName = extractAccountName(database);
        if (accountName == null) {
            return null;
        }
        
        Account account = new Account();
        account.setName(accountName);
        
        // Enrich with account metadata
        enrichWithAccountMetadata(account, database);
        
        return account;
    }

    /**
     * Extracts account name from the database connection.
     */
    private String extractAccountName(Database database) {
        try {
            // For Snowflake, account info is typically in the connection URL
            String url = database.getConnection().getURL();
            if (url != null && url.contains("snowflakecomputing.com")) {
                // Extract account from URL like: jdbc:snowflake://account.snowflakecomputing.com/
                String[] parts = url.split("//");
                if (parts.length > 1) {
                    String hostPart = parts[1].split("/")[0];
                    String[] hostParts = hostPart.split("\\.");
                    if (hostParts.length > 0) {
                        return hostParts[0].toUpperCase();
                    }
                }
            }
        } catch (Exception e) {
            // Fallback to generic account name
        }
        
        // Fallback - use a generic account identifier
        return "SNOWFLAKE_ACCOUNT";
    }

    /**
     * Enriches account object with additional metadata if available.
     */
    private void enrichWithAccountMetadata(Account account, Database database) {
        try {
            // Query for account-level metadata if available
            // Note: SHOW ORGANIZATION ACCOUNTS or similar queries might need special permissions
            
            // Try to get current account info
            String sql = "SELECT CURRENT_ACCOUNT() as ACCOUNT_NAME, CURRENT_REGION() as REGION";
            PreparedStatement stmt = ((JdbcConnection) database.getConnection()).prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String accountName = rs.getString("ACCOUNT_NAME");
                String region = rs.getString("REGION");
                
                if (accountName != null) {
                    account.setName(accountName.toUpperCase());
                }
                if (region != null) {
                    account.setRegion(region);
                }
            }
            
            rs.close();
            stmt.close();
            
            // Try to determine cloud provider from region (if available)
            if (account.getRegion() != null) {
                String region = account.getRegion().toLowerCase();
                if (region.startsWith("aws") || region.contains("us-east") || region.contains("us-west")) {
                    account.setCloud("AWS");
                } else if (region.startsWith("azure") || region.contains("east-us") || region.contains("west-us")) {
                    account.setCloud("AZURE");
                } else if (region.startsWith("gcp") || region.contains("us-central")) {
                    account.setCloud("GCP");
                }
            }
            
        } catch (SQLException | DatabaseException e) {
            // Account metadata is supplementary - continue without it if it fails
            // This is expected in some Snowflake environments or with limited permissions
        }
    }

    @Override
    public Class<? extends liquibase.snapshot.SnapshotGenerator>[] replaces() {
        return new Class[0];
    }
}