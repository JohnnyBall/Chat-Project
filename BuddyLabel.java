import java.awt.*;
import java.lang.*;
import java.util.*;
import java.io.*;
//import java.net.*;
import java.awt.event.*;
import javax.imageio.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

class BuddyLabel extends JLabel 
{
  ////////////////STATUS///////////////
  public static final int OFF_LINE = 0;
  public static final int ON_LINE  = 1;
  public static final int AWAY     = 2;
  /////////////////////////////////////
  String        buddyName;
  int           buddyStatus;
  
  BuddyLabel(int status,String buddyName)
  {
    super(buddyName);
    try
    {
      BufferedImage buffImage;
      this.buddyStatus = status;
      this.buddyName   = buddyName;
      if(buddyStatus == ON_LINE)
          buffImage = ImageIO.read(new File("ON_LINE.png"));
      else if (buddyStatus == AWAY)
          buffImage = ImageIO.read(new File("AWAY.png"));
      else
          buffImage = ImageIO.read(new File("OFF_LINE.png"));
      this.setIcon(new ImageIcon(buffImage));
      }
      catch(IOException ioe)
      {
          System.out.println("Error reading from the image files, when constructing the buddy labels.");
          ioe.printStackTrace();
      }
  }//end of constructor 

}