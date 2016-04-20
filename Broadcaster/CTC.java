import java.io.*;
import java.awt.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import javax.swing.*;

public class CTC
                implements Runnable
{
  MSGServer server;
  Talker talker;
  String id;
//====================================================================================================================
  public CTC(Socket socket,String id,MSGServer server)throws IOException
  {
    try
    {
      this.id     = id;
      this.server = server;
      talker      = new Talker(socket,id);
      talker.send("+MSG Welcome to the server, You are " + id + "!");
  
      new Thread(this).start();
    }
    catch(IOException ioe)
    {
      System.out.println("Error with constructing the talker for the MSGSERVER");
      System.out.println("");
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
        System.out.println("Error sending the message to the Client");
        System.out.println("");
        ioe.printStackTrace();
      }
  }
//==================================================================================================================== 
  public void run()
  {

     String msg;
     boolean connected = true;
     while(connected)
     {
      try
      {
        msg = talker.recieve();
        if(msg.startsWith("+MSG"))
          server.broadcast(msg,this);
      }
      catch(IOException ioe)
      {
        connected = false;
        System.out.println("Connection to the Client: " + this.id + " has been lossed or closed...");
        server.removeCTCfromList(this); 
        ioe.printStackTrace();    
      }
     }
  }
//====================================================================================================================
  //==================================================================================================================== 
  public String getID()
  {
    return id;
  }
//====================================================================================================================
}