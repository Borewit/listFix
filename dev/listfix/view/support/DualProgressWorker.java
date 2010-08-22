/*
 * listFix() - Fix Broken Playlists!
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
