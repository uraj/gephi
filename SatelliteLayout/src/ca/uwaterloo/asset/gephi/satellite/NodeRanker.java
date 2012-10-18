package ca.uwaterloo.asset.gephi.satellite;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.gephi.graph.api.Node;

/**
 *
 * @author uraj
 */
public class NodeRanker extends JPanel {
    private SatelliteLayout layout;
    private JList nodeList;
    private String selectedNode;
    private Node[] graphNodes;
    private NodeWrapper[] wrapData;
    
    private class NodeWrapper {
        public Node node;
        public Object sortKey;
        
        public NodeWrapper() {
            node = null;
            sortKey = null;
        }
        
        @Override
        public String toString() {
            if (node == null) {
                System.err.println("Trying to print null NodeWrapper");
                return super.toString();
            } else {
                return node.getNodeData().getLabel();
            }
        }
    }
    
    private class ListListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            int index = nodeList.getSelectedIndex();
            if (index == -1) {
                selectedNode = "";
            } else {
                selectedNode = wrapData[index].node.getNodeData().getLabel();
            }
        }
    }
    
    private abstract class SortMetric implements Comparator<NodeWrapper>{
        public String title;
        public SortMetric(String title) {
            this.title = title;
        }
        
        @Override
        public String toString() { return title; }
        abstract public NodeWrapper[] wrap(Node[] node);
    }
    
    private SortMetric[] getSortMetrics() {
        List<SortMetric> mlist = new ArrayList<SortMetric>();
        mlist.add(new SortMetric("Name") {
            @Override
            public NodeWrapper[] wrap(Node[] nodes) {
                int size = nodes.length;
                NodeWrapper[] ret = new NodeWrapper[size];
                for (int i = 0; i < size; ++i) {
                    ret[i] = new NodeWrapper();
                    ret[i].node = nodes[i];
                }
                return ret;
            }

            @Override
            public int compare(NodeWrapper o1, NodeWrapper o2) {
                return o1.node.getNodeData().getLabel()
                       .compareTo
                       (o2.node.getNodeData().getLabel());
            }
        });
        return mlist.toArray(new SortMetric[0]);
    }

    public NodeRanker(SatelliteLayout layout) {
        super();
        this.layout = layout;
        this.graphNodes = layout.getNodes();
        
        JPanel ctrPanel = new JPanel();
        ctrPanel.setLayout(new FlowLayout());
        ctrPanel.add(new JLabel("<html><b>Sort by:</b></html>"));
        JComboBox ctrBox = new JComboBox(getSortMetrics());
        ctrBox.setSelectedItem(null);
        ctrBox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox sortBox = (JComboBox)e.getSource();
                SortMetric metric = (SortMetric)sortBox.getSelectedItem();
                wrapData = metric.wrap(graphNodes);
                Arrays.sort(wrapData, metric);
                nodeList.setListData(wrapData);
            }
        });
        ctrPanel.add(ctrBox);
        
        this.nodeList = new JList();
        this.nodeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.nodeList.addListSelectionListener(new ListListener ());
        this.nodeList.setVisible(true);
        JScrollPane listScroller = new JScrollPane(nodeList);
        
        this.setLayout(new BorderLayout());
        this.add(ctrPanel, BorderLayout.NORTH);
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
