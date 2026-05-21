package liquibase.integration.ant.type;

import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.junit.Test;

import java.util.Vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * CWE-316 regression tests for the credential-clearing mechanism added to
 * {@link DatabaseType}. We exercise the {@code clearCredentials()} method and
 * the {@code registerCredentialCleanup()} listener-registration directly,
 * including the refid traversal — running through a full Ant build with a real
 * database would require the broader Ant test harness, but the listener
 * contract (Project fires {@code buildFinished} after the last task) is
 * well-defined and tested by Ant itself.
 */
public class DatabaseTypeCredentialClearTest {

    @Test
    public void clearCredentials_nulls_password_on_a_simple_databaseType() {
        Project project = new Project();
        DatabaseType type = new DatabaseType(project);
        type.setUrl("jdbc:h2:mem:test");
        type.setUser("regular-user");
        type.setPassword("supersecret-pw-12345");

        type.clearCredentials();

        assertNull("password must be nulled", type.getPassword());
        assertEquals("user must remain set", "regular-user", type.getUser());
        assertEquals("url must remain set", "jdbc:h2:mem:test", type.getUrl());
    }

    @Test
    public void clearCredentials_traverses_refid_and_clears_the_referenced_instance() {
        // Simulate the typedef+databaseRef pattern: a referenced DatabaseType
        // (id="mydb") that holds the actual credential, plus a local "shell"
        // DatabaseType whose getters delegate to the referenced one. The
        // local shell's clearCredentials must follow the refid to the actual
        // shared instance so the password Strings on the referenced field
        // also become GC-eligible.
        Project project = new Project();
        DatabaseType referenced = new DatabaseType(project);
        referenced.setUrl("jdbc:h2:mem:shared");
        referenced.setUser("regular-user");
        referenced.setPassword("supersecret-pw-12345");
        project.addReference("mydb", referenced);

        DatabaseType shell = new DatabaseType(project);
        org.apache.tools.ant.types.Reference ref = new org.apache.tools.ant.types.Reference(project, "mydb");
        shell.setRefid(ref);

        // Sanity check: the shell's getPassword delegates to the referenced.
        assertEquals("supersecret-pw-12345", shell.getPassword());

        shell.clearCredentials();

        assertNull("referenced password must be nulled via refid traversal", referenced.getPassword());
        assertNull("shell's delegating getPassword must reflect the cleared value", shell.getPassword());
        assertEquals("user must remain set on the referenced", "regular-user", referenced.getUser());
    }

    @Test
    public void registerCredentialCleanup_registers_a_BuildListener_that_fires_on_buildFinished() {
        Project project = new Project();
        DatabaseType type = new DatabaseType(project);
        type.setPassword("supersecret-pw-12345");

        int listenerCountBefore = project.getBuildListeners().size();
        type.registerCredentialCleanup();
        int listenerCountAfter = project.getBuildListeners().size();

        assertEquals("registerCredentialCleanup must add exactly one BuildListener",
                listenerCountBefore + 1, listenerCountAfter);

        // Fire buildFinished and verify the password is nulled.
        project.fireBuildFinished(null);
        assertNull("password must be nulled when the Project fires buildFinished",
                type.getPassword());
    }

    @Test
    public void registerCredentialCleanup_is_idempotent() {
        // Multiple Ant tasks consuming the same DatabaseType (whether by
        // <database> nesting or by databaseRef) will each call createDatabase,
        // which in turn calls registerCredentialCleanup. Only the first call
        // should register a listener; subsequent calls must be no-ops.
        Project project = new Project();
        DatabaseType type = new DatabaseType(project);

        int listenerCountBefore = project.getBuildListeners().size();
        type.registerCredentialCleanup();
        type.registerCredentialCleanup();
        type.registerCredentialCleanup();
        int listenerCountAfter = project.getBuildListeners().size();

        assertEquals("registerCredentialCleanup must be idempotent on the same DatabaseType",
                listenerCountBefore + 1, listenerCountAfter);
    }

