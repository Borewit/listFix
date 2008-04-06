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
   $Log: DialogDismissEvent.java,v $
   Revision 1.4  2001/03/12 05:56:36  markl
   Javadoc cleanup.

   Revision 1.3  2001/03/12 01:38:46  markl
   Source code cleanup.

   Revision 1.2  1999/01/10 03:26:20  markl
   added GPL header & RCS tag
   ----------------------------------------------------------------------------
*/

package listfix.view.support;

import listfix.controller.Task;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

public class ProgressDialog extends JDialog implements ProgressObserver
{
    private JProgressBar bar;
    private JLabel label,  iconLabel;
    private JPanel _main;
    private Vector _listeners = new Vector();

    /*
     * Common initialization.
     */
    private void _init()
    {
        getContentPane().setLayout(new GridLayout(1, 0));
        _main = new JPanel();
        _main.setOpaque(true);
        getContentPane().add(_main);
    }

    /** Get a reference to the main container (in this case, the
     * <code>KPanel</code> that is the child of the frame's content pane).
     */
    protected JPanel getMainContainer()
    {
        return (_main);
    }

    /** Add a <code>DialogDismissListener</code> to this dialog's list of
     * listeners.
     *
     * @param listener The listener to add.
     * @see #removeDialogDismissListener
     */
    public void addDialogDismissListener(DialogDismissListener listener)
    {
        _listeners.addElement(listener);
    }

    /** Remove a <code>DialogDismissListener</code> from this dialog's list
     * of listeners.
     *
     * @param listener The listener to remove.
     * @see #addDialogDismissListener
     */
    public void removeDialogDismissListener(DialogDismissListener listener)
    {
        _listeners.removeElement(listener);
    }

    /** Fire a <i>dialog dismissed</i> event. Notifies listeners that this dialog
     * is being dismissed.
     *
     * @param type The event type.
     */
    protected void fireDialogDismissed(int type)
    {
        fireDialogDismissed(type, null);
    }

    /** Fire a <i>dialog dismissed</i> event. Notifies listeners that this dialog
     * is being dismissed.
     *
     * @param type The event type.
     * @param userObj An arbitrary user object argument to pass in the event.
     */
    protected void fireDialogDismissed(int type, Object userObj)
    {
        DialogDismissEvent evt = null;
        DialogDismissListener listener;

        Enumeration e = _listeners.elements();
        while (e.hasMoreElements())
        {
            listener = (DialogDismissListener) e.nextElement();
            if (evt == null)
            {
                evt = new DialogDismissEvent(this, type, userObj);
            }
            listener.dialogDismissed(evt);
        }
    }

    /** Show or hide the dialog.
     *
     * @param flag A flag specifying whether the dialog should be shown
     * or hidden. If <code>true</code>, the <code>startFocus()</code>
     * method is called to allow the subclasser to request focus for a
     * given child component.
     *
     * @see #startFocus
     */
    @Override
    public void setVisible(boolean flag)
    {
        if (flag)
        {
            startFocus();
        }

        super.setVisible(flag);
    }

    /** This method is called when the dialog is made visible; it should
     * transfer focus to the appropriate child component. The default
     * implementation does nothing.
     */
    protected void startFocus()
    {
    }

    /** Turn the busy cursor on or off for this dialog.
     *
     * @param flag If <code>true</code>, the wait cursor will be set for
     * this dialog, otherwise the default cursor will be set.
     */
    public void setBusyCursor(boolean flag)
    {
        setCursor(Cursor.getPredefinedCursor(flag ? Cursor.WAIT_CURSOR
                : Cursor.DEFAULT_CURSOR));
    }

    /** Determine if this dialog can be closed.
     *
     * @return <code>true</code> if the dialog may be closed, and
     * <code>false</code> otherwise. The default implementation returns
     * <code>true</code>.
     */
    protected boolean canClose()
    {
        return (true);
    }

    /** Construct a new <code>ProgressDialog</code>.
     *
     * @param parent The parent window.
     * @param title The title for the dialog window.
     * @param modal A flag specifying whether the dialog should be modal.
     */
    public ProgressDialog(Frame parent, String title, boolean modal, int labelWidth, int labelHeight, boolean messageOnlyMode)
    {
        super(parent, title, modal);
        _init();

        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        JPanel main = this.getMainContainer();

        main.setLayout(new BorderLayout(5, 5));

        main.add("North", label = new JLabel("Please Wait"));
        label.setPreferredSize(new Dimension(labelWidth, labelHeight));
        label.setFont(new Font("Verdana", 0, 9));
        label.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
        bar = new JProgressBar();

        if (!messageOnlyMode)
        {
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

            bar.setMinimum(0);
            bar.setMaximum(100);
            bar.setValue(0);
            bar.setOpaque(false);
            bar.setPreferredSize(new java.awt.Dimension(main.getPreferredSize().width, bar.getPreferredSize().height));
            p.add(bar);

            main.add("Center", p);
        }

        if (getTitle().length() == 0)
        {
            setTitle(title);
        }

        pack();
    }

    /** Implementation of the <code>ProgressObserver</code> interface. */
    public void setProgress(int progress)
    {
        if (progress < 0)
        {
            progress = 0;
        }
        else if (progress > 100)
        {
            progress = 100;
        }

        bar.setValue(progress);
        if (progress == 100)
        {
            this.paintImmediately(bar);
            this.sleep(1);
            setVisible(false);
        }
    }

    private final void paintImmediately(Component c)
    {
        Graphics gc = c.getGraphics();
        if (gc != null)
        {
            c.paint(gc);
            gc.dispose();
        }
    }

    private final void sleep(int sec)
    {
        try
        {
            Thread.sleep(sec * 1000);
        }
        catch (InterruptedException ex)
        {
        }
    }

    /** Set the dialog's icon. The dialog's icon can be changed via a call to
     * this method. Animated GIF images add a professional touch when used with
     * <code>ProgressDialog</code>s.
     *
     * @param icon The new icon to use.
     */
    public void setIcon(Icon icon)
    {
        iconLabel.setIcon(icon);
    }

    /** Set the message for the dialog.
     *
     * @param message The message to display in the dialog.
     * @param runnable A runnable object to run in a thread once the dialog is
     * displayed.
     */
    public void setMessage(String message)
    {
        label.setText(message);
        pack();
    }

    /** Track the progress of a task. Displays the dialog and tracks the progress
     * of the task, updating the progress meter accordingly. When the task
     * is completed, the dialog automatically disappears.
     *
     * @param task The <code>Task</code> to track; it should not be currently
     * running, as the dialog will start the task itself.
     */
    public void track(Task task)
    {
        bar.setValue(0);
        task.addProgressObserver(this);
        Thread t = new Thread(task);
        t.start();
        setVisible(true);
        task.removeProgressObserver(this);
    }

    private void center()
    {
        Point parentLocation = this.getParent().getLocationOnScreen();
        double x = parentLocation.getX();
        double y = parentLocation.getY();
        int width = this.getParent().getWidth();
        int height = this.getParent().getHeight();

        this.setLocation((int) x + (width - this.getWidth()) / 2, (int) y + (height - this.getHeight()) / 2);
    }

    public void go()
    {
        this.center();
        this.setEnabled(true);
    }
}
