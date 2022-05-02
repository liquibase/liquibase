package liquibase.util;

import java.io.IOException;
import java.io.StringReader;

public class ExpressionExpander {
    private final boolean enableEscaping;

    public ExpressionExpander(boolean enableEscaping) {
        this.enableEscaping = enableEscaping;
    }

    public String expandExpressions(String text, ReplacementProvider replacementProvider) {
        if (text == null) {
            return null;
        }

        return expandExpressions(new StringReader(text), replacementProvider);
    }

    private String expandExpressions(StringReader reader, ReplacementProvider replacementProvider) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            int nextChar = reader.read();
            while (nextChar != -1) {
                switch (nextChar) {
                    case '}': {
                        return stringBuilder.toString();
                    }
                    case '$': {
                        reader.mark(1);
                        if (reader.read() == '{') {
                            reader.mark(1);
                            if (enableEscaping && reader.read() == ':') {
                                stringBuilder.append("${");
                                stringBuilder.append(expandExpressions(reader, replacementProvider));
                                stringBuilder.append("}");
                            } else {
                                reader.reset();
                                String expanded = expandExpressions(reader, replacementProvider);
                                String replacement = replacementProvider.getReplacement(expanded);
                                stringBuilder.append(replacement);
                            }
                        } else {
                            reader.reset();
                            stringBuilder.append("$");
                        }
                        break;
                    }
                    default: {
                        stringBuilder.append((char)nextChar);
                        break;
                    }
                }

                nextChar = reader.read();
            }
        } catch (IOException exception) {
            // Unreachable as we initialize the StringReader with a non-null string
        }

        return stringBuilder.toString();
    }

    public interface ReplacementProvider {
        String getReplacement(String property);
    }
}
