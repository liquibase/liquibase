package liquibase.parser.core.xml;

import org.junit.Test;

//@RunWith(PowerMockRunner.class)
public class ClassLoaderXsdStreamResolverTest {

    @Test
    public void placeHolder() {

    }

//    private static final String EXISTING_XSD_FILE = "liquibase/parser/core/xml/unused.xsd";
//    private static final String NON_EXISTING_XSD_FILE = "xsdFile";
//
//    @InjectMocks
//    private ClassLoaderXsdStreamResolver classLoaderXsdStreamResolver;
//
//    @Mock
//    private XsdStreamResolver successor;
//
//    @Mock
//    private ResourceAccessor resourceAccessor;
//
//    @Mock
//    private InputStream successorValue;
//
//    @Before
//    public void setUp() {
//        classLoaderXsdStreamResolver.setSuccessor(successor);
//
//        when(successor.getResourceAsStream(NON_EXISTING_XSD_FILE)).thenReturn(successorValue);
//    }
//
//    @Test
//    public void whenResourceStreamIsNotNullThenReturnStream() throws IOException {
//        InputStream returnValue = classLoaderXsdStreamResolver.getResourceAsStream(EXISTING_XSD_FILE);
//
//        assertThat(returnValue).isInstanceOf(InputStream.class);
//    }
//
//    @Test
//    public void whenContextClassLoaderIsNullThenReturnSuccessorValue() throws IOException {
//        InputStream returnValue = classLoaderXsdStreamResolver.getResourceAsStream(NON_EXISTING_XSD_FILE);
//
//        assertThat(returnValue).isSameAs(successorValue);
//    }

}
