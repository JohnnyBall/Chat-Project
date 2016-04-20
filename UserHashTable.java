import java.io.*;
import java.awt.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Hashtable;

public class UserHashTable<K,V> extends Hashtable<String,User>
{
  int numUsers;
  public UserHashTable(){}

  public UserHashTable(DataInputStream dis)
  {
    User tempUser;
    try
    {
      numUsers = dis.readInt();
      for(int n = 0;n < numUsers; n++)
      {
        tempUser = new User(dis);
        this.put(tempUser.userName,tempUser);
      }
    }
    catch(IOException ioe)
    {
      System.out.println("Error when creating hash table from file...");
      System.out.println("");
      ioe.printStackTrace();
    }
  }
//====================================================================================================================
  public void saveListtoFile(DataOutputStream dos)
  {
    try
    {
      Enumeration<User> enumer;
      enumer = this.elements();// forms an enumerator for running through the hash table.
      System.out.println("Attempting to write list to file...");

      numUsers = this.size();
      dos.writeInt(numUsers);//Stores the number of elements in the table.

      while(enumer.hasMoreElements()) // iterates through the hash table, telling each element (USER) to write themselves to the disk/file.
      {
        enumer.nextElement().store(dos);
      }
    }
    catch(IOException e)
    {
      System.out.println("Error writing to the file, please see stack trace for more information.");
      System.out.println("");
      e.printStackTrace();
    }
  }//end of saveListtoFile
//====================================================================================================================
}