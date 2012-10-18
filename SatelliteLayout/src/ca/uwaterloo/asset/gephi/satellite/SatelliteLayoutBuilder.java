package ca.uwaterloo.asset.gephi.satellite;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
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
                    return new SatelliteLayoutPanel((SatelliteLayout)layout);
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

}
