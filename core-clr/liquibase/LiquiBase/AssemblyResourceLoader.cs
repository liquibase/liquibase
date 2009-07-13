using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Liquibase.Resource {
    class AssemblyResourceLoader : liquibase.resource.ResourceAccessor {

        #region ResourceAccessor Members

        public java.io.InputStream getResourceAsStream(string str) {
            throw new NotImplementedException();
        }

        public java.util.Enumeration getResources(string str) {
            throw new NotImplementedException();
        }

        public java.lang.ClassLoader toClassLoader() {
            throw new NotImplementedException();
        }

        #endregion
    }
}
