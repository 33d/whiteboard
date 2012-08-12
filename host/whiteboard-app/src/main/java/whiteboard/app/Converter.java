package whiteboard.app;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.OutputStream;
import java.io.PrintStream;

public class Converter {

    private double reelSpacing;
    private double startX;
    private double startY;
    private double reelRadius;
    private double pulsesPerRadian;
    private double pulleyRadius;
    // The longest line that should be drawn
    private double breakDistance = 5;
    private AffineTransform transform;
    
    private double[] calculateLengths(double x, double y) {
        return new double[] {
            Math.sqrt(x*x + y*y - pulleyRadius*pulleyRadius),
            Math.sqrt(Math.pow(reelSpacing - x, 2) + y*y - pulleyRadius*pulleyRadius)
        };
    }
    
    public void convert(PathIterator path, OutputStream outStream) {
        PrintStream out = new PrintStream(outStream);
        
        double x = startX;
        double y = startY;
        double[] s = calculateLengths(x, y);
        final double[] coords = new double[6];
        
        Point2D start = null;
        path = new InterpolatingPathIterator(path, breakDistance);
        
        while (!path.isDone()) {
            int type = path.currentSegment(coords);
            Point2D p = (type == PathIterator.SEG_CLOSE)
                ? start
                : new Point2D.Double(coords[0], coords[1]);
            
            double[] s1 = calculateLengths(p.getX(), p.getY());
            double[] diff = new double[] { s1[0] - s[0], s1[1] - s[1] };
            switch (type) {
            case PathIterator.SEG_MOVETO:
                start = p;
                out.print("M ");
                break;
            case PathIterator.SEG_LINETO:
            case PathIterator.SEG_CLOSE:
                out.print("D ");
                break;
            default:
                throw new IllegalStateException("Unhandled segment type " + type);    
            }
            
            // convert the difference to pulses
            int[] pulses = new int[2];
            for (int i = 0; i < 2; i++)
                pulses[i] = (int) Math.round(diff[i] / reelRadius * pulsesPerRadian);
            
            out.printf("%d %d ", pulses[0], pulses[1]);
            
            s = s1;
            path.next();
        }
        
        out.flush();
    }
    
    public double getReelSpacing() {
        return reelSpacing;
    }
    public void setReelSpacing(double reelSpacing) {
        this.reelSpacing = reelSpacing;
    }
    public double getStartX() {
        return startX;
    }
    public void setStartX(double startX) {
        this.startX = startX;
    }
    public double getStartY() {
        return startY;
    }
    public void setStartY(double startY) {
        this.startY = startY;
    }
    public double getReelRadius() {
        return reelRadius;
    }
    public void setReelRadius(double reelRadius) {
        this.reelRadius = reelRadius;
    }
    public AffineTransform getTransform() {
        return transform;
    }
    public void setTransform(AffineTransform transform) {
        this.transform = transform;
    }
    public void setPulsesPerRotation(double pulsesPerRotation) {
        this.pulsesPerRadian = pulsesPerRotation / (Math.PI / 2);
    }
    public double getPulleyRadius() {
        return pulleyRadius;
    }
    public void setPulleyRadius(double pulleyRadius) {
        this.pulleyRadius = pulleyRadius;
    }
}
