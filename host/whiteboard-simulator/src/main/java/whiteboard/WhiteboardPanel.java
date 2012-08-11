package whiteboard;

import static java.lang.System.arraycopy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingSource;
import org.jdesktop.core.animation.timing.TimingTarget;
import org.jdesktop.core.animation.timing.TimingTargetAdapter;
import org.jdesktop.swing.animation.timing.sources.SwingTimerTimingSource;

public class WhiteboardPanel extends JPanel implements Whiteboard {

    private final BufferedImage output;
    private final double wheelRadius;
    private final double wheelSeparation;
    private static final Stroke wheelStroke 
        = new BasicStroke(0.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    private static final Stroke stringStroke 
        = new BasicStroke(0.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
    private static final Stroke drawStroke 
        = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
    private Animator animator;
    private static final double speed = 1.0/500; // radians per millisecond
    /** The position of each wheel, in radians.  0 is the starting position. */
    private final double[] pos = new double[2];
    /** The distance each wheel has to turn during this animation */
    private final double[] distance = new double[2];
    /** The length of each string, in millimetres. */
    private final double[] strings = new double[2];
    private Point2D prevPoint;
    /** What to scale the drawing by to fit the window */
    private double scale;
    private boolean running;
    private boolean drawing;
    private static final Map<Object, Object> renderingHints = new HashMap<Object, Object>();
    static {
        renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }
    private final TimingSource timingSource;
    
    /** A wheel shape 1 unit in radius, centred at 0,0 */
    private static final Shape wheelShape;
    static {
        Path2D s = new Path2D.Double();
        s.append(new Ellipse2D.Double(-1, -1, 2, 2), false);
        s.append(new Line2D.Double(-1, 0, 1, 0), false);
        wheelShape = s;
    }
    
    public WhiteboardPanel(double wheelRadius, double wheelSeparation, int canvasWidth) {
        output = new BufferedImage(canvasWidth, canvasWidth, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D og = output.createGraphics();
        og.setColor(Color.WHITE);
        og.fillRect(0,  0, output.getWidth(), output.getHeight());
        setBackground(Color.WHITE);
        
        this.wheelRadius = wheelRadius;
        this.wheelSeparation = wheelSeparation;
        strings[0] = wheelSeparation*2/3; strings[1] = strings[0];
        prevPoint = calculatePoint().point;
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                calculateScale();
                repaint();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                calculateScale();
                repaint();
            }
        });
        
        timingSource = new SwingTimerTimingSource(30, TimeUnit.MILLISECONDS);
        timingSource.init();
    }
    
    public void removeNotify() {
        super.removeNotify();
        timingSource.dispose();
    }
    
    private final TimingTarget timingTarget = new TimingTargetAdapter() {
        private double previous;
        
        @Override
        public void begin(Animator source) {
            super.begin(source);
            previous = 0;
            setRunning(true);
        }

        @Override
        public void timingEvent(Animator source, double fraction) {
            super.timingEvent(source, fraction);
            for (int i = 0; i < 2; i++) {
                double diff = distance[i] * (fraction - previous);
                pos[i] += diff;
                strings[i] += wheelRadius * diff;
            }
            previous = fraction;
            repaint();
        }

        @Override
        public void end(Animator source) {
            super.end(source);
            setRunning(false);
        }
    };
    
    @Override
    public void moveWheels(final int... w) {
        if (animator != null)
            animator.stop();
        double max = Math.max(Math.abs(w[0]), Math.abs(w[1]));
        long duration = (long) (max / speed);
        setRunning(true);
        if (duration > 0) {
            arraycopy(w, 0, distance, 0, 2);
            animator = new Animator.Builder(timingSource)
                .setDuration(duration, TimeUnit.MILLISECONDS)
                .addTarget(timingTarget)
                .build();
            animator.start();
        } else
            setRunning(false);
    }

    private static class Result {
        private Point2D point;
        /** The angle toward the +Y direction of each string from the wheel centre,
         * updated by {@link #calculatePoint()} */
        private double[] angles = new double[2];
    }
    
