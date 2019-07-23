package trap;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.systemsbiology.jrap.grits.stax.MSXMLParser;
import org.systemsbiology.jrap.grits.stax.MZXMLFileInfo;

public class TestMSXMLParser {

	public static void main(String[] args) {
		Display display = new Display ();
		Shell shell = new Shell(display);

		String pathToFile = "";
		try {
			FileDialog t_dialogSave = new FileDialog(shell);
			t_dialogSave.setFilterNames(new String[] { "(.mzXML)", "All files" });
			t_dialogSave.setFilterExtensions(new String[] { "*.mzXML", "*.*" });
			String t_file = t_dialogSave.open();
			if (t_file != null) {
				pathToFile = t_file;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		MSXMLParser parser = new MSXMLParser(pathToFile);
		MZXMLFileInfo header = parser.rapFileHeader();
		
	}

}
