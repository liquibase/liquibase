using LiquiBase;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using liquibase.database;
using LiquiBase.Database;

namespace liquibase_test.Database
{
    
    
    /// <summary>
    ///This is a test class for CsConnectionTest and is intended
    ///to contain all CsConnectionTest Unit Tests
    ///</summary>
    [TestClass()]
    public class AdoConnectionTest {


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
        //You can use the following additional attributes as you write your tests:
        //
        //Use ClassInitialize to run code before running the first test in the class
        //[ClassInitialize()]
        //public static void MyClassInitialize(TestContext testContext)
        //{
        //}
        //
        //Use ClassCleanup to run code after all tests in a class have run
        //[ClassCleanup()]
        //public static void MyClassCleanup()
        //{
        //}
        //
        //Use TestInitialize to run code before running each test
        //[TestInitialize()]
        //public void MyTestInitialize()
        //{
        //}
        //
        //Use TestCleanup to run code after each test has run
        //[TestCleanup()]
        //public void MyTestCleanup()
        //{
        //}
        //
        #endregion


        /// <summary>
        ///A test for CsConnection Constructor
        ///</summary>
        [TestMethod()]
        public void CsConnectionConstructorTest() {
            AdoConnection target = new AdoConnection();

            Assert.IsNotNull(target.GetUnderlyingConnection());
        }

        /// <summary>
        ///A test for liquibase.database.DatabaseConnection.rollback
        ///</summary>
        [TestMethod()]
        [DeploymentItem("liquibase.dll")]
        public void rollbackTest() {
            DatabaseConnection target = new AdoConnection(); 
            target.rollback();
        }

        /// <summary>
        ///A test for liquibase.database.DatabaseConnection.commit
        ///</summary>
        [TestMethod()]
        [DeploymentItem("liquibase.dll")]
        public void commitTest() {
            DatabaseConnection target = new AdoConnection();
            target.commit();
        }

        /// <summary>
        ///A test for liquibase.database.DatabaseConnection.close
        ///</summary>
        [TestMethod()]
        [DeploymentItem("liquibase.dll")]
        public void closeTest() {
            DatabaseConnection target = new AdoConnection();
            target.close();
        }

        /// <summary>
        ///A test for liquibase.database.DatabaseConnection.getURL
        ///</summary>
        [TestMethod()]
        [DeploymentItem("liquibase.dll")]
        public void getURLTest() {
            DatabaseConnection target = new AdoConnection();
            Assert.AreEqual("Provider=SQLOLEDB;Data Source=localhost\\SQL2005;Initial Catalog=liquibase;User Id=liquibase;", target.getURL());
        }

        [TestMethod()]
        [DeploymentItem("liquibase.dll")]
        public void getProductNameTest() {
            DatabaseConnection target = new AdoConnection();
            Assert.AreEqual("asdf", target.getDatabaseProductName());
        }

    }
}
