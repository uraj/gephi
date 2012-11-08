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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import org.gephi.graph.api.Node;

/**
 *
 * @author uraj
 */
public class NodeRanker extends JPanel {
    private SatelliteLayout layout;
    private JTable nodeTable;
    private String selectedNode;
    private Node[] graphNodes;
    private NodeWrapper[] wrapData;
        
    private class NodeWrapper {
        public Node node;
        public Comparable sortKey;
        
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
    
    private abstract class SortMetric implements Comparator<NodeWrapper>{
        public String title;
        public SortMetric(String title) {
            this.title = title;
        }
        
        @Override
        public String toString() { return title; }
        abstract public NodeWrapper[] wrap(Node[] node);
        
        @Override
        public int compare(NodeWrapper o1, NodeWrapper o2) {
            return o1.sortKey.compareTo(o2.sortKey);
        }
    }
    
    private SortMetric[] getSortMetrics() {
        List<SortMetric> mlist = new ArrayList<SortMetric>();
        mlist.add(new SortMetric("Lib Name") {
            @Override
            public NodeWrapper[] wrap(Node[] nodes) {
                int size = nodes.length;
                NodeWrapper[] ret = new NodeWrapper[size];
                for (int i = 0; i < size; ++i) {
                    ret[i] = new NodeWrapper();
                    ret[i].node = nodes[i];
                    ret[i].sortKey = nodes[i].getNodeData().getLabel();
                }
                return ret;
            }
        });
        mlist.add(new SortMetric("Lib Utilization") {
            @Override
            public NodeWrapper[] wrap(Node[] nodes) {
                int size = nodes.length;
                NodeWrapper[] ret = new NodeWrapper[size];
                for (int i = 0; i < size; ++i) {
                    ret[i] = new NodeWrapper();
                    ret[i].node = nodes[i];
                    ret[i].sortKey =  Float.valueOf(
                                (String)nodes[i]
                                .getNodeData()
                                .getAttributes()
                                .getValue("utilization"));
                }
                return ret;
            }
        });
        return mlist.toArray(new SortMetric[0]);
    }
    
    private class NodeTableModel extends AbstractTableModel {
        NodeWrapper[] data;
        private final String[] COL_NAMES = {"No.", "Library", "Metric Value"};
        public NodeTableModel(NodeWrapper[] data) {
            this.data = data;
        }
        @Override
        public int getRowCount() {
            return data.length;
        }
        @Override
        public String getColumnName(int col) {
            return COL_NAMES[col];
        }
        @Override
        public int getColumnCount() {
            return COL_NAMES.length;
        }
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch(columnIndex) {
                case 0:
                    return rowIndex;
                case 1:
                    return data[rowIndex].node.getNodeData().getLabel();
                case 2:
                    return data[rowIndex].sortKey;
                default:
                    return null;
            }    
        }
        @Override
        public boolean isCellEditable(int row, int col) { return false; }
    }
    
    

    public NodeRanker(SatelliteLayout layout) {
        super();
        this.layout = layout;
        this.graphNodes = null;
        
        JPanel ctrPanel = new JPanel();
        ctrPanel.setLayout(new FlowLayout());
        ctrPanel.add(new JLabel("<html><b>Rank by:</b></html>"));
        JComboBox ctrBox = new JComboBox(getSortMetrics());
        ctrBox.setSelectedItem(null);
        ctrBox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox sortBox = (JComboBox)e.getSource();
                SortMetric metric = (SortMetric)sortBox.getSelectedItem();
                if (graphNodes == null || graphNodes.length == 0) {
                    graphNodes = NodeRanker.this.layout.getNodes();
                }
                wrapData = metric.wrap(graphNodes);
                Arrays.sort(wrapData, metric);
                //nodeList.setListData(wrapData);
                nodeTable.setModel(new NodeTableModel(wrapData));
                nodeTable.getColumnModel().getColumn(0).setPreferredWidth(10);
            }
        });
        ctrPanel.add(ctrBox);
        
        this.nodeTable = new JTable() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                super.valueChanged(e);
                int index = this.getSelectedRow();
                if (index == -1) {
                    selectedNode = "";
                } else {
                    selectedNode = wrapData[index].node.getNodeData().getLabel();
                }
            }
        };
        nodeTable.setCellSelectionEnabled(false);
        nodeTable.setRowSelectionAllowed(true);
        nodeTable.setColumnSelectionAllowed(false);
        nodeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nodeTable.setVisible(true);
        nodeTable.getSelectionModel().addListSelectionListener(nodeTable);
        JScrollPane listScroller = new JScrollPane(nodeTable);
        
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
