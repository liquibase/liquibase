package liquibase.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import liquibase.exception.LiquibaseException;

@FunctionalInterface
public interface InputStreamSupplier extends Supplier<InputStream> {
	InputStream safeGet() throws IOException;
	default InputStream get() {
		try {
			return safeGet();
		} catch (IOException e) {
			throw new RuntimeException(e); // TODO
		}
	}
}
