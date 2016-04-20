import java.awt.*;
import java.lang.*;
import java.awt.event.*;
import java.io.*;
import java.text.ParseException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.GroupLayout.*;

public class ClientLoginDialog extends JDialog
                                 implements ActionListener,DocumentListener
{

  CTS cts;
  JLabel     userLabel;
  JLabel     passwordLabel;

  JTextField userTextField;
  JTextField passwordTextField;

  JButton    loginButton;
  JButton    registerButton;

  Container cp;
  JPanel center;
  JPanel bottom;
  ChatClient chatClient;

  public ClientLoginDialog(ChatClient cc,CTS cts)
  {
    chatClient        = cc;
    cts               = cts;
    userLabel         = new JLabel("User:");
    passwordLabel     = new JLabel("Password:");
    userTextField     = new JTextField(25);//caps the usernames and passwords at 25 character maximum.
    passwordTextField = new JTextField(25);
  
    userTextField.getDocument().addDocumentListener(this);
    passwordTextField.getDocument().addDocumentListener(this);

    bottom = new JPanel(new FlowLayout());
    center = new JPanel(new FlowLayout());
  /*GROUP LAYOUT STUFF*****************************************************************/
  /**/GroupLayout layout = new GroupLayout(center);
  /**/center.setLayout(layout);
  /**/layout.setAutoCreateGaps(true);
  /**/layout.setAutoCreateContainerGaps(true);
  /**/GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
  /**/
  /**/ hGroup.addGroup(layout.createParallelGroup().addComponent(userLabel).addComponent(passwordLabel));
  /**/ hGroup.addGroup(layout.createParallelGroup().addComponent(userTextField).addComponent(passwordTextField));
  /**/ layout.setHorizontalGroup(hGroup);
  /**/
  /**/GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
  /**/
  /**/vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
  /**/                  addComponent(userLabel).addComponent(userTextField));
  /**/vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
  /**/                  addComponent(passwordLabel).addComponent(passwordTextField));
  /**/layout.setVerticalGroup(vGroup);
  /***********************************************************************************/
 
    //loginButton settings
    loginButton = new JButton("Login");
    loginButton.setVerifyInputWhenFocusTarget(false);
    loginButton.setActionCommand ("LOGIN");
    loginButton.addActionListener(this);
    loginButton.setEnabled(false);
    bottom.add(loginButton);
    getRootPane().setDefaultButton(loginButton);

    //registerButton settings
    registerButton = new JButton("Register");
    registerButton.setEnabled(false);
    registerButton.setActionCommand ("REGISTER");
    registerButton.addActionListener(this);
    bottom.add(registerButton);

    cp = getContentPane();
    cp.add(center, BorderLayout.CENTER);
    cp.add(bottom, BorderLayout.SOUTH);
    setupMainFrame();
  }
//====================================================================================================================
  public void setupMainFrame()
  {
    Toolkit   tk = Toolkit.getDefaultToolkit();
    Dimension d  = tk.getScreenSize();
    setSize(350, 150);
    setMinimumSize(new Dimension(350, 150));
    setLocation(d.width/4, d.height/4);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    setTitle("Login...");
    setVisible(true);
  }
//=================================================================================================================
  public void actionPerformed(ActionEvent e)
  {
     if(e.getActionCommand().equals("LOGIN"))
     {
       cts = new CTS("127.0.0.1",12345,userTextField.getText().trim(),chatClient);
       cts.sendMessage("+LOGIN " + userTextField.getText().trim().replaceAll("\\W","") + " " + passwordTextField.getText().trim().replaceAll("\\W", ""));
       passwordTextField.setText("");
     }
     else if(e.getActionCommand().equals("REGISTER"))
     {
       cts = new CTS("127.0.0.1",12345,userTextField.getText().trim(), chatClient);
       cts.sendMessage("+REGISTER " + userTextField.getText().trim().replaceAll("\\W", "") + " " + passwordTextField.getText().trim().replaceAll("\\W", ""));
     }
  }
//=================================================================================================================
  public void changedUpdate(DocumentEvent e1){}
  public void insertUpdate(DocumentEvent e1)
  {
    if(userTextField.getText().trim().equals("") || passwordTextField.getText().trim().equals(""))
    {
     loginButton.setEnabled(false); 
     registerButton.setEnabled(false);  
    }
    else
    {
      loginButton.setEnabled(true);
      registerButton.setEnabled(true);
    }
  }
  public void removeUpdate(DocumentEvent e1)
  {
    if(userTextField.getText().trim().equals("") || passwordTextField.getText().trim().equals(""))
    {
     loginButton.setEnabled(false); 
     registerButton.setEnabled(false);  
    }
    else
    {
      loginButton.setEnabled(true);
      registerButton.setEnabled(true);
    }
  }
//=================================================================================================================
}