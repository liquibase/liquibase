package liquibase.dbdoc;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.structure.core.*;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableWriter extends HTMLWriter {

    public TableWriter(File rootOutputDir, Database database) {
        super(new File(rootOutputDir, "tables"), database);
    }

    @Override
    protected String createTitle(Object object) {
        return "Changes affecting table \""+object.toString() + "\"";
    }

    @Override
    protected void writeCustomHTML(Writer fileWriter, Object object, List<Change> changes, Database database) throws IOException {
        final Table table = (Table) object;
        writeTableRemarks(fileWriter, table, database);
        writeColumns(fileWriter, table, database);
        writeTableIndexes(fileWriter, table, database);
        writeTableForeignKeys(fileWriter, table, database);
    }

    private void writeColumns(Writer fileWriter, Table table, Database database) throws IOException {
        List<List<String>> cells = new ArrayList<>();

        for (Column column : table.getColumns()) {
            String remarks = column.getRemarks();
            cells.add(Arrays.asList(column.getType().toString(),
                    column.isNullable() ? "NULL" : "NOT NULL",
                    "<A HREF=\"../columns/" + table.getSchema().getName().toLowerCase() + "." + table.getName().toLowerCase() + "." + column.getName().toLowerCase() + ".html" + "\">" + column.getName() + "</A>", (remarks != null) ? remarks : ""));
            //todo: add foreign key info to columns?
        }


        writeTable("Current Columns", cells, fileWriter);
    }

    private void writeTableRemarks(Writer fileWriter, Table table, Database database) throws IOException {
        final String tableRemarks = table.getRemarks();
        if ((tableRemarks != null) && !tableRemarks.isEmpty()) {
        	final List<List<String>> cells = new ArrayList<>();
        	cells.add(Arrays.asList(tableRemarks));
        	writeTable("Table Description", cells, fileWriter);
        }
    }

    private void writeTableIndexes(Writer fileWriter, Table table, Database database) throws IOException {
        final List<List<String>> cells = new ArrayList<>();
        final PrimaryKey primaryKey = table.getPrimaryKey();
        if (!table.getIndexes().isEmpty()) {
            for (Index index : table.getIndexes()) {
                cells.add(Arrays.asList((primaryKey != null && primaryKey.getBackingIndex() == index ? "Primary Key " : index.isUnique() ? "Unique " : "Non-Unique ") +
                        (index.getClustered() == null ? "" : (index.getClustered() ? "Clustered" : "Non-Clustered")),
                        index.getName(),
                        index.getColumnNames().replace(index.getTable().getName() + ".","")));
            }
        writeTable("Current Table Indexes", cells, fileWriter);
        }
    }

    private void writeTableForeignKeys(Writer fileWriter, Table table, Database database) throws IOException {
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
