package openCVImpl;


public class Pixel {
	int xPixel;
	int yPixel;
	boolean isBlack;
	boolean isBarline = false;
	public Pixel(int xPixelNew,int yPixelNew, boolean isBlackNew){
		xPixel = xPixelNew;
		yPixel = yPixelNew;
		isBlack = isBlackNew;
	}
	public void setIsBarLine(boolean barline){
		isBarline = barline;
	}
	public int getxPixel(){
		return this.xPixel;
	}
	public int getyPixel(){
		return this.yPixel;
	}
	public boolean isBlack(){
		return this.isBlack;
	}
}