    private Result calculatePoint() {
        Result r = new Result();
        final double[] l = new double[2];
        for (int i = 0; i < 2; i++)
            l[i] = Math.sqrt(wheelRadius * wheelRadius + strings[i] * strings[i]);
        // find the angle of l[0] with the cosine rule
        double cosAlpha = (l[0] * l[0] + wheelSeparation * wheelSeparation - l[1] * l[1]) 
                / ( 2 * l[0] * wheelSeparation);
        double alpha = Math.acos(cosAlpha);
        r.point = new Point2D.Double(cosAlpha * l[0], Math.sin(alpha) * l[0]);
        // the angle is the angle between l and s, plus the angle between l 
        // and the horizontal
        r.angles[0] = Math.atan(wheelRadius / strings[0]) + alpha;
        cosAlpha = (l[1] * l[1] + wheelSeparation * wheelSeparation - l[0] * l[0]) 
                / ( 2 * l[1] * wheelSeparation);
        alpha = Math.acos(cosAlpha);
        r.angles[1] = Math.atan(wheelRadius / strings[1]) + alpha;
        return r;
    }

    private void calculateScale() {
        // Scale the whole thing to fit the window
        double xAspect = (double) getWidth() / (wheelSeparation + 2*wheelRadius);  
        double yAspect = (double) getHeight() / (wheelSeparation + 2*wheelRadius);
        scale = Math.min(xAspect, yAspect);
    }
    
    private void drawWheel(Graphics2D g) {
        g.setColor(Color.GRAY);
        g.setStroke(wheelStroke);
        g.draw(wheelShape);
    }
    
    private void drawString(Graphics2D g, double length) {
        g.setColor(Color.RED);
        g.setStroke(stringStroke);
        g.draw(new Line2D.Double(0, -1, length, -1));
    }
    
    @Override
    protected void paintComponent(Graphics gr) {
        super.paintComponent(gr);
        Graphics2D g = (Graphics2D) gr;
        AffineTransform t, t1;
        
        g.addRenderingHints(renderingHints);
        g.scale(scale, scale);
        
        Result r = calculatePoint();
        
        // Draw the line on the background
        if (drawing) {
            Graphics2D og = output.createGraphics();
            og.addRenderingHints(renderingHints);
            t = og.getTransform();
            og.scale(output.getWidth() / wheelSeparation, output.getWidth() / wheelSeparation);
            og.setColor(Color.BLACK);
            og.setStroke(drawStroke);
            og.draw(new Line2D.Double(prevPoint, r.point));
            og.setTransform(t);
        }
        prevPoint = r.point;
        
        // Draw the background
        t = g.getTransform();
        g.translate(wheelRadius, wheelRadius);
        g.scale(wheelSeparation / output.getWidth(), wheelSeparation / output.getWidth());
        g.drawImage(output, null, 0, 0);
        g.setTransform(t);

        // Draw the strings and wheels
        t = g.getTransform();
        g.translate(wheelRadius, wheelRadius);
        g.scale(wheelRadius, wheelRadius);
        t1 = g.getTransform();
        g.rotate(r.angles[0]);
        drawString(g, strings[0] / wheelRadius);
        g.setTransform(t1);
        g.rotate(pos[0]);
        drawWheel(g);
        g.setTransform(t);
        
        t = g.getTransform();
        g.translate(wheelSeparation + wheelRadius, wheelRadius);
        g.scale(-wheelRadius, wheelRadius);
        t1 = g.getTransform();
        g.rotate(r.angles[1]);
        drawString(g, strings[1] / wheelRadius);
        g.setTransform(t1);
        g.rotate(pos[1]);
        drawWheel(g);
        g.setTransform(t);
    }
    
    private void setRunning(boolean running) {
        firePropertyChange("running", this.running, this.running = running);
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }

    public boolean isDrawing() {
        return drawing;
    }

    @Override
    public void setDrawing(boolean drawing) {
        this.drawing = drawing;
    }

}
