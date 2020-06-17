package liquibase.integration.cdi;

import com.google.common.base.Strings;
import liquibase.integration.cdi.annotations.LiquibaseSchema;
import liquibase.integration.cdi.exceptions.CyclicDependencyException;
import liquibase.integration.cdi.exceptions.DependencyNotFoundException;
import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Nikita Lipatov (https://github.com/islonik), antoermo (https://github.com/dikeert)
 * @since 27/5/17.
 */
public class SchemesTreeBuilderTest {

    @Test(expected = DependencyNotFoundException.class)
    public void testMissingDependencies() throws Exception {
        Collection<LiquibaseSchema> schemes = getWithAbsentDependency();
        new SchemesTreeBuilder().build(UUID.randomUUID().toString(), schemes);
    }

    @Test(expected = CyclicDependencyException.class)
    public void testShortCyclic() throws Exception {
        Collection<LiquibaseSchema> liquibaseSchemas = getShortCyclic();
        new SchemesTreeBuilder().build(UUID.randomUUID().toString(), liquibaseSchemas);
    }

    @Test(expected = CyclicDependencyException.class)
    public void testMediumCyclic() throws Exception {
        Collection<LiquibaseSchema> liquibaseSchemas = getMediumCyclic();
        new SchemesTreeBuilder().build(UUID.randomUUID().toString(), liquibaseSchemas);
    }

    @Test(expected = CyclicDependencyException.class)
    public void testLongCyclic() throws Exception {
        Collection<LiquibaseSchema> liquibaseSchemas = getLongCyclic();
        new SchemesTreeBuilder().build(UUID.randomUUID().toString(), liquibaseSchemas);
    }

    @Test
    public void testWithDependencies() throws Exception {
        Collection<LiquibaseSchema> liquibaseSchemas = getDependent();

        Collection<LiquibaseSchema> resolved = new SchemesTreeBuilder().build(UUID.randomUUID().toString(), liquibaseSchemas);

        assertEquals(liquibaseSchemas.size(), resolved.size());

        Collection<LiquibaseSchema> previous = new ArrayList<LiquibaseSchema>(resolved.size());

        for (LiquibaseSchema liquibaseSchema : resolved) {
            if (!Strings.isNullOrEmpty(liquibaseSchema.depends())) {
                assertFalse(isDependencyMissed(liquibaseSchema, previous));
            }
            previous.add(liquibaseSchema);
        }
        Assert.assertEquals(9, previous.size());
    }

    @Test
    public void testMixed() throws Exception {
        Collection<LiquibaseSchema> schemes = new ArrayList<LiquibaseSchema>() {{
            addAll(getNonDependent());
            addAll(getDependent());
        }};

        Collection<LiquibaseSchema> resolved = new SchemesTreeBuilder().build(UUID.randomUUID().toString(), schemes);

        assertEquals(schemes.size(), resolved.size());

        for (LiquibaseSchema liquibaseSchema : schemes) {
            assertTrue(resolved.contains(liquibaseSchema));
        }

        Collection<LiquibaseSchema> previous = new ArrayList<LiquibaseSchema>(resolved.size());
        for (LiquibaseSchema liquibaseSchema : resolved) {
            if (!Strings.isNullOrEmpty(liquibaseSchema.depends())) {
                assertFalse(isDependencyMissed(liquibaseSchema, previous));
            }
            previous.add(liquibaseSchema);
        }
        Assert.assertEquals(16, previous.size());
    }

    private boolean isDependencyMissed(LiquibaseSchema liquibaseSchema, Collection<LiquibaseSchema> previous) {
        boolean isDependencyMissed = true;
        for (LiquibaseSchema tmp: previous) {
            if (tmp.name().equalsIgnoreCase(liquibaseSchema.depends())) {
                isDependencyMissed = false;
                break;
            }
        }
        return isDependencyMissed;
    }

    @Test
    public void testNoDependencies() throws Exception {
        Collection<LiquibaseSchema> liquibaseSchemas = getNonDependent();

        Collection<LiquibaseSchema> resolved = new SchemesTreeBuilder().build(UUID.randomUUID().toString(), liquibaseSchemas);

        assertEquals(liquibaseSchemas.size(), resolved.size());
        for (LiquibaseSchema liquibaseSchema : liquibaseSchemas) {
            assertTrue(resolved.contains(liquibaseSchema));
        }
        Assert.assertEquals(7, resolved.size());
    }

    private Collection<LiquibaseSchema> getWithAbsentDependency() {
        return list2Coll(Arrays.asList("A", "B", "C", "D", "E", "G", "H", "I"));
    }

    private Collection<LiquibaseSchema> getNonDependent() {
        return list2Coll(Arrays.asList("J", "K", "L", "M", "N", "O", "P"));
    }

    private Collection<LiquibaseSchema> getDependent() {
        return list2Coll(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I"));
    }

    private Collection<LiquibaseSchema> getShortCyclic() {
        return list2Coll(Arrays.asList("R", "Q"));
    }

    private Collection<LiquibaseSchema> getMediumCyclic() {
        return list2Coll(Arrays.asList("S", "T", "U"));
    }

    private Collection<LiquibaseSchema> getLongCyclic() {
        return list2Coll(Arrays.asList("Z", "W", "X", "Y", "ROOT"));
    }

    private Collection<LiquibaseSchema> list2Coll(List<String> list) {
        List<String> cdiList = new ArrayList<String>();
        for (String s : list) {
            cdiList.add(String.format("liquibase.integration.cdi.%s", s));
        }
        List<Class> classesList = new ArrayList<Class>();
        for (String s : cdiList) {
            classesList.add(this.getClass(s));
        }
        Set<Annotation> annotationsSet = new LinkedHashSet<Annotation>();
        for (Class clazz : classesList) {
            annotationsSet.add(clazz.getAnnotation(LiquibaseSchema.class));
        }
        List<LiquibaseSchema> liquibaseSchemaList = new ArrayList<LiquibaseSchema>();
        for (Annotation ann : annotationsSet) {
            liquibaseSchemaList.add((LiquibaseSchema) ann);
        }
        return liquibaseSchemaList;
    }

    private Class getClass(String s) {
        try {
            return Class.forName(s);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}


//with dependencies
@LiquibaseSchema(name = "A", resource = "A", depends = "I")
class A {
}

@LiquibaseSchema(name = "B", resource = "B", depends = "C")
class B {
}

@LiquibaseSchema(name = "C", resource = "C", depends = "D")
class C {
}

@LiquibaseSchema(name = "D", resource = "D", depends = "E")
class D {
}

@LiquibaseSchema(name = "E", resource = "E", depends = "F")
class E {
}

@LiquibaseSchema(name = "F", resource = "F", depends = "G")
class F {
}

@LiquibaseSchema(name = "G", resource = "G", depends = "H")
class G {
}

@LiquibaseSchema(name = "H", resource = "H", depends = "A")
class H {
}

@LiquibaseSchema(name = "I", resource = "I")
class I {
}

//not dependent
@LiquibaseSchema(name = "J", resource = "J")
class J {
}

@LiquibaseSchema(name = "K", resource = "K")
class K {
}

@LiquibaseSchema(name = "L", resource = "L")
class L {
}

@LiquibaseSchema(name = "M", resource = "M")
class M {
}

@LiquibaseSchema(name = "N", resource = "N")
class N {
}

@LiquibaseSchema(name = "O", resource = "O")
class O {
}

@LiquibaseSchema(name = "P", resource = "P")
class P {
}


//short cyclic
@LiquibaseSchema(name = "Q", resource = "Q", depends = "R")
class Q {
}

@LiquibaseSchema(name = "R", resource = "R", depends = "Q")
class R {
}

//medium cyclic
@LiquibaseSchema(name = "S", resource = "S", depends = "U")
class S {
}

@LiquibaseSchema(name = "T", resource = "T", depends = "T")
class T {
}

@LiquibaseSchema(name = "U", resource = "U", depends = "S")
class U {
}

//long cyclic
@LiquibaseSchema(name = "V", resource = "V", depends = "Z")
class V {
}

@LiquibaseSchema(name = "W", resource = "W", depends = "W")
class W {
}

@LiquibaseSchema(name = "X", resource = "X", depends = "X")
class X {
}

@LiquibaseSchema(name = "Y", resource = "Y", depends = "Y")
class Y {
}

@LiquibaseSchema(name = "Z", resource = "Z", depends = "ROOT")
class Z {
}

@LiquibaseSchema(name = "ROOT", resource = "ROOT")
class ROOT {
}