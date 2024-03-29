package listfix.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;


public class ExStack
{

  public static String toString(Throwable aThrowable)
  {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }


  public static String htmlFormatErrorForUser(String message, Throwable aThrowable)
  {
    return "<html><body>" + message + "<BR/><BR/>" + getHTMLDetails(aThrowable) + "</body></html>";
  }

  /**
   * Return an HTML-formatted stack trace for the specified Throwable,
   * including any exceptions chained to the exception. Note the use of the
   * Java 1.4 StackTraceElement to get stack details. The returned string
   * begins with "<html>" and is therefore suitable for display in Swing
   * components such as JLabel.
   */
  private static String getHTMLDetails(Throwable throwable)
  {
    StringBuilder b = new StringBuilder();

    Throwable tempThrow = throwable;
    // Start with the specified throwable and loop through the chain of causality for the throwable.
    while (tempThrow != null)
    {
      // Output Exception name and message, and begin a list
      b.append(throwable).append("<br/>");
      for (StackTraceElement ste : throwable.getStackTrace())
      {
        b.append("&nbsp;&nbsp;&nbsp;&nbsp;at ").append(ste.getClassName()).append(".")
          .append(ste.getMethodName()).append("(").append(ste.getFileName()).append(":")
          .append(ste.getLineNumber()).append(")<br/>");
      }
      // See if there is a cause for this exception
      tempThrow = null;
      // tempThrow = throwable.getCause();
    }
    return b.toString();
  }

  public static String textFormatErrorForUser(String message, Throwable aThrowable)
  {
    return message + "\n\n" + getTextDetails(aThrowable);
  }


  /**
   * Return an HTML-formatted stack trace for the specified Throwable,
   * including any exceptions chained to the exception. Note the use of the
   * Java 1.4 StackTraceElement to get stack details. The returned string
   * begins with "<html>" and is therefore suitable for display in Swing
   * components such as JLabel.
   */
  private static String getTextDetails(Throwable throwable)
  {
    StringBuilder b = new StringBuilder();

    Throwable tempThrow = throwable;
    // Start with the specified throwable and loop through the chain of causality for the throwable.
    while (tempThrow != null)
    {
      // Output Exception name and message, and begin a list
      b.append(throwable).append("\n");
      for (StackTraceElement ste : throwable.getStackTrace())
      {
        b.append("     at ").append(ste.getClassName()).append(".")
          .append(ste.getMethodName()).append("(").append(ste.getFileName()).append(":")
          .append(ste.getLineNumber()).append(")\n");
      }
      // See if there is a cause for this exception
      tempThrow = null;
      // tempThrow = throwable.getCause();
    }
    return b.toString();
  }
}
