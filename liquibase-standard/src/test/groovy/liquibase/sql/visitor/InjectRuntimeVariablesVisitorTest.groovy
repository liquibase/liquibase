package liquibase.sql.visitor

import liquibase.changelog.ChangeLogParameters

/** Class created just for being able to clear runtime properties */
class InjectRuntimeVariablesVisitorTest {
	static void clear() {
		if (InjectRuntimeVariablesVisitor.instance) {
			InjectRuntimeVariablesVisitor.instance.params = new ChangeLogParameters()
		}
	}
}
