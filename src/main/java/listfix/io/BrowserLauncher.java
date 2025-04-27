package listfix.io;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.*;
import listfix.view.controls.JTransparentTextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** A class who's sole responsibility is to launch the default web browser on various OSes. */
public class BrowserLauncher {
  private static final String _errMsg = "Error attempting to launch default web browser";
  private static final Logger _logger = LogManager.getLogger(BrowserLauncher.class);

  /**
   * Attempts to open the given url in the system's default web browser. Logs failure details and
   * silently fails if a browser can't be opened.
   *
   * @param uri The URI to display in the browser, if successfully launched.
   */
  public static void launch(String uri) {
    try {
      launch(new URI(uri));
    } catch (URISyntaxException e) {
      _logger.error(String.format("Invalid URI: %s", uri), e);
      JOptionPane.showMessageDialog(
          null, new JTransparentTextArea(_errMsg + ": " + e.getLocalizedMessage()));
    }
  }

  public static void launch(URI uri) {
    Desktop desk = Desktop.getDesktop();
    try {
      desk.browse(uri);
    } catch (IOException e) {
      _logger.error(String.format("Failed to open URL: %s", uri), e);
      JOptionPane.showMessageDialog(
          null, new JTransparentTextArea(_errMsg + ": " + e.getLocalizedMessage()));
    }
  }
}
