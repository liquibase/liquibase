package liquibase.analytics;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@FieldNameConstants(asEnum = true)
@Data
public class Event {
    private final Date timestamp = new Date();
    private final String command;
    private final String operationOutcome;
    private final String url;

    public Map<String, ?> getPropertiesAsMap() {
        Map<String, Object> properties = new HashMap<>();
        for (Fields field : Fields.values()) {
            try {
                Field refField = this.getClass().getDeclaredField(field.toString());
                refField.setAccessible(true);
                Object value = refField.get(this);
                properties.put(field.toString(), value);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return properties;
    }
}