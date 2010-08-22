
package listfix.io;

import java.util.Set;

public class AudioFileFilter extends FileExtensionFilterBase
{
    public AudioFileFilter()
    {
        super(_extensions);
    }

    @Override
    public String getDescription()
    {
        return "Audio Files and Playlists (*.m3u, *.m3u8, *.pls, *.mp3, *.flac, *.aac, *.ogg, *.aiff, *.au, *.wma)";
    }
    
    private static final Set<String> _extensions;

    static
    {
        _extensions = createExtensionSet("m3u", "m3u8", "pls", "mp3", "flac", "aac", "ogg", "aiff", "au", "wma");
    }
}
