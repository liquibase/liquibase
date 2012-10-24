package liquibase.logging.core;

import liquibase.logging.LogLevel;
import liquibase.util.StringUtils;
import org.json.JSONException;
import org.json.JSONWriter;

import java.io.*;
import java.text.DateFormat;
import java.util.Date;

public class JsonLogger extends DefaultLogger {
    @Override
    protected void print(LogLevel logLevel, String message) {
        if (StringUtils.trimToNull(message) == null) {
            return;
        }

        PrintStream stream = getStream();

        try {
            Writer writer = new OutputStreamWriter(stream);
            JSONWriter jsonWriter = new JSONWriter(writer);
            jsonWriter.object()
                    .key("Level").value(logLevel)
                    .key("Date").value(buildDate())
                    .key("name").value(getName())
                    .key("context").value(getContext())
                    .key("message").value(message)
                    .endObject();
            writer.append("\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
