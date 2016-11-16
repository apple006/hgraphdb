package io.hgraphdb;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.*;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Iterator;

import static org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils.count;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HBaseSchemaTest extends HBaseGraphTest {

    @Override
    protected HBaseGraphConfiguration generateGraphConfig(String graphName) {
        HBaseGraphConfiguration config = super.generateGraphConfig(graphName);
        config.setElementCacheMaxSize(0);
        config.setRelationshipCacheMaxSize(0);
        config.setLazyLoading(true);
        config.setStaleIndexExpiryMs(0);
        config.setUseSchema(true);
        return config;
    }

    @Test
    public void testVertexIndex() {
        assertEquals(0, count(graph.vertices()));

        graph.createIndex(IndexType.VERTEX, "a", "key1");

        try {
            graph.addVertex(T.id, id(10), T.label, "a", "key1", 11);
            fail("Vertex should be invalid");
        } catch (HBaseGraphNotValidException e) {
        }

        graph.createVertexLabel("b", ValueType.LONG, "key2", ValueType.LONG);

        try {
            graph.addVertex(T.id, id(10), T.label, "a", "key1", 11);
            fail("Vertex should be invalid");
        } catch (HBaseGraphNotValidException e) {
        }

        try {
            graph.addVertex(T.id, 10L, T.label, "b", "key1", 11);
            fail("Vertex should be invalid");
        } catch (HBaseGraphNotValidException e) {
        }

        try {
            graph.addVertex(T.id, 10L, T.label, "b", "key2", 11);
            fail("Vertex should be invalid");
        } catch (HBaseGraphNotValidException e) {
        }

        graph.addVertex(T.id, 10L, T.label, "b", "key2", 11L);

        Iterator<Vertex> it = graph.allVertices("b", "key2", 11L);
        assertEquals(1, count(it));
    }

    @Test
    public void testEdgeIndex() {
        assertEquals(0, count(graph.vertices()));

        graph.createIndex(IndexType.VERTEX, "a", "key1");

        graph.createVertexLabel("a", ValueType.STRING, "key0", ValueType.INT);
        graph.createVertexLabel("b", ValueType.STRING, "key1", ValueType.INT);
        graph.createVertexLabel("c", ValueType.STRING, "key2", ValueType.INT);
        graph.createVertexLabel("d", ValueType.STRING, "key3", ValueType.INT);

        Vertex v1 = graph.addVertex(T.id, id(10), T.label, "a", "key0", 10);
        Vertex v2 = graph.addVertex(T.id, id(11), T.label, "b", "key1", 11);
        Vertex v3 = graph.addVertex(T.id, id(12), T.label, "c", "key2", 12);
        Vertex v4 = graph.addVertex(T.id, id(13), T.label, "d", "key3", 13);

        graph.createEdgeLabel("knows", "a", "b", ValueType.STRING, "key2", ValueType.LONG, "when", ValueType.STRING);

        try {
            graph.addEdge(v3, v4, "foo", "blah", 11);
            fail("Edge should be invalid");
        } catch (HBaseGraphNotValidException e) {
        }

        try {
            graph.addEdge(v1, v4, "foo", "blah", 11);
            fail("Edge should be invalid");
        } catch (HBaseGraphNotValidException e) {
        }

        try {
            graph.addEdge(v1, v2, "foo", "blah", 11);
            fail("Edge should be invalid");
        } catch (HBaseGraphNotValidException e) {
        }

        try {
            graph.addEdge(v1, v2, "knows", "blah", 11);
            fail("Edge should be invalid");
        } catch (HBaseGraphNotValidException e) {
        }

        try {
            graph.addEdge(v1, v2, "knows", "when", 11);
            fail("Edge should be invalid");
        } catch (HBaseGraphNotValidException e) {
        }

        graph.addEdge(v1, v2, "knows", "when", "11");

        Iterator<Edge> it = v1.edges(Direction.OUT, "knows");
        assertEquals(1, count(it));
    }
}
