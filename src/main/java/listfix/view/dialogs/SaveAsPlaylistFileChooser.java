package listfix.view.dialogs;

import listfix.io.FileUtils;
import listfix.io.filters.SpecificPlaylistFileFilter;
import listfix.io.playlists.LizzyPlaylistUtil;

import javax.swing.*;
import javax.swing.plaf.basic.BasicFileChooserUI;
import java.io.File;

/**
 * Override the behaviour of the JFileChooser.
 * It preserves the filename entered by the user when a different file-extension is chosen.
 */
public class SaveAsPlaylistFileChooser extends JFileChooser
{
  /**
   * @param currentDirectory File object specifying the path to a file or directory
   */
  public SaveAsPlaylistFileChooser(File currentDirectory) {
    super(currentDirectory);
    this.init();
  }

  public SaveAsPlaylistFileChooser() {
    this.init();
  }

  private void init() {
    this.setDialogTitle("Save File:");
    this.setAcceptAllFileFilterUsed(false);
    this.setFileSelectionMode(JFileChooser.FILES_ONLY);
    // Generate filters from dynamically loaded Lizzy playlist service providers
    LizzyPlaylistUtil.getPlaylistExtensionFilters().forEach(this::addChoosableFileFilter);
    // Adjust target file name based on the filter selection
    this.addPropertyChangeListener(propertyChangeEvent -> {
      if (this.isVisible() && propertyChangeEvent.getPropertyName().equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY))
      {
        // We want the last filename the user entered
        String currentFileName = ((BasicFileChooserUI) this.getUI()).getFileName();
        SpecificPlaylistFileFilter fileFilter = (SpecificPlaylistFileFilter) propertyChangeEvent.getNewValue();
        if (currentFileName != null)
        {
          File userFile = new File(this.getCurrentDirectory(), currentFileName);
          // Current selected file is not compliant with FileFilter, let's adjust it
          if (!fileFilter.getContentType().accept(userFile))
          {
            String nameWithoutExtension = FileUtils.getExtension(currentFileName).map(
              ext -> currentFileName.substring(0, currentFileName.lastIndexOf("."))).orElse(currentFileName);
            String newName = nameWithoutExtension + fileFilter.getContentType().getExtensions()[0];
            this.setSelectedFile(new File(newName));
          }
        }
      }
    });
  }
}
