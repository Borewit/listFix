package listfix.swing;

import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import listfix.model.playlists.Playlist;
import listfix.view.controls.PlaylistEditCtrl;

public class JDocumentTabbedPane extends JTabbedPane {
  private final List<IDocumentChangeListener> documentChangeListeners = new ArrayList<>();

  public JDocumentTabbedPane() {
    this.addContainerListener(
        new ContainerListener() {
          @Override
          public void componentAdded(ContainerEvent e) {
            if (e.getChild() instanceof JPlaylistComponent) {
              documentChangeListeners.forEach(
                  listener -> listener.documentOpened((JPlaylistComponent) e.getChild()));
            }
          }

          @Override
          public void componentRemoved(ContainerEvent e) {
            if (e.getChild() instanceof JPlaylistComponent) {
              documentChangeListeners.forEach(
                  listener -> listener.documentClosed((JPlaylistComponent) e.getChild()));
            }
          }
        });

    this.addChangeListener(
        changeEvent -> {
          JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
          int index = sourceTabbedPane.getSelectedIndex();
          if (index != -1) {
            JPlaylistComponent doc = JDocumentTabbedPane.this.getComponentAt(index);
            documentChangeListeners.forEach(listener -> listener.documentActivated(doc));
          }
        });
  }

  public int getDocumentCount() {
    return super.getTabCount();
  }

  @Override
  public JPlaylistComponent getComponentAt(int documentIndex) {
    return (JPlaylistComponent) super.getComponentAt(documentIndex);
  }

  public JPlaylistComponent getActiveTab() {
    int index = super.getSelectedIndex();
    if (index == -1) return null;
    return this.getComponentAt(index);
  }

  public void setActivePlaylist(Playlist playlist) {
    int i = this.getDocumentIndexByPlaylist(playlist);
    if (i != -1) {
      super.setSelectedIndex(i);
    }
  }

  public void setActivePlaylist(JPlaylistComponent doc) {
    super.setSelectedIndex(this.getDocumentIndex(doc));
  }

  public int getDocumentIndex(JPlaylistComponent doc) {
    for (int i = 0; i < super.getTabCount(); ++i) {
      if (this.getComponentAt(i) == doc) {
        return i;
      }
    }
    return -1;
  }

  public int getDocumentIndexByPlaylist(Playlist playlist) {
    for (int i = 0; i < super.getTabCount(); ++i) {
      JPlaylistComponent doc = this.getComponentAt(i);
      if (playlist == doc.getPlaylist()) {
        return i;
      }
    }
    return -1;
  }

  public int getDocumentIndexByName(String name) {
    for (int i = 0; i < super.getTabCount(); ++i) {
      JPlaylistComponent doc = (JPlaylistComponent) super.getComponentAt(i);
      if (name.equals(doc.getName())) {
        return i;
      }
    }
    return -1;
  }

  public void nextDocument() {
    int nextIndex = (super.getSelectedIndex() + 1) % super.getTabCount();
    super.setSelectedIndex(nextIndex);
  }

  public void prevPlaylist() {
    int tabCount = super.getTabCount();
    int nextIndex = (tabCount + super.getSelectedIndex() - 1) % super.getTabCount();
    super.setSelectedIndex(nextIndex);
  }

  public JPlaylistComponent getPlaylist(Playlist playlist) {
    int i = getDocumentIndexByPlaylist(playlist);
    return i == -1 ? null : this.getComponentAt(i);
  }

  public JPlaylistComponent getPlaylist(Path playlistPath) {
    int tabCount = this.getTabCount();
    for (int i = 0; i < tabCount; ++i) {
      JPlaylistComponent playlist = this.getComponentAt(i);
      if (playlist.getPlaylist().getPath().equals(playlistPath)) {
        return playlist;
      }
    }
    return null;
  }

  /**
   * Remove document with corresponding path
   *
   * @param playlistPath Path to remove
   */
  public void remove(Path playlistPath) {
    JPlaylistComponent playlist = getPlaylist(playlistPath);
    if (playlist != null) {
      this.remove(playlist);
    }
  }

  /**
   * Remove given playlist
   *
   * @param playlist Playlist to remove
   */
  public void remove(Playlist playlist) {
    this.remove(this.getPlaylist(playlist));
  }

  public JPlaylistComponent openPlaylist(PlaylistEditCtrl editor, Playlist playlist) {
    JPlaylistComponent doc = new JPlaylistComponent(this, editor, playlist);
    int index = this.getTabCount();
    this.addTab(null, null, doc, doc.getPlaylist().toString());

    JClosableTabComponent tabComponent = doc.getTabComponent();
    this.setTabComponentAt(index, tabComponent);
    tabComponent.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            JDocumentTabbedPane.this.setActivePlaylist(doc);
          }
        });

    return doc;
  }

  public void closeAll() {
    while (super.getTabCount() > 0) {
      this.closePlaylist(0);
    }
  }

  public void closeActiveDocument() {
    int i = super.getSelectedIndex();
    if (i != -1) {
      this.closePlaylist(i);
    }
  }

  public void closePlaylist(int i) {
    JPlaylistComponent doc = this.getComponentAt(i);
    if (doc != null) {
      super.remove(i);
      doc.setPlaylist(null); // Clean up listener
    }
  }

  public void tryToClose(int i) {
    JPlaylistComponent doc = this.getComponentAt(i);
    for (IDocumentChangeListener listener : documentChangeListeners) {
      if (!listener.tryClosingDocument(doc)) {
        return; // closing cancelled
      }
    }
    // No objected to close the document
    this.remove(i);
  }

  public void addDocumentChangeListener(IDocumentChangeListener listener) {
    this.documentChangeListeners.add(listener);
  }

  public void removeCloseDocumentListener(IDocumentChangeListener listener) {
    this.documentChangeListeners.remove(listener);
  }

  public List<PlaylistEditCtrl> getPlaylistEditors() {
    List<PlaylistEditCtrl> list = new ArrayList<>();
    int tabCount = this.getTabCount();
    for (int i = 0; i < tabCount; ++i) {
      JPlaylistComponent doc = this.getComponentAt(i);
      list.add(doc.getComponent());
    }
    return list;
  }
}
