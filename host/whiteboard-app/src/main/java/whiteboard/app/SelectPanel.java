package whiteboard.app;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import whiteboard.preview.PreviewPanel;

public class SelectPanel extends JPanel {
    
    private final JFormattedTextField reelSpacingField = new JFormattedTextField(70.0);
    private final JFormattedTextField startXField = new JFormattedTextField(20.0);
    private final JFormattedTextField startYField = new JFormattedTextField(20.0);
    private final JTextField filenameField = new JTextField();
    private final JButton chooseFileButton 
            = new JButton(new AbstractAction("Choose file") {
        @Override
        public void actionPerformed(ActionEvent ev) {
            JFileChooser chooser = new JFileChooser();
            int ret = chooser.showOpenDialog(SelectPanel.this);
            if (ret == JFileChooser.APPROVE_OPTION)
                filenameField.setText(chooser.getSelectedFile().getPath());
        }
    });
    private final JButton previewButton
            = new JButton(new AbstractAction("Preview") {
        @Override
        public void actionPerformed(ActionEvent ev) {
            try {
                String uri = new File(filenameField.getText()).toURI().toASCIIString();
                PreviewPanel preview = new PreviewPanel(uri);
                JDialog d = new JDialog(SwingUtilities.getWindowAncestor(SelectPanel.this));
                d.setContentPane(preview);
                d.pack();
                d.setVisible(true);
            } catch (IOException e) {
                e.printStackTrace(); // todo: show a dialog
            }
        }
    });
    private final JButton simulateButton = new JButton("Simulate");

    public SelectPanel() {
        super(new MigLayout("", "[right]rel[300lp,grow,fill]"));
        
        add(new JLabel("Filename"));
        add(filenameField, "grow, span");
        add(chooseFileButton, "skip 1, wrap, grow 0");
        add(new JLabel("Reel spacing"));
        add(reelSpacingField, "wrap");
        add(new JLabel("Start position left"));
        add(startXField, "wrap");
        add(new JLabel("Start position down"));
        add(startYField, "wrap");
        add(previewButton, "span, split 2");
        add(simulateButton, "wrap");
    }
    
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setContentPane(new SelectPanel());
        f.pack();
        f.setVisible(true);
    }
}
