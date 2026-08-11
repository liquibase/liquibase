package liquibase.integration.servlet;

import liquibase.Scope;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Servlet that can be registered via web.xml to view the log of the Liquibase run from the LiquibaseServletListener.
 *
 * @see LiquibaseStatusServlet
 * @see LiquibaseJakartaStatusServlet
 */
class GenericStatusServlet {

    private static final long serialVersionUID = 1092565349351848089L;
    private static final List<LogRecord> liquibaseRunLog = new ArrayList<>();

    public static synchronized void logMessage(LogRecord message) {
        liquibaseRunLog.add(message);
    }

    protected void doGet(GenericServletWrapper.HttpServletRequest httpServletRequest, GenericServletWrapper.HttpServletResponse httpServletResponse) {
        httpServletResponse.setContentType("text/html");

        try {
            PrintWriter writer = httpServletResponse.getWriter();

            String logLevelToDisplay = httpServletRequest.getParameter("logLevel");
            Level currentLevel = Level.INFO;
            if (logLevelToDisplay != null) {
                try {
                    currentLevel = Level.parse(logLevelToDisplay);
                } catch (IllegalArgumentException illegalArgumentException) {
                    throw new IOException(illegalArgumentException);
                }

            }

            writer.println("<html>");
            writer.println("<head><title>Liquibase Status</title></head>");
            writer.println("<body>");
            if (liquibaseRunLog.isEmpty()) {
                writer.println("<b>Liquibase did not run</b>");
            } else {
                writer.println("<b>View level: " + getLevelLink(Level.SEVERE, currentLevel)
                        + " " + getLevelLink(Level.WARNING, currentLevel)
                        + " " + getLevelLink(Level.INFO, currentLevel)
                        + " " + getLevelLink(Level.CONFIG, currentLevel)
                        + " " + getLevelLink(Level.FINE, currentLevel)
                        + " " + getLevelLink(Level.FINER, currentLevel)
                        + " " + getLevelLink(Level.FINEST, currentLevel)
                        + "</b>");

                writer.println("<hr>");
                writer.println("<b>Liquibase started at " + DateFormat.getDateTimeInstance().format(new Date
                        (liquibaseRunLog.get(0).getMillis())));
                writer.println("<hr>");
                writer.println("<pre>");
                for (LogRecord record : liquibaseRunLog) {
                    if (record.getLevel().intValue() >= currentLevel.intValue()) {
                        writer.println(record.getLevel() + ": " + escapeHtml(record.getMessage()));
                        if (record.getThrown() != null) {
                            writer.println(escapeHtml(asString(record.getThrown())));
                        }
                    }
                }
                writer.println("");
                writer.println("");

                writer.println("</pre>");
                writer.println("<hr>");
                writer.println("<b>Liquibase finished at " + DateFormat.getDateTimeInstance().format(new Date
                        (liquibaseRunLog.get(liquibaseRunLog.size() - 1).getMillis())));
            }
            writer.println("</body>");
            writer.println("</html>");
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).severe("Error in doGet: "+e.getMessage(), e);
            httpServletResponse.setStatus(500);
        }
    }

    /**
     * Escapes text that is written into this page. Deliberately dependency-free: this servlet ships inside
     * liquibase-core, which cannot assume that jars beyond the ones in the distribution's internal/lib are on the
     * classpath. Covers the characters that matter both in element text and inside a quoted attribute.
     */
    private static String escapeHtml(String text) {
        if (text == null) {
            return null;
        }
        StringBuilder escaped = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            switch (character) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&#39;");
                    break;
                default:
                    escaped.append(character);
            }
        }
        return escaped.toString();
    }

    private static String asString(Throwable thrown) {
        StringWriter stackTrace = new StringWriter();
        try (PrintWriter stackTraceWriter = new PrintWriter(stackTrace)) {
            thrown.printStackTrace(stackTraceWriter);
        }
        return stackTrace.toString();
    }

    /**
     * Builds the link used to switch the displayed log level. The link is deliberately relative (query string only) so
     * that no part of the incoming request is reflected back into the response.
     */
    private String getLevelLink(Level level, Level currentLevel) {
        if (currentLevel.equals(level)) {
            return level.getName();
        } else {
            return "<a href=\"?logLevel=" + level.getName() + "\">" + level.getName() + "</a>";
        }
    }
}
