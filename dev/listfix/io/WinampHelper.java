/**
 * listFix() - Fix Broken Playlists!
 *
 * This file is part of listFix().
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, please see http://www.gnu.org/licenses/
 */

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
    public static BatchRepair getWinampBatchRepair(String[] mediaFiles)
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

