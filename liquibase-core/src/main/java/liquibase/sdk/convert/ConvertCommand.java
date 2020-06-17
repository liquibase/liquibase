package liquibase.sdk.convert;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.AbstractCommand;
import liquibase.command.CommandResult;
import liquibase.command.CommandValidationErrors;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.ChangeLogSerializerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ConvertCommand extends AbstractCommand {

    private String src;
    private String out;
    private String classpath;

    @Override
    public String getName() {
        return "convert";
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
    }

    public String getClasspath() {
        return classpath;
    }

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    @Override
    protected CommandResult run() throws Exception {
        List<ResourceAccessor> openers = new ArrayList<>();
        openers.add(new FileSystemResourceAccessor());
        openers.add(new ClassLoaderResourceAccessor());
        if (classpath != null) {
            openers.add(new FileSystemResourceAccessor(classpath));
        }
        ResourceAccessor resourceAccessor = new CompositeResourceAccessor(openers);

        ChangeLogParser sourceParser = ChangeLogParserFactory.getInstance().getParser(src, resourceAccessor);
        ChangeLogSerializer outSerializer = ChangeLogSerializerFactory.getInstance().getSerializer(out);

        DatabaseChangeLog changeLog = sourceParser.parse(src, new ChangeLogParameters(), resourceAccessor);

        File outFile = new File(out);
        if (!outFile.exists()) {
            outFile.getParentFile().mkdirs();
        }
        
        try (FileOutputStream outputStream = new FileOutputStream(outFile)) {
            outSerializer.write(changeLog.getChangeSets(), outputStream);
        }

        return new CommandResult("Converted successfully");
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }
}
