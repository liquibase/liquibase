package liquibase.integration.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;

import java.io.IOException;
import java.io.PrintWriter;

public class LiquibaseJakartaStatusServlet extends HttpServlet {


    private static final long serialVersionUID = -6601471901927004592L;
    private final GenericStatusServlet delegate;

    public LiquibaseJakartaStatusServlet() {
        this.delegate = new GenericStatusServlet();
    }

    @Override
    protected void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws ServletException, IOException {
        delegate.doGet(new JakartaHttpServletRequest(req), new JakartaHttpServletResponse(resp));
    }

    private static class JakartaHttpServletRequest extends GenericServletWrapper.HttpServletRequest {
        private final jakarta.servlet.http.HttpServletRequest request;

        public JakartaHttpServletRequest(jakarta.servlet.http.HttpServletRequest req) {
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

    private static class JakartaHttpServletResponse extends GenericServletWrapper.HttpServletResponse {

        private final jakarta.servlet.http.HttpServletResponse response;

        public JakartaHttpServletResponse(jakarta.servlet.http.HttpServletResponse resp) {
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
