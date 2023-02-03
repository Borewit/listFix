package listfix.view.dialogs;

import javax.swing.*;

public class FolderChooser extends JFileChooser
{
  public FolderChooser() {
    this.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    this.setAcceptAllFileFilterUsed(false);
    this.setApproveButtonText("Select");
  }
}
