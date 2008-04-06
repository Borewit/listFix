/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2008 Jeremy Caron
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

import listfix.view.support.*;
import java.util.Enumeration;
import java.util.Vector;

public abstract class Task extends Thread
{
    private Vector observers;
    private int progress = 0;

    /** Construct a new <code>Task</code>. */
    public Task()
    {
        observers = new Vector();
    }

    /** Run the task. This method is the body of the thread for this task. */
    @Override
    public abstract void run();

    /** Add a progress observer to this task's list of observers. Observers are
     * notified by the task of progress made.
     *
     * @param observer The observer to add.
     */
    public final void addProgressObserver(ProgressObserver observer)
    {
        observers.addElement(observer);
    }

    /** Remove a progress observer from this task's list of observers.
     *
     * @param observer The observer to remove.
     */
    public final void removeProgressObserver(ProgressObserver observer)
    {
        observers.removeElement(observer);
    }

    /** Notify all observers about the percentage of the task completed.
     *
     * @param percent The percentage of the task completed, an integer value
     * between 0 and 100 inclusive. Values outside of this range are silently
     * clipped.
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

        Enumeration e = observers.elements();
        while (e.hasMoreElements())
        {
            ((ProgressObserver) e.nextElement()).setProgress(percent);
        }
    }
    
    public int getProgress()
    {
        return progress;
    }
    
    public void setMessage(String message)
    {
        Enumeration e = observers.elements();
        while (e.hasMoreElements())
        {
            ((ProgressDialog)e.nextElement()).setMessage(message);
        }
    }
}
