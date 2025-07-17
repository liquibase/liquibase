package liquibase.validator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class RawChangeSet {
    private final String author;
    private final String id;
    private String filePath;
    private String labels;
    private String contexts;
    private String dbms;
    private String runWith;
    private String logicalFilePath;
    private List<String> preconditions;
    private QuotingStrategyEnum quotingStrategy;
    private OnValidationFailEnum onValidationFail;
    private String changeLogFormat;

    public RawChangeSet(String author, String id, String filePath) {
        this.author = author;
        this.id = id;
        this.filePath = filePath;
        this.preconditions = new ArrayList<>();
        this.quotingStrategy = QuotingStrategyEnum.getDefault();
        this.onValidationFail = OnValidationFailEnum.getDefault();
    }

    public enum QuotingStrategyEnum {
        LEGACY,
        QUOTE_ALL_OBJECTS,
        QUOTE_ONLY_RESERVED_WORDS;

        public static QuotingStrategyEnum getDefault() {
            return LEGACY;
        }

        public static QuotingStrategyEnum fromString(String value) {
            if (value == null || value.trim().isEmpty()) {
                return getDefault();
            }

            try {
                return QuotingStrategyEnum.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public enum OnValidationFailEnum {
        HALT,
        MARK_RAN;

        public static OnValidationFailEnum getDefault() {
            return HALT;
        }

        public static OnValidationFailEnum fromString(String value) {
            if (value == null || value.trim().isEmpty()) {
                return getDefault();
            }

            try {
                return OnValidationFailEnum.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    @Override
    public String toString() {
        return String.format("RawChangeSet{author='%s', id='%s', filePath='%s'}",
                author, id, filePath);
    }
}
