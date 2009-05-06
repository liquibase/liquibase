package liquibase.database.structure;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;

import java.sql.Types;

public class HibernateGenericDialect extends Dialect {
    private Dialect realDialect;

    public HibernateGenericDialect(Configuration cfg) throws Exception {
        String dialectClass = cfg.getProperty("hibernate.dialect");
        if (dialectClass == null) {
            dialectClass = cfg.getProperty("dialect");
        }

        realDialect = (Dialect) Class.forName(dialectClass).newInstance();
    }

    public String getTypeName(int code, int length, int precision, int scale) throws HibernateException {
        if (code == Types.BIGINT) {
            return "bigint";
        } else if (code == Types.BOOLEAN) {
            return "boolean";
        } else if (code == Types.BLOB) {
            return "blob";
        } else if (code == Types.CLOB) {
            return "clob";
        } else if (code == Types.DATE) {
            return "date";
        } else if (code == Types.FLOAT) {
            return "float";
        } else if (code == Types.TIME) {
            return "time";
        } else if (code == Types.TIMESTAMP) {
            return "datetime";
        } else if (code == Types.VARCHAR) {
            return "varchar";
        } else {
            return realDialect.getTypeName(code, length, precision, scale);
        }
    }
}
