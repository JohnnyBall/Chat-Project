import java.io.*;
import java.awt.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import javax.swing.*;

public class CTS
                implements Runnable
{
  Talker  talker;
  ChatClient chatClient;
  String  msg;// Must be placed here to pass to chat client, other wise must be effectivly final.
  String[] splitString;// see above.
  String id;
//====================================================================================================================
  public CTS(String serverName,int port,String id, ChatClient cc)
  {   try
      {
        this.id = id;
        talker  = new Talker(serverName,port,id);
        this.chatClient = cc;
        new Thread(this).start();
      }
      catch(IOException ioe)
      {
         System.out.println("Error sending the message to the server, server might not be running!");
         System.out.println("");
         JOptionPane.showMessageDialog(null, "Error connecting to chat server!", "Error connecting to chat server, please close and try again!", JOptionPane.ERROR_MESSAGE); 
         System.exit(1); 
         ioe.printStackTrace();
      }
  }//end of constructor 
//====================================================================================================================
  public synchronized  void sendMessage(String msgToSend)
  { 
    try
      {
        talker.send(msgToSend);
      }
      catch(IOException ioe)
      {
        System.out.println("Error sending message  to this server, connection might have been lost. ");
        ioe.printStackTrace();
      }
  }
//====================================================================================================================
  public void run()
  {
    boolean connected = true;
    try
      {
        while(connected)
        {
           msg = talker.recieve();
           if(msg.startsWith("+MSG"))
           {
             splitString = msg.toString().split(" ",3);
             chatClient.sendMessageToDialog(splitString[1],splitString[2]);//(sender ID,message)
           }           
           else if(msg.startsWith("+FRIENDSTART"))
           {            
            SwingUtilities.invokeLater(
              new Runnable()
              {
                public void run()
                {
                  chatClient.processFriendListUpdateAtStart(msg);
                }
              });
           }           
           else if(msg.startsWith("+REMOVEFRIEND"))
           {
             splitString = msg.split(" ");
             SwingUtilities.invokeLater(
              new Runnable()
              {
                public void run()
                {
                  chatClient.removeBuddy(splitString[1]);
                }
              });
           }
           else if(msg.startsWith("+FRIEND"))
           {
             splitString = msg.split(" ");
              SwingUtilities.invokeLater(
              new Runnable()
              {
                public void run()
                {
                  chatClient.addBuddy(Integer.parseInt(splitString[1]),splitString[2]);
                }
              });
           }      
           else if(msg.startsWith("+FR"))
           {
             splitString = msg.split(" ");
             SwingUtilities.invokeLater(
              new Runnable()
              {
                public void run()
                {
                  chatClient.processFriendRequest(splitString[1]);
                }
              });
           }
           else if(msg.startsWith("+FSTATUS"))
           {
             splitString = msg.split(" ");
             SwingUtilities.invokeLater(
              new Runnable()
              {
                public void run()
                {
                  chatClient.changeStatusOfBuddy(Integer.parseInt(splitString[1]),splitString[2]);//(int status, string username)
                }
              });
           }             
           else if (msg.startsWith("+LOGINOK"))
           {
             chatClient.logedIn = true;
              SwingUtilities.invokeLater(
              new Runnable()
              {
                public void run()
                {
                  chatClient.clientLoginDialog.dispose();
                  chatClient.userNameLabel.setText("UserName: " + id);
                  chatClient.setVisible(true);
                }
              });
            chatClient.cts = this;
             System.out.println("Login successful.");
           }
           else if (msg.startsWith("+LOGINFAILED"))
           {
             chatClient.logedIn = false;
             connected          = false;
             JOptionPane.showMessageDialog(null, "Failed to login, Username or password may be incorrect!", "Failed to login...", JOptionPane.ERROR_MESSAGE);
             System.out.println("Login failed.");
           }
           else if (msg.startsWith("+REGISTEROK"))
           {
             chatClient.logedIn = true;
             SwingUtilities.invokeLater(
              new Runnable()
              {
                public void run()
                {
                  chatClient.clientLoginDialog.dispose();
                  chatClient.userNameLabel.setText("UserName: " + id);
                  chatClient.setVisible(true);
                }
              });
              chatClient.cts = this;
             System.out.println("Registered successfully.");
           }
           else if (msg.startsWith("+REGISTERFAILED"))
           {
             chatClient.logedIn = false;
             connected          = false;
             JOptionPane.showMessageDialog(null, "Failed to register account, Username must already be taken!", "Failed to register...", JOptionPane.ERROR_MESSAGE);
             System.out.println("Registration failed.");
           }
           else if (msg.startsWith("+FILE_REQ"))
           {
             splitString = msg.split(" ");            
            SwingUtilities.invokeLater(
              new Runnable()
              {
                public void run()
                {
                  chatClient.processFileRequest(splitString[1],splitString[2],splitString[3]);//(String sender,String name,String size)
                }
              });
           }
           else if (msg.startsWith("+FILE_ACCEPTED"))
           {
             splitString = msg.split(" ");            
            SwingUtilities.invokeLater(
              new Runnable()
              {
                public void run()
                {
                  chatClient.fileAccepted(splitString[1],splitString[2],splitString[3]);//(String sender,String name,String size)
                }
              });
           }
        }
        talker.close();// CLOSES CONNECTION TO TALKER IF  NOT CONNECTED!!
      }
      catch(IOException ioe)
      {
         connected = false;
         System.out.println("Error connecting to server from the CTS of this Client, the connection was established but might have timed out!");
         JOptionPane.showMessageDialog(null, "Connection timed out", "Connection to the server has been lost, the program will now close. Please relaunch to start again!", JOptionPane.ERROR_MESSAGE);
         System.out.println("");
         ioe.printStackTrace();
         System.exit(1);
      }
  }
//====================================================================================================================
}