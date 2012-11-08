package ca.uwaterloo.asset.gephi.satellite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.Exceptions;
/**
 *
 * @author Pei Wang <p56wang@uwaterloo.ca>
 */
public class SatelliteLayout implements Layout {
    public enum Direction {
        Successor,
        Predecessor;
    }

    //Architecture
    private final LayoutBuilder builder;
    private GraphModel graphModel;
    //Flags
    private boolean executing = false;
    //Properties
    private Direction depType;
    private String earthLabel;
    private int areaSize;
    private float threshold;
    
    private static final Direction DEFAULT_DEP_TYPE = Direction.Successor;
    
    private static final float AMPLIFY_SCOPE = 0.5f;
    
    private static final int DEFAULT_AREA = 500;
    private static final int MIN_AREA = 200;
    private static final int MAX_AREA = 3000;
    
    private float DEFAULT_THRES = 0;
    private String DEFAULT_EARTH = "";
    
    private float MIN_WEIGHT;
    private float MAX_WEIGHT;
    
    private GraphView oldView;
    
    
    public SatelliteLayout(SatelliteLayoutBuilder builder) {
        this.builder = builder;
    }
    
    public Node[] getNodes() {
        if (graphModel != null) {
            return graphModel.getGraph().getNodes().toArray();
        } else {
            return null;
        }
    }
    
    @Override
    public void setGraphModel(GraphModel gm) {
        this.graphModel = gm;
        Graph graph = graphModel.getGraph();
        
        graph.readLock();
        Edge[] edges = graph.getEdges().toArray();
        int edgeCount = edges.length;
        for (int i = 0; i < edgeCount; ++i) {
            float weight = edges[i].getWeight();
            MIN_WEIGHT = MIN_WEIGHT > weight ? weight : MIN_WEIGHT;
            MAX_WEIGHT = MAX_WEIGHT < weight ? weight : MAX_WEIGHT;
        }
        graph.readUnlock();
        
        DEFAULT_THRES = 0.1f * MAX_WEIGHT;
        resetPropertiesValues();
        
        oldView = null;
    }
    
    @Override
    public void resetPropertiesValues() {
        depType = DEFAULT_DEP_TYPE;
        earthLabel = DEFAULT_EARTH;
        areaSize = DEFAULT_AREA;
        threshold = DEFAULT_THRES;
    }

    @Override
    public void initAlgo() {
        executing = true;
    }

    @Override
    public void goAlgo() {
        if (oldView != null) {
            graphModel.destroyView(oldView);
        }
        
        GraphView newView = graphModel.newView();
        if (!earthLabel.isEmpty()) {
            if (graphModel.isDirected()) {
                processDirected(graphModel.getDirectedGraph(newView));
            } else {
                processUndirected(graphModel.getUndirectedGraph(newView));
            }
        }
        graphModel.setVisibleView(newView);
        oldView = newView;
        executing = false;
    }

    @Override
    public void endAlgo() {
        executing = false;
    }

    @Override
    public boolean canAlgo() {
        return executing;
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String SATELLITELAYOUT = "Satellite Layout";
        try {
            properties.add(LayoutProperty.createProperty(
                    this, Direction.class,
                    "Satellites type",
                    SATELLITELAYOUT,
                    "Choose to show successors or predecessors of the Earth",
                    "getDepType", "setDepType"));
            properties.add(LayoutProperty.createProperty(
                    this, Integer.class,
                    "Area size",
                    SATELLITELAYOUT,
                    "The area size",
                    "getAreaSize", "setAreaSize"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    "Threshold",
                    SATELLITELAYOUT,
                    "Edges with weight less than Threshold will be amplified",
                    "getThreshold", "setThreshold"));
            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    "Earth",
                    SATELLITELAYOUT,
                    "The node surrounded by satellites",
                    "getEarth", "setEarth"));
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public LayoutBuilder getBuilder() {
        return builder;
    }
    
    public Direction getDepType() {
        return depType;
    }

    public void setDepType(Direction dir) {
        this.depType = dir;
    }

    public String getEarth() {
        return earthLabel;
    }

    public void setEarth(String earthLabel) {
        this.earthLabel = earthLabel;
    }

    public Integer getAreaSize() {
        return areaSize;
    }

    public void setAreaSize(Integer area) {
        this.areaSize = area < MAX_AREA ? area : MAX_AREA;
        this.areaSize = area > MIN_AREA ? area : MIN_AREA;
    }
    
    public Float getThreshold() {
        return threshold;
    }

    public void setThreshold(Float thes) {
        this.threshold = thes < MAX_WEIGHT ? thes : MAX_WEIGHT;
        this.threshold = thes > MIN_WEIGHT ? thes : MIN_WEIGHT;
    }
    
