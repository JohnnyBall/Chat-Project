import java.io.*;
import java.awt.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import java.awt.event.*;
import java.util.LinkedList;


public class MSGServer
{
  ServerSocket serverSocket;
  LinkedList<CTC> list;

//====================================================================================================================
  MSGServer()
  {
    try
    {
      System.out.println("This is the Server Console! (for the MSGServer!)");
      serverSocket = new ServerSocket(12345);
      serverSocket.setSoTimeout(200000);
      list = new LinkedList<CTC>();
    }
    catch(IOException ioe)
    {
      System.out.println("Error with constructing the socket for the MSGSERVER");
      ioe.printStackTrace();
    }
  }
//====================================================================================================================
  void accept()
  {

      Socket socket;
      CTC tempCTC;
      int ctcID;
      while(true)
      { 
        try
        {
          socket  = serverSocket.accept();
          ctcID   = list.size();
          ctcID++;
          tempCTC = new CTC(socket,"USER"+ ctcID,this);
          list.add(tempCTC);
          System.out.println("Adding user" + ctcID + " to the list.");
        }
        catch(IOException ioe)
        {
          System.out.println("Error with constructing the socket FOR the CTC in the MSGSERVER method .accept()");
          ioe.printStackTrace();
        }
      }
  }

//====================================================================================================================
  void broadcast(String msg,CTC senderCTC)
  {
    Iterator <CTC> it;
    CTC tempCTC;

    it = list.iterator();
    msg = msg.substring(4);
    while(it.hasNext())
    {
      tempCTC = it.next();
      if(tempCTC != senderCTC)
        tempCTC.sendMessage(senderCTC.getID() + ':'+ msg);
    }
  }
//====================================================================================================================
//====================================================================================================================
  void removeCTCfromList(CTC senderCTC)
  {
    Iterator <CTC> it;
    CTC tempCTC;
    System.out.println("Removing CTC from LIST!");
    it = list.iterator();
    while(it.hasNext())
    {
      tempCTC = it.next();
      if(tempCTC == senderCTC)
      {
        it.remove();
        System.out.println("CTC removed.");
      }
      else
        tempCTC.sendMessage(senderCTC.getID() +" has disconnected!");
    }
  }
//====================================================================================================================
}

