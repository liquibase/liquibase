package liquibase.parser.core.xml;

import liquibase.resource.ResourceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class ContextClassLoaderXsdStreamResolverTest {

    private static final String EXISTING_XSD_FILE = "liquibase/parser/core/xml/unused.xsd";
    private static final String NON_EXISTING_XSD_FILE = "xsdFile";

    @InjectMocks
    private ContextClassLoaderXsdStreamResolver contextClassLoaderXsdStreamResolver;

    @Mock
    private XsdStreamResolver successor;

    @Mock
    private ResourceAccessor resourceAccessor;

    @Mock
    private InputStream successorValue;

    @Before
    public void setUp() {
        contextClassLoaderXsdStreamResolver.setSuccessor(successor);

        when(successor.getResourceAsStream(NON_EXISTING_XSD_FILE)).thenReturn(successorValue);
    }

    @Test
    public void whenResourceStreamIsNotNullThenReturnStream() throws IOException {
        InputStream returnValue = contextClassLoaderXsdStreamResolver.getResourceAsStream(EXISTING_XSD_FILE);

        assertThat(returnValue).isInstanceOf(InputStream.class);
    }

    @Test
    public void whenContextClassLoaderIsNullThenReturnSuccessorValue() throws IOException {
        InputStream returnValue = contextClassLoaderXsdStreamResolver.getResourceAsStream(NON_EXISTING_XSD_FILE);

        assertThat(returnValue).isSameAs(successorValue);
    }

}