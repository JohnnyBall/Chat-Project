import java.io.*;
import java.awt.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import javax.swing.*;

public class CTC
                implements Runnable
{
  ChatServer server;
  Talker talker;
  String id;
//====================================================================================================================
  public CTC(Socket socket,String id,ChatServer server)throws IOException
  {
    try
    {
      this.id     = id;
      this.server = server;
      talker      = new Talker(socket,id);
      new Thread(this).start();
    }
    catch(IOException ioe)
    {
      System.out.println("Error with constructing the talker for the CTC");
      System.out.println("");
      ioe.printStackTrace();
    }
  }
  //====================================================================================================================
  public void  setID(String id)
  {
      this.id   = id;
      talker.id = id;
  }
  //====================================================================================================================
  public void sendMessage(String msgToSend)
  { 
    try
      {
        talker.send(msgToSend);
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
     String   msg;
     String[] splitString;
     boolean  connected = true;
     while(connected)
     {
      try
      {
        msg = talker.recieve();
        if(msg.startsWith("+LOGIN"))
        {
          splitString = msg.toString().split(" ");

          if(server.userCanLogIn(splitString[1], splitString[2],this))// if the user can login with the parameters sent then we will log them in.
          {
            this.setID(splitString[1]); 
          }
          else
          {
            this.sendMessage("+LOGINFAILED");
            connected = false;
          }
        }
        else if (msg.startsWith("+REGISTER"))
        {
          splitString = msg.toString().split(" ");
          if(server.userCanRegister(splitString[1], splitString[2],this))
          {
            this.setID(splitString[1]); 
            this.sendMessage("+REGISTEROK");
          }
          else
          {
            this.sendMessage("+REGISTERFAILED");
            connected = false;
          }
        }
/////////////////////////////////////////////////////FRIENDS AND STATUS SECTION/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        else if (msg.startsWith("+MSGBUDDY"))
        {
          splitString = msg.toString().split(" ",3);
          server.msgUser(splitString[1],id,splitString[2]);
        }
        else if (msg.startsWith("+SETSTATUS"))
        { 
          splitString = msg.toString().split(" ");
          server.setUserStatus(Integer.parseInt(splitString[1]),id);// Sets the users status and pushes the updated status to his friends
        }
        else if (msg.startsWith("+ADDF"))
        {
          splitString = msg.toString().split(" ");
          server.requestFriendship(splitString[1],id);//(Targetid,userid)
        }
        else if (msg.startsWith("+FRACCEPTED"))
        {
          splitString = msg.toString().split(" ");
          server.makeUsersFriends(splitString[1],id);//target userid// friend that send request, and this user that accepted.
        }
        else if (msg.startsWith("+DELETEFRIEND"))
        {
          splitString = msg.toString().split(" ");
          server.removeFriendship(splitString[1],id);//target userid// friend that send request, and this user that accepted.
        }
        else if(msg.startsWith("+FILE_REQ"))
        {
          splitString = msg.toString().split(" ");
          server.fileRequest(splitString[1],splitString[2],splitString[3],id);//"+FILE_REQ "+ buddyUserName+" "+fileName+" "+fileLength
        }
        else if(msg.startsWith("+FILE_ACCEPTED"))
        {
          splitString = msg.toString().split(" ");
          server.acceptRequest(splitString[1],splitString[2],splitString[3],id);//"+FILE_REQ "+ buddyUserName+" "+fileName+" "+fileLength
        }
      }
      catch(IOException ioe)
      {
        connected = false;
        System.out.println("Connection to the Client: " + this.id + " has been lost or closed...");
        server.closeUsersCTC(id);
        //ioe.printStackTrace();    
      }
     }//end of while
  }//end of method
}//end of CTC