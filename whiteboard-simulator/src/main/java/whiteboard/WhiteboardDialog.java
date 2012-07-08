package whiteboard;

import static java.lang.Boolean.FALSE;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

public class WhiteboardDialog extends JPanel {
    
    private final JTextField instructionField;
    private final AbstractButton goButton;
    private final WhiteboardPanel whiteboard;

    public WhiteboardDialog() {
        super(new BorderLayout());
        
        instructionField = new JTextField("D -1 0 D 0 -0.5 M 0 -0.5 D 1 0 D 0 1");
        goButton = new JToggleButton("Go");
        JPanel p = new JPanel(new BorderLayout());
        p.add(instructionField, BorderLayout.CENTER);
        p.add(goButton, BorderLayout.LINE_END);
        add(p, BorderLayout.NORTH);
        
        whiteboard = new WhiteboardPanel(25, 500, 800);
        whiteboard.addPropertyChangeListener(runningListener);
        add(whiteboard, BorderLayout.CENTER);

        goButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED && !whiteboard.isRunning())
                    step();
            }
        });
    }
    
    private PropertyChangeListener runningListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if ("running".equals(e.getPropertyName()) 
                    && FALSE.equals(e.getNewValue()) 
                    && goButton.isSelected()) {
                step();
            }
        }
    };
    
    private void step() {
        double left = Double.NaN;
        String[] sp = new String[] { "", instructionField.getText() };
        
        try {
            parseloop: while(true) {
                sp = sp[1].split(" +", 2);
                String token = sp[0].toUpperCase();
                if ("D".equals(token))
                    whiteboard.setDrawing(true);
                else if ("M".equals(token))
                    whiteboard.setDrawing(false);
                else {
                    try {
                        if (Double.isNaN(left))
                            left = Double.parseDouble(token);
                        else {
                            double right = Double.parseDouble(token);
                            whiteboard.moveWheels(left, right);
                            break parseloop;
                        }
                    } catch (NumberFormatException e) {
                        throw new ParseException(e);
                    }
                }
                
                if (sp.length == 1) {
                    throw new ParseException();
                }
            };

            instructionField.setText(sp.length > 1 ? sp[1] : "");
            
        } catch (ParseException e) {
            goButton.setSelected(false);
        }
    }
    
    private static class ParseException extends Exception {
        public ParseException() {
        }
        public ParseException(Throwable cause) {
            super(cause);
        }
    }
}
