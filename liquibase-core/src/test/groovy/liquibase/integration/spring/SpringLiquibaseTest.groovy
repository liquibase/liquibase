package liquibase.integration.spring

import org.springframework.beans.MutablePropertyValues
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.context.support.StaticApplicationContext
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.Connection
import java.sql.SQLException

class SpringLiquibaseTest extends Specification {

    def SpringLiquibaseLoads() {
        expect:
        def parentContext = new StaticApplicationContext();
        def connection = Mock(Connection)
        parentContext.registerSingleton("dataSource", MockDataSource, new MutablePropertyValues([connection: connection]))
        def context = new ClassPathXmlApplicationContext(["com/example/spring-test.xml"] as String[], parentContext);
    }

    private static class MockDataSource implements DataSource {

        private Connection connection;
        private PrintWriter logWriter;

        public void setConnection(Connection conn) {
            this.connection = conn;
        }

        @Override
        Connection getConnection() throws SQLException {
            return connection;
        }

        @Override
        Connection getConnection(String username, String password) throws SQLException {
            return connection;
        }

        @Override
        PrintWriter getLogWriter() throws SQLException {
            return logWriter;
        }

        @Override
        void setLogWriter(PrintWriter out) throws SQLException {
            this.logWriter = logWriter;
        }

        @Override
        void setLoginTimeout(int seconds) throws SQLException {

        }

        @Override
        int getLoginTimeout() throws SQLException {
            return 0
        }

        @Override
        def <T> T unwrap(Class<T> iface) throws SQLException {
            return null
        }

        @Override
        boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false
        }
    }
}
