package liquibase.parser.core.xml;

import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(StreamUtil.class)
public class ResourceAccessorXsdStreamResolverTest {

	private static final String XSD_FILE = "xsdFile";

	@InjectMocks
	private ResourceAccessorXsdStreamResolver resourceAccessorXsdStreamResolver;

	@Mock
	private XsdStreamResolver successor;

	@Mock
	private ResourceAccessor resourceAccessor;

	@Mock
	private InputStream inputStream, successorValue;

	@Before
	public void setUp() throws IOException {
		PowerMockito.mockStatic(StreamUtil.class);

		resourceAccessorXsdStreamResolver.setSuccessor(successor);

		when(successor.getResourceAsStream(XSD_FILE)).thenReturn(successorValue);
	}

	@Test
	public void whenResourceStreamIsNotNullThenReturnStream() throws IOException {
		when(StreamUtil.singleInputStream(XSD_FILE, resourceAccessor)).thenReturn(inputStream);

		InputStream returnValue = resourceAccessorXsdStreamResolver.getResourceAsStream(XSD_FILE);

		assertThat(returnValue).isSameAs(inputStream);
	}

	@Test
	public void whenResourceStreamIsNullThenReturnSuccessorValue() throws IOException {
		when(StreamUtil.singleInputStream(XSD_FILE, resourceAccessor)).thenReturn(null);

		InputStream returnValue = resourceAccessorXsdStreamResolver.getResourceAsStream(XSD_FILE);

		assertThat(returnValue).isSameAs(successorValue);
	}

	@Test
	public void whenIOExceptionOccursThenReturnSuccessorValue() throws IOException {
		when(StreamUtil.singleInputStream(XSD_FILE, resourceAccessor)).thenThrow(new IOException());

		InputStream returnValue = resourceAccessorXsdStreamResolver.getResourceAsStream(XSD_FILE);

		assertThat(returnValue).isSameAs(successorValue);
	}

}