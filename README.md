[![CI](https://github.com/Borewit/listFix/actions/workflows/ci.yml/badge.svg)](https://github.com/Borewit/listFix/actions/workflows/ci.yml)
![GitHub all releases](https://img.shields.io/github/downloads/Borewit/listfix/total)

# listFix() - Playlist Repair Done Right
Have you ever spent some time making a playlist, only to have it break when you reorganize your files? listFix() is a Swing application that solves this problem by finding the lost or missing entries in your playlists automatically.  Tell it where you keep your media files, load in the playlist you want to fix, and hit the locate button.  The program will search your media library for the file and update the playlist accordingly when it finds a match.

If a few files are still missing, they were probably renamed.  listFix() has a way of finding these files as well, by scoring the files in your media library with a "similarity" test and offering you a choice of the best potential matches, pre-selecting the matches it deems "best".

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
* UNC paths only supported on Windows (Linux users can smbmount a network drive and then use the mount point as a media directory)

## Run the application

From the command line:
```shell
java --add-exports java.desktop/com.sun.java.swing.plaf.windows=ALL-UNNAMED -jar listFix-2.6.0-all.jar
```

## Development

### Build project
In project folder, run:
```shell
gradlew build
```

### Build Windows executable
In project folder, run:
```shell
gradlew createExe
```

### Run application
In project folder, run:
```shell
gradlew run
```

### Build Windows distribution
In project folder, run:
```shell
gradlew assembleDist
```

### Build Windows installer

Requires to be build with [OpenJDK JDK 15 Project](https://jdk.java.net/java-se-ri/15).
It may work with other, but the executable did not work building with [Amazon Corretto 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html).

You must install [Inno Setup (iscc)](https://jrsoftware.org/isinfo.php) to generate an EXE installer and
[WIX Toolset (candle and light)](https://wixtoolset.org/) to generate an MSI file.
as explained in the [JavaPackager: Windows tools installation guide](https://github.com/fvarrui/JavaPackager/blob/master/docs/windows-tools-guide.md)

Download and install [WiX Toolset](https://github.com/wixtoolset/wix3).

A quick way to do that is run the following from [cmd shell which is run as administrator](https://www.howtogeek.com/194041/how-to-open-the-command-prompt-as-administrator-in-windows-8.1/):

```cmd
@"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command "iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))" && SET "PATH=%PATH%;%ALLUSERSPROFILE%\chocolatey\bin"
choco install -y innosetup wixtoolset
```

You need to add the Wix binary path (something like `C:\Program Files (x86)\WiX Toolset v3.11\bin`) to your PATH environment variable.

In project folder, run:
```shell
gradlew packageMyApp
```

Other documentation:
- [Release procedure](doc/Release Procedure.md)