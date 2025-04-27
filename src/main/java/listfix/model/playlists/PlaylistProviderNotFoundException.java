package listfix.model.playlists;

public class PlaylistProviderNotFoundException extends Exception {
  public PlaylistProviderNotFoundException(String message) {
    super(message);
  }

  public PlaylistProviderNotFoundException(Throwable cause) {
    super(cause);
  }

  public PlaylistProviderNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
