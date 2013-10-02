package liquibase.resource;

import static org.easymock.EasyMock.*;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

public class CompositeFileOpenerTest {
    ResourceAccessor first;
    ResourceAccessor second;
    ResourceAccessor hasDir;
    ResourceAccessor noDir;
    CompositeResourceAccessor composite;
    InputStream validStream;
    Enumeration<URL> empty;
    Enumeration<URL> hasElements;
    
    @Before
    public void setUp() throws IOException {
        first = createMock(ResourceAccessor.class);
        second = createMock(ResourceAccessor.class);
        composite = new CompositeResourceAccessor(first,second);
        validStream = this.getClass().getClassLoader().getResourceAsStream("liquibase/resource/CompositeFileOpenerTest.class");
        
        empty = new Vector<URL>().elements();
        hasElements = this.getClass().getClassLoader().getResources("liquibase");
    }
    
    @After
    public void tearDown() throws IOException {
        validStream.close();
        
    }
    
    @Test
    public void streamFirstHas() throws IOException {
        expect(first.getResourceAsStream("file")).andReturn(validStream);
        replay(first);
        replay(second);
        InputStream is = composite.getResourceAsStream("file");
        assertEquals(validStream,is);
        verify(first);
        verify(second);
    }
    
    @Test
    public void streamSecondHas() throws IOException {
        expect(first.getResourceAsStream("file")).andReturn(null);
        expect(second.getResourceAsStream("file")).andReturn(validStream);
        replay(first);
        replay(second);
        InputStream is = composite.getResourceAsStream("file");
        assertEquals(validStream,is);
        verify(first);
        verify(second);
    }
    
    @Test
    public void streamNeitherHas() throws IOException {
        expect(first.getResourceAsStream("file")).andReturn(null);
        expect(second.getResourceAsStream("file")).andReturn(null);
        replay(first);
        replay(second);
        InputStream is = composite.getResourceAsStream("file");
        assertNull(is);
        verify(first);
        verify(second);
    }
    
    @Test
    public void resourcesFirstHas() throws IOException {
        expect(first.getResources("file")).andReturn(hasElements);
        //expect(second.getResources("file")).andReturn(empty);
        replay(first);
        replay(second);
        Enumeration<URL> urls = composite.getResources("file");
//        assertEquals(hasElements,urls);
        verify(first);
        verify(second);
    }
    
    @Test
    public void resourcesSecondHas() throws IOException {
        expect(first.getResources("file")).andReturn(empty);
        expect(second.getResources("file")).andReturn(hasElements);
        replay(first);
        replay(second);
        Enumeration<URL> urls = composite.getResources("file");
//        assertEquals(hasElements,urls);
        verify(first);
        verify(second);
    }
    
    @Test
    public void resourcesNeitherHas() throws IOException {
        expect(first.getResources("file")).andReturn(empty);
        expect(second.getResources("file")).andReturn(empty);
        replay(first);
        replay(second);
        Enumeration<URL> urls = composite.getResources("file");
        assertFalse(urls.hasMoreElements());
        assertFalse(urls == empty);
        verify(first);
        verify(second);
    }
}