package whiteboard.svgreader;

import java.awt.geom.Dimension2D;
import java.awt.geom.PathIterator;
import java.io.IOException;

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

public class SvgReader {
    
    private Dimension2D size;
    private GraphicsNode root;
    private static final double FLATTENING = 0.1;
    
    public SvgReader(String uri) throws IOException {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        Document doc = f.createDocument(uri);
        
        GVTBuilder builder = new GVTBuilder();
        BridgeContext context = new SVG12BridgeContext(new UserAgentAdapter());
        root = builder.build(context, doc);
        size = context.getDocumentSize();
    }

    public Dimension2D getSize() {
        return size;
    }

    public PathIterator getPathIterator() {
        final GVTTreeWalker walker = new GVTTreeWalker(root);
        
        return new PathIterator() {
            GraphicsNode n = root;
            PathIterator current;
            
            {
                goToNext();
            }
            
            @Override
            public int currentSegment(double[] coords) {
                return current.currentSegment(coords);
            }

            @Override
            public int currentSegment(float[] coords) {
                return current.currentSegment(coords);
            }

            @Override
            public int getWindingRule() {
                return current.getWindingRule();
            }

            @Override
            public boolean isDone() {
                while (current != null && current.isDone())
                    goToNext();
                return current == null;
            }
            
            private void goToNext() {
                do {
                    n = walker.nextGraphicsNode();
                } while (n != null && !(n instanceof ShapeNode));
                if (n != null) {
                    ShapeNode s = (ShapeNode) n;
                    current = s.getShape().getPathIterator(n.getGlobalTransform(), FLATTENING);
                } else
                    current = null;
            }

            @Override
            public void next() {
                current.next();
            }
        };
    }
}
