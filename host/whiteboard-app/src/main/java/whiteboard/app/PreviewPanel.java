package whiteboard.app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Dimension2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import javax.swing.JPanel;

import org.apache.batik.gvt.GVTTreeWalker;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;

import whiteboard.svgreader.SvgReader;

public class PreviewPanel extends JPanel {
    
    private Dimension2D size;
    private static final double FLATTENING = 0.1;
    private final Path2D path;
    
    public PreviewPanel(SvgReader reader) throws IOException {
        path = new Path2D.Double();
        path.append(reader.getPathIterator(), false);
        size = reader.getSize();
        
        setPreferredSize(new Dimension(400, 400));
    }
    
    public void paintComponent(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        double scale = Math.min(
            (double) getWidth() / size.getWidth(),
            (double) getHeight() / size.getHeight()
        );
        g.scale(scale, scale);
        
        g.setColor(Color.WHITE);
        g.fill(new Rectangle2D.Double(0, 0, size.getWidth(), size.getHeight()));
        
        g.setColor(Color.BLACK);
        g.draw(path);
    }
}
