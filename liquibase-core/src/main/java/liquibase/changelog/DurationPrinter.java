package liquibase.changelog;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class DurationPrinter {

	private static final List<TimeUnit> DISPLAY_TIME_UNITS = Arrays.asList(
			TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, 
			TimeUnit.SECONDS, TimeUnit.MILLISECONDS);

	public static Map<TimeUnit, Long> computeDiff(long start, long end) {
		long diffInMillis = end - start;
		Map<TimeUnit, Long> result = new LinkedHashMap<TimeUnit, Long>();
		long millisRest = diffInMillis;
		for (TimeUnit unit : DISPLAY_TIME_UNITS) {
			if (DISPLAY_TIME_UNITS.contains(unit)) {
				long diff = unit.convert(millisRest, TimeUnit.MILLISECONDS);
				long diffInMillisForUnit = unit.toMillis(diff);
				millisRest = millisRest - diffInMillisForUnit;
				result.put(unit, diff);
			}
		}
		return result;
	}

	public static String prettyDuration(long start) {
		return prettyDuration(start, System.currentTimeMillis());
	}

	public static String prettyDuration(long start, long end) {
		Map<TimeUnit, Long> lookup = computeDiff(start, end);

		StringBuilder sb = new StringBuilder();
		for (TimeUnit timeUnit : DISPLAY_TIME_UNITS) {
			Long duration = lookup.get(timeUnit);
			if (duration != null && duration > 0) {
				String label = timeUnit.toString().toLowerCase();
				if (TimeUnit.MILLISECONDS.equals(timeUnit)) {
					label = "ms";
				} else if (duration == 1) {
					int labelLength = label.length();
					label = label.substring(0, labelLength - 1);
				}
				sb.append(String.format("%d %s", duration, label));
			}
		}
		return sb.toString();
	}

}
