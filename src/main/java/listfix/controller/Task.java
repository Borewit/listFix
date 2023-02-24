

package listfix.controller;

import listfix.view.support.IProgressObserver;

import java.util.ArrayList;
import java.util.List;


public abstract class Task extends Thread
{
  private final List<IProgressObserver<String>> observers;
  private int progress = 0;


  public Task()
  {
    observers = new ArrayList<>();
  }

  @Override
  public abstract void run();

  public final void addProgressObserver(IProgressObserver<String> observer)
  {
    observers.add(observer);
  }

  public final void removeProgressObserver(IProgressObserver<String> observer)
  {
    observers.remove(observer);
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

        for (IProgressObserver observer : observers)
    {
            observer.reportProgress(percent);
    }
  }

  public int getProgress()
  {
    return progress;
  }
}
