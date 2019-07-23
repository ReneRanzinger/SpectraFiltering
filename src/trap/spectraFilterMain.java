package trap;

import java.io.IOException;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import trap.dialog.SpectraFilteringDialog;

public class spectraFilterMain {
	
	public static void main (String[] args) {
		//SpectraFilteringDialog newDialog = new SpectraFilteringDialog(Display.getCurrent().getActiveShell());
		
		Display display = new Display ();
		Shell shell = new Shell(display);
		
		//shell.open();
		
		SpectraFilteringDialog newDialog = new SpectraFilteringDialog(shell);
		
		
		if (newDialog.open() == Window.OK) {
			GlycanData spectrumFilter = newDialog.getFilter();
			
			SpectrumProcessor processor = new SpectrumProcessor();
			
			try {
				processor.start(spectrumFilter);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
