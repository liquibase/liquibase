package liquibase.parser.core.xml;

import org.junit.Test;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest(StreamUtil.class)
public class ResourceAccessorXsdStreamResolverTest {

    @Test
    public void placeHolder() {

    }
//	private static final String XSD_FILE = "xsdFile";
//
//	@InjectMocks
//	private ResourceAccessorXsdStreamResolver resourceAccessorXsdStreamResolver;
//
//	@Mock
//	private XsdStreamResolver successor;
//
//	@Mock
//	private ResourceAccessor resourceAccessor;
//
//	@Mock
//	private InputStream inputStream, successorValue;
//
//	@Before
//	public void setUp() throws IOException {
//		PowerMockito.mockStatic(StreamUtil.class);
//
//		resourceAccessorXsdStreamResolver.setSuccessor(successor);
//
//		when(successor.getResourceAsStream(XSD_FILE)).thenReturn(successorValue);
//	}
//
//	@Test
//	public void whenResourceStreamIsNotNullThenReturnStream() throws IOException {
//		when(StreamUtil.singleInputStream(XSD_FILE, resourceAccessor)).thenReturn(inputStream);
//
//		InputStream returnValue = resourceAccessorXsdStreamResolver.getResourceAsStream(XSD_FILE);
//
//		assertThat(returnValue).isSameAs(inputStream);
//	}
//
//	@Test
//	public void whenResourceStreamIsNullThenReturnSuccessorValue() throws IOException {
//		when(StreamUtil.singleInputStream(XSD_FILE, resourceAccessor)).thenReturn(null);
//
//		InputStream returnValue = resourceAccessorXsdStreamResolver.getResourceAsStream(XSD_FILE);
//
//		assertThat(returnValue).isSameAs(successorValue);
//	}
//
//	@Test
//	public void whenIOExceptionOccursThenReturnSuccessorValue() throws IOException {
//		when(StreamUtil.singleInputStream(XSD_FILE, resourceAccessor)).thenThrow(new IOException());
//
//		InputStream returnValue = resourceAccessorXsdStreamResolver.getResourceAsStream(XSD_FILE);
//
//		assertThat(returnValue).isSameAs(successorValue);
//	}

}
