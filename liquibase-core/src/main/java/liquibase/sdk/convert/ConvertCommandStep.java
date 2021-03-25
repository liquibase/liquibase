package liquibase.sdk.convert;

import liquibase.Scope;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.*;
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

public class ConvertCommandStep extends AbstractCommandStep {

    public static final CommandArgumentDefinition<String> SRC_ARG;
    public static final CommandArgumentDefinition<String> OUT_ARG;
    public static final CommandArgumentDefinition<String> CLASSPATH_ARG;

    static {
        final CommandStepBuilder builder = new CommandStepBuilder(ConvertCommandStep.class);
        SRC_ARG = builder.argument("src", String.class).required().build();
        OUT_ARG = builder.argument("out", String.class).required().build();
        CLASSPATH_ARG = builder.argument("classpath", String.class).required().build();
    }

    @Override
    public String[] getName() {
        return new String[]{"convert"};
    }

    @Override
    public void run(CommandScope commandScope) throws Exception {
        String src = commandScope.getArgumentValue(SRC_ARG);
        String out = commandScope.getArgumentValue(OUT_ARG);
        String classpath = commandScope.getArgumentValue(CLASSPATH_ARG);

        List<ResourceAccessor> openers = new ArrayList<>();
        openers.add(new FileSystemResourceAccessor());
        openers.add(new ClassLoaderResourceAccessor());
        if (classpath != null) {
            openers.add(new FileSystemResourceAccessor(new File(classpath)));
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

        Scope.getCurrentScope().getUI().sendMessage("Converted successfully");
    }
}
