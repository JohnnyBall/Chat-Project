import java.io.*;
import java.awt.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import java.awt.event.*;
import java.util.Hashtable;
import javax.net.ssl.*;
import java.security.*;

public class ChatServer
{
  ServerSocket               serverSocket;
  UserHashTable<String,User> userTable;
  File                       listFile;
  String                     fileName = "userList.dat";
  DataOutputStream           dos;

//====================================================================================================================
  public ChatServer()
  {     
    DataInputStream dis;
    try
    {
      System.out.println("This is the Server Console! (for the Chat server!)");
      listFile = new File(fileName);

      if(listFile.exists())// Checks to see if the file list is created already or if one must be made, if it is already there then it is loaded from the list.
      {
        System.out.println("Loading user list from file...");
        dis       = new DataInputStream(new FileInputStream(listFile));
        userTable = new UserHashTable<String,User>(dis);
        System.out.println("Done loading.");
        dis.close();
      }
      else
      {
        System.out.println("List space allocated.");
        userTable = new UserHashTable<String,User>();
      }

      serverSocket = new ServerSocket(12345);
    }
    catch(IOException ioe)
    {
      System.out.println("Error with constructing the socket for the Chat server.");
      ioe.printStackTrace();
    }
  }
//====================================================================================================================
  boolean userCanLogIn(String username, String password, CTC ctc)
  {
    User tempUser;
    boolean isAuthentic = false;

    tempUser = userTable.get(username);

    if(tempUser == null || tempUser.userCTC != null)// if the tables returns a null then we will return a false, as in there is no user with that username.
      isAuthentic = false;
    else
    {
      isAuthentic = tempUser.hasPassword(password); // if the passwords match, returns true, if the passwords dont match then returns false.
      if(isAuthentic)
      {
        tempUser.userCTC = ctc;
        tempUser.setStatus(User.ON_LINE);// will update users friends with his status
        tempUser.userCTC.sendMessage("+LOGINOK ");
        this.sendUserFriendsList(tempUser); //sends the user his friends list. 
        this.setUserStatus(User.ON_LINE,username);
      }
    }
    return isAuthentic;
  }
//====================================================================================================================
  void sendUserFriendsList(User tempUser)
  {
    User          buddyUser;
    String        buddyListStr;
    String[]      splitStr;
    StringBuilder updatedFriendsList; // will contain the users status's
    int           counter; 
    
    updatedFriendsList = new StringBuilder();

    buddyListStr = tempUser.getBuddyList();
    
    splitStr     = buddyListStr.split(" ");
    System.out.println("BuddyList: "+ buddyListStr);

    updatedFriendsList.append(splitStr[0]);// sets the first int to be the size of the buddyList
    
    counter = 1;//1 so it skips the number of users in the list.
    while(counter < splitStr.length)
    {
      buddyUser = userTable.get(splitStr[counter]);
      updatedFriendsList.append(" "+buddyUser.status+" "+splitStr[counter]);      
      counter++;
    }
    
    tempUser.userCTC.sendMessage("+FRIENDSTART " + updatedFriendsList);
  }
//====================================================================================================================
  boolean userCanRegister(String username,String password,CTC ctc)
  {
    User tempUser;
    boolean isAuthentic = false;
    try
    {
      tempUser = userTable.get(username);
      if(tempUser == null)// if the tables returns a null then we will return a false, as in there is no user with that username.
      {
        dos = new DataOutputStream(new FileOutputStream(listFile));
        userTable.put(username,this.createUser(username,password,ctc));
        userTable.saveListtoFile(dos);
        isAuthentic = true;
      }
      dos.close();
    }
    catch(Exception e)
    {
      System.out.println("Exception opening file to save list in userCanRegister method of server.");
      e.printStackTrace();
    }
    return isAuthentic;
  }
//====================================================================================================================
  User createUser(String username, String password,CTC ctc)
  {
    User tempUser;
    tempUser = new User(username,password,ctc);
    return tempUser;
  }
//====================================================================================================================
  void closeUsersCTC(String username)
  {
    User tempUser;
    tempUser = userTable.get(username);
    if(tempUser.userCTC != null)
    {
      tempUser.userCTC = null;
      this.setUserStatus(User.OFF_LINE,username);
      System.out.println(username + " ctc set to null.");
    }
  }
//====================================================================================================================
  public void setUserStatus(int newStatus,String username)//ctcID is the same as username, so we send the ctcID to this function.
  { 
    User     tempUser;
    User     buddyUser;
    String   buddyListStr;
    String[] splitStr;
    int      counter; 

    tempUser = userTable.get(username);
    tempUser.setStatus(newStatus);

    buddyListStr = tempUser.getBuddyList();
    splitStr     = buddyListStr.split(" ");
    System.out.println("BuddyList: "+ buddyListStr);

    counter = 1;//1 so it skips the zero
    while(counter < splitStr.length)
    {
      buddyUser = userTable.get(splitStr[counter]);
      if(buddyUser.userCTC != null)
      {
        buddyUser.userCTC.sendMessage("+FSTATUS "+ newStatus +" "+username);
      }
      counter++;
    }
  }
//====================================================================================================================
  void requestFriendship(String target, String requester)
  {
    User tempUser;
    if(userTable.containsKey(target))
    {
      tempUser = userTable.get(target);
      if(tempUser.userCTC != null)
        tempUser.userCTC.sendMessage("+FR " + requester);
    }
  }
//====================================================================================================================
  void makeUsersFriends(String requester, String accepter)
  {    
    try
    {
      dos = new DataOutputStream(new FileOutputStream(listFile));
      User requesterUser;
      User accepterUser;
    
      System.out.println("USERS: "+requester+" "+accepter);
      requesterUser = userTable.get(requester);
      accepterUser  = userTable.get(accepter);

      requesterUser.addBuddy(accepter);
      accepterUser.addBuddy(requester);

      requesterUser.userCTC.sendMessage("+FRIEND " + accepterUser.status + " " + accepter);
      accepterUser.userCTC.sendMessage("+FRIEND " + requesterUser.status + " " + requester);

      userTable.saveListtoFile(dos);
      dos.close();
    }
    catch(Exception e)
    {
      System.out.println("Exception opening file to save list in makeUsersFriends method of server.");
      e.printStackTrace();
    }
  }
//====================================================================================================================
  void removeFriendship(String target, String requester)
  {    
    try
    {
      dos = new DataOutputStream(new FileOutputStream(listFile));
      User requesterUser;
      User targetUser;
    
      System.out.println("USERS: "+requester+" "+target);
      requesterUser = userTable.get(requester);
      targetUser    = userTable.get(target);

      requesterUser.removeBuddy(target);
      targetUser.removeBuddy(requester);
      
      if(targetUser.userCTC != null)
        targetUser.userCTC.sendMessage("+REMOVEFRIEND "+ requester);

      userTable.saveListtoFile(dos);
      dos.close();
    }
    catch(Exception e)
    {
      System.out.println("Exception opening file to save list in removeFriendship method of server.");
      e.printStackTrace();
    }
  }
//====================================================================================================================
    void msgUser(String target, String sender,String msg)
  {
    User tempUser;
    tempUser = userTable.get(target);
    if(tempUser.userCTC != null)
      tempUser.userCTC.sendMessage("+MSG " + sender + " " + msg);
  }
//====================================================================================================================
  void fileRequest(String target, String fileName,String fileLength,String sender)
  {
    User tempUser;
    tempUser = userTable.get(target);
    if(tempUser.userCTC != null)
      tempUser.userCTC.sendMessage("+FILE_REQ " + sender + " " + fileName +" "+fileLength);
  }
//====================================================================================================================
    void acceptRequest(String target,String ip,String port,String id)//serverSocket = new ServerSocket(12345);
  {
    User tempUser;
    tempUser = userTable.get(target);
    if(tempUser.userCTC != null)
      tempUser.userCTC.sendMessage("+FILE_ACCEPTED "+id+" "+ ip +" "+ port);
  }
//====================================================================================================================
  void accept()
  {
      Socket socket;
      CTC tempCTC;
      while(true)
      { 
        try
        {
          socket  = serverSocket.accept();
          tempCTC = new CTC(socket,"TEMPCTC",this);
        }
        catch(IOException ioe)
        {
          System.out.println("Error with constructing the socket FOR the CTC in the CHATSERVER method .accept()");
          ioe.printStackTrace();
        }
      }
  }
//====================================================================================================================
}// end of server class