public class TestChatClient
{
  public static void main (String args[])
  {
    ChatClient chatClient;
    System.setProperty("java.net.preferIPv4Stack" , "true");
    System.out.println("Starting Application...");
    chatClient = new ChatClient();
    System.out.println("Started");
  }
}