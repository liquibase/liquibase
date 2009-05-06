package liquibase.changelog;

import liquibase.PluginUtil;
import liquibase.exception.UnexpectedLiquibaseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeLogParserFactory {

    private static ChangeLogParserFactory instance = new ChangeLogParserFactory();

    private Map<String, ChangeLogParser> parsers = new HashMap<String, ChangeLogParser>();


    public static void reset() {
        instance = new ChangeLogParserFactory();
    }

    public static ChangeLogParserFactory getInstance() {
        return instance;
    }

    private ChangeLogParserFactory() {
        Class[] classes;
        try {
            classes = PluginUtil.getClasses("liquibase.changelog.parser", ChangeLogParser.class);

            for (Class clazz : classes) {
                    register((ChangeLogParser) clazz.getConstructor().newInstance());
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    public Map<String, ChangeLogParser> getParsers() {
        return parsers;
    }

    public ChangeLogParser getParser(String fileNameOrExtension) {
        fileNameOrExtension = fileNameOrExtension.replaceAll(".*\\.", ""); //just need the extension
        return parsers.get(fileNameOrExtension);
    }

    public void register(ChangeLogParser changeLogParser) {
        for (String extension : changeLogParser.getValidFileExtensions()) {
            parsers.put(extension, changeLogParser);
        }
    }

    public void unregister(ChangeLogParser changeLogParser) {
        List<Map.Entry<String, ChangeLogParser>> entrysToRemove = new ArrayList<Map.Entry<String, ChangeLogParser>>();
        for (Map.Entry<String, ChangeLogParser> entry : parsers.entrySet()) {
            if (entry.getValue().equals(changeLogParser)) {
                entrysToRemove.add(entry);
            }
        }

        for (Map.Entry<String, ChangeLogParser> entry : entrysToRemove) {
            parsers.remove(entry.getKey());
        }

    }
}
