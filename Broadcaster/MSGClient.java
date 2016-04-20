import java.io.*;
import java.awt.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.event.DocumentEvent;

public class MSGClient extends JFrame
                        implements ActionListener,DocumentListener
{
  CTS        cts;
  JButton    sendButton;
  JButton    exitButton;
  JPanel     topPanel;
  JPanel     centerPanel;
  JPanel     bottomPanel;
  JLabel     label;
  JTextField tfield;
  Container  cp;

//====================================================================================================================
  public MSGClient()
  {
    topPanel    = new JPanel(new FlowLayout());
    centerPanel = new JPanel(new FlowLayout());
    bottomPanel = new JPanel(new FlowLayout());

    tfield      = new JTextField(40);
    label       = new JLabel("MSG WILL BE HERE");
    
    sendButton  = new JButton("Send");
    sendButton.setActionCommand ("SEND");
    sendButton.addActionListener(this);
    sendButton.setEnabled(false);

    exitButton  = new JButton("Exit");
    exitButton.setActionCommand ("EXIT");
    exitButton.addActionListener(this);

    tfield.getDocument().addDocumentListener(this);
    tfield.setActionCommand("SEND"); 
    tfield.addActionListener(this);
    topPanel.add(label);
    centerPanel.add(tfield);
    bottomPanel.add(sendButton);
    bottomPanel.add(exitButton);

    cts       = new CTS("127.0.0.1",12345,"boop",label);//"127.0.0.1"

    cp = getContentPane();
    cp.add(topPanel, BorderLayout.NORTH);
    cp.add(centerPanel, BorderLayout.CENTER);
    cp.add(bottomPanel, BorderLayout.SOUTH);
    setupMainFrame();
  }
  public void setupMainFrame()
  {
    Toolkit   tk = Toolkit.getDefaultToolkit();
    Dimension d  = tk.getScreenSize();
    setSize(d.width/2, d.height/2);
    setLocation(d.width/4, d.height/4);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setTitle("WebCrawler Frame");
    setVisible(true);
  }
//====================================================================================================================
  public void actionPerformed(ActionEvent e)
  {
     if(e.getActionCommand().equals("SEND"))// getActionCommand can cause issues with certain swing components (Timer), better to use e.getSource() == exitButton
     {
       cts.sendMessage(tfield.getText().trim());
       label.setText("Sent: " + tfield.getText().trim());
       tfield.setText("");
     }
     else if(e.getActionCommand().equals("EXIT"))
     {
        System.exit(1);
     }
  }
//====================================================================================================================
  public void changedUpdate(DocumentEvent e1)
  {
  }
  public void insertUpdate(DocumentEvent e1)
  {
    if(tfield.getText().trim().equals(""))
      sendButton.setEnabled(false);
    else
      sendButton.setEnabled(true);
  }
  public void removeUpdate(DocumentEvent e1)
  {
    if(tfield.getText().trim().equals(""))
      sendButton.setEnabled(false);
    else
      sendButton.setEnabled(true);
  }
//====================================================================================================================
}
