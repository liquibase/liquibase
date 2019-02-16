package liquibase.util.csv;

import liquibase.util.ISODateFormat;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CSVWriter extends liquibase.util.csv.opencsv.CSVWriter {
    private static final ISODateFormat ISO_DATE_FORMAT = new ISODateFormat();

    public CSVWriter(Writer writer) {
        super(writer);
    }

    public CSVWriter(Writer writer, char c) {
        super(writer, c);
    }

    public CSVWriter(Writer writer, char c, char c1) {
        super(writer, c, c1);
    }

    public CSVWriter(Writer writer, char c, char c1, char c2) {
        super(writer, c, c1, c2);
    }

    public CSVWriter(Writer writer, char c, char c1, String s) {
        super(writer, c, c1, s);
    }

    public CSVWriter(Writer writer, char c, char c1, char c2, String s) {
        super(writer, c, c1, c2, s);
    }

    
    private String getColumnValue(ResultSet rs, int colType, int colIndex) throws SQLException, IOException {

        Object value = rs.getObject(colIndex);
        if (rs.wasNull()) {
            return "NULL";
        }

        if (value instanceof java.sql.Date) {
            return ISO_DATE_FORMAT.format((java.sql.Date) value);
        } else if (value instanceof java.sql.Time) {
            return ISO_DATE_FORMAT.format((java.sql.Time) value);
        } else if (value instanceof java.sql.Timestamp) {
            return ISO_DATE_FORMAT.format((java.sql.Timestamp) value);
//        } else if (value instanceof oracle.sql.TIMESTAMP) {
//            return ISO_DATE_FORMAT.format((oracle.sql.TIMESTAMP) value);
        } else {
            return value.toString();
        }

//        if (colType == Types.BIT) {
//            Object bit = rs.getObject(colIndex);
//            if (rs.wasNull()) {
//                return null;
//            } else {
//                return String.valueOf(bit);
//            }
//        } else if (colType == Types.BOOLEAN) {
//            boolean b = rs.getBoolean(colIndex);
//            if (rs.wasNull()) {
//                return null;
//            } else {
//                return Boolean.valueOf(b).toString();
//            }
//        } else if (colType == Types.CLOB) {
//            Clob c = rs.getClob(colIndex);
//            if (rs.wasNull()) {
//                return null;
//            } else {
//                return read(c);
//            }
//        } else if (colType == Types.BIGINT
//                || colType == Types.DECIMAL
//                || colType == Types.DOUBLE
//                || colType == Types.FLOAT
//                || colType == Types.REAL
//                || colType == Types.NUMERIC) {
//            BigDecimal bd = rs.getBigDecimal(colIndex);
//            if (rs.wasNull()) {
//                return null;
//            } else {
//                return String.valueOf(bd.doubleValue());
//            }
//
//        } else if (colType == Types.INTEGER
//                || colType == Types.TINYINT
//                || colType == Types.SMALLINT) {
//            int intValue = rs.getInt(colIndex);
//            if (rs.wasNull()) {
//                return null;
//            } else {
//                return String.valueOf(intValue);
//            }
//        }
//
//        case Types.JAVA_OBJECT:
//            Object obj = rs.getObject(colIndex);
//            if (obj != null) {
//                value = String.valueOf(obj);
//            }
//            break;
//        case Types.DATE:
//            java.sql.Date date = rs.getDate(colIndex);
//            if (date != null) {
//                value = ISO_DATE_FORMAT.format(date);
//                ;
//            }
//            break;
//        case Types.TIME:
//            Time t = rs.getTime(colIndex);
//            if (t != null) {
//                value = t.toString();
//            }
//            break;
//        case Types.TIMESTAMP:
//            Timestamp tstamp = rs.getTimestamp(colIndex);
//            if (tstamp != null) {
//                value = ISO_DATE_FORMAT.format(tstamp);
//            }
//            break;
//        case Types.LONGVARCHAR:
//        case Types.VARCHAR:
//        case Types.CHAR:
//            value = rs.getString(colIndex);
//            break;
//        default:
//            value = "";
//    }

//        if (value == null)
//
//        {
//            value = "";
//        }
//
//        return value;

    }

//    private static String read(Clob c) throws SQLException, IOException {
//        StringBuffer sb = new StringBuffer((int) c.length());
//        Reader r = c.getCharacterStream();
//        char[] cbuf = new char[2048];
//        int n = 0;
//        while ((n = r.read(cbuf, 0, cbuf.length)) != -1) {
//            if (n > 0) {
//                sb.append(cbuf, 0, n);
//            }
//        }
//        return sb.toString();
//    }

}
