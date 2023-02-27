

package listfix.view.support;

import listfix.io.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public final class WindowSaver implements AWTEventListener
{
  private static final String PROP_FILE = Constants.DATA_DIR + "position.ini";
  private static WindowSaver saver;
  private Map framemap;
  private static final Logger _logger = LogManager.getLogger(WindowSaver.class);

  private WindowSaver()
  {
    framemap = new HashMap();
  }

  public static WindowSaver getInstance()
  {
    if (saver == null)
    {
      saver = new WindowSaver();
    }
    return saver;
  }

  @Override
  public void eventDispatched(AWTEvent evt)
  {
    try
    {
      if (evt.getID() == WindowEvent.WINDOW_OPENED)
      {
        ComponentEvent cev = (ComponentEvent) evt;
        if (cev.getComponent() instanceof JFrame)
        {
          JFrame frame = (JFrame) cev.getComponent();
          loadSettings(frame);
        }
      }
    }
    catch (Exception ex)
    {
      _logger.warn(ex);
    }
  }

  public void loadSettings(JFrame frame)
  {
    Properties settings = new Properties();
    String name = frame.getName();
    if ( new File(PROP_FILE).exists())
    {
      try
      {
        settings.load(new FileInputStream(PROP_FILE));
        int x = getInt(settings, name + ".x", 100);
        int y = getInt(settings, name + ".y", 100);
        int w = getInt(settings, name + ".w", 500);
        int h = getInt(settings, name + ".h", 500);
        frame.setLocation(x, y);
        frame.setSize(new Dimension(w, h));
      }
      catch (IOException ex)
      {
        _logger.info(ex);
      }
      saver.framemap.put(name, frame);
      frame.validate();
    }
  }

  public int getInt(Properties props, String name, int value)
  {
    String v = props.getProperty(name);
    if (v == null)
    {
      return value;
    }
    return Integer.parseInt(v);
  }

  public void saveSettings()
  {
    Properties settings = new Properties();

    Iterator it = saver.framemap.keySet().iterator();
    while (it.hasNext())
    {
      String name = (String) it.next();
      JFrame frame = (JFrame) saver.framemap.get(name);
      settings.setProperty(name + ".x", "" + frame.getX());
      settings.setProperty(name + ".y", "" + frame.getY());
      settings.setProperty(name + ".w", "" + frame.getWidth());
      settings.setProperty(name + ".h", "" + frame.getHeight());
    }
    try
    {
      settings.store(new FileOutputStream(PROP_FILE), null);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

}
