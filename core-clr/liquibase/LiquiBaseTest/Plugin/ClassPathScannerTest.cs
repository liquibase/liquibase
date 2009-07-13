using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using liquibase.util.plugin;
using LiquiBase.Database;
using java.lang;
using liquibase.database;

namespace liquibase_test.Plugin {
    /// <summary>
    /// Summary description for UnitTest1
    /// </summary>
    [TestClass]
    public class ClassPathScannerTest {
        public ClassPathScannerTest() {
            //
            // TODO: Add constructor logic here
            //
        }

        private TestContext testContextInstance;

        /// <summary>
        ///Gets or sets the test context which provides
        ///information about and functionality for the current test run.
        ///</summary>
        public TestContext TestContext {
            get {
                return testContextInstance;
            }
            set {
                testContextInstance = value;
            }
        }

        #region Additional test attributes
        //
        // You can use the following additional attributes as you write your tests:
        //
        // Use ClassInitialize to run code before running the first test in the class
        // [ClassInitialize()]
        // public static void MyClassInitialize(TestContext testContext) { }
        //
        // Use ClassCleanup to run code after all tests in a class have run
        // [ClassCleanup()]
        // public static void MyClassCleanup() { }
        //
        // Use TestInitialize to run code before running each test 
        // [TestInitialize()]
        // public void MyTestInitialize() { }
        //
        // Use TestCleanup to run code after each test has run
        // [TestCleanup()]
        // public void MyTestCleanup() { }
        //
        #endregion

        [TestMethod]
        public void getClasses() {
            ClassPathScanner scanner = ClassPathScanner.getInstance();
            Class console = Class.forName(typeof(DatabaseConnection).AssemblyQualifiedName);
            Assert.IsNotNull(console);
            Assert.AreEqual("liquibase.database.DatabaseConnection", console.getName());


            Class[] classes = scanner.getClasses(console);
            Assert.AreEqual(1, classes.Length);
        }
    }
}
