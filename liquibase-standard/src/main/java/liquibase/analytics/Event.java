package liquibase.analytics;

import lombok.Data;

import java.util.Date;

@Data
public class Event {
    private final Date timestamp = new Date();
    private final String command;
}
