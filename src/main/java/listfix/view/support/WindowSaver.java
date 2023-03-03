package listfix.view.support;

import listfix.config.IApplicationState;
import listfix.json.JsonFrameSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;

public final class WindowSaver implements AWTEventListener
{
  private final IApplicationState applicationState;

  private static final Logger _logger = LogManager.getLogger(WindowSaver.class);

  public WindowSaver(IApplicationState applicationState)
  {
    this.applicationState = applicationState;
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

  private JsonFrameSettings getFrameSettingsForFrame(Frame frame)
  {
    final String name = frame.getName();
    JsonFrameSettings frameSettings = applicationState.getFramePositions().get(name);
    if (frameSettings == null)
    {
      // Inialize frame settings with default values;
      frameSettings = new JsonFrameSettings();
      frameSettings.x = 250;
      frameSettings.y = 250;
      frameSettings.width = 500;
      frameSettings.height = 500;
      applicationState.getFramePositions().put(name, frameSettings);
    }
    return frameSettings;
  }

  public void loadSettings(Frame frame)
  {
    JsonFrameSettings frameSettings = getFrameSettingsForFrame(frame);
    frame.setLocation(frameSettings.x, frameSettings.y);
    frame.setSize(new Dimension(frameSettings.width, frameSettings.height));
    frame.validate();

    frame.addComponentListener(new ComponentListener()
    {
      @Override
      public void componentResized(ComponentEvent e)
      {
        if (e.getComponent() instanceof Frame)
        {
          Frame frame = (Frame) e.getComponent();
          JsonFrameSettings frameSettings = WindowSaver.this.getFrameSettingsForFrame(frame);
          frameSettings.width = frame.getWidth();
          frameSettings.height = frame.getHeight();
        }
      }

      @Override
      public void componentMoved(ComponentEvent e)
      {
        if (e.getComponent() instanceof Frame)
        {
          Frame frame = (Frame) e.getComponent();
          JsonFrameSettings frameSettings = WindowSaver.this.getFrameSettingsForFrame(frame);
          frameSettings.x = frame.getX();
          frameSettings.y = frame.getY();
        }
      }

      @Override
      public void componentShown(ComponentEvent e)
      {
      }

      @Override
      public void componentHidden(ComponentEvent e)
      {
      }
    });
  }

}
