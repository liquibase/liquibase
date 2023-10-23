package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.DockerDatabaseWrapper;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.utility.DockerImageName;

public class MSSQLTestSystem extends DatabaseTestSystem {
    private String collation;

    public MSSQLTestSystem() {
        super("mssql");
    }

    public MSSQLTestSystem(Definition definition) {
        super(definition);
    }

    @SuppressWarnings("java:S2095")
    @Override
    protected @NotNull DatabaseWrapper createContainerWrapper() {
        return new DockerDatabaseWrapper(new MSSQLServerContainer(
                DockerImageName.parse(getImageName()).withTag(getVersion())
        )
                .withUrlParam("encrypt", "false"),
                this
        ) {
            @Override
            protected Runnable requireLicense() {
                return ((MSSQLServerContainer) getContainer())::acceptLicense;
            }
        };
    }

    @Override
    public String getConnectionUrl() {
        final JdbcDatabaseContainer container = ((DockerDatabaseWrapper) wrapper).getContainer();

        return "jdbc:sqlserver://" + container.getHost() + ":" + container.getMappedPort(MSSQLServerContainer.MS_SQL_SERVER_PORT)+";databaseName="+getCatalog()+";encrypt=false";
    }

    @Override
    protected String[] getSetupSql() {
        return new String[]{
                "CREATE LOGIN [" + getUsername() + "] with password=N'" + getPassword() + "', CHECK_EXPIRATION=OFF",

                "CREATE DATABASE " + getCatalog() + (collation == null ? "" : " COLLATE " + collation ),
                "EXEC lbcat..sp_addsrvrolemember @loginame = N'" + getUsername() + "', @rolename = N'sysadmin'",

                "CREATE DATABASE " + getAltCatalog(),
                "EXEC lbcat..sp_addsrvrolemember @loginame = N'" + getUsername() + "', @rolename = N'sysadmin'",

                "USE [" + getCatalog() + "]",
//                "ALTER DATABASE ["+getCatalog()+"] MODIFY FILEGROUP [PRIMARY] DEFAULT",
                "ALTER DATABASE [" + getCatalog() + "] ADD FILEGROUP [" + getAltTablespace() + "]",

                "ALTER DATABASE [" + getCatalog() + "] ADD FILE ( NAME = N'" + getAltTablespace() + "', FILENAME = N'/tmp/" + getAltTablespace() + ".ndf' , SIZE = 8192KB , FILEGROWTH = 65536KB ) TO FILEGROUP [" + getAltTablespace() + "]",
                "CREATE SCHEMA [" + getAltSchema() + "] AUTHORIZATION [dbo]",
                "USE [" + getAltCatalog() + "]",
                "ALTER DATABASE [" + getAltCatalog() + "] ADD FILEGROUP [" + getAltTablespace() + "]",
                "ALTER DATABASE [" + getAltCatalog() + "] ADD FILE ( NAME = N'" + getAltTablespace() + "', FILENAME = N'/tmp/" + getAltTablespace() + ".ndf' , SIZE = 8192KB , FILEGROWTH = 65536KB ) TO FILEGROUP [" + getAltTablespace() + "]",
                "CREATE SCHEMA [" + getAltSchema() + "] AUTHORIZATION [dbo]"
        };
    }

    public String getCollation() {
        return collation;
    }

    public void setCollation(String collation) {
        this.collation = collation;
    }
}
