package liquibase.test;

import static org.junit.Assert.fail;

import java.util.Optional;
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
		return checkContainsObject(object.getClass(), object::equals, object + " not found");
	}

	public <T extends DatabaseObject> SnapshotAssert containsObject(Class<T> type, Predicate<T> condition) {
		return checkContainsObject(type, condition, type.getSimpleName() + " satisfying condition not found");
	}

	private <T extends DatabaseObject> SnapshotAssert checkContainsObject(Class<T> type, Predicate<T> condition, String failMessage) {
		Optional<? extends DatabaseObject> first = snapshot.get(type).stream()
			.filter(condition)
			.findFirst();
		if (!first.isPresent()) {
			fail(failMessage);
		}
		return this;
	}
}
