import java.io.*;
import java.awt.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import javax.swing.*;

public class CTS
                implements Runnable
{

  Talker talker;
  JLabel label;
  String msg;// MUST BE HERE OR COMPILER FREAKS
//====================================================================================================================
  public CTS(String serverName,int port,String id,JLabel label)
  {   try
      {
        this.label = label;
        talker = new Talker(serverName,port,id);
        new Thread(this).start();
      }
      catch(IOException ioe)
      {
         System.out.println("Error sending the message to the server, server might not be running!");
         System.out.println("");
         JOptionPane.showMessageDialog(null, "Error connecting to MSG server!", "Error connecting to MSG server, please close and try again!", JOptionPane.ERROR_MESSAGE); 
         System.exit(1); 
         ioe.printStackTrace();
      }
  }
//====================================================================================================================
  public void sendMessage(String msgToSend)
  { 
    try
      {
        talker.send("+MSG " + msgToSend);
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
              msg = msg.substring(4);
               SwingUtilities.invokeLater(
                   new Runnable()
                   {
                       public void run()
                       {
                           label.setText(msg);
                       }
                   }
                                         );
           }
        }
      }
      catch(IOException ioe)
      {
         connected = false;
         System.out.println("Error connecting to server from the CTS of this Client, the connection was established but might have timedout!");
         JOptionPane.showMessageDialog(null, "Connection timed out", "Connection to the server has been lost, please close the program and relaunch to start again!", JOptionPane.ERROR_MESSAGE);
         System.out.println("");
         label.setText("Connection Lost to server!");
         ioe.printStackTrace();
      }
  }
//====================================================================================================================
}