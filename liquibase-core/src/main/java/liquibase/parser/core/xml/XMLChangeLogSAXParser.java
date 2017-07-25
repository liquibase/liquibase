package liquibase.parser.core.xml;

import liquibase.changelog.ChangeLogParameters;
import liquibase.exception.ChangeLogParseException;
import liquibase.logging.LogFactory;
import liquibase.logging.LogTarget;
import liquibase.parser.core.ParsedNode;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.UtfBomStripperInputStream;
import liquibase.util.StreamUtil;
import liquibase.util.file.FilenameUtils;
import org.xml.sax.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

public class XMLChangeLogSAXParser extends AbstractChangeLogParser {
    
    public static final String LIQUIBASE_SCHEMA_VERSION = "3.6";
    private SAXParserFactory saxParserFactory;

    public XMLChangeLogSAXParser() {
        saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setValidating(true);
        saxParserFactory.setNamespaceAware(true);
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public static String getSchemaVersion() {
        return LIQUIBASE_SCHEMA_VERSION;
    }

    @Override
    public boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
        return changeLogFile.toLowerCase().endsWith("xml");
    }

    protected SAXParserFactory getSaxParserFactory() {
        return saxParserFactory;
    }

    @Override
    protected ParsedNode parseToNode(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        try (
            InputStream inputStream = StreamUtil.singleInputStream(physicalChangeLogLocation, resourceAccessor)) {
            SAXParser parser = saxParserFactory.newSAXParser();
            trySetSchemaLanguageProperty(parser);
    
            XMLReader xmlReader = parser.getXMLReader();
            LiquibaseEntityResolver resolver=new LiquibaseEntityResolver(this);
            resolver.useResoureAccessor(resourceAccessor,FilenameUtils.getFullPath(physicalChangeLogLocation));
            xmlReader.setEntityResolver(resolver);
            xmlReader.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    LogFactory.getLog(getClass()).warn(LogTarget.LOG, exception.getMessage());
                    throw exception;
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    LogFactory.getLog(getClass()).error(LogTarget.LOG, exception.getMessage());
                    throw exception;
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    LogFactory.getLog(getClass()).error(LogTarget.LOG, exception.getMessage());
                    throw exception;
                }
            });
        	
            if (inputStream == null) {
                if (physicalChangeLogLocation.startsWith("WEB-INF/classes/")) {
                    // Correct physicalChangeLogLocation and try again.
                    return parseToNode(
                        physicalChangeLogLocation.replaceFirst("WEB-INF/classes/", ""),
                            changeLogParameters, resourceAccessor);
                } else {
                    throw new ChangeLogParseException(physicalChangeLogLocation + " does not exist");
                }
            }

            XMLChangeLogSAXHandler contentHandler = new XMLChangeLogSAXHandler(physicalChangeLogLocation, resourceAccessor, changeLogParameters);
            xmlReader.setContentHandler(contentHandler);
            xmlReader.parse(new InputSource(new UtfBomStripperInputStream(inputStream)));

            return contentHandler.getDatabaseChangeLogTree();
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
        }
    }
    
    /**
     * Try to set the parser property "schemaLanguage", but do not mind if the parser does not understand it.
     * @todo If we do not mind, why do we set it in the first place? Need to resarch in git...
     * @param parser the parser to configure
     */
    private void trySetSchemaLanguageProperty(SAXParser parser) {
        try {
            parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        } catch (SAXNotRecognizedException|SAXNotSupportedException ignored) {
            //ok, parser need not support it
        }
    }
}
