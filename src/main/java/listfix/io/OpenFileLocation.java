package listfix.io;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.*;
import listfix.util.ExStack;
import listfix.util.OperatingSystem;
import listfix.view.controls.JTransparentTextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OpenFileLocation {
  private final Logger _logger = LogManager.getLogger(OpenFileLocation.class);

  private final Component parent;

  public OpenFileLocation(Component parent) {
    this.parent = parent;
  }

  public void openFileLocation(Path path) {
    if (Desktop.isDesktopSupported()) {
      Desktop desktop = Desktop.getDesktop();
      if (desktop.isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
        desktop.browseFileDirectory(path.toFile());
        return;
      } else if (OperatingSystem.isWindows()) {
        try {
          Runtime.getRuntime().exec(String.format("explorer.exe /select,\"%s\"", path));
          return;
        } catch (IOException e) {
          _logger.error("Failed to open Explorer", e);
          throw new RuntimeException(e);
        }
      } else if (desktop.isSupported(Desktop.Action.OPEN)) {
        Path folder = Files.isRegularFile(path) ? path.getParent() : path;
        try {
          desktop.open(folder.toFile());
          return;
        } catch (IOException e) {
          String message = String.format("Failed to open file \"%s\"", path);
          _logger.error(message, e);
          JOptionPane.showMessageDialog(
              parent,
              new JTransparentTextArea(ExStack.textFormatErrorForUser(message, e.getCause())),
              "Failed to open file location",
              JOptionPane.ERROR_MESSAGE);
          return;
        }
      }
    }
    JOptionPane.showMessageDialog(
        parent,
        "Could not find supported desktop action to open file",
        "Failed to open file location",
        JOptionPane.ERROR_MESSAGE);
  }
}
