package whiteboard.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;

import org.junit.Test;

public class InterpolatingPathIteratorTest {
    @Test
    public void testLine() {
        Line2D line = new Line2D.Double(10, 10, 19, 19);
        PathIterator it = new InterpolatingPathIterator(line.getPathIterator(new AffineTransform()), 5);
        double[] coords = new double[6];
        
        assertFalse(it.isDone());
        assertEquals(PathIterator.SEG_MOVETO, it.currentSegment(coords));
        assertEquals(10.0, coords[0], 0.1);
        assertEquals(10.0, coords[1], 0.1);
        it.next();
        assertFalse(it.isDone());
        assertEquals(PathIterator.SEG_LINETO, it.currentSegment(coords));
        assertEquals(13.0, coords[0], 0.1);
        assertEquals(13.0, coords[1], 0.1);
        it.next();
        assertFalse(it.isDone());
        assertEquals(PathIterator.SEG_LINETO, it.currentSegment(coords));
        assertEquals(16.0, coords[0], 0.1);
        assertEquals(16.0, coords[1], 0.1);
        it.next();
        assertFalse(it.isDone());
        // Lines aren't closed
        assertEquals(PathIterator.SEG_LINETO, it.currentSegment(coords));
        assertEquals(19.0, coords[0], 0.1);
        assertEquals(19.0, coords[1], 0.1);
        it.next();
        assertTrue(it.isDone());
    }
}
