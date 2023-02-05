/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2014 Jeremy Caron
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

package listfix.controller.tasks;

import listfix.config.MediaLibraryConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * @author jcaron
 */
public class WriteMediaLibraryIniTask extends listfix.controller.Task
{
  private static final Logger _logger = LogManager.getLogger(WriteMediaLibraryIniTask.class);

  private final MediaLibraryConfiguration mediaLibraryConfiguration;

  public WriteMediaLibraryIniTask(MediaLibraryConfiguration mediaLibraryConfiguration)
  {
    this.mediaLibraryConfiguration = mediaLibraryConfiguration;
  }

  /**
   * Run the task. This method is the body of the thread for this task.
   */
  @Override
  public void run()
  {
    try
    {
      this.mediaLibraryConfiguration.write();
    }
    catch (IOException e)
    {
      _logger.error("Error writing configuration to " + this.mediaLibraryConfiguration.getFile().getName(), e);
    }
  }
}
