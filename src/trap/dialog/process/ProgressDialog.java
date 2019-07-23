package trap.dialog.process;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * Progress Dialog to show progress indicator
 *
 * @author kitaemyoung
 *
 */
public class ProgressDialog extends Dialog implements IProgressReporter {
	// log4J Logger
	private static final Logger logger = Logger.getLogger(ProgressDialog.class);
	/** Instance of the thread doing the work while progress dialog is shown */
	protected ProgressDialogThread m_worker = null;
	/**
	 * Button at the bottom of the dialog. Used as cancel button at first, later as
	 * Finish button if there are error messages to review by the users.
	 */
	protected Button m_button = null;
	/** Text field that shows the notification messages (Warnings and Errors) */
	protected Text m_textNotification = null;
	/** Main progress bar */
	protected TextProgressBar m_progressBarMain = null;

	protected Composite cancelComposite;
	protected Shell m_shell;
	protected Display display = null;

	private int max = 0;
	private double count = 0.0;
	private double increment = 0.0;

	protected boolean isCanceled = false;

	public ProgressDialog(Shell parent) {
		super(parent);
	}

	public Shell getShell() {
		return m_shell;
	}

	public void setShell(Shell shell) {
		this.m_shell = shell;
	}

	public ProgressDialogThread getWorker() {
		return m_worker;
	}

	public void setWorker(ProgressDialogThread a_worker) {
		this.m_worker = a_worker;
	}

	public void setDisplay(Display display) {
		this.display = display;
	}

	@Override
	public void setProcessMessageLabel(String a_message) {
		// need final variable
		final String t_message = new String(a_message);
		// create sync thread that allows to change the display
		this.display.syncExec(new Runnable() {
			@Override
			public void run() {
				m_progressBarMain.setText(t_message);
			}
		});

	}

	@Override
	public void updateProgresBar(String msg) {
		// need final variable
		final String t_message = new String(msg);
		this.display.syncExec(new Runnable() {
			@Override
			public void run() {
				m_progressBarMain.setSelection((int) (count + increment));
				count = count + increment;
				m_progressBarMain.setText(t_message);
			}
		});
	}

