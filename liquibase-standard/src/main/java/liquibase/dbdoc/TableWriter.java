package liquibase.dbdoc;

import liquibase.Scope;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.resource.PathHandlerFactory;
import liquibase.resource.Resource;
import liquibase.structure.core.*;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TableWriter extends HTMLWriter {

    public TableWriter(Resource rootOutputDir, Database database) {
        super(rootOutputDir.resolve("tables"), database);
    }

    @Override
    protected String createTitle(Object object) {
        return "Changes affecting table \""+object.toString() + "\"";
    }

    @Override
    protected void writeCustomHTML(Writer fileWriter, Object object, List<Change> changes, Database database) throws IOException {
        final Table table = (Table) object;
        writeTableRemarks(fileWriter, table);
        writeColumns(fileWriter, table);
        writeTableIndexes(fileWriter, table);
        writeTableForeignKeys(fileWriter, table);
    }

    public void writeHTML(Object object, List<Change> ranChanges, List<Change> changesToRun, String changeLog, String schema) throws DatabaseHistoryException, IOException, DatabaseException {
        super.outputDir = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class).getResource(super.baseOutputDir.getPath() + System.getProperty("file.separator") + schema);
        super.writeHTML(object, ranChanges, changesToRun, changeLog);

    }

    private void writeColumns(Writer fileWriter, Table table) throws IOException {
        List<List<String>> cells = new ArrayList<>();

        for (Column column : table.getColumns()) {
            String remarks = column.getRemarks();
            cells.add(Arrays.asList(column.getType().toString(),
                    column.isNullable() ? "NULL" : "NOT NULL",
                    "<A HREF=\"../columns/" + (table.getSchema().getName() != null ? table.getSchema().getName().toLowerCase() + "." : "") + table.getName().toLowerCase() + "." + column.getName().toLowerCase() + ".html" + "\">" + column.getName() + "</A>", (remarks != null) ? remarks : ""));
            //todo: add foreign key info to columns?
        }


        writeTable("Current Columns", cells, fileWriter);
    }

    private void writeTableRemarks(Writer fileWriter, Table table) throws IOException {
        final String tableRemarks = table.getRemarks();
        if ((tableRemarks != null) && !tableRemarks.isEmpty()) {
        	final List<List<String>> cells = new ArrayList<>();
        	cells.add(Collections.singletonList(tableRemarks));
        	writeTable("Table Description", cells, fileWriter);
        }
    }

    private void writeTableIndexes(Writer fileWriter, Table table) throws IOException {
        final List<List<String>> cells = new ArrayList<>();
        final PrimaryKey primaryKey = table.getPrimaryKey();
        if (!table.getIndexes().isEmpty()) {
            for (Index index : table.getIndexes()) {
                cells.add(Arrays.asList((primaryKey != null && primaryKey.getBackingIndex() == index ? "Primary Key " : index.isUnique() ? "Unique " : "Non-Unique ") +
                        (index.getClustered() == null ? "" : (index.getClustered() ? "Clustered" : "Non-Clustered")),
                        index.getName(),
                        index.getColumnNames().replace(index.getRelation().getName() + ".","")));
            }
        writeTable("Current Table Indexes", cells, fileWriter);
        }
    }

    private void writeTableForeignKeys(Writer fileWriter, Table table) throws IOException {
        final List<List<String>> cells = new ArrayList<>();
        if(!table.getOutgoingForeignKeys().isEmpty())
        {
            for (ForeignKey outgoingForeignKey : table.getOutgoingForeignKeys()) {
                cells.add(Arrays.asList(outgoingForeignKey.getName(),
                        outgoingForeignKey.getForeignKeyColumns().toString().replace(table.getName() + ".", "").replaceAll("[\\[\\]]", ""),
                        outgoingForeignKey.getPrimaryKeyTable().toString(),
                        outgoingForeignKey.getPrimaryKeyColumns().toString().replace(outgoingForeignKey.getPrimaryKeyTable().toString() + ".", "").replaceAll("[\\[\\]]", "")));
            }
            writeTable("Current Table Foreign Keys", cells, fileWriter);
        }
    }
}
