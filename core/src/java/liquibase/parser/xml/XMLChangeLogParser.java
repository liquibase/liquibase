package liquibase.parser.xml;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogSerializer;
import liquibase.resource.FileOpener;
import liquibase.util.log.LogFactory;
import org.xml.sax.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class XMLChangeLogParser implements ChangeLogParser {

    public static String getSchemaVersion() {
        return "1.9";
    }

    public String[] getValidFileExtensions() {
        return new String[] {
            "xml"
        };
    }

    public DatabaseChangeLog parse(String physicalChangeLogLocation, Map<String, Object> changeLogParameters, FileOpener fileOpener) throws ChangeLogParseException {

        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        if (System.getProperty("java.vm.version").startsWith("1.4")) {
            saxParserFactory.setValidating(false);
            saxParserFactory.setNamespaceAware(false);
        } else {
            saxParserFactory.setValidating(true);
            saxParserFactory.setNamespaceAware(true);
        }

        InputStream inputStream = null;
        try {
            SAXParser parser = saxParserFactory.newSAXParser();
            try {
                parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
            } catch (SAXNotRecognizedException e) {
                //ok, parser must not support it
            } catch (SAXNotSupportedException e) {
                //ok, parser must not support it
            }

            XMLReader xmlReader = parser.getXMLReader();
            xmlReader.setEntityResolver(new LiquibaseSchemaResolver());
            xmlReader.setErrorHandler(new ErrorHandler() {
                public void warning(SAXParseException exception) throws SAXException {
                    LogFactory.getLogger().warning(exception.getMessage());
                    throw exception;
                }

                public void error(SAXParseException exception) throws SAXException {
                    LogFactory.getLogger().severe(exception.getMessage());
                    throw exception;
                }

                public void fatalError(SAXParseException exception) throws SAXException {
                    LogFactory.getLogger().severe(exception.getMessage());
                    throw exception;
                }
            });
        	
            inputStream = fileOpener.getResourceAsStream(physicalChangeLogLocation);
            if (inputStream == null) {
                throw new ChangeLogParseException(physicalChangeLogLocation + " does not exist");
            }

            XMLChangeLogHandler contentHandler = new XMLChangeLogHandler(physicalChangeLogLocation, fileOpener, changeLogParameters);
            xmlReader.setContentHandler(contentHandler);
            xmlReader.parse(new InputSource(inputStream));

            return contentHandler.getDatabaseChangeLog();
        } catch (ChangeLogParseException e) {
            throw e;
        } catch (IOException e) {
            throw new ChangeLogParseException("Error Reading Migration File: " + e.getMessage(), e);
        } catch (SAXParseException e) {
            throw new ChangeLogParseException("Error parsing line " + e.getLineNumber() + " column " + e.getColumnNumber() + " of " + physicalChangeLogLocation +": " + e.getMessage(), e);
        } catch (SAXException e) {
            Throwable parentCause = e.getException();
            while (parentCause != null) {
                if (parentCause instanceof ChangeLogParseException) {
                    throw ((ChangeLogParseException) parentCause);
                }
                parentCause = parentCause.getCause();
            }
            String reason = e.getMessage();
            String causeReason = null;
            if (e.getCause() != null) {
                causeReason = e.getCause().getMessage();
            }

//            if (reason == null && causeReason==null) {
//                reason = "Unknown Reason";
//            }
            if (reason == null) {
                if (causeReason != null) {
                    reason = causeReason;
                } else {
                    reason = "Unknown Reason";
                }
            }

            throw new ChangeLogParseException("Invalid Migration File: " + reason, e);
        } catch (Exception e) {
            throw new ChangeLogParseException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // probably ok
                }
            }
        }
    }
}
