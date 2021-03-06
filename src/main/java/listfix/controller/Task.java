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

package listfix.controller;

import java.util.ArrayList;
import java.util.List;

import listfix.view.support.IProgressObserver;

/**
 *
 * @author jcaron
 */
public abstract class Task extends Thread
{
  private final List<IProgressObserver> observers;
  private int progress = 0;

  /**
   *
   */
  public Task()
  {
    observers = new ArrayList<>();
  }

  /**
   *
   */
  @Override
  public abstract void run();

  /**
   *
   * @param observer
   */
  public final void addProgressObserver(IProgressObserver observer)
  {
    observers.add(observer);
  }

  /**
   *
   * @param observer
   */
  public final void removeProgressObserver(IProgressObserver observer)
  {
    observers.remove(observer);
  }

  /**
   *
   * @param percent
   */
  public final void notifyObservers(int percent)
  {
    if (percent < 0)
    {
      percent = 0;
    }
    else if (percent > 100)
    {
      percent = 100;
    }
    progress = percent;

        for (IProgressObserver observer : observers)
    {
            observer.reportProgress(percent);
    }
  }

  /**
   *
   * @return
   */
  public int getProgress()
  {
    return progress;
  }
}
