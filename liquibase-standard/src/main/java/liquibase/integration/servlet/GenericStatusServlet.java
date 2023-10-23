package liquibase.integration.servlet;

import liquibase.Scope;

import java.io.IOException;
import java.io.PrintWriter;
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
                writer.println("<b>View level: " + getLevelLink(Level.SEVERE, currentLevel, httpServletRequest)
                        + " " + getLevelLink(Level.WARNING, currentLevel, httpServletRequest)
                        + " " + getLevelLink(Level.INFO, currentLevel, httpServletRequest)
                        + " " + getLevelLink(Level.CONFIG, currentLevel, httpServletRequest)
                        + " " + getLevelLink(Level.FINE, currentLevel, httpServletRequest)
                        + " " + getLevelLink(Level.FINER, currentLevel, httpServletRequest)
                        + " " + getLevelLink(Level.FINEST, currentLevel, httpServletRequest)
                        + "</b>");

                writer.println("<hr>");
                writer.println("<b>Liquibase started at " + DateFormat.getDateTimeInstance().format(new Date
                        (liquibaseRunLog.get(0).getMillis())));
                writer.println("<hr>");
                writer.println("<pre>");
                for (LogRecord record : liquibaseRunLog) {
                    if (record.getLevel().intValue() >= currentLevel.intValue()) {
                        writer.println(record.getLevel() + ": " + record.getMessage());
                        if (record.getThrown() != null) {
                            record.getThrown().printStackTrace(writer);
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

    private String getLevelLink(Level level, Level currentLevel, GenericServletWrapper.HttpServletRequest request) {
        if (currentLevel.equals(level)) {
            return level.getName();
        } else {
            return "<a href=" + request.getRequestURI() + "?logLevel=" + level.getName() + ">" + level.getName() + "</a>";
        }
    }
}