    private void processDirected(DirectedGraph graph) {
        graph.writeLock();
        
        // Find earth
        Node earth = null;
        int nodeCount = graph.getNodeCount();
        Node[] nodes = graph.getNodes().toArray();
        Edge[] edges, prunedEdges;
        for (int i = 0; i < nodeCount; ++i) {
            Node node = nodes[i];
            if (earthLabel.equals(node.getNodeData().getLabel())) {
                earth = node;
                break;
            }
        }
        
        if (earth == null) {
            graph.clear();
        } else {
            int earthId = earth.getId();
            int edgeCount;
            if (depType == Direction.Successor) {
                for (int i = 0; i < nodeCount; ++i) {
                    Node node = nodes[i];
                    if (node.getId() != earthId && !graph.isSuccessor(earth, node)) {   
                        graph.removeNode(node);
                    }
                }
                edgeCount = graph.getEdgeCount();
                edges = graph.getEdges().toArray();
                for (int i = 0; i < edgeCount; ++i) {
                    Edge edge = edges[i];
                    if (edge.getSource().getId() != earth.getId()) {
                        graph.removeEdge(edge);
                    }
                }
                prunedEdges = graph.getOutEdges(earth).toArray();
            } else {
                for (int i = 0; i < nodeCount; ++i) {
                    Node node = nodes[i];
                
                    if (node.getId() != earthId && !graph.isPredecessor(earth, node)) {   
                        graph.removeNode(node);
                    }
                }
                edgeCount = graph.getEdgeCount();
                edges = graph.getEdges().toArray();
                for (int i = 0; i < edgeCount; ++i) {
                    Edge edge = edges[i];
                    if (edge.getTarget().getId() != earth.getId()) {
                        graph.removeEdge(edge);
                    }
                }
                prunedEdges = graph.getInEdges(earth).toArray();
            }

            Collections.shuffle(Arrays.asList(prunedEdges));
            int prunedCount = prunedEdges.length;
            double baseRadian = 2 * Math.PI / prunedCount;
            earth.getNodeData().setX(0);
            earth.getNodeData().setY(0);
            float maxW = 0;
            for (int i = 0; i < prunedCount; ++i) {
                float curW = prunedEdges[i].getWeight();
                maxW = curW > maxW ? curW : maxW;
            }
            
            for (int i = 0; i < prunedCount; ++i) {
                Node node;
                if (depType == Direction.Successor) {
                    node = prunedEdges[i].getTarget();
                } else {
                    node = prunedEdges[i].getSource();
                }
                float weight = prunedEdges[i].getWeight();
                float radius, x, y;
                if (weight < threshold) {
                    radius = (threshold - weight) / threshold * AMPLIFY_SCOPE;
                    radius += 1 - AMPLIFY_SCOPE;
                    radius *= areaSize;
                } else {
                    radius = maxW - threshold - weight;
                    radius *= 1 - AMPLIFY_SCOPE;
                    radius *= areaSize / (maxW - threshold);
                }
                x = (float)(radius * Math.cos(baseRadian * i));
                y = (float)(radius * Math.sin(baseRadian * i));
                node.getNodeData().setX(x);
                node.getNodeData().setY(y);
            }
        }
        graph.writeUnlock();
    }
    
    private void processUndirected(UndirectedGraph graph) {graph.writeLock();
        graph.writeLock();
        
        // Find earth
        Node earth = null;
        int nodeCount = graph.getNodeCount();
        Node[] nodes = graph.getNodes().toArray();
        for (int i = 0; i < nodeCount; ++i) {
            Node node = nodes[i];
            if (earthLabel.equals(node.getNodeData().getLabel())) {
                earth = node;
                break;
            }
        }
        
        if (earth == null) {
            graph.clear();
        } else {
            int earthId = earth.getId();
            for (int i = 0; i < nodeCount; ++i) {
                Node node = nodes[i];
                if (node.getId() != earthId && !graph.isAdjacent(earth, node)) {
                    graph.removeNode(node);
                }
            }
            int edgeCount = graph.getEdgeCount();
            Edge[] edges = graph.getEdges().toArray();
            for (int i = 0; i < edgeCount; ++i) {
                Edge edge = edges[i];
                if (edge.getSource().getId() != earth.getId() &&
                    edge.getTarget().getId() != earth.getId()) {
                    graph.removeEdge(edge);
                }
            }
            
            Node[] neighbors = graph.getNeighbors(earth).toArray();
            int neighborCount = neighbors.length;
            double baseRadian = 2 * Math.PI / neighborCount;
            earth.getNodeData().setX(0);
            earth.getNodeData().setY(0);
            for (int i = 0; i < neighborCount; i++) {
                Node node = neighbors[i];
                float weight = graph.getEdge(node, earth).getWeight();
                float radius, x, y;
                if (weight < threshold) {
                    radius = (threshold - weight) / threshold * AMPLIFY_SCOPE;
                    radius += 1 - AMPLIFY_SCOPE;
                    radius *= areaSize;
                } else {
                    radius = MAX_WEIGHT - threshold - weight;
                    radius *= 1 - AMPLIFY_SCOPE;
                    radius *= areaSize / (MAX_WEIGHT - threshold);
                }
                x = (float)(radius * Math.cos(baseRadian * i));
                y = (float)(radius * Math.sin(baseRadian * i));
                node.getNodeData().setX(x);
                node.getNodeData().setY(y);
            }
        }
        graph.writeUnlock();
    }
        
    public GraphModel getGraphModel() {
        if (graphModel == null) {
            return null;
        } else {
            return graphModel;
        }
    }
}