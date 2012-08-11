package whiteboard;

import java.beans.PropertyChangeListener;

// TODO: This doesn't belong in this project
public interface Whiteboard {
    public void setDrawing(boolean drawing);
    public boolean isRunning();
    public void moveWheels(final int... w);
    public void addPropertyChangeListener(PropertyChangeListener runningListener);
}
