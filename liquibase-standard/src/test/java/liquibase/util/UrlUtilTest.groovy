package liquibase.util

import liquibase.report.DatabaseInfo
import spock.lang.Specification

class UrlUtilTest extends Specification {
    def "should bring important url parameters to front of connection string and remove other params"(String url, String expected) {
        when:
        def modified = UrlUtil.handleSqlServerDbUrlParameters(DatabaseInfo.DB_URL_VISIBLE_KEYS, url)
        then:
        modified == expected
        where:
        url | expected
        "jdbc:sqlserver://localhost:1433;connectRetryInterval=10;connectRetryCount=1;maxResultBuffer=-1;sendTemporalDataTypesAsStringForBulkCopy=true;delayLoadingLobs=true;useFmtOnly=false;useBulkCopyForBatchInsert=false;cancelQueryTimeout=-1;sslProtocol=TLS;jaasConfigurationName=SQLJDBCDriver;statementPoolingCacheSize=0;serverPreparedStatementDiscardThreshold=10;enablePrepareOnFirstPreparedStatementCall=false;fips=false;socketTimeout=0;authentication=NotSpecified;authenticationScheme=nativeAuthentication;xopenStates=false;datetimeParameterType=datetime2;sendTimeAsDatetime=true;replication=false;trustStoreType=JKS;trustServerCertificate=true;TransparentNetworkIPResolution=true;iPAddressPreference=IPv4First;serverNameAsACE=false;sendStringParametersAsUnicode=true;selectMethod=direct;responseBuffering=adaptive;queryTimeout=-1;packetSize=8000;multiSubnetFailover=false;loginTimeout=30;lockTimeout=-1;lastUpdateCount=true;prepareMethod=prepexec;encrypt=True;disableStatementPooling=true;databaseName=cyclopsbi_dev;columnEncryptionSetting=Disabled;applicationName=Microsoft JDBC Driver for SQL Server;applicationIntent=readwrite;" | "jdbc:sqlserver://localhost:1433;databaseName=cyclopsbi_dev"
        "jdbc:sqlserver://localhost:1433;someParam=nothing;databaseName=blah" | "jdbc:sqlserver://localhost:1433;databaseName=blah"
        "jdbc:postgresql://other@localhost/otherdb?connect_timeout=10&application_name=myapp" | "jdbc:postgresql://other@localhost/otherdb?connect_timeout=10&application_name=myapp"
    }
}
