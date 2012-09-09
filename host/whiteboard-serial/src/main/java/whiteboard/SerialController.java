package whiteboard;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import javax.swing.SwingUtilities;

public class SerialController implements Closeable {

    private final SerialPort port;
    private final OutputStream out;
    private final InputStream in;
    private boolean running = false;
    private final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

    public SerialController(String portName) throws IOException {
        try {
            CommPortIdentifier portIdentifier = 
                    CommPortIdentifier.getPortIdentifier(portName);
            if (portIdentifier.isCurrentlyOwned())
                throw new IOException("Port " + portName + " is in use");

            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

            if (!(commPort instanceof SerialPort))
                throw new IOException(portName + " isn't a serial port");

            port = (SerialPort) commPort;
            port.setSerialPortParams(9600, SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            port.addEventListener(portListener);
            
            in = port.getInputStream();
            out = port.getOutputStream();

        } catch (NoSuchPortException e) {
            throw new IOException(e);
        } catch (PortInUseException e) {
            throw new IOException(e);
        } catch (UnsupportedCommOperationException e) {
            throw new IOException(e);
        } catch (TooManyListenersException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void run(int motor1, int motor2) {
        if (running)
            throw new IllegalStateException("Already running");
        setRunning(true);
        port.notifyOnDataAvailable(true);
        try {
            out.write(String.format("M %d %d ", motor1, motor2).getBytes("US-ASCII"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void stop() {
        setRunning(false);
    }
    
    public void close() {
        port.close();
    }
    
    private SerialPortEventListener portListener = new SerialPortEventListener() {
        @Override
        public void serialEvent(SerialPortEvent ev) {
            try {
                if (in.read() > -1) {
                    port.notifyOnDataAvailable(false);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            setRunning(false);
                        }
                    });
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };
    
    private void setRunning(boolean running) {
        propertySupport.firePropertyChange("running", this.running, this.running = running);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertySupport.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertySupport.removePropertyChangeListener(l);
    }
}
