package openCVImpl;

public class Range {
	private int start;
	private int end;
	private String note;
	
	public Range(int start,int end, String note){
		this.start = start;
		this.end = end;
		this.note = note;
	}
	public boolean contains(int n){
		if(n >= this.start && n <= this.end){
			return true;
		}
		return false;
	}
	public String getNote(){
		return this.note;
	}
}
