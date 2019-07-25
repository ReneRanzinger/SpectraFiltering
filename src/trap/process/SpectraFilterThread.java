package trap.process;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.systemsbiology.jrap.grits.stax.MSXMLParser;
import org.systemsbiology.jrap.grits.stax.Scan;
import org.systemsbiology.jrap.grits.stax.ScanHeader;

import trap.GlycanData;
import trap.dialog.process.ProgressDialogThread;
import trap.util.MzXMLWriter;

public class SpectraFilterThread extends ProgressDialogThread {
	private static final Logger logger = Logger.getLogger(SpectraFilterThread.class);

	private GlycanData m_filter;
	private MSXMLParser m_parser;

	private boolean m_writeParentScan = false;

	public SpectraFilterThread(GlycanData a_filter) {
		this.m_filter = a_filter;
		this.m_parser = new MSXMLParser(this.m_filter.getOpenLocation());
	}

	@Override
	public boolean threadStart() throws Exception {

		LinkedList<Integer> lFilteredScans;
		this.m_progressReporter.setProcessMessageLabel("Task 1 of 2: Filter scans");
		try {
			lFilteredScans = this.filterScans();
			if ( lFilteredScans == null || lFilteredScans.isEmpty() ) {
				this.m_progressReporter.setDescriptionText("All scans are filtered out. The results are not outputted.");
				return true;
			}
		} catch (CancelProcessException e) {
			return false;
		} catch (Exception e) {
			this.m_progressReporter.setDescriptionText(
					"Error reading scans from " + this.m_filter.getOpenLocation() + "\n" + e.getMessage());
			logger.error("Error reading scans from mzXML file: " + this.m_filter.getOpenLocation(), e);
			return false;
		}

		this.m_progressReporter.setProcessMessageLabel("Task 2 of 2: Create filtered mzXML file");
		try {
			this.writeMzXML(lFilteredScans);
		} catch (IOException e) {
			this.m_progressReporter.setDescriptionText("Error when writting the new mzXML file: " + e.getMessage());
			this.deleteMzXMLFile();
			return false;
		} catch (CancelProcessException e) {
			this.deleteMzXMLFile();
			return false;
		} catch (Exception e) {
			this.m_progressReporter.setDescriptionText(
					"Error creating mzXML file from " + this.m_filter.getOpenLocation() + "\n" + e.getMessage());
			logger.error("Error creating mzXML file from mzXML file: " + this.m_filter.getOpenLocation(), e);
			return false;
		}

		return true;
	}

	private LinkedList<Integer> filterScans() throws CancelProcessException {
		LinkedList<Integer> lFilteredScans = new LinkedList<>();

		// Filter scans
		int nMax = m_parser.getMaxScanNumber();
		this.m_progressReporter.setMax(nMax);
		int nCheckPoint = 1;
		if ( nMax > 1000 ) {
			this.m_progressReporter.setMax(1000);
			nCheckPoint = nMax / 1000;
		}
		for( int i = 1; i < nMax + 1; i++ ) {
			ScanHeader header = m_parser.rapHeader(i);
			if ( header == null || header.getMsLevel() != 1 )
				continue;

			// MS1 scan
			if ( i % nCheckPoint == 0 )
				this.m_progressReporter.updateProgresBar("Reading Scan #" + header.getNum());
			int iMS1 = i;

			if (this.m_canceled)
				throw new CancelProcessException();

			List<Integer> lScans = new ArrayList<>();
			lScans.add(iMS1);

			// Reads through next MS1 scan to seek subscans
			while( i < nMax + 1 ) {
				i++;

				if (this.m_canceled)
					throw new CancelProcessException();

				ScanHeader headerSub = m_parser.rapHeader(i);
				if ( headerSub == null )
					continue;
				// Break at next MS1 scan
				if( headerSub.getMsLevel() == header.getMsLevel() ) {
					i--;
					break;
				}
				if ( i % nCheckPoint == 0 )
					this.m_progressReporter.updateProgresBar("Reading Scan #" + header.getNum());

				// Skips if precursor scan is not parent MS1 scan
				if ( headerSub.getPrecursorScanNum() != iMS1 )
					continue;

				// Skips if no peaks in this scan
				if ( headerSub.getPeaksCount() == 0 )
					continue;

				Scan scan = m_parser.rap(i);
				if ( scan == null )
					continue;
				// Filters scan
				if ( this.filterScan(scan) )
					lScans.add(i);
			}
			// Removes MS1 scan if the flag is true
			if ( !this.m_writeParentScan )
				lScans.remove(0);
			if ( lScans.isEmpty() )
				continue;
			lFilteredScans.addAll(lScans);
		}

		return lFilteredScans;
	}

