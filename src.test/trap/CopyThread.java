package trap;

import java.io.File;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
 
public class CopyThread extends Thread {
 
    private Display display;
    private ProgressBar progressBar;
    private Button buttonCopy;
    private Button buttonCancel;
    private Label labelInfo;
    private boolean cancel;
 
    public CopyThread(Display display, ProgressBar progressBar, //
            Label labelInfo, Button buttonCopy, Button buttonCancel) {
        this.display = display;
        this.progressBar = progressBar;
        this.buttonCopy = buttonCopy;
        this.buttonCancel = buttonCancel;
        this.labelInfo = labelInfo;
    }
 
    @Override
    public void run() {
        if (display.isDisposed()) {
            return;
        }
        this.updateGUIWhenStart();
 
        // Copy All file In C:/Windows
        File dir = new File("C:/Windows");
        File[] files = dir.listFiles();
        int count = files.length;
 
        int i = 0;
        for (File file : files) {
            if (cancel) {
                break;
            }
            i++;
            if (file.isFile()) {
                this.copy(file);
            } else {
                continue;
            }
            this.updateGUIInProgress(file, i, count);
        }
        //
        this.updateGUIWhenFinish();
    }
 
    private void copy(File file) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
    }
 
    private void updateGUIWhenStart() {
        display.asyncExec(new Runnable() {
 
            @Override
            public void run() {
                buttonCopy.setEnabled(false);
                buttonCancel.setEnabled(true);
            }
        });
    }
 
    private void updateGUIWhenFinish() {
        display.asyncExec(new Runnable() {
 
            @Override
            public void run() {
                buttonCopy.setEnabled(true);
                buttonCancel.setEnabled(false);
                progressBar.setSelection(0);
                progressBar.setMaximum(1);
                if (cancel) {
                    labelInfo.setText("Cancelled!");
                } else {
                    labelInfo.setText("Finished!");
                }
            }
        });
    }
 
    private void updateGUIInProgress(File file, int value, int count) {
        display.asyncExec(new Runnable() {
 
            @Override
            public void run() {
                labelInfo.setText("Copying file: " + file.getAbsolutePath());
                progressBar.setMaximum(count);
                progressBar.setSelection(value);
            }
        });
    }
 
    public void cancel() {
        this.cancel = true;
    }
 
}
