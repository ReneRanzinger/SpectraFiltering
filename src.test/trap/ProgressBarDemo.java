package trap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
 
public class ProgressBarDemo {
 
    public ProgressBarDemo() {
 
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText(" Spectrum Filtering Progress");
        shell.setSize(450, 200);
 
        ProgressBar progressBar = new ProgressBar(shell, SWT.SMOOTH);

        progressBar.setMinimum(3);
        progressBar.setMaximum(25);
        progressBar.setSelection(200);        
        progressBar.setBounds(140, 40, 200, 20);

 
        Label label2 = new Label(shell, SWT.NULL);
        label2.setText("SWT.SMOOTH");

        label2.setAlignment(SWT.RIGHT);

        label2.setBounds(10, 40, 120, 20);
  
        shell.open();
 
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
 
        display.dispose();
    }
 
    public static void main(String[] args) {
        new ProgressBarDemo();
    }
     
}
