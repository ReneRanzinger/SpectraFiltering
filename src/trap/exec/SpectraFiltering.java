package trap.exec;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import trap.dialog.SpectraFilteringDialog;
import trap.dialog.process.ProgressDialog;
import trap.process.SpectraFilterThread;

public class SpectraFiltering {
	
	public static void main (String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);

		// Open UI to set conditions for filtering scans
		SpectraFilteringDialog newDialog = new SpectraFilteringDialog(shell);
		
		if (newDialog.open() == Window.OK) {
			// OK was pressed now start processing the settings
			// creating a progress dialog and let the spectra filter thread
			// do the work
			ProgressDialog t_pDialog = new ProgressDialog(shell);
			SpectraFilterThread filter = new SpectraFilterThread(newDialog.getFilter());
			t_pDialog.setWorker(filter);
			t_pDialog.open();
		}
		shell.dispose();
		display.dispose();
	}

}
