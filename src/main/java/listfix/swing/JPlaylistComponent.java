package listfix.swing;

import java.awt.*;
import javax.swing.*;
import listfix.model.playlists.Playlist;
import listfix.view.controls.PlaylistEditCtrl;
import listfix.view.support.IPlaylistModifiedListener;
import listfix.view.support.ImageIcons;

public class JPlaylistComponent extends JPanel {
  private Playlist playlist;

  private final JClosableTabComponent closableTabComponent;

  public JPlaylistComponent(
      final JDocumentTabbedPane pane, PlaylistEditCtrl mainComponent, Playlist playlist) {
    super.add(mainComponent);

    this.closableTabComponent = new JClosableTabComponent(pane);

    this.setPlaylist(playlist);
    this.setLayout(new GridLayout(1, 1)); // Use all available tab space
  }

  public PlaylistEditCtrl getComponent() {
    return (PlaylistEditCtrl) super.getComponent(0);
  }

  public String getTitle() {
    return this.getName();
  }

  public ImageIcon getIcon() {
    return this.closableTabComponent.getIcon();
  }

  public void setIcon(ImageIcon icon) {
    this.closableTabComponent.setIcon(icon);
  }

  public Playlist getPlaylist() {
    return this.playlist;
  }

  private IPlaylistModifiedListener playlistListener = this::onPlaylistModified;

  public void setPlaylist(Playlist playlist) {
    if (this.playlist != null) {
      this.playlist.removeModifiedListener(this.playlistListener);
    }

    this.playlist = playlist;
    this.getComponent().setPlaylist(playlist);
    if (this.playlist == null) {
      return;
    }

    this.closableTabComponent.setTitle(playlist.getPath().getFileName().toString());
    this.closableTabComponent.setTooltip(playlist.getPath().toString());

    playlist.addModifiedListener(this.playlistListener);

    this.onPlaylistModified(this.playlist);
  }

  private void onPlaylistModified(Playlist list) {
    this.closableTabComponent.setTitle(playlist.getPath().getFileName().toString());
    this.closableTabComponent.setTooltip(playlist.getPath().toString());
    this.setIcon(this.getIconForPlaylist());
  }

  private ImageIcon getIconForPlaylist() {
    ImageIcon icon;
    int missing = this.playlist.getMissingCount();
    if (missing > 0) {
      icon = ImageIcons.IMG_MISSING;
    } else {
      if (this.playlist.getFixedCount() > 0 || this.playlist.isModified()) {
        icon = ImageIcons.IMG_FIXED;
      } else {
        icon = ImageIcons.IMG_FOUND;
      }
    }
    return icon;
  }

  public JClosableTabComponent getTabComponent() {
    return this.closableTabComponent;
  }
}
