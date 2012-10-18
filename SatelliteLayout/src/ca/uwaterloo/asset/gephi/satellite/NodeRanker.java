package ca.uwaterloo.asset.gephi.satellite;

import java.awt.BorderLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;

/**
 *
 * @author uraj
 */
public class NodeRanker extends JPanel {
    private SatelliteLayout layout;
    private JList nodeList;
    private String selectedNode;
    private Node[] data;
    
    private class MyListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            int index = nodeList.getSelectedIndex();
            if (index == -1) {
                selectedNode = "";
            } else {
                selectedNode = data[index].getNodeData().getLabel();
            }
        }
    }
    
    public NodeRanker(SatelliteLayout layout) {
        super();
        this.layout = layout;
        this.data = layout.getNodes();
        
        System.err.println("Data size = " + data.length);
        this.nodeList = new JList(data);
        this.nodeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.nodeList.addListSelectionListener(new MyListener ());
        this.nodeList.setVisible(true);
        
        JScrollPane listScroller = new JScrollPane(nodeList);
        
        this.setLayout(new BorderLayout());
        this.add(listScroller, BorderLayout.CENTER);
        
        selectedNode = layout.getEarth();
    }
    
    public String getSelection() {
        return selectedNode;
    }
    
    public void commitSelection() {
        layout.setEarth(selectedNode);
    }
}
