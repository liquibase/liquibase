package liquibase.integration.servlet;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericStatusServletTest {

    private static final String MALICIOUS_URI = "/status\"><script>alert(1)</script>";

    @Test
    void doGetDoesNotWriteUnescapedUserControlledDataIntoTheResponse() {
        GenericStatusServlet.logMessage(new LogRecord(Level.SEVERE, "<script>alert(2)</script>"));

        RecordingResponse response = new RecordingResponse();
        new GenericStatusServlet().doGet(new StubRequest(MALICIOUS_URI), response);

        String html = response.getBody();

        // The page really was rendered, so the assertions below are meaningful.
        assertTrue(html.contains("<title>Liquibase Status</title>"), "expected the status page to be rendered: " + html);
        assertEquals(0, response.status, "doGet should not have failed");

        // The request URI is no longer reflected at all - the level links are relative.
        assertFalse(html.contains(MALICIOUS_URI), "request URI must not be reflected into the response");
        assertFalse(html.contains("/status"), "request URI must not be reflected into the response");
        assertTrue(html.contains("<a href=\"?logLevel=SEVERE\">SEVERE</a>"), "expected a relative, quoted level link");

        // Log messages are escaped rather than emitted as markup.
        assertFalse(html.contains("<script>alert(2)</script>"), "log message must be escaped");
        assertTrue(html.contains("&lt;script&gt;alert(2)&lt;/script&gt;"), "log message must be escaped");
    }

    @Test
    void doGetReportsExceptionsWithoutExposingTheStackTrace() {
        LogRecord record = new LogRecord(Level.SEVERE, "failed");
        record.setThrown(new IllegalStateException("<img src=x onerror=alert(3)>"));
        GenericStatusServlet.logMessage(record);

        RecordingResponse response = new RecordingResponse();
        new GenericStatusServlet().doGet(new StubRequest("/status"), response);

        String html = response.getBody();

        // The type and message are reported, escaped, so the page still says what went wrong.
        assertTrue(html.contains("IllegalStateException: &lt;img src=x onerror=alert(3)&gt;"),
                "expected an escaped exception type and message: " + html);
        assertFalse(html.contains("<img src=x"), "exception message must be escaped");

        // The stack trace itself stays out of the response - it goes to the server log instead.
        assertFalse(html.contains("at liquibase."), "stack trace must not be written to the response");
        assertFalse(html.contains(GenericStatusServletTest.class.getName()),
                "stack trace must not be written to the response");
    }

    private static class StubRequest extends GenericServletWrapper.HttpServletRequest {
        private final String requestUri;

        private StubRequest(String requestUri) {
            this.requestUri = requestUri;
        }

        @Override
        public String getParameter(String key) {
            return null;
        }

        @Override
        public String getRequestURI() {
            return requestUri;
        }
    }

    private static class RecordingResponse extends GenericServletWrapper.HttpServletResponse {
        private final StringWriter body = new StringWriter();
        private final PrintWriter writer = new PrintWriter(body);
        private int status;

        @Override
        public void setStatus(int status) {
            this.status = status;
        }

        @Override
        public void setContentType(String type) {
            // not asserted on
        }

        @Override
        public PrintWriter getWriter() {
            return writer;
        }

        private String getBody() {
            writer.flush();
            return body.toString();
        }
    }
}
