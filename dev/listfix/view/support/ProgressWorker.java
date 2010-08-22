/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package listfix.view.support;

import javax.swing.SwingWorker;

// implement doInBackground() for easiest use. Designed for the single progress
// bar operation of ProgressDialog.
public abstract class ProgressWorker<T,V> extends SwingWorker<T,V> implements IProgressObserver<V>
{
    public void reportProgress(int progress)
    {
        setProgress(progress);
    }

    public void reportProgress(int progress, V state)
    {
        setProgress(progress);
        if (state != null)
            publish(state);
    }
}
