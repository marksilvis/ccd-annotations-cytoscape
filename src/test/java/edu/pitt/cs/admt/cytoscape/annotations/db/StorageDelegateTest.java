package edu.pitt.cs.admt.cytoscape.annotations.db;

import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotToEntity;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.ExtendedAttributeType;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author Nikos R. Katsipoulakis
 */
public class StorageDelegateTest {

  @Test
  public void initTest() {
    StorageDelegateFactory factory = StorageDelegateFactory.INSTANCE;
    StorageDelegate delegate = factory.newDelegate();
    try {
      delegate.init();
    } catch (SQLException e) {
      e.printStackTrace();
      assertTrue("unexpected SQLException thrown (1)", false);
    }
    delegate.close();
    factory.destroyDelegate(delegate.getId());
  }

  @Test
  public void insertNodeTest() {
    StorageDelegateFactory factory = StorageDelegateFactory.INSTANCE;
    StorageDelegate delegate = factory.newDelegate();
    try {
      delegate.init();
    } catch (SQLException e) {
      assertTrue("unexpected SQLException thrown (0)", false);
    }
    try {
      delegate.insertNewNode(1);
    } catch (SQLException e) {
      e.printStackTrace();
      assertTrue("first insertion of node " + 1 + " failed.", false);
    }
    boolean thrown = false;
    try {
      delegate.insertNewNode(1);
    } catch (SQLException e) {
      thrown = true;
    }
    assertTrue("second insertion of node " + 1 + " succeeded", thrown);
    thrown = false;
    try {
      delegate.insertNewNode(-1);
    } catch (Exception e) {
      thrown = true;
    }
    assertTrue("insertion of negative node-id allowed", thrown);
    delegate.close();
    factory.destroyDelegate(delegate.getId());
  }

  @Test
  public void insertEdgeTest() {
    StorageDelegateFactory factory = StorageDelegateFactory.INSTANCE;
    StorageDelegate delegate = factory.newDelegate();
    try {
      delegate.init();
    } catch (SQLException e) {
      assertTrue("unexpected SQLException thrown (1)", false);
    }
    try {
      delegate.insertNewNode(1);
      delegate.insertNewNode(2);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    boolean thrown = false;
    try {
      delegate.insertNewEdge(-1, 1, 2);
    } catch (SQLException e) {
      thrown = true;
    }
    assertTrue("insertion of edge with negative id allowed", thrown);
    thrown = false;
    try {
      delegate.insertNewEdge(1, 4, 5);
    } catch (SQLException e) {
      thrown = true;
    }
    assertTrue("insertion of edge to non existing nodes is allowed", thrown);
    try {
      delegate.insertNewEdge(1, 1, 2);
    } catch (SQLException e) {
      e.printStackTrace();
      assertTrue("insertion of normal edge failed", false);
    }
    delegate.close();
    factory.destroyDelegate(delegate.getId());
  }

  @Test
  public void insertAnnotationTest() {
    StorageDelegateFactory factory = StorageDelegateFactory.INSTANCE;
    StorageDelegate delegate = factory.newDelegate();
    UUID firstUUID = UUID.randomUUID();
    try {
      delegate.init();
    } catch (SQLException e) {
      assertTrue("unexpected SQLException thrown (1)", false);
    }
    try {
      delegate.insertNewNode(1);
      delegate.insertNewNode(2);
      delegate.insertNewEdge(1, 1, 2);
    } catch (SQLException e) {
      e.printStackTrace();
      assertTrue("insertion of nodes and edges failed", false);
    }
    boolean thrown = false;
    try {
      delegate.insertAnnotation(null, null, ExtendedAttributeType.CHAR, "should-fail");
    }catch (Exception e) {
      thrown = true;
    }
    assertTrue("insertion with null id", thrown);
    thrown = false;
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < 65; ++i)
      builder.append("a");
    try {
      delegate.insertAnnotation(firstUUID, "some-ann", ExtendedAttributeType.CHAR,
          builder.toString());
    } catch (IllegalArgumentException e) {
      thrown = true;
    } catch (SQLException e1) {
      e1.printStackTrace();
    }
    assertTrue("insertion with long description", thrown);
    try {
      delegate.insertAnnotation(firstUUID, "some-ann", ExtendedAttributeType.CHAR,
          "annotation test alpha");
    } catch (SQLException e) {
      assertTrue("insertion of normal annotation failed", false);
    }
    delegate.close();
    factory.destroyDelegate(delegate.getId());
  }
  
}