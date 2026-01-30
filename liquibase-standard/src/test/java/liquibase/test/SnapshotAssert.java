package liquibase.test;

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

	public void containsObject(DatabaseObject object) {
		checkContainsObject(object.getClass(), object::equals, object + " not found");
	}

	public <T extends DatabaseObject> T containsObject(Class<T> type, Predicate<T> condition) {
		return checkContainsObject(type, condition, type.getSimpleName() + " satisfying condition not found");
	}

	private <T extends DatabaseObject> T checkContainsObject(Class<T> type, Predicate<T> condition, String failMessage) {
		return snapshot.get(type).stream()
			.filter(condition)
			.findFirst()
			.orElseThrow(() -> new AssertionError(failMessage));
	}
}