	@Override
	public void threadFinished(boolean successful) {
		if (!isCanceled) {
			// called by the new thread shortly before finishing
			// should trigger the finish button to become available
			this.display.syncExec(new Runnable() {
				@Override
				public void run() {
					m_progressBarMain.setSelection(100);
					m_button.setText("Finish");
					m_button.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							isCanceled = false;
							// then close
							m_shell.close();
						}
					});
					// check if there is no error
//					if (m_textNotification.getText().equals("") && successful) {
//						// then automatically close the dialog
//						isCanceled = false;
//						// then close
//						m_shell.close();
//					} else {
//						// TODO warning message
//					}
				}
			});
		} else {
			this.display.syncExec(new Runnable() {
				@Override
				public void run() {
					isCanceled = true;
					// then close
					m_shell.close();
				}
			});
		}
	}

	public int open() {
		this.createContents();

		// find the center of a main monitor
		Monitor t_primaryMonitor = m_shell.getDisplay().getPrimaryMonitor();
		Rectangle t_boundsMonitor = t_primaryMonitor.getBounds();
		Rectangle t_boundsShell = m_shell.getBounds();
		int x = t_boundsMonitor.x + (t_boundsMonitor.width - t_boundsShell.width) / 2;
		int y = t_boundsMonitor.y + (t_boundsMonitor.height - t_boundsShell.height) / 2;
		m_shell.setLocation(x, y);

		m_shell.open();
		m_shell.layout();

		this.m_worker.setDialog(this);
		this.m_worker.start();
		Display display = getParent().getDisplay();
		while (!m_shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		if (isCanceled) {
			return SWT.CANCEL;
		}
		return SWT.OK;
	}

	private Shell getModalDialog(Shell parent) {
		// return PropertyHandler.getModalDialog(getParent());
		return new Shell(parent,
				SWT.APPLICATION_MODAL | SWT.BORDER | SWT.TITLE & (~SWT.RESIZE) & (~SWT.MAX) & (~SWT.MIN));
	}

	protected void createContents() {
		// shell = new Shell(getParent(), SWT.TITLE | SWT.PRIMARY_MODAL);
		m_shell = getModalDialog(getParent());

		display = m_shell.getDisplay();
		final GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 10;

		m_shell.setLayout(gridLayout);
		m_shell.setText("Progress Dialog");

		// progress indicator
		// Create a smooth progress bar
		this.m_progressBarMain = new TextProgressBar(m_shell, SWT.HORIZONTAL | SWT.SMOOTH);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		this.m_progressBarMain.setMinimum(0);
		this.m_progressBarMain.setMaximum(this.max);
		this.m_progressBarMain.setLayoutData(gd);

		m_button = new Button(m_shell, SWT.NONE);
		m_button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isCanceled = true;
				// close the shell
				clickCancel();
			}
		});
		gd = new GridData(78, SWT.DEFAULT);
		gd.horizontalAlignment = SWT.END;
		m_button.setLayoutData(gd);
		m_button.setText("Cancel");

		m_shell.pack();
		m_shell.setSize(483, m_shell.getSize().y);
	}

	protected void clickCancel() {
		// notify worker
		this.m_worker.cancelWork();
	}

	private void createNotificationText() {
		// need to create a field!
		GridData descriptionTextData = new GridData(GridData.FILL, GridData.CENTER, true, true);
		descriptionTextData.minimumHeight = 80;
		descriptionTextData.horizontalSpan = 2;
		m_textNotification = new Text(m_shell, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		m_textNotification.setLayoutData(descriptionTextData);
		m_textNotification.setEditable(false);

		m_textNotification.moveAbove(m_button);
		m_shell.pack();
		m_shell.setSize(483, m_shell.getSize().y);
		m_shell.layout();
	}

	/**
	 * This area is only for errors
	 *
	 * @param description
	 */
	@Override
	public void setDescriptionText(String description) {
		final String t_message = new String(description);
		// create sync thread that is allow to change the display
		this.display.syncExec(new Runnable() {
			@Override
			public void run() {
				if ( m_textNotification == null )
					createNotificationText();
				StringBuffer sb = new StringBuffer();
				if (m_textNotification.getText().equals("")) {
					sb.append(t_message);
				} else {
					sb.append(m_textNotification.getText());
					sb.append(System.lineSeparator());
					sb.append(t_message);
				}
				m_textNotification.setText(sb.toString());
				// auto scroll down!!
				m_textNotification.setSelection(m_textNotification.getCharCount());
			}
		});
	}

	@Override
	public void setMax(int max) {
		this.max = max;
		this.increment = 100.00 / max;
		// start from beginning
		this.display.syncExec(new Runnable() {
			@Override
			public void run() {
				m_progressBarMain.setSelection(0);
				m_progressBarMain.setText("");
				count = 0;
			}
		});
	}

	public Display getDisplay() {
		return display;
	}

	@Override
	public void endWithException(final Exception e) {
		// need to close
		this.display.syncExec(new Runnable() {
			@Override
			public void run() {
				logger.error(e.getMessage(), e);
//				if (ErrorUtils.createErrorMessageBoxReturn(m_shell, "Error", e) == 1) {
				if (createErrorMessageBoxReturn(m_shell, "Error", e) == 1) {
					isCanceled = true;
					// then close
					m_shell.close();
				}
			}
		});
	}

	/**
	 * 1-OK button; 0-Close button
	 * 
	 * @param shell
	 * @param errmsg
	 * @param e
	 * @return
	 */
	private static int createErrorMessageBoxReturn(Shell shell, String errmsg, Exception e) {
		MessageBox messageBox = new MessageBox(shell, SWT.OK & (~SWT.CLOSE));
		messageBox.setText(errmsg);
		if (e.getMessage() == null) {
			messageBox.setMessage("Argument cannot be null");
		} else {
			messageBox.setMessage(e.getMessage());
		}
		int response = messageBox.open();
		if (response == SWT.OK) {
			// need to keep going
			return 1;
		}
		// then close the program
		return 0;
	}

}
