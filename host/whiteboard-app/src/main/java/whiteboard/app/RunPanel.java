package whiteboard.app;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import whiteboard.SerialController;
import whiteboard.Whiteboard;

public class RunPanel extends JPanel implements Whiteboard {

    private enum Side { LEFT, RIGHT };
    private enum Direction { CCW, CW };
    private class MoveButton extends JButton {
        private Side side;
        private Direction direction;
        public MoveButton(Side side, Direction direction) {
            super(direction == Direction.CCW ? "\u21b6" : "\u21b7");
            this.side = side;
            this.direction = direction;
        }
    }
    
    List<MoveButton> moveButtons = Arrays.asList(
        new MoveButton(Side.LEFT, Direction.CCW), 
        new MoveButton(Side.LEFT, Direction.CW),
        new MoveButton(Side.RIGHT, Direction.CCW), 
        new MoveButton(Side.RIGHT, Direction.CW)
    );
    private MoveButton currentButton;
    private final JButton startButton = new JButton("Go!");
    private boolean running = false;
    private SerialController controller;
    private static final int bump = 2;
    
    public RunPanel(SerialController controller) {
        super(new MigLayout("", "center"));
        
        this.controller = controller;
        controller.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent ev) {
                if ("running".equals(ev.getPropertyName()))
                    setRunning(Boolean.TRUE.equals(ev.getNewValue()));
            }
        });
        
        Iterator<MoveButton> it = moveButtons.iterator();
        add(it.next(), "split 4");
        add(it.next());
        add(it.next());
        add(it.next(), "wrap");
        
        add(startButton);
        
        for (MoveButton b: moveButtons)
            b.addMouseListener(moveButtonListener);
    }
    
    private MouseListener moveButtonListener = new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent ev) {
            currentButton = null;
        }
        
        @Override
        public void mousePressed(MouseEvent ev) {
            currentButton = (MoveButton) ev.getSource();
            bump();
        }
    };
    
    private void bump() {
        if (currentButton == null)
            return;
        int value = bump;
        if (currentButton.direction == Direction.CCW)
            value = -value;
        controller.run(
                currentButton.side == Side.LEFT ? value : 0,
                currentButton.side == Side.RIGHT ? value : 0
        );
    }

    @Override
    public void setDrawing(boolean drawing) {
    }
    
    private void setRunning(boolean running) {
        // If a move button is down, start moving again
        boolean needsBump = !running && this.running;
        firePropertyChange("running", this.running, this.running = running);
        if (needsBump)
            bump();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void moveWheels(double... w) {
        // TODO Auto-generated method stub
        
    }
}
