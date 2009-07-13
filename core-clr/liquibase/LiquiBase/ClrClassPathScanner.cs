using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using liquibase.util.plugin;
using System.Reflection;
using liquibase.resource;
using java.lang;

namespace LiquiBase {
    public class ClrClassPathScanner : ClassPathScanner {


        public ClrClassPathScanner() : base(null) {
        }

        public override void setResourceAccessor(liquibase.resource.ResourceAccessor resourceAccessor) {
        }

        public override java.lang.Class[] getClasses(java.lang.Class requiredInterface) {
            //Console.WriteLine("looking up " + requiredInterface.getName());
            List<Class> returnClasses = new List<Class>();

            Type requiredType = Type.GetType(requiredInterface.getName()+", liquibase-core");
            if (requiredType == null) {
                Console.WriteLine("could not find required type for " + requiredInterface.getName());
                return new java.lang.Class[0];
            }

            Assembly assembly = Assembly.GetExecutingAssembly();
            foreach (Type type in assembly.GetTypes()) {
                //if (type.AssemblyQualifiedName.Contains("LiquiBase")) {
                //    Console.WriteLine("type: " + type);
                //}
                if (!type.IsInterface && !type.IsAbstract && requiredType.IsAssignableFrom(type)) {
                    //Console.WriteLine("Adding class " + type.AssemblyQualifiedName);
                    returnClasses.Add(Class.forName(type.AssemblyQualifiedName));
                }
            }

            foreach (AssemblyName refAsm in assembly.GetReferencedAssemblies()) {

                foreach (Type type in Assembly.Load(refAsm).GetTypes()) {
                    //if (type.AssemblyQualifiedName.Contains("LiquiBase")) {
                    //    Console.WriteLine("type: " + type); 
                    //}
                    if (!type.IsInterface && !type.IsAbstract && requiredType.IsAssignableFrom(type)) {
                        //Console.WriteLine("Adding class " + type.AssemblyQualifiedName);
                        returnClasses.Add(Class.forName(type.AssemblyQualifiedName));
                    }
                }
            }

            return returnClasses.ToArray();

        }
    }
}
