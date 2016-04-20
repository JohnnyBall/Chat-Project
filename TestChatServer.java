public class TestChatServer
{
  public static void main (String args[])
  {
    ChatServer chatServer;
    System.out.println("Starting Application...");
    chatServer = new ChatServer();
    chatServer.accept();
  }
}