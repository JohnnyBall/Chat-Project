import java.awt.*;
import java.lang.*;
import java.awt.event.*;
import java.io.*;
import java.text.ParseException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.GroupLayout.*;

public class AddFriendDialog extends JDialog
                                 implements ActionListener,DocumentListener
{
  JLabel     friendLabel;

  JTextField friendTextField;

  JButton    addFriendButton;

  Container cp;
  JPanel center;
  JPanel bottom;

  ChatClient chatClient;

  public AddFriendDialog(ChatClient cc)
  {
    chatClient      = cc;
    friendLabel     = new JLabel("Buddy:");
    friendTextField = new JTextField(25);
  
    friendTextField.getDocument().addDocumentListener(this);

    bottom = new JPanel(new FlowLayout());
    center = new JPanel(new FlowLayout());
  /*GROUP LAYOUT STUFF*****************************************************************/
    GroupLayout layout = new GroupLayout(center);
    center.setLayout(layout);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
    
     hGroup.addGroup(layout.createParallelGroup().addComponent(friendLabel));
     hGroup.addGroup(layout.createParallelGroup().addComponent(friendTextField));
     layout.setHorizontalGroup(hGroup);
    
    GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
    
    vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
                    addComponent(friendLabel).addComponent(friendTextField));
    layout.setVerticalGroup(vGroup);
  /***********************************************************************************/
    //loginButton settings
    addFriendButton = new JButton("Add Friend");
    addFriendButton.setVerifyInputWhenFocusTarget(false);
    addFriendButton.setActionCommand ("ADD");
    addFriendButton.addActionListener(this);
    addFriendButton.setEnabled(false);
    bottom.add(addFriendButton);
    getRootPane().setDefaultButton(addFriendButton);

    cp = getContentPane();
    cp.add(center, BorderLayout.CENTER);
    cp.add(bottom, BorderLayout.SOUTH);
    setupMainFrame();
  }
//=================================================================================================================
  public void setupMainFrame()
  {
    Toolkit   tk = Toolkit.getDefaultToolkit();
    Dimension d  = tk.getScreenSize();
    setSize(200, 120);
    setMinimumSize(new Dimension(200, 120));
    setLocation(d.width/4, d.height/4);
    setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    setTitle("Add Friend...");
    setVisible(true);
  }
//=================================================================================================================
  public void actionPerformed(ActionEvent e)
  {
     String tempBudStr;
     if(e.getActionCommand().equals("ADD"))
     {
       tempBudStr = friendTextField.getText().trim().replaceAll("\\W","");
       friendTextField.setText("");
       if(!chatClient.isBuddy(tempBudStr))
       {
         chatClient.cts.sendMessage("+ADDF " + tempBudStr);
         System.out.println("adding friend "+ tempBudStr);
         this.setVisible(false);
         JOptionPane.showMessageDialog(null,"Friend request sent to user:"+tempBudStr+", if the user is an actual user and is on line then they will receive the message, if they are not a real user then they will not receive the message. ","Friend Request Sent...", JOptionPane.INFORMATION_MESSAGE);
        }
        else if (!chatClient.cts.id.equals(tempBudStr))
        {
          JOptionPane.showMessageDialog(null, "Why are you trying to add yourself?", "YOU CANT BE YOUR FRIEND!", JOptionPane.ERROR_MESSAGE);
        }
       else 
         JOptionPane.showMessageDialog(null, "This user is already your Buddy List! Perhaps try someone else?", "Buddy Already in Buddy List!", JOptionPane.ERROR_MESSAGE);
     }
  }
//=================================================================================================================
  public void changedUpdate(DocumentEvent e1){}
  public void insertUpdate(DocumentEvent e1)
  {
    if(friendTextField.getText().trim().equals(""))
     addFriendButton.setEnabled(false); 
    else
      addFriendButton.setEnabled(true);
  }
  public void removeUpdate(DocumentEvent e1)
  {
    if(friendTextField.getText().trim().equals(""))
     addFriendButton.setEnabled(false);   
    else
      addFriendButton.setEnabled(true);
  }
//=================================================================================================================
}