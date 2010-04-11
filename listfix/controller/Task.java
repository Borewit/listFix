/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2009 Jeremy Caron
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
	private Vector<IProgressObserver> observers;
	private int progress = 0;

	public Task()
	{
		observers = new Vector<IProgressObserver>();
	}

	@Override
	public abstract void run();

	public final void addProgressObserver(IProgressObserver observer)
	{
		observers.addElement(observer);
	}

	public final void removeProgressObserver(IProgressObserver observer)
	{
		observers.removeElement(observer);
	}

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

		Enumeration<IProgressObserver> e = observers.elements();
		while (e.hasMoreElements())
		{
			e.nextElement().setProgress(percent);
		}
	}

	public int getProgress()
	{
		return progress;
	}

	public void setMessage(String message)
	{
		Enumeration<IProgressObserver> e = observers.elements();
		while (e.hasMoreElements())
		{
			((ProgressPopup) e.nextElement()).setMessage(message);
		}
	}
}
