package whiteboard.app;

import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

public class InterpolatingPathIterator implements PathIterator {

    private final PathIterator it;
    private final double maxLength;
    private int stepCount = 1;
    private int steps = 0;
    private Point2D end = new Point2D.Double(), diff;
    private int type;
    
    public InterpolatingPathIterator(PathIterator it, double maxLength) {
        this.it = it;
        this.maxLength = maxLength;
    }

    @Override
    public int currentSegment(float[] arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int currentSegment(double[] coords) {
        if (stepCount > steps) {
            type = it.currentSegment(coords);
            diff = new Point2D.Double(coords[0] - end.getX(), coords[1] - end.getY());
            end = new Point2D.Double(coords[0], coords[1]);
            if (type == PathIterator.SEG_MOVETO)
                return type;
            double length = Math.sqrt(Math.pow(diff.getX(), 2) + Math.pow(diff.getY(), 2));
            steps = (int) (length / maxLength) + 1;
            stepCount = 1;
        }
        
        coords[0] = end.getX() - (diff.getX() * (1 - (double) stepCount / steps));
        coords[1] = end.getY() - (diff.getY() * (1 - (double) stepCount / steps));
        if (type == PathIterator.SEG_CLOSE) {
            return stepCount == steps ? PathIterator.SEG_CLOSE : PathIterator.SEG_LINETO;
        } else
            return type;
    }

    @Override
    public int getWindingRule() {
        return it.getWindingRule();
    }

    @Override
    public boolean isDone() {
        return stepCount <= steps ? false : it.isDone();
    }

    @Override
    public void next() {
        ++stepCount;
        if (stepCount > steps)
            it.next();
    }
}
