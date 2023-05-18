package liquibase.integration.servlet;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Version of {@link GenericStatusServlet} that uses javax.servlet and NOT jakarta.servlet
 */
public class LiquibaseStatusServlet extends HttpServlet {


    private static final long serialVersionUID = -5518519597664302292L;
    private final GenericStatusServlet delegate;

    public LiquibaseStatusServlet() {
        this.delegate = new GenericStatusServlet();
    }

    @Override
    protected void doGet(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse resp)  {
        delegate.doGet(new JavaxHttpServletRequest(req), new JavaxHttpServletResponse(resp));
    }

    private static class JavaxHttpServletRequest extends GenericServletWrapper.HttpServletRequest {
        private final javax.servlet.http.HttpServletRequest request;

        public JavaxHttpServletRequest(javax.servlet.http.HttpServletRequest req) {
            this.request = req;
        }

        @Override
        public String getParameter(String key) {
            return request.getParameter(key);
        }

        @Override
        public String getRequestURI() {
            return request.getRequestURI();
        }
    }

    private static class JavaxHttpServletResponse extends GenericServletWrapper.HttpServletResponse {

        private final javax.servlet.http.HttpServletResponse response;

        public JavaxHttpServletResponse(javax.servlet.http.HttpServletResponse resp) {
            this.response = resp;
        }

        @Override
        public void setStatus(int status) {
            this.response.setStatus(status);
        }

        @Override
        public void setContentType(String type) {
            this.response.setContentType(type);
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return response.getWriter();
        }
    }

}
