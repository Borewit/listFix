package listfix.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class Dates
{

  public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss.SSS";


  public static String now()
  {
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
    return sdf.format(cal.getTime());
  }
}
