import java.io.*;
import java.awt.*;
import java.awt.event.*;
import static java.lang.System.exit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.*;

public class FileChooser extends JPanel implements ActionListener {
    static private final String newline = "\n";
    JButton openButton, saveButton;
    JTextArea log;
    JFileChooser fc;
    
    File caminhoPadrao;

    public FileChooser(File path) {
        super(new BorderLayout());

        //Create the log first, because the action listeners
        //need to refer to it.
        caminhoPadrao = path;
        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

        //Create a file chooser
        fc = new JFileChooser();

        openButton = new JButton("Open a File...",
                                 createImageIcon("images/Open16.gif"));
        openButton.addActionListener(this);


        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(openButton);

        //Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {

        //Handle open button action.
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(FileChooser.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                //This is where a real application would open the file.
                log.append("Create: " + file.getAbsolutePath()+ "." + newline);
                try {
                    caminhoPadrao = new File(caminhoPadrao.getName()+"/"+file.getName());
                    CopiaArquivo.copyFile(file, caminhoPadrao);
                    log.append("Finalizado.\n");
                    log.append("Reinicie seu Torrent para finalizar.\n");
                } catch (IOException ex) {
                    Logger.getLogger(FileChooser.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
        } 
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = FileChooser.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

}
