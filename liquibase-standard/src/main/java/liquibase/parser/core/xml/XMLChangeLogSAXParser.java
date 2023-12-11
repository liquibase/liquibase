package liquibase.parser.core.xml;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.changelog.ChangeLogParameters;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.core.ParsedNode;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import liquibase.util.BomAwareInputStream;
import liquibase.util.FileUtil;
import liquibase.util.LiquibaseUtil;
import org.xml.sax.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

public class XMLChangeLogSAXParser extends AbstractChangeLogParser {

    public static final String LIQUIBASE_SCHEMA_VERSION;
    private final SAXParserFactory saxParserFactory;

    static {
        LIQUIBASE_SCHEMA_VERSION = computeSchemaVersion(LiquibaseUtil.getBuildVersion());
    }

    private final LiquibaseEntityResolver resolver = new LiquibaseEntityResolver();

    public XMLChangeLogSAXParser() {
        saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setValidating(GlobalConfiguration.VALIDATE_XML_CHANGELOG_FILES.getCurrentValue());
        saxParserFactory.setNamespaceAware(true);
        if (GlobalConfiguration.SECURE_PARSING.getCurrentValue()) {
            try {
                saxParserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (Throwable e) {
                Scope.getCurrentScope().getLog(getClass()).fine("Cannot enable FEATURE_SECURE_PROCESSING: " + e.getMessage(), e);
            }
        }
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

    /**
     * When set to true, a warning will be printed to the console if the XSD version used does not match the version
     * of Liquibase. If "latest" is used as the XSD version, no warning is printed.
     */
    public void setShouldWarnOnMismatchedXsdVersion(boolean shouldWarnOnMismatchedXsdVersion) {
        resolver.setShouldWarnOnMismatchedXsdVersion(shouldWarnOnMismatchedXsdVersion);
    }

    @Override
    protected ParsedNode parseToNode(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        try {
            Resource resource = resourceAccessor.get(physicalChangeLogLocation);
            SAXParser parser = saxParserFactory.newSAXParser();
            if (GlobalConfiguration.SECURE_PARSING.getCurrentValue()) {
                try {
                    parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "http,https"); //need to allow external schemas on http/https to support the liquibase.org xsd files
                } catch (SAXException e) {
                    Scope.getCurrentScope().getLog(getClass()).fine("Cannot enable ACCESS_EXTERNAL_SCHEMA: " + e.getMessage(), e);
                }
            }
            trySetSchemaLanguageProperty(parser);

            XMLReader xmlReader = parser.getXMLReader();
            xmlReader.setEntityResolver(resolver);
            xmlReader.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    Scope.getCurrentScope().getLog(getClass()).warning(exception.getMessage());
                    throw exception;
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    Scope.getCurrentScope().getLog(getClass()).severe(exception.getMessage());
                    throw exception;
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    Scope.getCurrentScope().getLog(getClass()).severe(exception.getMessage());
                    throw exception;
                }
            });

            if (!resource.exists()) {
                if (physicalChangeLogLocation.startsWith("WEB-INF/classes/")) {
                    // Correct physicalChangeLogLocation and try again.
                    return parseToNode(
                            physicalChangeLogLocation.replaceFirst("WEB-INF/classes/", ""),
                            changeLogParameters, resourceAccessor);
                } else {
                    throw new ChangeLogParseException(FileUtil.getFileNotFoundMessage(physicalChangeLogLocation));
                }
            }

            XMLChangeLogSAXHandler contentHandler = new XMLChangeLogSAXHandler(physicalChangeLogLocation, resourceAccessor, changeLogParameters);
            xmlReader.setContentHandler(contentHandler);
            try (InputStream stream = resource.openInputStream()) {
                xmlReader.parse(new InputSource(new BomAwareInputStream(stream)));
            }

            return contentHandler.getDatabaseChangeLogTree();
        } catch (ChangeLogParseException e) {
            throw e;
        } catch (IOException e) {
            throw new ChangeLogParseException("Error Reading Changelog File: " + e.getMessage(), e);
        } catch (SAXParseException e) {
            throw new ChangeLogParseException("Error parsing line " + e.getLineNumber() + " column " + e.getColumnNumber() + " of " + physicalChangeLogLocation + ": " + e.getMessage(), e);
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
     * Attempts to set the "schemaLanguage" property of the given parser, but ignores any errors that may occur if the parser
     * does not recognize this property.
     *
     * @param parser the parser to configure
     * @todo Investigate why we set this property if we don't mind if the parser does not recognize it.
     */
    private void trySetSchemaLanguageProperty(SAXParser parser) {
        try {
            parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        } catch (SAXNotRecognizedException | SAXNotSupportedException ignored) {
            //ok, parser need not support it
        }
    }

    static String computeSchemaVersion(String version) {
        String finalVersion = null;

        if (version != null && version.contains(".")) {
            String[] splitVersion = version.split("\\.");
            finalVersion = splitVersion[0] + "." + splitVersion[1];
        }
        if (finalVersion == null) {
            finalVersion = "latest";
        }
        return finalVersion;
    }
}
