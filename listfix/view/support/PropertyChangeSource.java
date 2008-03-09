/* ----------------------------------------------------------------------------
   The Kiwi Toolkit
   Copyright (C) 1998-2001 Mark A. Lindner

   This file is part of Kiwi.
   
   This library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Library General Public
   License as published by the Free Software Foundation; either
   version 2 of the License, or (at your option) any later version.

   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Library General Public License for more details.

   You should have received a copy of the GNU Library General Public
   License along with this library; if not, write to the Free
   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 
   The author may be contacted at:
   
   mark_a_lindner@yahoo.com
   ----------------------------------------------------------------------------
   $Log: PropertyChangeSource.java,v $
   Revision 1.3  2001/03/12 01:38:49  markl
   Source code cleanup.

   Revision 1.2  1999/01/10 03:29:53  markl
   added GPL header & RCS tag
   ----------------------------------------------------------------------------
*/

package listfix.view.support;

import java.beans.PropertyChangeListener;

/** Interface that must be implemented by objects that are sources of
  * <code>PropertyChangeEvent</code>s.
  *
  * @see java.beans.PropertyChangeEvent
  *
  * @author Mark Lindner
  * @author PING Software Group
  */

public interface PropertyChangeSource
  {

  /** Register a new property change listener.
    *
    * @param listener The <code>PropertyChangeListener</code> to be added to
    * this object's list of listeners.
    */

  public void addPropertyChangeListener(PropertyChangeListener listener);

  /** Unregister a property change listener.
    *
    * @param listener The <code>PropertyChangeListener</code> to be removed
    * from this object's list of listeners.
    */

  public void removePropertyChangeListener(PropertyChangeListener listener);
  }

/* end of source file */
