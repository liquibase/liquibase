package liquibase.parser.core.xml;

import java.util.List;

public interface IncludeAllFilter {
    List<String> filter(List<String> includedChangeLogs);
}