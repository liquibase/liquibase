package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * CWE-316 regression tests for the credential-field nulling added to
 * {@link AbstractLiquibaseMojo} and its {@link LiquibaseDatabaseDiff} subclass.
 * <p>
 * The cleanup is invoked from {@link AbstractLiquibaseMojo#execute()}'s finally
 * block; here we test the {@code clearCredentialFields()} hook directly to avoid
 * pulling in the full Maven-plugin test harness for a memory-hygiene assertion.
 * The runtime wiring (the call from {@code execute()}'s finally) is verified by
 * code review plus Java's {@code finally} semantics.
 */
public class AbstractLiquibaseMojoCredentialClearTest {

    /**
     * Bare concrete subclass of {@link AbstractLiquibaseMojo} — supplies the
     * one abstract method so the class is instantiable. We do not invoke
     * {@code execute()} here; we only need a valid concrete subclass to exercise
     * {@code clearCredentialFields()}.
     */
    private static final class TestMojo extends AbstractLiquibaseMojo {
        @Override
        protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
            // not invoked in these tests
        }
    }

    @Test
    public void clearCredentialFields_nulls_password_but_leaves_other_fields_intact() {
        TestMojo mojo = new TestMojo();
        mojo.password = "supersecret-pw-12345";
        mojo.username = "regular-user";
        mojo.url = "jdbc:postgresql://host:5432/db";
        mojo.driver = "org.postgresql.Driver";

        mojo.clearCredentialFields();

        assertNull("password must be nulled", mojo.password);
        assertEquals("username must remain set", "regular-user", mojo.username);
        assertEquals("url must remain set", "jdbc:postgresql://host:5432/db", mojo.url);
        assertEquals("driver must remain set", "org.postgresql.Driver", mojo.driver);
    }

    @Test
    public void clearCredentialFields_is_idempotent_when_password_already_null() {
        TestMojo mojo = new TestMojo();
        mojo.password = null;

        mojo.clearCredentialFields();

        assertNull(mojo.password);
    }

    @Test
    public void liquibaseDatabaseDiff_override_nulls_password_and_referencePassword() {
        LiquibaseDatabaseDiff mojo = new LiquibaseDatabaseDiff();
        mojo.password = "supersecret-pw-12345";
        mojo.referencePassword = "supersecret-ref-67890";
        mojo.username = "regular-user";
        mojo.url = "jdbc:postgresql://host:5432/db";

        mojo.clearCredentialFields();

        assertNull("password must be nulled by super.clearCredentialFields()", mojo.password);
        assertNull("referencePassword must be nulled by the override", mojo.referencePassword);
        assertEquals("username must remain set", "regular-user", mojo.username);
        assertEquals("url must remain set", "jdbc:postgresql://host:5432/db", mojo.url);
    }
}
