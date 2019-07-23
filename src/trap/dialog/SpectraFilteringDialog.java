package trap.dialog;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import trap.GlycanData;

public class SpectraFilteringDialog extends TitleAreaDialog {

	public SpectraFilteringDialog(Shell a_parentShell) {
		super(a_parentShell);
		// TODO Auto-generated constructor stub
	}

	private Text browseLocationText;
	private Text mzvalueText;
	private Text accuracyValueText;
	private Text cutOfValueText;
	private Text saveLocationText;
	private Combo cmbAccuracy;
	private Combo cmbCutOffTypePrecursor;

	private Boolean PPM;
	private Boolean PercentageCutOf;
	private String browseLocation;
	private Double mzValue;
	private Double accuracyValue;
	private Double cutOfValue;
	private String saveLocation;

	private GlycanData spectrumFilter = new GlycanData();

	public static final String EXTENSION = ".mzXML";
	private static final String DEFAULT_FILENAME = "filteredMass" + EXTENSION;
	private static final Logger logger = Logger.getLogger(SpectraFilteringDialog.class);

	@Override
	public void create() {
		super.create();
		setTitle("Spectra Filtering option");
		setMessage("This is a Dialog box to filter required spectra", IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(5, false);
		layout.horizontalSpacing = 10;
		layout.verticalSpacing = 10;
		area.setSize(100, 100);
		container.setLayout(layout);
		createSpectraFilter(container);
		return area;
	}

	private Button addAButton(Composite container, String label, int horizontalAlignment) {
		Button button = new Button(container, SWT.PUSH);
		button.setText(label);
		GridData buttonGridData = new GridData(horizontalAlignment);
		buttonGridData.horizontalSpan = 1;
		button.setLayoutData(buttonGridData);
		return button;
	}

	private void createSpectraFilter(Composite container) {
		// Browse Button
		Label openLocationLabel = new Label(container, SWT.NONE);
		openLocationLabel.setText("Select the location to load the mzxml file from");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		browseLocationText = new Text(container, SWT.BORDER);

		GridData browseData = new GridData(SWT.FILL, SWT.TOP, true, true, 4, 1);
		browseLocationText.setLayoutData(browseData);
		browseLocationText.setEnabled(false);
		Button browseButton = new Button(container, GridData.HORIZONTAL_ALIGN_END);
		browseButton.setText("Browse..");
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent a_event) {
				try {
					FileDialog t_dialogSave = new FileDialog(container.getShell());
					t_dialogSave.setFilterNames(new String[] { "(.mzXML)", "All files" });
					t_dialogSave.setFilterExtensions(new String[] { "*.mzXML", "*.*" });
					String t_file = t_dialogSave.open();
					if (t_file != null) {
						browseLocationText.setText(t_file);
					}
				} catch (Exception e) {
					logger.fatal("Unable to select a file.", e);
				}
			}
		});
		browseButton.setFocus();

		// mzValue Label
		Label mzValueLabel = new Label(container, SWT.NONE);
		mzValueLabel.setText("Enter m/z value");
		GridData mzvalueGrid = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		mzvalueText = new Text(container, SWT.BORDER);
		mzvalueText.setLayoutData(mzvalueGrid);

		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		// accuracy Label
		Label accuracyLabel = new Label(container, SWT.NONE);
		accuracyLabel.setText("Enter accuracy");
		GridData dataAccuracy = new GridData(GridData.FILL_BOTH);
		accuracyValueText = new Text(container, SWT.BORDER);
		accuracyValueText.setLayoutData(dataAccuracy);

		new Label(container, SWT.NONE);

		final Combo c = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		String items[] = { "Dalton", "PPM" };
		c.setItems(items);
		GridData comboPermethylated = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		comboPermethylated.horizontalSpan = 1;
		c.setLayoutData(comboPermethylated);
		c.select(1);
		PPM = true;
		c.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (c.getText().equals("Dalton")) {
					PPM = false;
				} else if (c.getText().equals("PPM")) {
					PPM = true;
				}
			}
		});

		new Label(container, SWT.NONE);

		// cut of value Label
		Label cutOfValueLabel = new Label(container, SWT.NONE);
		cutOfValueLabel.setText("Enter cut of  value");
		GridData dataCutOfValue = new GridData(GridData.FILL_BOTH);
		cutOfValueText = new Text(container, SWT.BORDER);
		cutOfValueText.setLayoutData(dataCutOfValue);

		new Label(container, SWT.NONE);

		final Combo cutOf = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		String cutOfItems[] = { "Absolute", "Percentage" };
		cutOf.setItems(cutOfItems);
		GridData comboCutof = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		comboCutof.horizontalSpan = 1;
		cutOf.setLayoutData(comboCutof);
		cutOf.select(1);
		PercentageCutOf = true;
		cutOf.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (cutOf.getText().equals("Absolute")) {
					PercentageCutOf = false;
				} else if (cutOf.getText().equals("Percentage")) {
					PercentageCutOf = true;
				}
			}
		});

		new Label(container, SWT.NONE);

		// Save Button
		Label saveLocationLabel = new Label(container, SWT.DOWN);
		saveLocationLabel.setText("Select the location to save the mzxml file to");

		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		saveLocationText = new Text(container, SWT.BORDER);
		GridData saveData = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		saveData.horizontalSpan = 4;
		saveLocationText.setLayoutData(saveData);
		saveLocationText.setText(System.getProperty("user.home") + File.separator + DEFAULT_FILENAME);
		saveLocationText.setEnabled(false);
		saveLocationText.setLayoutData(saveData);
		Button saveButton = addAButton(container, "Save...", GridData.HORIZONTAL_ALIGN_END);
		saveButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setErrorMessage(null);
				FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
				fileDialog.setText("Select File");
				fileDialog.setFilterExtensions(new String[] { "*"+EXTENSION });
				fileDialog.setFilterNames(new String[] { "Spectra Filtering (" + EXTENSION + ")" });
				fileDialog.setFileName(DEFAULT_FILENAME);
				fileDialog.setOverwrite(true);
				String selected = fileDialog.open();
				if (selected != null) {
					saveLocationText.setText(selected);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		saveButton.setFocus();
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	@Override
	protected void okPressed() {
		browseLocation = browseLocationText.getText();
		if (browseLocation.equals("")) {
			setErrorMessage("Select the location to load mzXML file");
			return;
		}
		try {
			mzValue = Double.parseDouble(mzvalueText.getText());
		} catch (Exception e) {
			setErrorMessage("mzValue Value should be Double");
			return;
		}
		try {
			accuracyValue = Double.parseDouble(accuracyValueText.getText());
		} catch (Exception e) {
			setErrorMessage("accuracyValue Value should be Double");
			return;
		}
		try {
			cutOfValue = Double.parseDouble(cutOfValueText.getText());
		} catch (Exception e) {
			setErrorMessage("cutOfValue Value should be Double");
			return;
		}

		saveLocation = saveLocationText.getText();
		if (saveLocation.equals("")) {
			setErrorMessage("Select the location to save mzXML file");
			return;
		}

		spectrumFilter = new GlycanData();
		spectrumFilter.setOpenLocation(browseLocation);
		spectrumFilter.setMzValue(mzValue);
		spectrumFilter.setAccuracy(accuracyValue);
		spectrumFilter.setCutOfValue(cutOfValue);
		spectrumFilter.setSaveLocation(saveLocation);
		if (PPM == true) {
			spectrumFilter.setPPM(true);
		} else {
			spectrumFilter.setPPM(false);
		}
		if (PercentageCutOf == true) {
			spectrumFilter.setPercentage(true);
		} else {
			spectrumFilter.setPercentage(false);
		}
		super.okPressed();
	}

	public GlycanData getFilter() {
		return spectrumFilter;
	}
}
