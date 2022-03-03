package liquibase.util

import liquibase.ExtensibleObject

import java.lang.reflect.Modifier

/**
 * Test-centric utility methods
 */
public abstract class TestUtil {

    def static Map<Class, SortedSet<Class>> allClasses

    /**
     * Creates all permutations of a given class without adding additional null values
     */
    public static <T extends ExtensibleObject> List<T> createAllPermutationsWithoutNulls(Class<T> type, Map<String, List<Object>> defaultValues) throws Exception {
        return createAllPermutations(type, defaultValues, false)
    }

    public static <T extends ExtensibleObject> List<T> createAllPermutations(Class<T> type, Map<String, List<Object>> defaultValues, boolean addNulls = true) throws Exception {
        List<T> returnList = new ArrayList<>();
        for (Map<String, Object> parameterValues : CollectionUtil.permutations(defaultValues, addNulls)) {
            T obj = type.getConstructor().newInstance();
            for (Map.Entry<String, ?> entry : parameterValues.entrySet()) {
                if (obj.getObjectMetaData().getAttribute(entry.getKey()) == null) {
                    throw new RuntimeException("No attribute "+entry.getKey()+" on "+type.getName())
                }
                obj.set(entry.getKey(), entry.getValue());
            }
            returnList.add(obj);
        }

        return returnList;
    }


    static SortedSet<Class> getClasses(Class baseClass) {
        if (allClasses == null) {
            allClasses = [:]
            findAllClasses()
        }

        return allClasses.get(baseClass)
    }


    private static findAllClasses() {
        def workingDir = new File(".")

        def startDir = new File(workingDir, "target/classes")
        startDir.traverse {
            if (!it.isFile()) {
                return
            }

            def file = it
            if (file.name.endsWith(".class")) {
                if (file.name.contains('$_$') || file.name.find(/_closure\d+/)) { // a groovy closure
                    return;
                }
                if (file.name.contains("Abstract")) {
                    return;
                }

                def className = file.absolutePath.replace(startDir.absolutePath, "")
                        .replace("\\", ".")
                        .replace("/", ".")
                        .replaceFirst(/\.class$/, "")
                        .replaceFirst(/^\./, "")
                Class clazz = Class.forName(className);

                if (!isValidClass(clazz)) {
                    return;
                }

                Class superClass = clazz.superclass;
                Set<Class> interfaces = createClassSortedSet()
                addInterfaces(clazz, interfaces)

                while (superClass != null && !superClass.equals(Object.class)) {
                    def classList = allClasses.get(superClass)
                    if (classList == null) {
                        classList = createClassSortedSet();
                        allClasses.put(superClass, classList)
                    }
                    allClasses.get(superClass).add(clazz)
                    addInterfaces(superClass, interfaces)

                    superClass = superClass.superclass
                }

                for (def iface : interfaces) {
                    def classList = allClasses.get(iface)
                    if (classList == null) {
                        classList = createClassSortedSet();
                        allClasses.put(iface, classList)
                    }
                    classList.add(clazz);
                }
            }
        }
    }

    private static TreeSet createClassSortedSet() {
        new TreeSet<>(new Comparator<Class>() {
            @Override
            int compare(Class o1, Class o2) {
                return o1.getName().compareTo(o2.getName())
            }
        })
    }

    private static addInterfaces(Class<?> clazz, Set<Class> interfaces) {
        def thisInterfaces = clazz.interfaces
        if (thisInterfaces.size() > 0) {
            for (Class iface : thisInterfaces) {
                interfaces.add(iface)
                addInterfaces(iface, interfaces)
            }
        }

    }

    private static isValidClass(Class<?> clazz) {
        try {
            if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()) || clazz.isSynthetic() || clazz.isAnonymousClass()) {
                return false;
            }
        } catch (NoClassDefFoundError e) {
            println "Error with "+clazz.name
            throw e
        }

        return true;
    }

}
