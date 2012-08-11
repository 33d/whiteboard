package whiteboard;

import static java.lang.Boolean.FALSE;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;

public class WhiteboardParser {
    
    private final Whiteboard whiteboard;
    private boolean stepping = false;
    private final ByteArrayInputStream in;

    public WhiteboardParser(Whiteboard whiteboard, byte[] data) {
        this.whiteboard = whiteboard;
        whiteboard.addPropertyChangeListener(runningListener);
        in = new ByteArrayInputStream(data);
    }
    
    public void start() {
        step();
    }
    
    private PropertyChangeListener runningListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if ("running".equals(e.getPropertyName()) 
                    && FALSE.equals(e.getNewValue()) 
                    && !stepping) {
                while (!whiteboard.isRunning() && in.available() > 0)
                    step();
            }
        }
    };
    
    private String nextToken() {
        StringBuilder b = new StringBuilder();
        int c;
        while (true) {
            c = in.read();
            if (c == ' ' || c == -1)
                break;
            b.append((char) c);
        }
        return b.toString();
    }
    
    private void step() {
        stepping = true;
        int left = Integer.MIN_VALUE;
        
        try {
            parseloop: while(true) {
                String token = nextToken();
                if ("D".equals(token))
                    whiteboard.setDrawing(true);
                else if ("M".equals(token))
                    whiteboard.setDrawing(false);
                else {
                    try {
                        if (Integer.MIN_VALUE == left)
                            left = Integer.parseInt(token);
                        else {
                            int right = Integer.parseInt(token);
                            whiteboard.moveWheels(left, right);
                            break parseloop;
                        }
                    } catch (NumberFormatException e) {
                        throw new ParseException(e);
                    }
                }
            };

        } catch (ParseException e) {
        }
        stepping = false;
    }
    
    private static class ParseException extends Exception {
        public ParseException() {
        }
        public ParseException(Throwable cause) {
            super(cause);
        }
    }
}
