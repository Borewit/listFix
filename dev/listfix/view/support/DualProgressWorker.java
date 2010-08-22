/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package listfix.view.support;

import java.util.List;
import javax.swing.SwingWorker;

// doInBackgroiund() - perform work
// process() - made this abstract to force an override in any derived classes (worker is useless without it)
public abstract class DualProgressWorker<T,V> extends SwingWorker<T,DualProgressWorker.ProgressItem<V>> implements IDualProgressObserver<V>
{
    @Override
    protected abstract void process(List<ProgressItem<V>> chunks);

    public void reportTaskProgress(int percentComplete, V state)
    {
        publish(new ProgressItem(true, percentComplete, state));
    }

    public void reportOverallProgress(int percentComplete, V state)
    {
        publish(new ProgressItem(false, percentComplete, state));
    }

    protected static class ProgressItem<V>
    {
        public ProgressItem(boolean isTask, int percentComplete, V state)
        {
            this.isTask = isTask;
            this.percentComplete = percentComplete;
            this.state = state;
        }

        public boolean isTask;
        public int percentComplete;
        public V state;
    }

    protected void getEffectiveItems(List<ProgressItem<V>> items, ProgressItem<V> titem, ProgressItem<V> oitem)
    {
        titem.percentComplete = -1;
        titem.state = null;
        oitem.percentComplete = -1;
        oitem.state = null;
        for (int ix = items.size() - 1; ix >= 0; ix--)
        {
            ProgressItem<V> item = items.get(ix);
            ProgressItem<V> other = item.isTask ? titem : oitem;
            if (other.percentComplete < 0 && item.percentComplete >= 0)
                other.percentComplete = item.percentComplete;
            if (other.state == null && item.state != null)
                other.state = item.state;
            if (titem.percentComplete >= 0 && oitem.percentComplete >= 0 && titem.state != null && oitem.state != null)
                break;
        }
    }

}
