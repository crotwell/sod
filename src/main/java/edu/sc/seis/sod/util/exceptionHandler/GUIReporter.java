package edu.sc.seis.sod.util.exceptionHandler;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: This class can be used to display the GUI showing the exception
 * along with useful information. It also shows the stackTrace. It also gives
 * the option of saving the exception stack trace along with other useful
 * information added by the user. Created: Thu Jan 31 16:39:57 2002
 * 
 * @author Srinivasa Telukutla 
 * @version
 */
public class GUIReporter implements ExceptionReporter {

    public void report(String message, Throwable e, List sections) {
        if(displayFrame != null) {
            if(atMostOneAtATime) {
                // in this case, there is already a GUIReporter up, with a
                // previous exception. So, we bounce this exception. The
                // GlobalExceptionHandler will send it to the log files, so
                // there is no reason to annoy the user with a follow up
                // problem that may have been caused by the previous one
                return;
            } else {
                // user want to see multiple frames, create a new one
                displayFrame = null;
                displayPanel = null;
            }
        }
        this.message = message;
        this.e = e;
        this.sections = sections;
        createFrame();
        displayPanel.add(createGUI(e, message, sections), BorderLayout.CENTER);
        displayPanel.revalidate();
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                displayFrame.pack();
                displayFrame.show();
            }
        });
    }

    /**
     * Determines if the display will show more than one exception at a time.
     * The default is TRUE.
     */
    public void setAtMostOneAtATime(boolean b) {
        atMostOneAtATime = b;
    }

    private JTabbedPane createGUI(Throwable e, String message, List sections) {
        JTabbedPane tabbedPane = new JTabbedPane();
        if(greeting != null) {
            tabbedPane.addTab(greeting.getName(),
                              createTextArea(greeting.getContents()));
        }
        tabbedPane.addTab("Details", createTextArea(message));
        tabbedPane.addTab("Stack Trace",
                          createTextArea(ExceptionReporterUtils.getTrace(e)));
        Iterator it = sections.iterator();
        while(it.hasNext()) {
            Section sec = (Section)it.next();
            tabbedPane.addTab(sec.getName(), createTextArea(sec.getContents()));
        }
        Dimension dimension = new Dimension(800, 300);
        tabbedPane.setPreferredSize(dimension);
        tabbedPane.setMinimumSize(dimension);
        return tabbedPane;
    }

    private static JScrollPane createTextArea(String message) {
        JTextArea messageArea = new JTextArea();
        messageArea.setLineWrap(true);
        messageArea.setFont(new Font("Serif", Font.PLAIN, 14));
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        if(message != null) {
            messageArea.setText(message);
        }
        return new JScrollPane(messageArea);
    }

    private void createFrame() {
        displayFrame = new JFrame();
        displayFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        displayFrame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                displayPanel = null;
                displayFrame = null;
            }
        });
        displayPanel = new JPanel(new BorderLayout());
        displayPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        Dimension dimension = new Dimension(800, 400);
        displayPanel.setPreferredSize(dimension);
        displayFrame.setContentPane(displayPanel);
        displayFrame.setSize(dimension);
    }

    private JPanel createButtonPanel() {
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                displayFrame.dispose();
                displayFrame = null;
                displayPanel = null;
            }
        });
        JButton saveToFile = new JButton("Save");
        saveToFile.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ex) {
                try {
                    writeFile();
                } catch(IOException e) {
                    int result = JOptionPane.showConfirmDialog(displayPanel,
                                                               "We were unable to write to that file. Try again?",
                                                               "Trouble writing exception file",
                                                               JOptionPane.OK_CANCEL_OPTION,
                                                               JOptionPane.WARNING_MESSAGE);
                    if(result == JOptionPane.OK_OPTION) {
                        actionPerformed(null);
                    }
                }
            }

            public void writeFile() throws IOException {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new File(ExceptionReporterUtils.getClassName(e)
                        + ".txt"));
                int rtnVal = fileChooser.showSaveDialog(displayPanel);
                if(rtnVal == JFileChooser.APPROVE_OPTION) {
                    FileWriterReporter writer = new FileWriterReporter(fileChooser.getSelectedFile()
                            .getAbsoluteFile());
                    writer.report(message, e, sections);
                }
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        buttonPanel.add(saveToFile);
        return buttonPanel;
    }

    private static void setGreeting(String title, String contents) {
        greeting = new Section(title, contents);
    }

    public static void appendToGreeting(String title, String contents) {
        if(greeting == null) {
            setGreeting(title, contents);
        } else {
            greeting.setContents(greeting.getContents() + contents);
        }
    }

    public static void swapGreetingAndHandle(Throwable t, String temporaryGreeting) {
        Section currentGreeting = greeting;
        setGreeting("Information", temporaryGreeting);
        GlobalExceptionHandler.handle(t);
        greeting = currentGreeting;
    }

    private boolean atMostOneAtATime = true;

    private JFrame displayFrame;

    private static Section greeting;

    private String message;

    private Throwable e;

    private List sections;

    private JPanel displayPanel;

    private static Logger logger = LoggerFactory.getLogger(GUIReporter.class);
}// ExceptionHandlerGUI
