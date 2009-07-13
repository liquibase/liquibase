using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace LiquiBase.Resource {
    class ClrResourceAccessor : liquibase.resource.ResourceAccessor {

        public java.io.InputStream getResourceAsStream(string str) {
            throw new NotImplementedException();
        }

        public java.util.Enumeration getResources(string str) {
            throw new NotImplementedException();
        }

        public java.lang.ClassLoader toClassLoader() {
            throw new NotImplementedException();
        }

    }
}
