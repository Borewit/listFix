
package listfix.io;

import java.io.File;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import listfix.model.BatchRepair;
import listfix.model.BatchRepairItem;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class WinampHelper
{
    static public BatchRepair getWinampBatchRepair(String[] mediaFiles)
    {
        String homePath = System.getProperty("user.home");
        final String winAmpPath = homePath + "\\AppData\\Roaming\\Winamp\\Plugins\\ml\\";
        String playlistPath = winAmpPath + "playlists.xml";

        final BatchRepair br = new BatchRepair(mediaFiles, new File(winAmpPath));
        br.setDescription("Batch Repair: Winamp Playlists");

        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            XMLReader parser = saxParser.getXMLReader();
            parser.setContentHandler(new DefaultHandler()
            {
                @Override
                public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException
                {
                    if (name.equals("playlist"))
                    {
                        String title = attributes.getValue("title");
                        String filename = attributes.getValue("filename");
                        if (filename != null && !filename.isEmpty())
                        {
                            br.add(new BatchRepairItem(winAmpPath + filename, title));
                        }
                    }
                }
            });

            parser.parse(playlistPath);
            return br;
        }
        catch(Exception ex)
        {
            return null;
        }
    }

}

