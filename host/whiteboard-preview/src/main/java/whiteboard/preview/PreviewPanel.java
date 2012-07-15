package whiteboard.preview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Dimension2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GVTTreeWalker;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;

public class PreviewPanel extends JPanel {
    
    private Dimension2D size;
    private GraphicsNode root;
    private static final double FLATTENING = 0.1;
    
    public PreviewPanel(String uri) throws IOException {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        Document doc = f.createDocument(uri);
        
        GVTBuilder builder = new GVTBuilder();
        BridgeContext context = new SVG12BridgeContext(new UserAgentAdapter());
        root = builder.build(context, doc);
        size = context.getDocumentSize();
        
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
        GraphicsNode n = root;
        GVTTreeWalker walker = new GVTTreeWalker(n);
        double[] coords = new double[6];
        while (n != null) {
            if (n instanceof ShapeNode) {
                ShapeNode s = (ShapeNode) n;
                PathIterator p = s.getShape().getPathIterator(n.getGlobalTransform(), FLATTENING);
                Point2D first = null, prev = null;
                while (!p.isDone()) {
                    int type = p.currentSegment(coords);
                    Point2D next = new Point2D.Double(coords[0], coords[1]);
                    switch (type) {
                    case PathIterator.SEG_MOVETO:
                        first = next; break;
                    case PathIterator.SEG_LINETO:
                        g.draw(new Line2D.Double(prev, next)); break;
                    case PathIterator.SEG_CLOSE:
                        g.draw(new Line2D.Double(prev, first)); break;
                    }
                    prev = next;
                    p.next();
                }
            }
            n = walker.nextGraphicsNode();
        }
    }
}