    @Test
    public void registerCredentialCleanup_listener_only_implements_buildFinished_meaningfully() {
        // The listener must not throw or have side effects on the non-credential
        // lifecycle events (targetStarted, taskStarted, etc.) — those fire during
        // the build and we cannot null the password yet.
        Project project = new Project();
        DatabaseType type = new DatabaseType(project);
        type.setPassword("supersecret-pw-12345");

        type.registerCredentialCleanup();

        Vector<BuildListener> listeners = project.getBuildListeners();
        BuildListener last = listeners.lastElement();
        assertNotNull(last);

        // Fire the during-build events; password must still be present.
        org.apache.tools.ant.BuildEvent event = new org.apache.tools.ant.BuildEvent(project);
        last.buildStarted(event);
        last.targetStarted(event);
        last.taskStarted(event);
        last.taskFinished(event);
        last.targetFinished(event);
        last.messageLogged(event);
        assertEquals("password must be retained during the build",
                "supersecret-pw-12345", type.getPassword());

        // Now fire buildFinished and verify clearing.
        last.buildFinished(event);
        assertNull("password must be nulled at buildFinished", type.getPassword());
    }

    @Test
    public void clearCredentials_is_safe_on_a_refid_shell_whose_referenced_was_dropped() {
        // Defensive: if the project tear-down removed the reference before our
        // BuildListener fires (unusual but possible), clearCredentials must not
        // throw — it should still null its own (empty) field cleanly.
        Project project = new Project();
        DatabaseType shell = new DatabaseType(project);
        org.apache.tools.ant.types.Reference ref =
                new org.apache.tools.ant.types.Reference(project, "no-such-ref");
        shell.setRefid(ref);

        // No exception expected — clearCredentials catches BuildException from getCheckedRef.
        shell.clearCredentials();
        assertTrue("clearCredentials must not throw on a dangling refid", true);
    }

    @Test
    public void registerCredentialCleanup_listener_deregisters_itself_after_firing() {
        // CWE-316 regression for the long-lived embedded Ant case (per @wwillard7800's
        // review on #7743). In one-shot Ant CLI builds the Project is GC'd after the
        // build and any attached listeners go with it. In embedded hosts that reuse a
        // single Project across many builds (build daemons, IDE plugins, MPS-style
        // tools), each new DatabaseType would otherwise add a permanent listener that
        // holds a strong reference back to the (now-cleared) DatabaseType — defeating
        // the residency-window goal this PR series is trying to achieve. The listener
        // must deregister itself from the Project once buildFinished fires.
        Project project = new Project();
        int listenerCountBefore = project.getBuildListeners().size();

        DatabaseType type = new DatabaseType(project);
        type.setPassword("supersecret-pw-12345");
        type.registerCredentialCleanup();

        // Sanity: the listener was registered.
        assertEquals("registerCredentialCleanup must add exactly one BuildListener",
                listenerCountBefore + 1, project.getBuildListeners().size());

        // Fire buildFinished — listener should run clearCredentials AND remove itself.
        project.fireBuildFinished(null);

        assertNull("password must be nulled when buildFinished fires", type.getPassword());
        assertEquals("listener must deregister itself from the Project after firing",
                listenerCountBefore, project.getBuildListeners().size());
    }

    @Test
    public void multiple_databaseTypes_all_deregister_their_listeners_in_embedded_scenario() {
        // CWE-316 regression for accumulation (per @wwillard7800): in a long-lived
        // embedded Ant host that reuses one Project across N builds, N DatabaseType
        // instances should result in zero residual listeners after all builds finish —
        // not N listeners stuck holding references to empty DatabaseType shells.
        Project project = new Project();
        int listenerCountBefore = project.getBuildListeners().size();

        DatabaseType firstBuild  = new DatabaseType(project);
        DatabaseType secondBuild = new DatabaseType(project);
        DatabaseType thirdBuild  = new DatabaseType(project);
        firstBuild.registerCredentialCleanup();
        secondBuild.registerCredentialCleanup();
        thirdBuild.registerCredentialCleanup();

        assertEquals("three DatabaseType instances must register three distinct listeners",
                listenerCountBefore + 3, project.getBuildListeners().size());

        // One buildFinished cycle drains all three at once.
        project.fireBuildFinished(null);

        assertEquals("all three listeners must deregister themselves — no accumulation",
                listenerCountBefore, project.getBuildListeners().size());
    }

