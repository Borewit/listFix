package listfix.io;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import listfix.io.playlists.LizzyPlaylistUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** To create a list of all the playlists found in a given directory and its subdirectories. */
public class PlaylistScanner {

  private static final Logger logger = LogManager.getLogger(PlaylistScanner.class);

  public static List<Path> getAllPlaylists(Path directory) throws IOException {
    List<Path> result = new ArrayList<>();
    Files.walkFileTree(
        directory,
        new FileVisitor<>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (LizzyPlaylistUtil.isPlaylist(file)) {
              result.add(file);
            }
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFileFailed(Path file, IOException exc) {
            logger.warn(String.format("Failed to access \"%s\"", file));
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            return FileVisitResult.CONTINUE;
          }
        });
    return result;
  }
}
