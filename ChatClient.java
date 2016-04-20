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
import java.util.Hashtable;
import javax.sound.sampled.*;
import java.applet.*;
import javax.net.ssl.*;
import java.security.*;


 class ChatClient extends JFrame
                                  implements ActionListener
{
//STATUS/////////////////////////////////
//public static final int OFF_LINE = 0;
//public static final int ON_LINE  = 1;
//public static final int AWAY     = 2;
/////////////////////////////////////////
  CTS                 cts;
  JPanel              topPanel;
  JPanel              bottomPanel;
  JLabel              userNameLabel;
  Container           cp;
  
  JTable              table;

  JPopupMenu          popupMenu;
  MouseListener       popupListener;

  JButton             addFriendButton;
  JButton             chatButton;
  JButton             removeFriendButton;
  
  boolean             logedIn;
  ClientLoginDialog   clientLoginDialog;
  AddFriendDialog     addFriendDialog;

  DefaultListModel  <BuddyLabel>       buddyList;
  JList             <BuddyLabel>       buddyLabelJList;
  JScrollPane                          scrollPane;
  int                                  buddyListSize;
  Hashtable<String,ClientChatDialog>   chatDialogTable;
//=================================================================================================================
   ChatClient()
  {
    buddyListSize   = 0;
    userNameLabel   = new JLabel();
    topPanel        = new JPanel(new FlowLayout());
    bottomPanel     = new JPanel(new FlowLayout());
    chatDialogTable = new Hashtable<String,ClientChatDialog>();
    
    topPanel.add(userNameLabel);

    buddyList       = new DefaultListModel<BuddyLabel>();
    
    buddyLabelJList = new JList<BuddyLabel>(buddyList);
    buddyLabelJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    buddyLabelJList.setBackground(Color.WHITE);
    buddyLabelJList.setFont(new Font("Courier",Font.BOLD,12));
    buddyLabelJList.setCellRenderer(new BuddyListRender());

    scrollPane      = new JScrollPane(buddyLabelJList);
    logedIn         = false;
    cts             = null;

    popupListener   = new PopupListener();
    buddyLabelJList.addMouseListener(popupListener);

    popupMenu       = new JPopupMenu("Friend Menu");
    popupMenu.add(newItem("Message Selected","MSGFRIEND",this,"SendMessage to Selected Friend."));
    popupMenu.add(newItem("Delete Selected","DELETEFRIEND",this,"Delete Selected Friend."));

    addFriendButton = new JButton("Add Friend");
    addFriendButton.setActionCommand ("NEWFRIEND");
    addFriendButton.addActionListener(this);
    bottomPanel.add(addFriendButton);

    removeFriendButton = new JButton("Remove Friend");
    removeFriendButton.setActionCommand ("DELETEFRIEND");
    removeFriendButton.addActionListener(this);
    bottomPanel.add(removeFriendButton);

    chatButton = new JButton("Chat");
    chatButton.setActionCommand ("MSGFRIEND");
    chatButton.addActionListener(this);
    bottomPanel.add(chatButton);

    cp = getContentPane();
    cp.add(topPanel ,BorderLayout.NORTH);
    cp.add(scrollPane, BorderLayout.CENTER);
    cp.add(bottomPanel, BorderLayout.SOUTH);

    setJMenuBar(newMenuBar());

    setupMainFrame();
    clientLoginDialog = new ClientLoginDialog(this,cts);
  }
//=================================================================================================================
  public void setupMainFrame()
  {
    Toolkit   tk = Toolkit.getDefaultToolkit();
    Dimension d  = tk.getScreenSize();
    this.setSize(400, 500);
    this.setMinimumSize(new Dimension(310, 300));
    this.setLocation(d.width/4, d.height/4);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setTitle("Client User Frame");
    this.setVisible(false);
  }
//=================================================================================================================
//Subclass of mouse adapter for mouse listener
class PopupListener extends MouseAdapter
{
  public void mouseClicked(MouseEvent e)
  {
    if(e.getClickCount() == 2)
      chatButton.doClick();
  }
  //=============================================
  public void mousePressed(MouseEvent e)
  {
    maybeShowPopup(e);
  }
  //=============================================
  public void mouseReleased(MouseEvent e)
  {
    maybeShowPopup(e);
  }
  //=============================================
  private void maybeShowPopup(MouseEvent e)
  {
    int index;
    if (e.isPopupTrigger())
    {
      index = buddyLabelJList.locationToIndex(new Point(e.getX(),e.getY()));
      buddyLabelJList.setSelectedIndex(index);
      popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }
  }
}//end of mouse adapter
//=================================================================================================================
  public boolean isBuddy(String buddyToCheck)//Checks to see if the buddy is in the buddylist(DefaultListModel)
  {
    BuddyLabel tempBud;
    boolean    found   = false;
    int        counter = 0;
    
    while(counter < buddyList.size() && !found)
    {
      found  = buddyToCheck.equals(buddyList.elementAt(counter).buddyName);
      counter++;
    }
    return found;
  }
//=================================================================================================================
  public void addBuddy(int status,String buddyToAdd)
  {
    System.out.println("Adding buddy: "+status+" "+buddyToAdd);
    BuddyLabel tempBud = new BuddyLabel(status,buddyToAdd);
    buddyList.addElement(tempBud);
  }
//=================================================================================================================
  public void removeBuddy(String buddyToRemove)
  {
    int     counter = 0;
    boolean found   = false;

    while(counter < buddyList.size() && !found)
    {
      found = buddyToRemove.equals(buddyList.elementAt(counter).buddyName);
      if(found)
      {
        buddyList.remove(counter);
        System.out.println(buddyToRemove + " removed!!!!");
      }
      counter++;
    }
  }
//=================================================================================================================
  public boolean changeStatusOfBuddy(int status,String buddyToCheck)
  {
    BuddyLabel tempBud;
    boolean    found   = false;
    int        counter = 0;
    ClientChatDialog tempCCD;

    while(counter < buddyList.size() && !found)
    {
      found   = buddyToCheck.equals(buddyList.elementAt(counter).buddyName);
      if(found)
      {
        tempBud = new BuddyLabel(status,buddyToCheck); 
        tempCCD = chatDialogTable.get(buddyToCheck);

        if(tempCCD != null  && status == BuddyLabel.OFF_LINE)
        {
          tempCCD.sendButton.setEnabled(false);
          tempCCD.chatTextArea.setEnabled(false);
          tempCCD.isAcceptingFiles   =  false;
          if(tempCCD.isVisible())
            JOptionPane.showMessageDialog(null,buddyToCheck+ " is offline, messaging to this user has been disabled.");
        }
        else if(tempCCD != null && status == BuddyLabel.ON_LINE)
        {
          tempCCD.sendButton.setEnabled(true);
          tempCCD.chatTextArea.setEnabled(true);
          tempCCD.isAcceptingFiles   =  true;
        }
        buddyList.set(counter,tempBud);
        System.out.println(buddyToCheck + " status changed!!!!");
      }
      counter++;
    }
    return found; 
  }
//=================================================================================================================
  public void processFriendListUpdateAtStart(String buddyList)
  {
    String[] splitStr;
    int      buddyCounter = 0;
    int      incrementer  = 2;
    splitStr              = buddyList.split(" ");
    buddyListSize         = Integer.parseInt(splitStr[1]);
    System.out.println("Buddy List Size: "+ buddyListSize);

    while(buddyCounter < buddyListSize)
    {
      this.addBuddy(Integer.parseInt(splitStr[incrementer]),splitStr[incrementer+1]);
      incrementer = incrementer + 2;
      buddyCounter++;
    }
  }
//=================================================================================================================
  public void processFriendRequest(String requester)
  {
    int selection;
    URL url = getClass().getResource("/Sounds/newplayer.wav");
    AudioClip clip = Applet.newAudioClip(url);
    clip.play();
    selection = JOptionPane.showConfirmDialog(null,requester + " would like to add you as a buddy, would you like to be his buddy?", "Add Buddy?", JOptionPane.YES_NO_OPTION);
    if(selection == JOptionPane.YES_OPTION)
    {
      cts.sendMessage("+FRACCEPTED "+ requester);
      selection = 0;
    }
  }
//=================================================================================================================
    public void sendMessageToDialog(String sender,String msg)
  {
    ClientChatDialog tempCCD;

    URL url;
    AudioClip clip;

    tempCCD = chatDialogTable.get(sender);
    if(tempCCD == null)
    {
      url = getClass().getResource("/Sounds/activated.wav");
      clip = Applet.newAudioClip(url);
      clip.play();

      tempCCD = new ClientChatDialog(sender,this);
      chatDialogTable.put(sender, tempCCD);// KEY BEING USERNAME OBJECT BEING THE CHAT DIALOG
    }
    if (!tempCCD.isVisible())
    {
       tempCCD.setVisible(true);
       url = getClass().getResource("/Sounds/hitsound.wav");
       clip = Applet.newAudioClip(url);
       clip.play();
    }
    else
       tempCCD.toFront();

     tempCCD.processMessage(msg);
  }
//=================================================================================================================
  public void processFileRequest(String sender,String name,String size)
  {
    ClientChatDialog tempCCD;
    URL              url;
    AudioClip        clip;
    int              selection;

    selection = JOptionPane.showConfirmDialog(null,sender+" wants to send you a file, FileName: "+name+" Size: "+ size+" bytes. Do you accept??", "File Request...", JOptionPane.YES_NO_OPTION);
    if(selection == JOptionPane.YES_OPTION)
    {
      tempCCD = chatDialogTable.get(sender);
      if(tempCCD == null)
      {
        tempCCD = new ClientChatDialog(sender,this);
        chatDialogTable.put(sender, tempCCD);// KEY BEING USERNAME OBJECT BEING THE CHAT DIALOG
      }
      if (!tempCCD.isVisible())
         tempCCD.setVisible(true);
      else
         tempCCD.toFront();

      tempCCD.fileProcessingBegginging(sender,name,size);
      url = getClass().getResource("/Sounds/whoosh.wav");
      clip = Applet.newAudioClip(url);
      clip.play();
    }
  }
//======================================================`===========================================================
    public void fileAccepted(String sender,String ip,String port)
  {
    ClientChatDialog tempCCD;

    tempCCD = chatDialogTable.get(sender);
    if (!tempCCD.isVisible())
       tempCCD.setVisible(true);
    else
       tempCCD.toFront();

     tempCCD.startSendingFile(ip,port);

  }
//=================================================================================================================
public void actionPerformed(ActionEvent e)
{
/////////////////////////////////////////////////NEW FRIEND BELOW
   if(e.getActionCommand().equals("NEWFRIEND"))
   {
     if(addFriendDialog == null)
       addFriendDialog = new AddFriendDialog(this);
     else if (!addFriendDialog.isVisible())
       addFriendDialog.setVisible(true);
     else
       addFriendDialog.toFront();
   }
/////////////////////////////////////////////////MSG FRIEND BELOW
   else if(e.getActionCommand().equals("MSGFRIEND"))
   { 
     int i = buddyLabelJList.getSelectedIndex();
     int selection;
     BuddyLabel tempBud;
     ClientChatDialog tempCCD;

     if(i != -1)
     { 
        tempBud = buddyList.elementAt(i);
        if(tempBud.buddyStatus != BuddyLabel.OFF_LINE)
        {
          System.out.println("Paging doctor " + tempBud.buddyName+"!");
          tempCCD = chatDialogTable.get(tempBud.buddyName);
          if(tempCCD == null)
          {
            tempCCD = new ClientChatDialog(tempBud.buddyName,this);
            chatDialogTable.put(tempBud.buddyName, tempCCD);// KEY BEING USERNAME OBJECT BEING THE CHAT DIALOG
          }

          if(!tempCCD.isVisible())
            tempCCD.setVisible(true);
          else
            tempCCD.toFront();
        }
        else
          JOptionPane.showMessageDialog(null,"This user is off line, please select someone else to chat with, off line message storage may be implemented later.");
     }
     else
       JOptionPane.showMessageDialog(null,"Select someone from the list.");
   }
/////////////////////////////////////////////////////////////////////////////////////DELETEFRIEND BELOW
   else if(e.getActionCommand().equals("DELETEFRIEND"))
   {
     int i = buddyLabelJList.getSelectedIndex();
     int selection;
     String buddyName;

     if(i != -1)
     { 
       selection = JOptionPane.showConfirmDialog(null,"Are you sure you want to remove this friend?", "Remove Friend?", JOptionPane.YES_NO_OPTION);
       if(selection == JOptionPane.YES_OPTION)
       {
         buddyName = buddyList.elementAt(i).buddyName;
         buddyList.remove(i);
         System.out.println(buddyName+ " Removed from buddy list!");
         cts.sendMessage("+DELETEFRIEND "+ buddyName);
       }
     }
     else
       JOptionPane.showMessageDialog(null,"Select someone from the list.");
   }
/////////////////////////////////////////////////////////////////////////////////////Statuses below
   else if(e.getActionCommand().equals("SS_AWAY"))
   {
      cts.sendMessage("+SETSTATUS " + BuddyLabel.AWAY);
   }
   else if(e.getActionCommand().equals("SS_ONLINE"))
   {
      cts.sendMessage("+SETSTATUS " + BuddyLabel.ON_LINE);
   }
   else if(e.getActionCommand().equals("INTERGLACTIC"))
   {
    URL url = getClass().getResource("/Sounds/intergalactic.wav");
    AudioClip clip = Applet.newAudioClip(url);
    clip.play();
   }
}//end of Action performed
//=================================================================================================================
  //MENUBAR SECTION FOR DISPLAYING THE MENU
private JMenuBar newMenuBar()
{
  JMenuBar menuBar;
  JMenu    subMenu;

  menuBar = new JMenuBar();
  subMenu = new JMenu("Status");
  subMenu.add(newItem("Set status to Away","SS_AWAY",this,"sets the status of your account to away... i mean is this explanation really needed?"));
  subMenu.add(newItem("Set status to Online","SS_ONLINE",this,"man you know what this does..."));
  menuBar.add(subMenu);
  subMenu = new JMenu("Friend");
  subMenu.add(newItem("New","NEWFRIEND",this,"Create New Friend"));
  subMenu.add(newItem("Message Selected","MSGFRIEND",this,"SendMessage to Selected Friend."));
  subMenu.add(newItem("Delete Selected","DELETEFRIEND",this,"Delete Selected Friend."));
  subMenu.add(newItem("InterGalactic","INTERGLACTIC",this,"Select me!!!"));
  menuBar.add(subMenu);

  return menuBar;
}
//=================================================================================================================
private JMenuItem newItem(String label,String actionCommand, ActionListener menuListener, String toolTipText)
{
  JMenuItem mItem;
  mItem = new JMenuItem(label);
  mItem.getAccessibleContext().setAccessibleDescription(toolTipText);
  mItem.setToolTipText(toolTipText);
  mItem.setActionCommand(actionCommand);
  mItem.addActionListener(menuListener);
  return mItem;
}
//=================================================================================================================
}//End of ChatClient
