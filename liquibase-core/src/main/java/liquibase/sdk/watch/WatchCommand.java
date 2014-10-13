package liquibase.sdk.watch;

import liquibase.change.ColumnConfig;
import liquibase.command.AbstractCommand;
import liquibase.command.CommandValidationErrors;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.integration.commandline.CommandLineResourceAccessor;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.sdk.Main;
import liquibase.sdk.TemplateService;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.core.SelectFromDatabaseChangeLogStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.util.ISODateFormat;
import liquibase.util.StringUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class WatchCommand extends AbstractCommand {

    private String url;
    private String username;
    private String password;
    private int port = 8080;

    private Main mainApp;

    public WatchCommand(Main mainApp) {
        this.mainApp = mainApp;
    }

    @Override
    public String getName() {
        return "watch";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }

    @Override
    protected Object run() throws Exception {
        Server server = new Server(port);

        List<URL> jarUrls = new ArrayList<URL>();
        File libDir = new File("../../lib/");
        for (File file : libDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("jar");
            }
        })) {
            jarUrls.add(file.toURL());
        }
        CompositeResourceAccessor resourceAccessor = new CompositeResourceAccessor(
                new CommandLineResourceAccessor(new URLClassLoader(jarUrls.toArray(new URL[jarUrls.size()]), this.getClass().getClassLoader()))
        );
        Database database = DatabaseFactory.getInstance().openDatabase(url, username, password, null, resourceAccessor);

        ResourceHandler staticHandler = new ResourceHandler();
        staticHandler.setDirectoriesListed(false);
        staticHandler.setWelcomeFiles(new String[]{"index.html"});
        staticHandler.setResourceBase(getClass().getClassLoader().getResource("liquibase/sdk/watch/index.html.vm").toExternalForm().replaceFirst("index.html.vm$", ""));

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{new DynamicContentHandler(database), staticHandler, new DefaultHandler()});

        server.setHandler(handlers);
        server.start();

        mainApp.out("Liquibase Watch running on http://localhost:"+getPort()+"/");
        server.join();


        return "Started";
    }

    private static class DynamicContentHandler extends AbstractHandler {
        private final Database database;
        private final Executor executor;

        public DynamicContentHandler(Database database) {
            this.database = database;
            executor = ExecutorService.getInstance().getExecutor(database);
        }

        @Override
        public void handle(String url, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
            try {

                if (url.equals("/favicon.ico")) {
                    httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    request.setHandled(true);
                } else if (url.equals("/index.html") || url.equals("/") || url.equals("")) {
                    Map<String, Object> context = new HashMap<String, Object>();
                    this.loadIndexData(context);

                    httpServletResponse.setContentType("text/html");
                    httpServletResponse.setStatus(HttpServletResponse.SC_OK);

                    TemplateService.getInstance().write("liquibase/sdk/watch/index.html.vm", httpServletResponse.getWriter(), context);
                    request.setHandled(true);
                } else if (url.equals("/liquibase-status.json")) {
                    if (SnapshotGeneratorFactory.getInstance().hasDatabaseChangeLogTable(database)) {
                        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
                        lockService.waitForLock();
                        List<Map<String, ?>> rows;
                        try {
                            SelectFromDatabaseChangeLogStatement select = new SelectFromDatabaseChangeLogStatement(new ColumnConfig().setName("COUNT(*) AS ROW_COUNT", true), new ColumnConfig().setName("MAX(DATEEXECUTED) AS LAST_EXEC", true));
                            rows = executor.queryForList(select);
                        } finally {
                            lockService.releaseLock();
                        }
                        PrintWriter writer = httpServletResponse.getWriter();

                        httpServletResponse.setContentType("application/json");
                        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                        if (rows.size() == 0) {
                            writer.print("{\"count\": 0}");
                        } else {
                            Map<String, ?> row = rows.iterator().next();
                            writer.print("{\"count\":" + row.get("ROW_COUNT") + ", \"lastExec\": \"" + new ISODateFormat().format((Date) row.get("LAST_EXEC")) + "\"}");
                        }
                        request.setHandled(true);
                    } else {
                        PrintWriter writer = httpServletResponse.getWriter();

                        httpServletResponse.setContentType("application/json");
                        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                        writer.print("{\"count\": -1}");
                        request.setHandled(true);
                    }
                }
            } catch (Throwable e) {
                throw new ServletException(e);
            }
        }

        protected void writeDatabaseChangeLogTab(Map<String, Object> context) throws DatabaseException {
            String outString;
            String changeLogDetails = "";
            if (SnapshotGeneratorFactory.getInstance().hasDatabaseChangeLogTable(database)) {

                outString = "<table class='table table-striped table-bordered table-condensed'>";
                outString += "<tr><th>Id</th><th>Author</th><th>Path</th><th>ExecType</th><th>Tag</th></tr>";

                SelectFromDatabaseChangeLogStatement select = new SelectFromDatabaseChangeLogStatement("FILENAME", "AUTHOR", "ID", "MD5SUM", "DATEEXECUTED", "ORDEREXECUTED", "EXECTYPE", "DESCRIPTION", "COMMENTS", "TAG", "LIQUIBASE").setOrderBy("DATEEXECUTED DESC", "ORDEREXECUTED DESC"); //going in opposite order for easier reading
                List<Map> ranChangeSets = (List) ExecutorService.getInstance().getExecutor(database).queryForList(select);

                for (Map row : ranChangeSets) {
                    String id = cleanHtmlId(row.get("ID") + ":" + row.get("AUTHOR") + ":" + row.get("FILENAME"));
                    outString += "<tr>" +
                            "<td><a style='color:black' class='object-name' href='#" + id + "'>" + StringUtils.escapeHtml((String) row.get("ID")) + "</a></td>" +
                            "<td><a style='color:black' class='object-name' href='#" + id + "'>" + StringUtils.escapeHtml((String) row.get("AUTHOR")) + "</a></td>" +
                            "<td><a style='color:black' class='object-name' href='#" + id + "'>" + StringUtils.escapeHtml((String) row.get("FILENAME")) + "</a></td>" +
                            "<td><a style='color:black' class='object-name' href='#" + id + "'>" + row.get("EXECTYPE") + "</a></td>" +
                            "<td><a style='color:black' class='object-name' href='#" + id + "'>" + StringUtils.escapeHtml((String) StringUtils.trimToEmpty((String) row.get("TAG"))) + "</a></td>" +
                            "</tr>";

                    changeLogDetails += wrapDetails(id, row.get("ID") + " :: " + row.get("AUTHOR") + " :: " + row.get("FILENAME"), writeDatabaseChangeLogDetails(row));
                }
                outString += "</table>";
            } else {
                outString = "<h2 style='margin-top:0px; padding-top: 10px; padding-left:10px'>No DatabaseChangeLog Table</h2>";
            }
            context.put("changeLog", outString);
            context.put("changeLogDetails", changeLogDetails);
        }

        private String cleanHtmlId(String id) {
            return id.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        }

        protected String writeDatabaseChangeLogDetails(Map row) throws DatabaseException {
            String outString = "<table class='table table-striped table-bordered table-condensed'>";


            outString += "<tr><td><strong>Id</strong></td><td>" + row.get("ID") + "</td></tr>\n" +
                    "<tr><td><strong>Author</strong></td><td>" + row.get("AUTHOR") + "</td></tr>\n" +
                    "<tr><td><strong>Filename</strong></td><td>" + row.get("FILENAME") + "</td></tr>\n" +
                    "<tr><td><strong>DateExecuted</strong></td><td>" + new ISODateFormat().format((Date) row.get("DATEEXECUTED")) + "</td></tr>\n" +
                    "<tr><td><strong>OrderExecuted</strong></td><td>" + row.get("ORDEREXECUTED") + "</td></tr>\n" +
                    "<tr><td><strong>ExecType</strong></td><td>" + row.get("EXECTYPE") + "</td></tr>\n" +
                    "<tr><td><strong>MD5Sum</strong></td><td>" + row.get("MD5SUM") + "</td></tr>\n" +
                    "<tr><td><strong>Description</strong></td><td>" + row.get("DESCRIPTION") + "</td></tr>\n" +
                    "<tr><td><strong>Comments</strong></td><td>" + row.get("COMMENTS") + "</td></tr>\n" +
                    "<tr><td><strong>Tag</strong></td><td>" + StringUtils.trimToNull((String) row.get("TAG")) + "</td></tr>\n" +
                    "<tr><td><strong>Liquibase</strong></td><td>" + row.get("LIQUIBASE") + "</td></tr>\n";
            outString += "</table>";

            return outString;
        }

        public void loadIndexData(Map<String, Object> context) {
            try {
                DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(database.getDefaultSchema(), database, new SnapshotControl(database));

                StringBuilder buffer = new StringBuilder();
                Database database = snapshot.getDatabase();

                buffer.append("<div class='panel panel-primary'>");
                buffer.append("<div class='panel-heading'><h2 style='margin-top:0px; margin-bottom:0px'>").append(StringUtils.escapeHtml(database.getConnection().getURL())).append("</h2></div>\n");
                buffer.append("<div class='panel-body'>");
                buffer.append("<strong>Database type:</strong> ").append(StringUtils.escapeHtml(database.getDatabaseProductName())).append("<br>\n");
                buffer.append("<strong>Database version:</strong> ").append(StringUtils.escapeHtml(database.getDatabaseProductVersion())).append("<br>\n");
                buffer.append("<strong>Database user:</strong> ").append(StringUtils.escapeHtml(database.getConnection().getConnectionUserName())).append("<br>\n");

                Set<Schema> schemas = snapshot.get(Schema.class);
                if (schemas.size() > 1) {
                    throw new UnexpectedLiquibaseException("Can only display one schema");
                }
                Schema schema = schemas.iterator().next();
                if (database.supportsSchemas()) {
                    buffer.append("<strong>Catalog & Schema:</strong> ").append(schema.getCatalogName()).append(" / ").append(schema.getName()).append("<br>\n");
                } else {
                    buffer.append("<strong>Catalog:</strong> ").append(schema.getCatalogName()).append("<br>\n");
                }

                buffer.append("</div>\n");
                buffer.append("</div>\n");

                SnapshotControl snapshotControl = snapshot.getSnapshotControl();
                List<Class> includedTypes = sort(snapshotControl.getTypesToInclude());

                StringBuilder catalogBuffer = new StringBuilder();

                StringBuilder detailsBuilder = new StringBuilder();

                catalogBuffer.append("<ul class='nav nav-tabs' id='tabs'>\n");
                catalogBuffer.append("<li><a href='#databasechangelog-tab' data-toggle='tab'>DatabaseChangeLog</a></li>\n");
                for (Class type : includedTypes) {
                    if (schema.getDatabaseObjects(type).size() > 0) {
                        catalogBuffer.append("<li><a href='#").append(type.getSimpleName()).append("-tab' data-toggle='tab'>").append(type.getSimpleName()).append("(s)</a></li>\n");
                    }
                }
                catalogBuffer.append("</ul>\n");

                catalogBuffer.append("<div class='tab-content' style='margin-bottom:20px;'>\n");

                catalogBuffer.append("<div class='tab-pane' style='border: 1px #ddd solid; border-top:none' id='databasechangelog-tab'>\n");
                writeDatabaseChangeLogTab(context);
                detailsBuilder.append(context.get("changeLogDetails"));
                catalogBuffer.append(context.get("changeLog"));
                catalogBuffer.append("</div>");

                for (Class type : includedTypes) {
                    List<? extends DatabaseObject> databaseObjects = sort(schema.getDatabaseObjects(type));
                    if (databaseObjects.size() > 0) {
                        catalogBuffer.append("<div class='tab-pane' style='border: 1px #ddd solid; border-top:none' id='").append(type.getSimpleName()).append("-tab'>\n");

                        catalogBuffer.append("<div style='padding:10px; font-color:black'><ol>\n");

                        StringBuilder typeBuffer = new StringBuilder();
                        for (DatabaseObject databaseObject : databaseObjects) {
                            String id = databaseObject.getClass().getName() + "-" + databaseObject.getName();
                            id = cleanHtmlId(id);
                            typeBuffer.append("<li><a style='color:black' class='object-name' href='#" + id + "'>").append(StringUtils.escapeHtml(databaseObject.getName())).append("</a></li>\n");
                            detailsBuilder.append(wrapDetails(id, type.getSimpleName()+" "+databaseObject.getName(), writeDatabaseObject(databaseObject, new HashSet<String>(), databaseObject.getName()))).append("\n");
                        }

                        catalogBuffer.append(StringUtils.indent(typeBuffer.toString(), 4)).append("\n");

                        catalogBuffer.append("</ol></div>\n");
                        catalogBuffer.append("</div>\n");
                    }
                }
                catalogBuffer.append("</div>\n");
                buffer.append(StringUtils.indent(catalogBuffer.toString(), 4));


                context.put("snapshot", buffer.toString()); //standardize all newline chars
                context.put("details", detailsBuilder.toString()); //standardize all newline chars

            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }

        }

        protected String wrapDetails(String id, String title, String details) {
            StringBuilder buffer = new StringBuilder();
            buffer.append("<div class='modal fade' id='"+id+"'><div class='modal-dialog modal-lg'><div class='modal-content'>");
            buffer.append("<div class='modal-header'><button type='button' class='close' data-dismiss='modal' aria-hidden='true'>&times;</button><h4 class='modal-title'>").append(StringUtils.escapeHtml(title)).append("</h4></div>\n");
            buffer.append("<div class='modal-body'>");
            buffer.append(StringUtils.indent(details, 4));
            buffer.append("</div>");
            buffer.append("<div class='modal-footer'><button type='button' class='btn btn-default' data-dismiss='modal'>Close</button></div>");
            buffer.append("</div></div></div>");

            return buffer.toString();

        }

        protected String writeDatabaseObject(final DatabaseObject databaseObject, Set<String> oldParentNames, String newParentName) {
            final Set<String> parentNames = new HashSet<String>(oldParentNames);
            parentNames.add(newParentName);

            StringBuilder singleValueOut = new StringBuilder();
            StringBuilder multiValueOut = new StringBuilder();
            final List<String> attributes = sort(databaseObject.getAttributes());
            for (String attribute : attributes) {
                if (attribute.equals("name")) {
                    continue;
                }
                if (attribute.equals("schema")) {
                    continue;
                }

                Object value = databaseObject.getAttribute(attribute, Object.class);

                if (value instanceof Schema) {
                    continue;
                }

                boolean multiValue = false;
                if (value instanceof DatabaseObject) {
                    if (parentNames.contains(((DatabaseObject) value).getName())) {
                        value = null;
                    } else {
                        value = databaseObject.getSerializableFieldValue(attribute);
                    }
                } else if (value instanceof Collection) {
                    if (((Collection) value).size() == 0) {
                        value = null;
                    } else {
                        multiValue = true;
                        Object firstValue = ((Collection) value).iterator().next();
                        if (firstValue instanceof DatabaseObject) {
                            final List<String> rowAttributes = new ArrayList<String>();
                            rowAttributes.add("name");
                            for (DatabaseObject obj : ((Collection<DatabaseObject>) value)) {
                                for (String rowAttribute : obj.getAttributes()) {
                                    if (!rowAttributes.contains(rowAttribute)) {
                                        Object cellValue = obj.getAttribute(rowAttribute, Object.class);
                                        if (cellValue instanceof DatabaseObject && parentNames.contains(((DatabaseObject) cellValue).getName())) {
                                            continue;
                                        } else {
                                            if (cellValue == null || (cellValue instanceof Collection && ((Collection) cellValue).size() == 0)) {
                                                continue;
                                            } else {
                                                rowAttributes.add(rowAttribute);
                                            }
                                        }
                                    }
                                }
                            }

                            value = StringUtils.join((Collection) value, "\n", new StringUtils.StringUtilsFormatter() {
                                @Override
                                public String toString(Object obj) {
                                    if (obj instanceof DatabaseObject) {
                                        String row = "<tr>";
                                        for (String attribute : rowAttributes) {
                                            if (((DatabaseObject) obj).getAttributes().contains(attribute)) {
                                                row += "<td>" + StringUtils.escapeHtml(((DatabaseObject) obj).getSerializableFieldValue(attribute).toString());
                                            } else {
                                                row += "<td></td>";
                                            }
                                        }
                                        row += "</tr>";
                                        return row;
                                    } else {
                                        return obj.toString();
                                    }
                                }
                            });
                            String header = "";
                            for (String rowAttribute : rowAttributes) {
                                header += "<th>" + rowAttribute + "</th>";
                            }
                            value = "<div overflow='scroll' style='overflow-x:auto'><table class='table table-bordered table-condensed' style='margin-bottom:0px'><tr>" + header + "</tr>\n" + StringUtils.indent((String) value, 4) + "</table></div>";
                        } else {
                            value = databaseObject.getSerializableFieldValue(attribute);
                        }
                    }
                } else {
                    value = databaseObject.getSerializableFieldValue(attribute);
                }

                if (value != null) {
                    if (multiValue) {
                        multiValueOut.append("<h4>").append(attribute).append(":</h4>");
                        multiValueOut.append(StringUtils.escapeHtml(value.toString()));
                        multiValueOut.append("<br>");
                    } else {
                        singleValueOut.append("<tr><td><strong>").append(attribute).append("</strong></td><td>");
                        singleValueOut.append(value);
                        singleValueOut.append("</td></tr>");
                    }
                }
            }

            String finalOut = singleValueOut.toString();
            if (finalOut.length() > 0) {
                finalOut = "<h4>attributes:</h4><table class='table table-bordered table-condensed'>" + finalOut + "</table><br>";
            }
            finalOut = finalOut + multiValueOut.toString();
            return finalOut;

        }

        private List sort(Collection objects) {
            return sort(objects, new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    if (o1 instanceof Comparable) {
                        return ((Comparable) o1).compareTo(o2);
                    } else if (o1 instanceof Class) {
                        return ((Class) o1).getName().compareTo(((Class) o2).getName());
                    } else {
                        throw new ClassCastException(o1.getClass().getName() + " cannot be cast to java.lang.Comparable or java.lang.Class");
                    }
                }
            });
        }

        private <T> List<T> sort(Collection objects, Comparator<T> comparator) {
            List returnList = new ArrayList(objects);
            Collections.sort(returnList, comparator);

            return returnList;
        }
    }

}
