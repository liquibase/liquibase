package liquibase.parser.core.yaml;

import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomConstructYamlTimestamp extends SafeConstructor.ConstructYamlTimestamp {
    private final static Pattern TIMESTAMP_REGEXP = Pattern.compile(
            "^([0-9][0-9][0-9][0-9])-([0-9][0-9]?)-([0-9][0-9]?)(?:(?:[Tt]|[ \t]+)([0-9][0-9]?):([0-9][0-9]):([0-9][0-9])(?:\\.([0-9]*))?(?:[ \t]*(?:Z|([-+][0-9][0-9]?)(?::([0-9][0-9])?)?))?)?$");
    private final static Pattern YMD_REGEXP =
            Pattern.compile("^([0-9][0-9][0-9][0-9])-([0-9][0-9]?)-([0-9][0-9]?)$");

    private Calendar calendar;

    @Override
    public Object construct(Node node) {
        ScalarNode scalar = (ScalarNode) node;
        String nodeValue = scalar.getValue();
        Matcher match = YMD_REGEXP.matcher(nodeValue);
        if (match.matches()) {
            String year_s = match.group(1);
            String month_s = match.group(2);
            String day_s = match.group(3);
            calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.clear();
            calendar.set(Calendar.YEAR, Integer.parseInt(year_s));
            // Java's months are zero-based...
            calendar.set(Calendar.MONTH, Integer.parseInt(month_s) - 1); // x
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day_s));
            return calendar.getTime();
        } else {
            match = TIMESTAMP_REGEXP.matcher(nodeValue);
            if (!match.matches()) {
                throw new YAMLException("Unexpected timestamp: " + nodeValue);
            }
            String year_s = match.group(1);
            String month_s = match.group(2);
            String day_s = match.group(3);
            String hour_s = match.group(4);
            String min_s = match.group(5);
            // seconds and milliseconds
            String seconds = match.group(6);
            String millis = match.group(7);
            if (millis != null) {
                seconds = seconds + "." + millis;
            }
            double fractions = Double.parseDouble(seconds);
            int sec_s = (int) Math.round(Math.floor(fractions));
            int usec = (int) Math.round((fractions - sec_s) * 1000);
            // timezone
            String timezoneh_s = match.group(8);
            String timezonem_s = match.group(9);
            TimeZone timeZone = null;
            if (timezoneh_s != null) {
                String time = timezonem_s != null ? ":" + timezonem_s : "00";
                timeZone = TimeZone.getTimeZone("GMT" + timezoneh_s + time);
                calendar = Calendar.getInstance(timeZone);
                ;
            } else {
                calendar = Calendar.getInstance();
            }
            calendar.set(Calendar.YEAR, Integer.parseInt(year_s));
            // Java's months are zero-based...
            calendar.set(Calendar.MONTH, Integer.parseInt(month_s) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day_s));
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour_s));
            calendar.set(Calendar.MINUTE, Integer.parseInt(min_s));
            calendar.set(Calendar.SECOND, sec_s);
            calendar.set(Calendar.MILLISECOND, usec);
            if (timeZone == null) {
                return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
            } else {
                return calendar.getTime().toString();
            }
        }
    }
}
