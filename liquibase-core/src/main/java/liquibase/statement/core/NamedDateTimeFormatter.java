package liquibase.statement.core;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;
import java.util.stream.Stream;

public enum NamedDateTimeFormatter implements CurrentDateTimeFunction {

	BASIC_ISO_DATE("BASIC_ISO_DATE", DateTimeFormatter.BASIC_ISO_DATE)
	, ISO_LOCAL_DATE("ISO_LOCAL_DATE", DateTimeFormatter.ISO_LOCAL_DATE)
	, ISO_OFFSET_DATE("ISO_OFFSET_DATE", DateTimeFormatter.ISO_OFFSET_DATE)
	, ISO_DATE("ISO_DATE", DateTimeFormatter.ISO_DATE)
	, ISO_LOCAL_TIME("ISO_LOCAL_DATE", DateTimeFormatter.ISO_LOCAL_TIME)
	, ISO_OFFSET_TIME("ISO_OFFSET_TIME", DateTimeFormatter.ISO_OFFSET_TIME)
	, ISO_TIME("ISO_TIME", DateTimeFormatter.ISO_TIME)
	, ISO_LOCAL_DATE_TIME("ISO_LOCAL_DATE_TIME", DateTimeFormatter.ISO_LOCAL_DATE_TIME)
	, ISO_OFFSET_DATE_TIME("ISO_OFFSET_DATE_TIME", DateTimeFormatter.ISO_OFFSET_DATE_TIME)
	, ISO_ZONED_DATE_TIME("ISO_ZONED_DATE_TIME", DateTimeFormatter.ISO_ZONED_DATE_TIME)
	, ISO_DATE_TIME("ISO_DATE_TIME", DateTimeFormatter.ISO_DATE_TIME)
	, ISO_ORDINAL_DATE("ISO_ORDINAL_DATE", DateTimeFormatter.ISO_ORDINAL_DATE)
	, ISO_WEEK_DATE("ISO_WEEK_DATE", DateTimeFormatter.ISO_WEEK_DATE)
	, ISO_INSTANT("ISO_INSTANT", DateTimeFormatter.ISO_INSTANT)
	, RFC_1123_DATE_TIME("RFC_1123_DATE_TIME", DateTimeFormatter.RFC_1123_DATE_TIME)
	, OFLOCALIZEDTIME("ofLocalizedTime", DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
	, OFLOCALIZEDDATE("ofLocalizedDate", DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
	, OFLOCALIZEDDATETIME("ofLocalizedDateTime", DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));


	private final String name;
	private final DateTimeFormatter formatter;

	NamedDateTimeFormatter(String name, DateTimeFormatter formatter) {
		this.name = name;
		this.formatter = formatter;
	}

	public static CurrentDateTimeFunction byNameOrPattern(String nameOrPattern) {
		return Stream.of(NamedDateTimeFormatter.values())
				.filter(ndtf -> Objects.equals(ndtf.getName(), nameOrPattern))
				.findFirst()
				.map(CurrentDateTimeFunction.class::cast)
				.orElse(nameOrPattern != null ? () -> DateTimeFormatter.ofPattern(nameOrPattern).format(ZonedDateTime.now()) : null);
	}

	public String getName() {
		return name;
	}

	public DateTimeFormatter getFormatter() {
		return formatter;
	}

	@Override
	public String getTime() {
		return "'" + formatter.format(ZonedDateTime.now()) + "'";
	}


}