    @Test
    public void clearCredentials_wipes_credential_bearing_connectionProperty_values() {
        // CWE-316 regression for @filipelautert's review gap on #7743: a build that
        // uses <connectionProperty name="password" value="hunter2"/> instead of (or in
        // addition to) <password> would otherwise leave the raw value in the Property
        // list past buildFinished. clearCredentials() must walk connectionProperties
        // and overwrite credential-bearing values.
        Project project = new Project();
        DatabaseType type = new DatabaseType(project);
        type.setUrl("jdbc:h2:mem:test");
        type.setUser("regular-user");

        ConnectionProperties props = new ConnectionProperties();
        org.apache.tools.ant.taskdefs.Property credentialProp = new org.apache.tools.ant.taskdefs.Property();
        credentialProp.setProject(project);
        credentialProp.setName("password");
        credentialProp.setValue("hunter2");
        props.addConnectionProperty(credentialProp);

        org.apache.tools.ant.taskdefs.Property nonCredentialProp = new org.apache.tools.ant.taskdefs.Property();
        nonCredentialProp.setProject(project);
        nonCredentialProp.setName("connectTimeout");
        nonCredentialProp.setValue("30000");
        props.addConnectionProperty(nonCredentialProp);

        type.addConnectionProperties(props);

        type.clearCredentials();

        assertEquals("credential-bearing <connectionProperty> value must be wiped",
                "*****", credentialProp.getValue());
        assertEquals("non-credential <connectionProperty> value must pass through unchanged",
                "30000", nonCredentialProp.getValue());
    }

    @Test
    public void clearCredentials_matches_credential_property_names_case_insensitively_locale_safe() {
        // CWE-316 regression: case-insensitive substring matching must use
        // Locale.ROOT — the Turkish "I" → "ı" quirk would otherwise let
        // <connectionProperty name="APIKEY" .../> escape the sweep on a JVM
        // whose default locale is Turkish. Also exercises a few common
        // credential-naming variants to lock the denylist coverage in.
        Project project = new Project();
        DatabaseType type = new DatabaseType(project);
        type.setUrl("jdbc:h2:mem:test");

        ConnectionProperties props = new ConnectionProperties();
        org.apache.tools.ant.taskdefs.Property[] credentialProps = new org.apache.tools.ant.taskdefs.Property[]{
                makeProp(project, "PASSWORD",            "uppercase-pw"),
                makeProp(project, "Passwd",              "mixed-pw"),
                makeProp(project, "APIKEY",              "uppercase-i-key"),
                makeProp(project, "client_secret",       "embedded-secret"),
                makeProp(project, "accessKey",           "aws-key"),
                makeProp(project, "awsCredentials",      "creds-blob"),
                makeProp(project, "ldap.bearerToken",    "embedded-token-name"),
        };
        for (org.apache.tools.ant.taskdefs.Property p : credentialProps) {
            props.addConnectionProperty(p);
        }
        // A non-credential property among them to verify no over-masking.
        org.apache.tools.ant.taskdefs.Property nonCred = makeProp(project, "fetchSize", "1000");
        props.addConnectionProperty(nonCred);

        // Set Turkish locale for the duration of the call so the Locale.ROOT
        // guarantee is actually exercised (a bare String.toLowerCase() would
        // turn "APIKEY" into "apıkey" with dotless ı under tr-TR and miss
        // the "apikey" token).
        java.util.Locale savedDefault = java.util.Locale.getDefault();
        try {
            java.util.Locale.setDefault(java.util.Locale.forLanguageTag("tr-TR"));
            type.addConnectionProperties(props);
            type.clearCredentials();
        } finally {
            java.util.Locale.setDefault(savedDefault);
        }

        for (org.apache.tools.ant.taskdefs.Property p : credentialProps) {
            assertEquals("credential-named property '" + p.getName() + "' must be wiped",
                    "*****", p.getValue());
        }
        assertEquals("non-credential property must pass through",
                "1000", nonCred.getValue());
    }

