package liquibase.database.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import liquibase.database.Database;
import liquibase.database.structure.Column.LengthSemantics;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;

public class InformixDatabaseSnapshot extends SqlDatabaseSnapshot {
    
	public InformixDatabaseSnapshot() {
		super();
    }

	public InformixDatabaseSnapshot(Database database) throws JDBCException {
		super(database);
    }
	
    public InformixDatabaseSnapshot(Database database, String schema) throws JDBCException {
        super(database, schema);
    }

    public InformixDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners) throws JDBCException {
        super(database, statusListeners);
    }

    public InformixDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners, String requestedSchema) throws JDBCException {
        super(database, statusListeners, requestedSchema);
    }
    
    private static final Map<Integer, String> qualifiers = new HashMap<Integer, String>();
    
    static {
    	qualifiers.put(0, "YEAR");
    	qualifiers.put(2, "MONTH");
    	qualifiers.put(4, "DAY");
    	qualifiers.put(6, "HOUR");
    	qualifiers.put(8, "MINUTE");
    	qualifiers.put(10, "SECOND");
    	qualifiers.put(11, "FRACTION(1)");
    	qualifiers.put(12, "FRACTION(2)");
    	qualifiers.put(13, "FRACTION(3)");
    	qualifiers.put(14, "FRACTION(4)");
    	qualifiers.put(15, "FRACTION(5)");
    }
    
    @Override
    protected void getColumnTypeAndDefValue(Column columnInfo, ResultSet rs, Database database) throws SQLException, JDBCException {
    	// See http://publib.boulder.ibm.com/infocenter/idshelp/v115/topic/com.ibm.sqlr.doc/sqlr07.htm
    	String typeName = rs.getString("TYPE_NAME").toUpperCase();
    	if ("DATETIME".equals(typeName) || "INTERVAL".equals(typeName)) {
    		int collength = columnInfo.getColumnSize();
    		//int positions = collength / 256;
    		int firstQualifierType = (collength % 256) / 16;
    		int lastQualifierType = (collength % 256) % 16;
    		String type = "DATETIME".equals(typeName) ? "DATETIME" : "INTERVAL";
    		String firstQualifier = qualifiers.get(firstQualifierType);
    		String lastQualifier = qualifiers.get(lastQualifierType);
    		columnInfo.setTypeName(type + " " + firstQualifier + " TO " + lastQualifier);
    		columnInfo.setLengthSemantics(Column.LengthSemantics.BYTE);
    	} else {
        	super.getColumnTypeAndDefValue(columnInfo, rs, database);
    	}
    }
    
}
