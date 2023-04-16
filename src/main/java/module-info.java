module listFix.app {
  requires com.fasterxml.jackson.databind;
  requires org.apache.logging.log4j;
  requires java.desktop;
  requires io.github.borewit.lizzy;
  requires org.apache.commons.io;
  requires say.swing.JFontChooser;

  opens listfix.json;

  uses io.github.borewit.lizzy.playlist.SpecificPlaylistProvider;
  uses org.apache.logging.log4j.spi.Provider;
}