	private boolean filterScan(Scan a_scan) throws CancelProcessException {
		if (this.m_canceled)
			throw new CancelProcessException();

		// Calculate interval
		Double accuracyValue = m_filter.getAccuracy();
		Double mzValue = m_filter.getMzValue();
		Double tolerance = accuracyValue;
		if ( m_filter.getPPM() )
			tolerance *= (mzValue / 1000000);
		Double minInterval = mzValue - tolerance;
		Double maxInterval = mzValue + tolerance;

		// Double highestPeakIntensityInSpectra =
		// a_scan.getPeaklist().get(0).getIntensity();
		Double highestPeakIntensityInSpectra = a_scan.getMassIntensityList()[1][0];
		/* first vector is masses and second vector is the intensity */

		Double rangeIntensity = 0.0;

		// iterate over peaks
		for (int i = 1; i < a_scan.getMassIntensityList()[1].length; i++) {
			if (this.m_canceled)
				throw new CancelProcessException();

			if (a_scan.getMassIntensityList()[1][i] > highestPeakIntensityInSpectra) {
				highestPeakIntensityInSpectra = a_scan.getMassIntensityList()[1][i];
			}
		}

		for (int i = 0; i < a_scan.getMassIntensityList()[0].length; i++) {
			if (this.m_canceled)
				throw new CancelProcessException();

			if (a_scan.getMassIntensityList()[0][i] < maxInterval
					&& a_scan.getMassIntensityList()[0][i] > minInterval) {
				// if (a_scan.getPeaklist().get(i).getIntensity() >
				// rangeIntensity) {
				// System.out.print("Found Range mz ");
				// System.out.println(a_scan.getMassIntensityList()[0][i]);
				rangeIntensity = a_scan.getMassIntensityList()[1][i];
			}
		}

		Double cutOffValue = m_filter.getCutOfValue();
		if ( m_filter.getPercentage() )
			cutOffValue *= highestPeakIntensityInSpectra / 100;

		if (rangeIntensity > 0) {
			if (rangeIntensity > cutOffValue) {
				return true;
			}
		}

		return false;
	}

	private void writeMzXML(LinkedList<Integer> a_lScanIndexes) throws IOException, CancelProcessException {

		this.m_progressReporter.setMax(a_lScanIndexes.size() + 3);

		// Create the MzXML writer
		MzXMLWriter writer = new MzXMLWriter();

		// Create mzXML file
		writer.createMZXML(this.m_filter.getSaveLocation());

		if (this.m_canceled)
			throw new CancelProcessException();

		String strStartTime = m_parser.rapHeader(a_lScanIndexes.getFirst()).getRetentionTime();
		String strEndTime = m_parser.rapHeader(a_lScanIndexes.getLast()).getRetentionTime();
		writer.setMsRun(a_lScanIndexes.size(), strStartTime, strEndTime);

		// Write Header
		this.m_progressReporter.updateProgresBar("Writing header");
		writer.writeHeader();

		for (int iScan : a_lScanIndexes) {
			if (this.m_canceled) {
				writer.closeFile();
				throw new CancelProcessException();
			}
			Scan scan = this.m_parser.rap(iScan);
			this.m_progressReporter.updateProgresBar("Writing scan #" + scan.getHeader().getNum());
			writer.writeScan(scan);
		}

		this.m_progressReporter.updateProgresBar("Writing footer");
		writer.writeFooter();

		// close the file
		writer.closeFile();
		this.m_progressReporter.updateProgresBar("Done!");
	}

	private void deleteMzXMLFile() {
		if ( !Files.exists(Paths.get(this.m_filter.getSaveLocation())) )
				return;
		try {
			Files.delete(Paths.get(this.m_filter.getSaveLocation()));
		} catch (IOException e) {
			logger.error("Unable to delete database file: " + this.m_filter.getSaveLocation(), e);
		}

	}
}
