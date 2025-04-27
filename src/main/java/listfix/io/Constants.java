package listfix.io;

import java.io.File;
import java.nio.file.Path;

/**
 * Contains app-wide constants that are used throughout the listFix() codebase. Some are listFix()
 * specific notions while others are just shortcuts for System properties.
 *
 * @author jcaron
 */
public class Constants {
  /** The file system's separator on the current OS. */
  public static final String FS = System.getProperty("file.separator");

  /** The user's home directory on the current OS's file system. */
  public static final String HOME_DIR = System.getProperty("user.home");

  /** The directory on disk where the listFix() config files are stored. */
  public static final Path DATA_DIR = Path.of(HOME_DIR, ".listFix()");

  /** The line break string on the current OS. */
  public static final String BR = System.getProperty("line.separator");

  /** Contains characters that are invalid for filenames under Windows. */
  public static final String INVALID_WINDOWS_FILENAME_CHARACTERS = "*|\\/:\"<>?";

  /** Boolean specifying if the current OS's file system is case-sensitive. */
  public static final boolean FILE_SYSTEM_IS_CASE_SENSITIVE = File.separatorChar == '/';
}