    @Test
    public void clearCredentials_traverses_refid_and_wipes_referenced_connectionProperty_values() {
        // CWE-316 regression: the typedef+databaseRef pattern shares one
        // DatabaseType (with its connectionProperties) across many tasks. The
        // refid traversal in clearCredentials must reach the referenced
        // ConnectionProperties so the actual shared Property list is wiped.
        Project project = new Project();
        DatabaseType referenced = new DatabaseType(project);
        referenced.setUrl("jdbc:h2:mem:shared");

        ConnectionProperties props = new ConnectionProperties();
        org.apache.tools.ant.taskdefs.Property pw = makeProp(project, "password", "shared-pw");
        props.addConnectionProperty(pw);
        referenced.addConnectionProperties(props);

        project.addReference("mydb", referenced);

        DatabaseType shell = new DatabaseType(project);
        org.apache.tools.ant.types.Reference ref = new org.apache.tools.ant.types.Reference(project, "mydb");
        shell.setRefid(ref);

        shell.clearCredentials();

        assertEquals("referenced <connectionProperty name=\"password\"> must be wiped via refid traversal",
                "*****", pw.getValue());
    }

    private static org.apache.tools.ant.taskdefs.Property makeProp(Project project, String name, String value) {
        org.apache.tools.ant.taskdefs.Property p = new org.apache.tools.ant.taskdefs.Property();
        p.setProject(project);
        p.setName(name);
        p.setValue(value);
        return p;
    }

    @Test
    public void registerCredentialCleanup_resets_idempotency_flag_so_subsequent_builds_re_register() {
        // CWE-316 regression for @coderabbitai's review on #7743: the one-shot
        // BuildListener calls removeBuildListener(this) at buildFinished but
        // (pre-fix) never reset credentialCleanupRegistered. In long-lived
        // embedded hosts that reuse one DatabaseType across multiple builds,
        // build 1's credentials would be cleared but build 2's registerCredentialCleanup
        // would early-return on the stale flag — no listener registered — and
        // build 2's credentials would silently survive.
        Project project = new Project();
        int listenerCountBefore = project.getBuildListeners().size();

        DatabaseType type = new DatabaseType(project);

        // ----- Build 1 cycle -----
        type.setPassword("build-1-pw");
        type.registerCredentialCleanup();
        assertEquals("build 1: listener attached", listenerCountBefore + 1, project.getBuildListeners().size());

        project.fireBuildFinished(null);
        assertNull("build 1: password cleared at buildFinished", type.getPassword());
        assertEquals("build 1: listener detached at buildFinished", listenerCountBefore, project.getBuildListeners().size());

        // ----- Build 2 cycle on the SAME DatabaseType instance -----
        // This is the case CodeRabbit flagged. Without the idempotency-flag
        // reset, registerCredentialCleanup() would early-return on the stale
        // flag and the new password would survive past buildFinished.
        type.setPassword("build-2-pw");
        type.registerCredentialCleanup();
        assertEquals("build 2: listener re-attached (idempotency flag was reset)",
                listenerCountBefore + 1, project.getBuildListeners().size());

        project.fireBuildFinished(null);
        assertNull("build 2: password cleared at buildFinished", type.getPassword());
        assertEquals("build 2: listener detached at buildFinished", listenerCountBefore, project.getBuildListeners().size());

        // ----- Build 3 cycle (one more, to lock the contract for N builds) -----
        type.setPassword("build-3-pw");
        type.registerCredentialCleanup();
        project.fireBuildFinished(null);
        assertNull("build 3: password cleared", type.getPassword());
        assertEquals("build 3: listener detached", listenerCountBefore, project.getBuildListeners().size());
    }
}
