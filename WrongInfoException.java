  public class WrongInfoException extends Exception 
  {
    public WrongInfoException()
    { 
      super(); 
    }
    public WrongInfoException(String message)
    {
      super(message); 
    }
    public WrongInfoException(String message, Throwable cause) 
    {
      super(message, cause); 
    }
    public WrongInfoException(Throwable cause) 
    {
      super(cause); 
    }
  }