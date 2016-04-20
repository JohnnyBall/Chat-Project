import java.awt.*;
import java.lang.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.ParseException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.GroupLayout.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.net.ssl.*;
import java.security.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

class ClientChatDialog extends JDialog
                                 implements ActionListener,DocumentListener, DropTargetListener, Runnable 
{
  JTextArea   chatTextArea;
  JButton     sendButton;

  ServerSocket serverSocket;
  Socket       sock;

  boolean    isAcceptingFiles;
  DropTarget dropTarget;
  File       fileForProcessing;
  long       fileLength;
  long       totalBytesRead;
  String     fileName;
  boolean    isServer;

  String      buddyUserName;
  String      yourUserName;
  JLabel      buddyNameLabel;
  JLabel      fileProgressLabel;
  ChatClient  chatClient;
  JEditorPane jeditorPane;

  JProgressBar progressBar;

  Container   cp;
  JPanel      top;
  JPanel      bottom;

  JScrollPane scrollPane;

  ClientChatDialog(String buddyID, ChatClient chatClient)
  {
    yourUserName       = chatClient.cts.id;
    this.buddyUserName = buddyID;
    this.chatClient    = chatClient;
    buddyNameLabel     = new JLabel("BuddyName: "+buddyUserName);
    fileProgressLabel   = new JLabel("FileProgressBar: ");
    chatTextArea       = new JTextArea("",5,60);
    jeditorPane        = new JEditorPane();
    isAcceptingFiles   = true;
    isServer           = false; 
    progressBar        = new JProgressBar(0, 100);

    jeditorPane.setContentType("text/html");
    jeditorPane.setEditable(false);
  
    chatTextArea.getDocument().addDocumentListener(this);
    chatTextArea.setLineWrap(true);
    chatTextArea.setWrapStyleWord(true);
    chatTextArea.setBackground(Color.LIGHT_GRAY);

    top        = new JPanel(new FlowLayout());
    bottom     = new JPanel(new FlowLayout());
    scrollPane = new JScrollPane(jeditorPane);
    
    top.add(buddyNameLabel);
    top.add(fileProgressLabel);
    top.add(progressBar);
    progressBar.setVisible(false);
    fileProgressLabel.setVisible(false);
    bottom.add(chatTextArea);

    sendButton = new JButton("Send");
    sendButton.setPreferredSize(new Dimension(150, 80));
    sendButton.setVerifyInputWhenFocusTarget(false);
    sendButton.setActionCommand ("SEND");
    sendButton.addActionListener(this);
    sendButton.setEnabled(false);
    bottom.add(sendButton);
    getRootPane().setDefaultButton(sendButton);

    cp = getContentPane();
    cp.add(top, BorderLayout.NORTH);
    cp.add(bottom, BorderLayout.SOUTH);
    cp.add(scrollPane, BorderLayout.CENTER);
    dropTarget = new DropTarget(jeditorPane, this);
    setupMainFrame();
  }
//====================================================================================================================
  public void setupMainFrame()
  {
    Toolkit   tk = Toolkit.getDefaultToolkit();
    Dimension d  = tk.getScreenSize();
    this.setSize(850,500);
    this.setMinimumSize(new Dimension(850,500));
    this.setLocation(d.width/4, d.height/4);
    setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    setTitle("Chatting with "+ buddyUserName);
    setVisible(false);
  }
//====================================================================================================================
  public void processMessage(String msg)
  {
    HTMLDocument htmldoc;
    Element      elem;
    try
    { 
      htmldoc = (HTMLDocument) jeditorPane.getDocument();
      elem    = htmldoc.getElement(htmldoc.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.P);
      htmldoc.insertBeforeEnd(elem,"<FONT FACE=\"Arial\" COLOR=\"BLUE\"><BR>" + buddyUserName + ": " + msg.replaceAll("\n", "<br />") + "</BR></FONT>");
    }
    catch(BadLocationException ble)
    {
      System.out.println("Error appending the message to the htmldoc BadLocationException");
      ble.printStackTrace();
    }
    catch(IOException ioe)
    {
      System.out.println("Error appending the message to the htmldoc IOException");
      ioe.printStackTrace();
    }
  }
//====================================================================================================================
  public void actionPerformed(ActionEvent e)
  {
    String       message;
    HTMLDocument htmldoc;
    Element      elem;
    if(e.getActionCommand().equals("SEND"))
    {
      try
      { 
        chatClient.cts.sendMessage("+MSGBUDDY "+buddyUserName+" "+chatTextArea.getText().trim());//see this, what i done did here.. it be tricky.
        message = chatTextArea.getText().trim();
        htmldoc = (HTMLDocument) jeditorPane.getDocument();
        elem    = htmldoc.getElement(htmldoc.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.P);
        htmldoc.insertBeforeEnd(elem,"<FONT FACE=\"Arial\" COLOR=\"GREEN\"><BR>" + yourUserName + ": " + message.replaceAll("\n", "<br />") + "</BR></FONT>");
        chatTextArea.setText("");
      }
      catch(BadLocationException exc)
      {
        System.out.println("Error appending the message to the htmldoc");
        exc.printStackTrace();
      }
      catch(IOException ioe)
      {
        System.out.println("Error appending the message to the htmldoc IOException");
        ioe.printStackTrace();
      }
    }
  }
//====================================================================================================================DocumentListener methods bellow 
  public void changedUpdate(DocumentEvent e1){}
  public void insertUpdate(DocumentEvent e1)
  {
    if(chatTextArea.getText().trim().equals(""))
     sendButton.setEnabled(false); 
    else
     sendButton.setEnabled(true);
  }
  public void removeUpdate(DocumentEvent e1)
  {
    if(chatTextArea.getText().trim().equals(""))
     sendButton.setEnabled(false);   
    else
     sendButton.setEnabled(true);
  }
//**********************************************************************************************************************************************************************************************************************************
  //DropTargetListener  section
public void drop(DropTargetDropEvent dtde)
{
  java.util.List<File> fileList;
  Transferable transferableData;
  transferableData = dtde.getTransferable();
  DefaultListModel<File> dlm;
  int selection = 0;
  try
  {
    if(transferableData.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
    {
      dtde.acceptDrop(DnDConstants.ACTION_COPY);
      fileList = (java.util.List<File>)(transferableData.getTransferData(DataFlavor.javaFileListFlavor));// Causing the XLINT ERROR, no real fix unless i do my own check, and then it will still error.
      dlm      = new DefaultListModel<File>();

      System.out.println("FileDroped ON window of chatFrame;");

      for(int n = 0;n < fileList.size(); n++)
      {
        dlm.addElement(fileList.get(n));
      }
       fileForProcessing = dlm.elementAt(0);
       fileLength        = fileForProcessing.length();
       fileName          = fileForProcessing.getName();
       selection         = JOptionPane.showConfirmDialog(null,"Are you sure you want to send this file to this user? FileName: "+fileName+" Length: "+fileLength+" bytes.", "Send File?", JOptionPane.YES_NO_OPTION);
       if(selection == JOptionPane.YES_OPTION)
       {
          chatClient.cts.sendMessage("+FILE_REQ "+ buddyUserName+" "+fileName+" "+fileLength);
       }
    }
  }
  catch(FileNotFoundException fnfe)
  {
    JOptionPane.showMessageDialog(null,"File not found, though this prolly shouldn't have happened, I mean you did drag and drop it onto the window... maybe you deleted it?");
    fnfe.printStackTrace();
  }
  catch(UnsupportedFlavorException usfe)
  {
    JOptionPane.showMessageDialog(null,"Unsupported flavor found!");
    usfe.printStackTrace();
  }
  catch(IOException ioe)
  {
    JOptionPane.showMessageDialog(null,"IOexception, couldnt parse file.");
    ioe.printStackTrace();
  }
}
//====================================================================================
void fileProcessingBegginging(String fileSender,String fileName, String fileSize)
{
  try
  {
  serverSocket = new ServerSocket(7777);
  isServer     = true;

  chatClient.cts.sendMessage("+FILE_ACCEPTED "+ fileSender+" 127.0.0.1" +" "+7777);////" "+serverSocket.getInetAddress().toString()+" "+7777);//" 127.0.0.1" +" "+7777);//
  this.fileName = fileName;
  progressBar.setVisible(true);
  fileProgressLabel.setVisible(true);
  fileLength = Long.parseLong(fileSize);
  new Thread(this).start();
  }
  catch(IOException ioe)
  {
    System.out.println("error creating the server socket for file transfer.");
    ioe.printStackTrace();
  }
}
//====================================================================================
void startSendingFile(String ip,String port)
{
  OutputStream outStream;
  try
  {
    sock = new Socket(ip,Integer.parseInt(port));
    new Thread(this).start();
    progressBar.setVisible(true);
    fileProgressLabel.setVisible(true);
    System.out.println("Connected to SERVER/HOST "+ip+" "+port);
  }
  catch(IOException ioe)
  {
    System.out.println("error creating the server socket for file transfer.");
    ioe.printStackTrace();
  }
}
//====================================================================================
public void run()
{
  Socket           socket;
  InputStream      instream;
  FileInputStream  fis;
  FileOutputStream fos;
  DataOutputStream dos;
  DataInputStream  dis;
  File             outFile;
  int              bytesRead;
  byte[]           byteBuffer;

  if(isServer)// this if is for the fileReciever (sender);
  {
    try
    {
      byteBuffer     = new byte[10000];//byte buffer for storing the bytes read
      socket         = serverSocket.accept();// Socket for accepting the connecting party (forming socket)
      System.out.println("Someone has connected.");
      outFile        = new File(fileName);// opens or creats file if its not there
      dis            = new DataInputStream(socket.getInputStream());//creates an inputstream of data from the CPU for the 
      fos            = new FileOutputStream(outFile);// creats the file output stream from the file that we are outputing to so that it can write to it
      totalBytesRead = 0;   //initializes the ammount of bytes that have been written (none)
      while(totalBytesRead < fileLength)//while the read bytes is less then the length of the file that is being read in, read more bytes
      {
        bytesRead = dis.read(byteBuffer);//reads bytes into byte buffer from a DIS(bytebuffer) returns the ammount of bytes actually read.
        fos.write(byteBuffer,0,bytesRead);// uses the file output stream to write the bytes to the file
        totalBytesRead += bytesRead;// adds the bytes read to the total
        SwingUtilities.invokeAndWait(
        new Runnable()
        {
          public void run()
          {
            progressBar.setValue((int)(100*totalBytesRead/fileLength));// updates the progress bar
          }
        });
      }//end of while
      JOptionPane.showMessageDialog(null,"Transfer complete, please refresh the folder you are looking in to see the file!");
      serverSocket.close();
      serverSocket = null;
      isServer     = false;
    }//endoftry
    catch(IOException ioe)
    { try
      {
      System.out.println("Error with recieving file, from sender (ERRORIN:SERVER_SECTION)");
      JOptionPane.showMessageDialog(null,"Connection seems to have been lost before the file transfer could complete.");
      serverSocket.close();
      serverSocket = null;
      ioe.printStackTrace();
      }
      catch(Exception e)
      {
        System.out.println("IDKWHYINEDTHIS");
      }//endoftryforthiscatch (yup)
    }
    catch(Exception ie)
    {
      System.out.println("Error interupt");
      ie.printStackTrace();
    }
  }//end of is server if
  else  // this method will have the running section for the file sender.
  {
    try
    {
      byteBuffer     = new byte[10000];
      dos            = new DataOutputStream(sock.getOutputStream());// creats a DOS from the sock
      fis            = new FileInputStream(fileForProcessing);// uses a File input stream to read from the file
      totalBytesRead = 0;
      while(totalBytesRead < fileLength)
      {
        bytesRead = fis.read(byteBuffer);// reads some bytes into the buffer stores bytes read into the integer
        totalBytesRead += bytesRead;// 
        dos.write(byteBuffer,0,bytesRead);
        SwingUtilities.invokeAndWait(
        new Runnable()
        {
          public void run()
          {
            progressBar.setValue((int)(100*totalBytesRead/fileLength));
          }
        });
      }
      sock.close();
      sock=null;
      JOptionPane.showMessageDialog(null,"Transfer complete!");
    }//end of try
    catch(IOException ioe)
    {
      System.out.println("Error with sending file to receiver (ERRORIN:Sender_Section)");
      JOptionPane.showMessageDialog(null,"Connection seems to have been lost before the file transfer could complete.");
      ioe.printStackTrace();
    }
    catch(Exception ie)
    {
      System.out.println("Error interupt");
      ie.printStackTrace();
    }
  }//end of else
}//end of run
//====================================================================================
  //Below this line is just here so the compiler doesn't yell at us.
 public void dropActionChanged(DropTargetDragEvent dtde)
 {}
 public void dragEnter(DropTargetDragEvent dtde)
 {}
 public void dragExit(DropTargetEvent dte)
 {}
 public void dragOver(DropTargetDragEvent dtde)
 {}
//======================================================================================
}// end of ClientChatdialog



//====================================================================================
/*    void accept()
  {
      Socket socket;
      CTC tempCTC;
      boolean hasConnected = false;
      while(hasConnected)
      { 
        try
        {
          socket  = serverSocket.accept();
        }
        catch(IOException ioe)
        {
          System.out.println("Error with constructing the socket FOR the CTC in the CHATSERVER method .accept()");
          ioe.printStackTrace();
        }
      }
      System.out.println("Someone has connected.");
  }*/