public class TestMSGClient
{
  public static void main (String args[])
  {
    MSGClient msgClient;
    System.setProperty("java.net.preferIPv4Stack" , "true");
    System.out.println("Starting Application...");
    msgClient = new MSGClient();
    System.out.println("Started");
  }
}

