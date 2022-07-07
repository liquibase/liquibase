package liquibase.changelog;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.UnknownChangeLogParameterException;
import liquibase.parser.ChangeLogParserConfiguration;

import java.io.IOException;
import java.io.StringReader;

class ExpressionExpander {
    private final boolean enableEscaping;
    private final ChangeLogParameters parameters;

    public ExpressionExpander(ChangeLogParameters parameters) {
        this.enableEscaping = ChangeLogParserConfiguration.SUPPORT_PROPERTY_ESCAPING.getCurrentValue();
        this.parameters = parameters;
    }

    public String expandExpressions(String text, DatabaseChangeLog changeLog) throws UnknownChangeLogParameterException {
        if (text == null) {
            return null;
        }

        return expandExpressions(new StringReader(text), changeLog, false);
    }

    private String expandExpressions(StringReader reader, DatabaseChangeLog changeLog, boolean inExpression) throws UnknownChangeLogParameterException {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            int nextChar = reader.read();
            while (nextChar != -1) {
                if (nextChar == '$') {
                    reader.mark(1);
                    if (reader.read() == '{') {
                        String paramExpression = expandExpressions(reader, changeLog, true);

                        Object paramValue;
                        if (paramExpression.startsWith("${")) {
                            paramValue = paramExpression; //was not actually a valid expression
                        } else if (paramExpression.startsWith(":") && enableEscaping) {
                            paramValue = "${" + paramExpression.substring(1).trim() + "}";
                        } else {
                            paramValue = parameters.getValue(paramExpression.trim(), changeLog);

                            if (paramValue == null) {
                                final ChangeLogParserConfiguration.MissingPropertyMode missingPropertyMode = ChangeLogParserConfiguration.MISSING_PROPERTY_MODE.getCurrentValue();
                                switch (missingPropertyMode) {
                                    case EMPTY:
                                        paramValue = "";
                                        break;
                                    case ERROR:
                                        throw new UnknownChangeLogParameterException("Could not resolve expression `${" + paramExpression + "}` in file " + changeLog.getPhysicalFilePath());
                                    case PRESERVE:
                                        paramValue = "${" + paramExpression + "}";
                                        break;
                                    default:
                                        throw new UnexpectedLiquibaseException("Unknown MissingPropertyMode: " + missingPropertyMode);
                                }
                            } else {
                                if (paramValue instanceof String) {
                                    paramValue = expandExpressions((String) paramValue, changeLog);
                                }
                            }
                        }

                        stringBuilder.append(paramValue);
                    } else {
                        stringBuilder.append("$");
                        reader.reset();
                        nextChar = reader.read();
                        continue;
                    }
                } else {
                    if (nextChar == '}' && inExpression) {
                        return stringBuilder.toString();
                    }

                    stringBuilder.append((char) nextChar);
                }

                nextChar = reader.read();
            }
        } catch (IOException exception) {
            // Unreachable as we initialize the StringReader with a non-null string
        }

        if (inExpression) {
            //never got to the trailing `}`, return the string as-is
            return "${"+stringBuilder;
        } else {
            return stringBuilder.toString();
        }
    }
}
