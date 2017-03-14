package openCVImpl;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;



public class Stave {
	ArrayList<Line> staffLines = new ArrayList<Line>();
	ArrayList<Bar> bars = new ArrayList<Bar>();
	String clef = "";
	public String[] notes = new String[7];
	public int midPoint;
	private HashMap<Integer,String> ranges = new HashMap<Integer,String>();
	
	public boolean checkIfLineContained(Line l){
		for(int i=0;i<staffLines.size();i++){
			if(staffLines.get(i).getInitialY() == l.getInitialY()){
				return true;
			}
		}
		return false;
	}
	
	public void findSymbols(Pixel[][] score, int staveDistance, int spaceDistance, Score currentScore){
		for(int i=0;i<bars.size();i++){
			System.out.println("Bar: " + i);
			this.findNoteArea(currentScore);
			bars.get(i).findSymbols(score, staveDistance, spaceDistance, currentScore, ranges);
		}
	}
	
	public void findBars(Pixel[][] score){
	BufferedImage image2;
	try {
		image2 = ImageIO.read(new File("simple-test.bmp"));
	
	int barLineHeight = staffLines.get(staffLines.size()-1).getInitialY() - staffLines.get(0).getInitialY() ;
	ArrayList<Line> potentialBarLines = new ArrayList<Line>();
	for(int i=0;i<staffLines.get(0).getPixels().size();i++){
		boolean isBarLine = true;
		Line currentLine = new Line();
		for(int j=0;j<barLineHeight;j++){
			if(score[staffLines.get(0).getPixels().get(i).getxPixel()][staffLines.get(0).getInitialY()+j].isBlack){
				if(isBarLine){
					image2.setRGB(staffLines.get(0).getPixels().get(i).getxPixel(),staffLines.get(0).getInitialY()+j , Color.BLUE.getRGB());
					currentLine.addPixel(score[staffLines.get(0).getPixels().get(i).getxPixel()][staffLines.get(0).getInitialY()+j]);
				}
			}else{
				isBarLine = false;
			}
		}
		if(isBarLine){
			potentialBarLines.add(currentLine);
		}
	}
	
	ArrayList<Integer> measurementHolder = new ArrayList<Integer>();
	Double additionDouble = (staffLines.get(0).getLineLength() * 0.05);
	int addition = additionDouble.intValue(); //distance to get rid of key and clef
	measurementHolder.add(staffLines.get(0).getPixels().get(0).getxPixel() + addition); //add the first line.
	for(int i=0;i<potentialBarLines.size()-1;i++){
		if((potentialBarLines.get(i+1).getPixels().get(0).xPixel - potentialBarLines.get(i).getPixels().get(0).xPixel) > 5 ){
			image2.setRGB(potentialBarLines.get(i+1).getPixels().get(0).xPixel, potentialBarLines.get(i).getInitialY(), Color.BLUE.getRGB());
			measurementHolder.add(potentialBarLines.get(i+1).getPixels().get(0).xPixel);
		}
	}
	BufferedImage barImage = ImageIO.read(new File("simple-test.bmp"));
	ArrayList<Bar> barsInStave = new ArrayList<Bar>();
	for(int i=0;i<measurementHolder.size()-1;i++){
		barsInStave.add(new Bar(measurementHolder.get(i), measurementHolder.get(i+1), staffLines));
		barImage.setRGB(measurementHolder.get(i), staffLines.get(0).getInitialY(), Color.ORANGE.getRGB());
	}
	int lengthSum = 0;
	for(int i=0; i<barsInStave.size();i++){
		int currentLength = barsInStave.get(i).endingXPixel - barsInStave.get(i).beginningXPixel;
		lengthSum = lengthSum + currentLength;
	}
	int averageLength = lengthSum/barsInStave.size();
	for(int i=0; i<barsInStave.size();i++){
		int currentLength = barsInStave.get(i).endingXPixel - barsInStave.get(i).beginningXPixel;
		if(currentLength > (averageLength * 0.9)){
			bars.add(barsInStave.get(i));
		}
	}
	
	boolean holder = ImageIO.write(barImage, "png", new File("bars"+ staffLines.get(0).getInitialY() +".png"));
	
	System.out.println("number of bars: " + bars.size());
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
	private void findNoteArea(Score currentScore){
		String[] trebleNotes = {"F","E","D","C","B","A","G"};
		String[] bassNotes = {"A","G","F","E","D","C","B"};
		int octaveCounter;
		int octaveCounterUp;
		int octaveCounterDown;
		if(this.clef == "treble"){
			notes = trebleNotes;
			octaveCounter = 5;
			octaveCounterUp = 5;
			octaveCounterDown = 4;	
		}else{
			notes = bassNotes;
			octaveCounter = 3;
			octaveCounterUp = 3;
			octaveCounterDown = 2;	
		}
		
		int averageDistance = 0;
		for(int i=0;i<this.staffLines.size()-1;i++){
			averageDistance = averageDistance + (int)Math.abs(staffLines.get(i).getInitialY() - staffLines.get(i+1).getInitialY());
		}
		averageDistance = (averageDistance / (this.staffLines.size()-1)) + 2;

		HashMap<Integer,String> noteYValues = new HashMap<Integer,String>();
		int counter = 0;
		int counterUp = 6;
		int counterDown = 2;
		
		
		
		for(int i=0;i<this.staffLines.size()-1;i++){
			int currentDistance = (int)Math.abs(staffLines.get(i).getInitialY() - staffLines.get(i+1).getInitialY());
			//on the lines
			noteYValues.put(this.staffLines.get(i).getInitialY(), notes[counter]+":"+octaveCounter);
			//System.out.println("adding lines: " + notes[counter]+":"+octaveCounter);
			currentScore.testImage.setRGB(200, this.staffLines.get(i).getInitialY(), Color.BLUE.getRGB());
			counter++;
			if(counter == 7){
				counter = 0;
			}
			if(notes[counter] == "B"){
				octaveCounter--;
			}
			//in the spaces
			noteYValues.put(this.staffLines.get(i).getInitialY() + (currentDistance/2), notes[counter]+":"+octaveCounter);
			//System.out.println("adding lines: " + notes[counter]+":"+octaveCounter);
			currentScore.testImage.setRGB(200, this.staffLines.get(i).getInitialY() + (currentDistance/2), Color.BLUE.getRGB());
			counter++;
			if(counter == 7){
				counter = 0;
			}
			if(notes[counter] == "B"){
				octaveCounter--;
			}
			noteYValues.put(staffLines.get(staffLines.size()-1).getInitialY(),notes[counter]+":"+octaveCounter);
		}
		int topToMove = this.staffLines.get(0).getInitialY() - averageDistance;
		int topToMoveHalf = this.staffLines.get(0).getInitialY() - (averageDistance/2);
		for(int i=1;i<6;i++){
			//lines going up half
			noteYValues.put(topToMoveHalf, notes[counterUp]+":"+octaveCounterUp);
			//System.out.println("adding up: " + notes[counterUp]+":"+octaveCounterUp);
			currentScore.testImage.setRGB(200, this.staffLines.get(0).getInitialY() - ((averageDistance/2)*i), Color.RED.getRGB());
			
			counterUp--;
			if(counterUp == 0){
				counterUp = 6;
			}
			if(notes[counter] == "D"){
				octaveCounterUp++;
			}
			topToMoveHalf = topToMoveHalf - averageDistance;
			
			
			noteYValues.put(this.staffLines.get(staffLines.size()-1).getInitialY() + ((averageDistance/2)*i), notes[counterDown]+":"+octaveCounterDown);
			//System.out.println("adding down: " + notes[counterDown]+":"+octaveCounterDown);
			currentScore.testImage.setRGB(200, this.staffLines.get(staffLines.size()-1).getInitialY() + ((averageDistance/2)*i), Color.GREEN.getRGB());
			counterDown++;
			if(counterDown == 7){
				counterDown = 0;
			}
			if(notes[counter] == "B"){
				octaveCounterDown--;
			}
		}
		this.midPoint = staffLines.get(2).getInitialY();
		
		this.ranges = noteYValues;
		
	}
	
}
