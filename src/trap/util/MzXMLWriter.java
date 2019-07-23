package trap.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.systemsbiology.jrap.grits.stax.Scan;
import org.systemsbiology.jrap.grits.stax.ScanHeader;

public class MzXMLWriter {
	
	private int m_nScanCount;
	private String m_strStartTime;
	private String m_strEndTime;
	
	private BufferedWriter	bw	= null;
	
	public void createMZXML(String fileName) throws IOException {
		bw = new BufferedWriter(new FileWriter(fileName));
	}

	public void setMsRun(int nScanCount, String strStartTime, String strEndTime) {
		this.m_nScanCount = nScanCount;
		this.m_strStartTime = strStartTime;
		this.m_strEndTime = strEndTime;
	}

	public void writeHeader() throws IOException {
		String content
		= "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
		+ "<mzXML xmlns=\"http://sashimi.sourceforge.net/schema_revision/mzXML_3.2\"\n"
		+ "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
		+ "       xsi:schemaLocation=\"http://sashimi.sourceforge.net/schema_revision/mzXML_3.2 http://sashimi.sourceforge.net/schema_revision/mzXML_3.2/mzXML_idx_3.2.xsd\">"
		+ "\n" + getMsRunTag() + "\n";
		
		bw.write(content);
	}

	private String getMsRunTag() {
		return "  <msRun scanCount=\""+this.m_nScanCount+"\" startTime=\""+this.m_strStartTime+"\" endTime=\""+this.m_strEndTime+"\">";
	}

	public void writeFooter() throws IOException {
		String closeTag = "  </msRun>\n" + "</mzXML>";
		bw.write(closeTag);
	}

	public void writeScan(Scan scan) throws IOException {
		
		Element t_elementScan = new Element("scan");
		
		ScanHeader scanHeader = scan.getHeader();
		
		t_elementScan.setAttribute("num", Integer.toString(scanHeader.getNum()));
		t_elementScan.setAttribute("scanType", scanHeader.getScanType());
		t_elementScan.setAttribute("centroided", Integer.toString(scanHeader.getCentroided()));
		t_elementScan.setAttribute("msLevel", Integer.toString(scanHeader.getMsLevel()));
		t_elementScan.setAttribute("peaksCount", Integer.toString(scan.getMassIntensityList()[0].length));
		
		// String polarity = (scanHeader.getPolarity() == "+") ? "+" : "-";
		String polarity = scanHeader.getPolarity(); // verify once
		
		t_elementScan.setAttribute("polarity", polarity);
		
		String retentionTime = scanHeader.getRetentionTime();
		
		t_elementScan.setAttribute("retentionTime", retentionTime);
		t_elementScan.setAttribute("collisionEnergy", Float.toString(scanHeader.getCollisionEnergy()));
		
		t_elementScan.setAttribute("lowMz", Double.toString(scanHeader.getLowMz()));
		t_elementScan.setAttribute("highMz", Double.toString(scanHeader.getHighMz()));
		
		t_elementScan.setAttribute("basePeakMz", Float.toString(scanHeader.getBasePeakMz()));
		t_elementScan.setAttribute("basePeakIntensity", Float.toString(scanHeader.getBasePeakIntensity()));
		t_elementScan.setAttribute("totIonCurrent", Float.toString(scanHeader.getTotIonCurrent()));
		
		Element t_elementPrecuror = new Element("precursorMz");
		
		t_elementPrecuror.setAttribute("precursorScanNum", Integer.toString(scanHeader.getPrecursorScanNum()));
		t_elementPrecuror.setAttribute("precursorIntensity", Double.toString(scanHeader.getPrecursorIntensity()));
		t_elementPrecuror.setAttribute("precursorCharge", Integer.toString(scanHeader.getPrecursorCharge()));
		t_elementPrecuror.setAttribute("activationMethod", scanHeader.getActivationMethod());
		
		t_elementPrecuror.setText(Double.toString(scanHeader.getPrecursorMz()));
		
		Element t_elementPeaks = new Element("peaks");
		
		t_elementPeaks.setAttribute("compressionType", scanHeader.getCompressionType());
		t_elementPeaks.setAttribute("compressedLen", Integer.toString(scanHeader.getCompressedLen()));
		t_elementPeaks.setAttribute("precision", Integer.toString(scanHeader.getPrecision()));
		t_elementPeaks.setAttribute("byteOrder", scanHeader.getByteOrder());
		t_elementPeaks.setAttribute("contentType", scanHeader.getContentType());
		
		// String encode64 =
		// Base64SpectraUtil.encodeBase64(scan.getMassIntensityList());
		
		t_elementPeaks.setText(createBase64String(scan.getMassIntensityList()));
		
		// add the precursor and peak tag to the scan tag
		t_elementScan.addContent(t_elementPrecuror);
		t_elementScan.addContent(t_elementPeaks);
		
		XMLOutputter t_outputter = new XMLOutputter(Format.getPrettyFormat());
		
		String t_xml = t_outputter.outputString(t_elementScan);
		t_xml = "    "+t_xml.replaceAll("\n", "\n    ");
		bw.write(t_xml + "\n");
		
	}
	
	private String createBase64String(double[][] a_peaklist) {
		int size = a_peaklist[0].length;
		float[][] t_spectra = new float[2][size];
		for (int i = 0; i < size; i++) {
			Double mzDouble = a_peaklist[0][i];
			Double intensityDouble = a_peaklist[1][i];
			
			t_spectra[0][i] = mzDouble.floatValue();
			t_spectra[1][i] = intensityDouble.floatValue();
		}

		String encode64 = Base64SpectraUtil.encodeBase64(t_spectra);
		return encode64;
	}
	
	public void closeFile() throws IOException {
		bw.flush();
		bw.close();
	}
}
