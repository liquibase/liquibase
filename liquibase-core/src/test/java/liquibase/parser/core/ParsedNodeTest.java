package liquibase.parser.core;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class ParsedNodeTest {

  @Test
  public void testClone() throws Exception {
    ParsedNode pnParent = new ParsedNode("hello", "world");
    ParsedNode pnChild1 = new ParsedNode("hello1", "child1");
    ParsedNode pnChild2 = new ParsedNode("hello2", "child2");
    ParsedNode pnChild11 = new ParsedNode("hello11", "child11");
    
    pnChild1.addChild(pnChild11);
    pnParent.addChild(pnChild1).addChild(pnChild2);
    
    ParsedNode pnParentClone = pnParent.clone();
    
    assertEquals(pnParent, pnParentClone);
    assertNotSame(pnParent, pnParentClone);
    
    List<ParsedNode> parentChildren = pnParent.getChildren();
    List<ParsedNode> parentCloneChildren = pnParentClone.getChildren();
    assertEquals(parentChildren, parentCloneChildren);
    assertNotSame(parentChildren, parentCloneChildren);
    
    assertNotSame(parentChildren.get(0), parentCloneChildren.get(0));
    assertNotSame(parentChildren.get(1), parentCloneChildren.get(1));
  }

}
