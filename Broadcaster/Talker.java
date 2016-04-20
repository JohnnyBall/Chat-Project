import java.awt.*;
import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.datatransfer.*;

public class Talker
{
  String serverName;
  int port;
  String id;
  private Socket sock;
  private DataInputStream dis;
  private DataOutputStream dos;
  private BufferedReader buffRdr;
//*********************************************************
  public Talker(String serverName,int port,String id)throws IOException
  {
      this.serverName = serverName;
      this.port       = port;
      this.id         = id;
      sock            = new Socket(serverName,port);
      dos             = new DataOutputStream(sock.getOutputStream());
      dis             = new DataInputStream(sock.getInputStream());
      buffRdr         = new BufferedReader(new InputStreamReader(dis));
  }
//*********************************************************
  public Talker(Socket sock,String id)throws IOException
  {
      this.sock = sock;
      this.id   = id;
      dos       = new DataOutputStream(sock.getOutputStream());
      dis       = new DataInputStream(sock.getInputStream());
      buffRdr   = new BufferedReader(new InputStreamReader(dis));
  }
//=========================================================
  public void send(String strToSend)throws IOException     //SEND METHOD
  {
    dos.writeBytes(strToSend + '\n');
    System.out.println("SENT>>>> " + id + ": " + strToSend);
  }
//=========================================================
  public String recieve()throws IOException                //RECIEVE METHOD
  {
    String recievedString;
    recievedString = buffRdr.readLine();
    System.out.println("RECD<<<< " + id + ": " + recievedString);
    return recievedString;
  }
//=========================================================
  public void expect(String responsePrefix) throws WrongInfoException,IOException 
  {
    String resp;
    resp = this.recieve().toUpperCase();
    if(!resp.startsWith(responsePrefix))
      throw new WrongInfoException("Excpected: " + responsePrefix +" from" + id +", found: " + resp);
  }
//=========================================================
}//End of Class