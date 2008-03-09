package listfix.view.support;

import java.util.Enumeration;
import java.util.Vector;

/** This class represents an asynchronous task whose progress can be tracked
  * by a <code>ProgressObserver</code>.
  *
  * @author Mark Lindner
  * @author PING Software Group
  *
  * @see kiwi.util.ProgressObserver
  * @see kiwi.ui.dialog.ProgressDialog
  * @see java.lang.Runnable
  */

public abstract class Task extends Thread
  {
  private Vector observers;

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
    if(percent < 0) percent = 0;
    else if(percent > 100) percent = 100;
    
    Enumeration e = observers.elements();
    while(e.hasMoreElements())
      ((ProgressObserver)e.nextElement()).setProgress(percent);
    }
  
  }
