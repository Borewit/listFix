[![CI](https://github.com/Borewit/listFix/actions/workflows/ci.yml/badge.svg)](https://github.com/Borewit/listFix/actions/workflows/ci.yml)

# listFix() - Playlist Repair Done Right
Have you ever spent some time making a playlist, only to have it break when you reorganize your files? listFix() is a Swing application that solves this problem by finding the lost or missing entries in your playlists automatically.  Tell it where you keep your media files, load in the playlist you want to fix, and hit the locate button.  The program will search your media library for the file and update the playlist accordingly when it finds a match.

If a few files are still missing, they were probably renamed.  listFix() has a way of finding these files as well, by scoring the files in your media library with a "similarity" test and offering you a choice of the best potential matches, pre-selecting the matches it deems "best".

iTunes fans, see [Using listFix() to fix iTunes playlists](https://sourceforge.net/apps/mediawiki/listfix/index.php?title=Fix_iTunes_playlists)

## History

[listFix](https://github.com/Borewit/listFix) is cloned from [sourceforge.net](http://listfix.sourceforge.net/) using [a reposurgeon based script](https://github.com/Borewit/migrate-listFx).

## Features
* M3U/M3U8/PLS/WPL Support, which translates into support for programs such as:
  * Winamp, Windows Media Player, VirtualDJ, VLC, Foobar2000, XMMS, etc...
* Find lost/missing/renamed playlist entries
* Exact Matches Search
* Closest Matches Search
* Insert/Move/Delete/Replace/Append entries
* Copy/export selected files from any supported playlist to a directory of your choice
* Insert/Append Playlists
* Sort the playlist by filename, status, and location
* Randomize the list
* Remove duplicates and/or missing tracks from the playlist
* Launch an entry or playlist in the system's default media program
* Support for URL & UNC path entries
* Save playlists with absolute or relative references
* Winamp media library support
* Repair all Winamp media library playlists in place
* Extract Winamp media library playlists to a new location, with their actual names!

## Requirements
* JRE 1.6 - no promises of compatibility for OS-specific features on Macs as I don't own one
* UNC paths only supported on Windows (Linux users can smbmount a network drive and then use the mount point as a media directory)

## Development

### Build project
In project folder, run:
```shell
./gradlew build
```

### Run application
In project folder, run:
```shell
./gradlew run
```
