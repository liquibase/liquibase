package liquibase.parser;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

public class ChangeLogParserFactory {

    private static ChangeLogParserFactory instance;

    private Map<String, SortedSet<ChangeLogParser>> parsers = new HashMap<String, SortedSet<ChangeLogParser>>();


    public static void reset() {
        instance = new ChangeLogParserFactory();
    }

    public static ChangeLogParserFactory getInstance() {
        if (instance == null) {
             instance = new ChangeLogParserFactory();
        }
        return instance;
    }

    private ChangeLogParserFactory() {
        Class<? extends ChangeLogParser>[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(ChangeLogParser.class);

            for (Class<? extends ChangeLogParser> clazz : classes) {
                    register((ChangeLogParser) clazz.getConstructor().newInstance());
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    public Map<String, SortedSet<ChangeLogParser>> getParsers() {
        return parsers;
    }

    public ChangeLogParser getParser(String fileNameOrExtension) {
        fileNameOrExtension = fileNameOrExtension.replaceAll(".*\\.", ""); //just need the extension
        SortedSet<ChangeLogParser> parseres = parsers.get(fileNameOrExtension);
        if (parseres == null || parseres.size() == 0) {
            return null;
        }
        return parseres.iterator().next();
    }

    public void register(ChangeLogParser changeLogParser) {
        for (String extension : changeLogParser.getValidFileExtensions()) {
            if (!parsers.containsKey(extension)) {
                parsers.put(extension, new TreeSet<ChangeLogParser>(new Comparator<ChangeLogParser>() {
                    public int compare(ChangeLogParser o1, ChangeLogParser o2) {
                        return Integer.valueOf(o2.getPriority()).compareTo(o1.getPriority());
                    }
                }));
            }
            parsers.get(extension).add(changeLogParser);
        }
    }

    public void unregister(ChangeLogParser changeLogParser) {
        for (Iterator<Map.Entry<String, SortedSet<ChangeLogParser>>> i = parsers.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, SortedSet<ChangeLogParser>> entry = i.next();
            if (entry.getValue().first().equals(changeLogParser)) {
                i.remove();
                break;
            }
        }
    }
}
