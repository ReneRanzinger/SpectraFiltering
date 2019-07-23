package trap;

public class GlycanData {
	
	private String	openLocation	= null;
	private Double	mzValue			= null;
	private Double	accuracy		= null;
	private Double	cutOfValue		= null;
	private String	saveLocation	= null;
	private Boolean	PPM;
	private Boolean	percentage;
	
	public String getOpenLocation() {
		return openLocation;
	}
	
	public void setOpenLocation(String a_openLocation) {
		this.openLocation = a_openLocation;
	}
	
	public Double getMzValue() {
		return mzValue;
	}
	
	public void setMzValue(Double a_mzValue) {
		this.mzValue = a_mzValue;
	}
	
	public Double getAccuracy() {
		return accuracy;
	}
	
	public void setAccuracy(Double a_accuracy) {
		this.accuracy = a_accuracy;
	}
	
	public Double getCutOfValue() {
		return cutOfValue;
	}
	
	public void setCutOfValue(Double a_cutOfValue) {
		this.cutOfValue = a_cutOfValue;
	}
	
	public String getSaveLocation() {
		return saveLocation;
	}
	
	public void setSaveLocation(String a_saveLocation) {
		this.saveLocation = a_saveLocation;
	}
	
	public Boolean getPPM() {
		return PPM;
	}
	
	public void setPPM(Boolean ppm) {
		this.PPM = ppm;
	}
	
	public Boolean getPercentage() {
		return percentage;
	}
	
	public void setPercentage(Boolean prcnt) {
		this.percentage = prcnt;
	}
	
}

