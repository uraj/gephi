package ca.uwaterloo.asset.gephi.satellite;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.layout.spi.LayoutUI;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Pei Wang <p56wang@uwaterloo.ca>
 */
@ServiceProvider(service = LayoutBuilder.class)
public class SatelliteLayoutBuilder implements LayoutBuilder {
    private static final String DESC =
            "Pick one central node in the graph " + 
            "and layout all its sucessors (directed graph) " +
            "or neibours (undirected graph) around it.";

    private DialogDescriptor dialog;
    
    @Override
    public String getName() {
        return "Satellite Layout";
    }

    @Override
    public LayoutUI getUI() {
        return new LayoutUI() {
            @Override
            public String getDescription() {
                return DESC;
            }

            @Override
            public Icon getIcon() {
                return null;
            }

            @Override
            public JPanel getSimplePanel(Layout layout) {
                if (layout instanceof SatelliteLayout) {
                    return constructSatellitePanel((SatelliteLayout)layout);
                } else {
                    return null;
                }
            }

            @Override
            public int getQualityRank() {
                return -1;
            }

            @Override
            public int getSpeedRank() {
                return -1;
            }
        };
    }

    @Override
    public Layout buildLayout() {
        return new SatelliteLayout(this);
    }

    private class LayoutNode extends AbstractNode {

        private Layout layout;
        private Node.PropertySet[] propertySets;

        public LayoutNode(Layout layout) {
            super(Children.LEAF);
            this.layout = layout;
            setName(layout.getBuilder().getName());
        }

        @Override
        public Node.PropertySet[] getPropertySets() {
            if (propertySets == null) {
                try {
                    Map<String, Sheet.Set> sheetMap = new HashMap<String, Sheet.Set>();
                    for (LayoutProperty layoutProperty : layout.getProperties()) {
                        Sheet.Set set = sheetMap.get(layoutProperty.getCategory());
                        if (set == null) {
                            set = Sheet.createPropertiesSet();
                            set.setDisplayName(layoutProperty.getCategory());
                            sheetMap.put(layoutProperty.getCategory(), set);
                        }
                        set.put(layoutProperty.getProperty());
                    }
                    propertySets = sheetMap.values().toArray(new Node.PropertySet[0]);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
            }
            return propertySets;
        }

        public Layout getLayout() {
            return layout;
        }
    }
    
    private JPanel constructSatellitePanel(SatelliteLayout layout) {
        JPanel main = new JPanel();
        main.setLayout(new GridBagLayout());
        NodeRanker nr = new NodeRanker(layout);
        dialog = new DialogDescriptor(nr, "View Node Ranking", false, null);
        GridBagConstraints constraints;
        
        PropertySheet ps = new PropertySheet();
        ps.setNodes(new org.openide.nodes.Node[] 
        {new SatelliteLayoutBuilder.LayoutNode(layout)});
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new java.awt.Insets(0, 0, 0, 0);
        main.add(ps, constraints);

        JSeparator sep = new JSeparator();
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new java.awt.Insets(5, 0, 5, 0);
        main.add(sep, constraints);
        
        JButton button = new JButton("Show Node Rankings");
        button.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                DialogDisplayer.getDefault().notify(dialog);
            }
        
        });
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1.0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new java.awt.Insets(5, 0, 5, 0);
        main.add(button, constraints);
        
        return main;
    }
}
