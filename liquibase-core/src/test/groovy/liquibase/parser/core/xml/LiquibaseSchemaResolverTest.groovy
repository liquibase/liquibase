package liquibase.parser.core.xml;

import org.junit.Test;

public class LiquibaseSchemaResolverTest {

//	private static final String SYSTEM_ID = "systemId";
//	private static final String PUBLIC_ID = "publicId";
//	private static final String XSD_FILE = "xsdFile";
//
//	private LiquibaseSchemaResolver liquibaseSchemaResolver;
//
//	@Mock
//	private LiquibaseParser liquibaseParser;
//
//	@Mock
//	private LiquibaseSerializer liquibaseSerializer;
//
//	@Mock
//	private NamespaceDetailsFactory namespaceDetailsFactory;
//
//	@Mock
//	private NamespaceDetails nameSpaceDetialsForParser, namespaceDetailsForSerializer;
//
//	@Mock
//	private ResourceAccessorXsdStreamResolver resourceAccessorXsdStreamResolver;
//
//	@Mock
//	private InputStream inputStream;
//
//	@Mock
//	private ResourceAccessor resourceAccessor;
//
//	@Before
//	public void setUp() {
//		PowerMockito.mockStatic(NamespaceDetailsFactory.class);
//
//		liquibaseSchemaResolver = new LiquibaseSchemaResolver(SYSTEM_ID, PUBLIC_ID, resourceAccessor);
//		Whitebox.setInternalState(liquibaseSchemaResolver, "resourceAccessorXsdStreamResolver", resourceAccessorXsdStreamResolver);
//
//		when(NamespaceDetailsFactory.getInstance()).thenReturn(namespaceDetailsFactory);
//
//		when(namespaceDetailsFactory.getNamespaceDetails(liquibaseParser, SYSTEM_ID)).thenReturn(nameSpaceDetialsForParser);
//		when(namespaceDetailsFactory.getNamespaceDetails(liquibaseSerializer, SYSTEM_ID)).thenReturn(namespaceDetailsForSerializer);
//
//		when(nameSpaceDetialsForParser.getLocalPath(SYSTEM_ID)).thenReturn(XSD_FILE);
//		when(namespaceDetailsForSerializer.getLocalPath(SYSTEM_ID)).thenReturn(XSD_FILE);
//
//		when(resourceAccessorXsdStreamResolver.getResourceAsStream(XSD_FILE)).thenReturn(inputStream);
//	}
//
//	@Test
//	public void shouldReturnInputSourceWhenResourceAsStreamFound() {
//		InputSource inputSource = liquibaseSchemaResolver.resolve(liquibaseParser);
//
//		assertThat(inputSource.getByteStream()).isEqualTo(inputStream);
//		assertThat(inputSource.getPublicId()).isEqualTo(PUBLIC_ID);
//		assertThat(inputSource.getPublicId()).isEqualTo(PUBLIC_ID);
//	}
//
//	@Test
//	public void shouldReturnNullWhenNoResourceAsStreamFound() {
//		when(resourceAccessorXsdStreamResolver.getResourceAsStream(XSD_FILE)).thenReturn(null);
//
//		InputSource inputSource = liquibaseSchemaResolver.resolve(liquibaseParser);
//
//		assertThat(inputSource).isNull();
//	}
//
//	@Test
//	public void shouldReturnNullWhenSystemIdIsNull() {
//		liquibaseSchemaResolver = new LiquibaseSchemaResolver(null, PUBLIC_ID, resourceAccessor);
//
//		InputSource inputSource = liquibaseSchemaResolver.resolve(liquibaseParser);
//
//		assertThat(inputSource).isNull();
//	}
//
//	@Test
//	public void shouldReturnNullWhenXsdFileFoundIsNull() {
//		when(nameSpaceDetialsForParser.getLocalPath(SYSTEM_ID)).thenReturn(null);
//
//		InputSource inputSource = liquibaseSchemaResolver.resolve(liquibaseParser);
//
//		assertThat(inputSource).isNull();
//	}
//
//	@Test
//	public void shouldThrowExceptionWhenSerializerGivenIsNull() {
//		try {
//			liquibaseSchemaResolver.resolve((LiquibaseSerializer) null);
//			fail("Excepted RuntimeException. But no Exception was thrown");
//		} catch (RuntimeException e){
//			assertThat(e.getMessage()).isEqualTo("Serializer can not be null");
//		}
//	}
//
//	@Test
//	public void shouldThrowExceptionWhenParserGivenIsNull() {
//		try {
//			liquibaseSchemaResolver.resolve((LiquibaseParser) null);
//			fail("Excepted RuntimeException. But no Exception was thrown");
//		} catch (RuntimeException e){
//			assertThat(e.getMessage()).isEqualTo("Parser can not be null");
//		}
//	}
//
//	@Test
//	public void shouldReturnNullWhenExceptionOccurs() {
//		when(resourceAccessorXsdStreamResolver.getResourceAsStream(XSD_FILE)).thenThrow(new RuntimeException());
//
//		InputSource inputSource = liquibaseSchemaResolver.resolve(liquibaseParser);
//
//		assertThat(inputSource).isNull();
//	}

}
