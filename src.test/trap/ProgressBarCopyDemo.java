package trap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
 
public class ProgressBarCopyDemo {
 
    private CopyThread copyThread = null;
 
    public ProgressBarCopyDemo() {
 
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SWT ProgressBar (o7planning.org)");
        shell.setSize(450, 200);
 
        shell.setLayout(null);
 
        ProgressBar progressBar = new ProgressBar(shell, SWT.NONE);
        progressBar.setBounds(10, 23, 350, 17);
 
        Label labelInfo = new Label(shell, SWT.NONE);
        labelInfo.setBounds(10, 46, 350, 15);
        labelInfo.setText(" ...");
 
        // Button Copy
        Button buttonCopy = new Button(shell, SWT.NONE);
        buttonCopy.setBounds(122, 67, 75, 25);
        buttonCopy.setText("Copy");
 
        // Button Cancel
        Button buttonCancel = new Button(shell, SWT.NONE);
        buttonCancel.setBounds(200, 67, 75, 25);
        buttonCancel.setText("Cancel");
        buttonCancel.setEnabled(false);
 
        buttonCopy.addSelectionListener(new SelectionAdapter() {
 
            @Override
            public void widgetSelected(SelectionEvent e) {
                copyThread = new CopyThread(display, progressBar, labelInfo, buttonCopy, buttonCancel);
                copyThread.start();
            }
        });
 
        buttonCancel.addSelectionListener(new SelectionAdapter() {
 
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (copyThread != null) {
                    copyThread.cancel();
                }
            }
        });
 
        shell.open();
 
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
 
        display.dispose();
    }
 
    public static void main(String[] args) {
        new ProgressBarCopyDemo();
    }
 
}