import java.io.*;
import java.awt.*;
import java.net.*;
import java.lang.*;
import java.util.*;

public class User
{
//STATUS/////////////////////////////////
public static final int OFF_LINE = 0;
public static final int ON_LINE  = 1;
public static final int AWAY     = 2;
/////////////////////////////////////////
CTC                 userCTC;
String              userName;
String              password;
int                 status;
int                 buddyListSize;
Vector<String>      buddyList;

public User(String name,String pass, CTC ctc)
{
 userName      = name;
 password      = pass;
 userCTC       = ctc;
 status        = ON_LINE;
 buddyListSize = 0;
 buddyList     = new Vector<String>();
}

public User(DataInputStream dis)throws IOException
{
 buddyList     = new Vector<String>();
 userName      = dis.readUTF();
 password      = dis.readUTF();
 buddyListSize = dis.readInt();

 if(buddyListSize != 0)
 {
   for(int n = 0; n < buddyListSize; n++)
   {
     buddyList.addElement(dis.readUTF());
   }
 }

 userCTC         = null;
 status          = OFF_LINE;
 System.out.println("USER: "+ userName +" Password: "+ password +" buddyListSize: "+ buddyListSize +" created!");
}
public User(){} //re-Default constructer 
//===========================================================================================================================
public void store(DataOutputStream dos)throws IOException
{
  dos.writeUTF(userName);
  System.out.println("wrote: "+ userName + " <-username to disk.");
  dos.writeUTF(password);
  System.out.println("wrote: "+ password + " <-password to disk.");
  dos.writeInt(buddyListSize);
  System.out.println("wrote: "+ buddyListSize + " <-#ofBuddys to disk.");
  if(buddyListSize != 0)
  {
   for(int n = 0; n < buddyList.size(); n++)
   {
     dos.writeUTF(buddyList.elementAt(n)); 
     System.out.println("wrote: "+ buddyList.elementAt(n) + " <-buddy name to disk."); 
   }
  }
}
//===========================================================================================================================
public boolean hasPassword(String password)
{
  return password.equals(this.password);// if this password is equal to the password they sent, then we have a match and send back a true.
}
//===========================================================================================================================
  //itterates through the users buddylist and appends the list to a temporary string builder, the string builder is then set to a string and returned.
public String getBuddyList()
{
  StringBuilder     tempSB;
  tempSB = new StringBuilder();
  tempSB.append(buddyListSize + " ");

  for(int n = 0; n < buddyList.size(); n++)
  {
    tempSB.append(buddyList.elementAt(n) + " "); 
  }  
  return tempSB.toString(); 
}
//===========================================================================================================================
public void addBuddy(String buddyToAdd)
{
  buddyList.add(buddyToAdd);
  buddyListSize = buddyList.size();
  System.out.println(buddyToAdd + " added to "+userName+" buddy list!");
}
//===========================================================================================================================
public boolean removeBuddy(String buddyToRemove)
{
  boolean removedBuddy = false;
  int     counter = 0;

  System.out.println("Attempting to remove buddy: "+buddyToRemove);
  while(counter < buddyList.size() && !removedBuddy)
  {
    if(buddyToRemove.equals(buddyList.elementAt(counter)))
    {
      buddyList.removeElementAt(counter);
      System.out.println("Removed buddy: " + buddyToRemove);
      buddyListSize = buddyList.size();
      removedBuddy = true;
    }
    counter++;
  }
  return removedBuddy;
}
//===========================================================================================================================
public void setStatus(int newStatus)
{
  status = newStatus;
}
//===========================================================================================================================
}//end of user class