package liquibase.sqlgenerator.core;

import liquibase.statement.CommentStatement;
import liquibase.sqlgenerator.core.CommentGenerator;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;

public class CommentGeneratorTest extends AbstractSqlGeneratorTest<CommentStatement> {

	public CommentGeneratorTest() throws Exception {
		this(new CommentGenerator());
	}
	public CommentGeneratorTest(CommentGenerator generatorUnderTest) throws Exception {
		super(generatorUnderTest);
	}

	@Override
	protected CommentStatement createSampleSqlStatement() {
		return new CommentStatement("comment text");
	}

}
