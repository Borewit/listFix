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
   $Log: DialogDismissListener.java,v $
   Revision 1.4  2001/03/12 01:38:46  markl
   Source code cleanup.

   Revision 1.3  1999/06/29 02:03:54  markl
   Extended EventListener.

   Revision 1.2  1999/01/10 03:26:20  markl
   added GPL header & RCS tag
   ----------------------------------------------------------------------------
*/

package listfix.view.support;

import java.util.EventListener;

/** This class represents a listener that is notified when a dialog window is
  * dismissed. A convenience method for firing <code>DialogDismissEvent</code>s
  * is provided in <code>kiwi.ui.dialog.KDialog</code>.
  *
  * @see kiwi.ui.dialog.KDialog#fireDialogDismissed
  *
  * @author Mark Lindner
  * @author PING Software Group
  */

public interface DialogDismissListener extends EventListener
{

    /** Invoked after a dialog is dismissed.
    *
    * @param evt The event.
    */

    public void dialogDismissed(DialogDismissEvent evt);
}
