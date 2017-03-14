package openCVImpl;

import java.util.ArrayList;

public class Line {
	ArrayList<Pixel> pixels = new ArrayList<Pixel>();
	int blackLineLength = 0; //the length of the line the to left of it.
	public Line(){
		
	}
	public int getLineLength(){
		return this.pixels.size();
	}
	public ArrayList<Pixel> getPixels(){
		return this.pixels;
	}
	public void addPixel(Pixel newPixel){
		this.pixels.add(newPixel);
	}
	public int getInitialY(){
		return this.pixels.get(0).yPixel;
	}
}
