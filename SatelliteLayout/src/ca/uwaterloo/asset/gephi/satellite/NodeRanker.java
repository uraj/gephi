package ca.uwaterloo.asset.gephi.satellite;

import javax.swing.JPanel;
import org.gephi.graph.api.GraphModel;

/**
 *
 * @author uraj
 */
public class NodeRanker extends JPanel {
    private SatelliteLayout layout;
    private GraphModel graphModel;
    
    public NodeRanker(SatelliteLayout layout) {
        super();
        this.graphModel = layout.getGraphModel();
        this.layout = layout;
    }
}
