package liquibase.test;

import static org.junit.Assert.fail;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;

public class SnapshotAssert {
	private DatabaseSnapshot snapshot;

	private SnapshotAssert(DatabaseSnapshot snapshot) {
		this.snapshot = snapshot;
	}

	public static SnapshotAssert assertThat(DatabaseSnapshot snapshot) {
		return new SnapshotAssert(snapshot);
	}

	public SnapshotAssert containsObject(DatabaseObject object) {
		return checkContainsObject(object.getClass(), object::equals, ignore -> {}, object + " not found");
	}

	public <T extends DatabaseObject> SnapshotAssert containsObject(Class<T> type, Predicate<T> condition) {
		return containsObject(type, condition, ignore -> {});
	}

	public <T extends DatabaseObject> SnapshotAssert containsObject(Class<T> type, Predicate<T> condition, Consumer<T> verfifier) {
		return checkContainsObject(type, condition, verfifier, type.getSimpleName() + " satisfying condition not found");
	}

	private <T extends DatabaseObject> SnapshotAssert checkContainsObject(Class<T> type, Predicate<T> condition, Consumer<T> verfifier, String failMessage) {
		Optional<? extends DatabaseObject> first = snapshot.get(type).stream()
			.filter(condition)
			.peek(verfifier)
			.findFirst();
		if (!first.isPresent()) {
			fail(failMessage);
		}
		return this;
	}
}
