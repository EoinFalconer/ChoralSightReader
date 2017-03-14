package openCVImpl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

public class Symbol {
	
	public int startX;
	public int endX;
	
	private int startY;
	private int endY;
	
	private int barLength;
	
	private int symbolWidth;
	private int symbolHeight;
	
	public boolean recognised;
	
	private int numberOfWholeNoteHeads = 0;
	
	public String rhythmNames = "";
	
	public String noteValues = "";
	public String octaves = "";
	
	public String noteHeadYValues = "";
	
	public boolean singleIsDotted;
	
	
	private ArrayList<Line> symbolStems = new ArrayList<Line>();
	
	private HashMap<Integer,String> ranges;
	
	public Symbol(int sX, int eX, int sY, int eY, int bL, Score currentScore, HashMap<Integer,String> ranges){
		startX = sX;
		endX = eX;
		startY = sY;
		endY = eY;
		barLength = bL;
		symbolWidth = endX - startX;
		symbolHeight = endY - startY;
		this.ranges = ranges;
		System.out.println("------- NEW SYMBOL ------");
		Pixel[][] score = currentScore.buildArrayFromBitmap(currentScore.symbolImage);
		
		detectNoteHeads(currentScore, score);
		
	}
	public boolean isWholeBarRest(){
		if((endX - startX) > barLength*0.55){
			return true;
		}
		return false;
	}
	public void detectNoteHeads(Score currentScore, Pixel[][] score){
		ArrayList<Pixel> potentialWholeNotes = new ArrayList<Pixel>();
		boolean hasStems = this.checkForStem(currentScore, score);
		boolean needToBreak = false;
		for(int i=0;i<symbolWidth;i++){
			if(needToBreak){
				break;
			}
			for(int j=0;j<symbolHeight;j++){
			
			int blackAbove = 0;
			int blackBelow = 0;
			int blackLeft = 0;
			int blackRight = 0;
			
			if( (startX + i) < score.length && (startY + j) < score[0].length){
				//System.out.println("inside the complex loops");
				if(score[startX + i][startY + j].isBlack()){
					int movingPixel = 1;
					//check above
					while(score[startX + i][startY + j-movingPixel].isBlack()){
						//currentScore.symbolImage.setRGB(i, j-movingPixel, Color.ORANGE.getRGB());
						movingPixel++;
					}
					blackAbove = movingPixel;
					
					movingPixel = 1;
					//check below
					while(score[startX + i][startY + j+movingPixel].isBlack()){
						//currentScore.symbolImage.setRGB(i, j+movingPixel, Color.RED.getRGB());
						movingPixel++;
					}
					blackBelow = movingPixel;
					
					movingPixel = 1;
					//check left
					while(score[startX + i-movingPixel][startY + j].isBlack()){
						//currentScore.symbolImage.setRGB(i, j-movingPixel, Color.ORANGE.getRGB());
						movingPixel++;
					}
					blackLeft = movingPixel;
					
					movingPixel = 1;
					//check right
					while(score[startX + i+movingPixel][startY + j].isBlack()){
						//currentScore.symbolImage.setRGB(i, j+movingPixel, Color.ORANGE.getRGB());
						movingPixel++;
					}
					blackRight = movingPixel;
					
					if((blackAbove == blackBelow || blackAbove - blackBelow == 1 || blackBelow - blackAbove == 1) && blackAbove != 1 && blackBelow != 1){
						if((blackLeft == blackRight || blackLeft - blackRight == 1 || blackRight - blackLeft == 1) && blackLeft != 1 && blackRight != 1){
							/*for(int k=0;k<blackLeft;k++){
								currentScore.symbolImage.setRGB(startX + i - k, startY + j, Color.PINK.getRGB());
								currentScore.symbolImage.setRGB(startX + i + k, startY + j, Color.PINK.getRGB());
							}*/
							/*for(int k=0;k<(currentScore.spaceHeight);k++){
								currentScore.symbolImage.setRGB(startX + i - k, startY + j, Color.GREEN.getRGB());
								currentScore.symbolImage.setRGB(startX + i + k, startY + j, Color.GREEN.getRGB());
							}*/
							
							if((blackAbove + blackBelow) >= (currentScore.spaceHeight*0.8) && (blackLeft + blackRight) > (currentScore.spaceHeight) /*&& (blackLeft + blackRight) < (currentScore.spaceHeight)*/){
								potentialWholeNotes.add(new Pixel(startX + i,startY + j, true));
								currentScore.symbolImage.setRGB(startX + i, startY + j, Color.CYAN.getRGB());
	
							}
						}
					}
				}else{
					//check for an open notehead
				}
			}else{
				needToBreak = true;
			}
		}
	}	
		
	
		System.out.println("potentialWholeNotes.size(); " + potentialWholeNotes.size());
		System.out.println("symbolStems: " + symbolStems.size());
		if(potentialWholeNotes.size() > 0){
			/*for(int i=0;i<potentialWholeNotes.size();i++){
				if(hasStems){
					potentialWholeNotes.remove(i);
				}
			}*/
			//go through potentials with stem and then if there's something in the accepted ones
			//that matches it don't add it.
			ArrayList<Pixel> accepted = new ArrayList<Pixel>();
			int numberOfHeads = 0;
			accepted.add(potentialWholeNotes.get(0));
			numberOfHeads++;
			
			
			for(int i=1;i<potentialWholeNotes.size();i++){
				
				for(int j=0;j<accepted.size();j++){
						if(((Math.abs(potentialWholeNotes.get(i).getxPixel() - accepted.get(j).getxPixel()) > 4) && (Math.abs(potentialWholeNotes.get(i).getyPixel() - accepted.get(j).getyPixel()) > 4))){
							accepted.add(potentialWholeNotes.get(i));
							currentScore.symbolImage.setRGB(potentialWholeNotes.get(j).xPixel, potentialWholeNotes.get(j).yPixel, Color.BLUE.getRGB());
							this.numberOfWholeNoteHeads++;
						}
				}
			}
			System.out.println("symbolStems.size(): " + symbolStems.size());
			//if there is one stem then it is either a single crotchet or a single 
			if(symbolStems.size() == 1){
					System.out.println("IN THE SINGLE STEM");
					boolean isSameNote = true;
					int averageYValue = 0;
					for(int i=0;i<potentialWholeNotes.size();i++){
						averageYValue = averageYValue + potentialWholeNotes.get(i).getyPixel();
						for(int j=0;j<potentialWholeNotes.size();j++){
							if(Math.abs(potentialWholeNotes.get(j).xPixel - potentialWholeNotes.get(i).xPixel) > currentScore.spaceHeight){
								isSameNote = false;
							}
						}
					}
					/*
					 * Get the note value
					 */
					averageYValue = averageYValue/potentialWholeNotes.size();
					currentScore.testImage.setRGB(potentialWholeNotes.get(0).getxPixel(), averageYValue, Color.GREEN.getRGB());
					currentScore.symbolImage.setRGB(potentialWholeNotes.get(0).getxPixel(), averageYValue, Color.RED.getRGB());
					int minDistance = currentScore.symbolImage.getHeight();
					int closestPoint = 0;
					for(Integer current : ranges.keySet()){
						//System.out.println("memeber of keyset: " + current + " averageYValue: " + averageYValue + " difference: " + (int)Math.abs(current-averageYValue) +  "this note is: " + ranges.get(current));
						if((int)Math.abs(current-averageYValue) < minDistance){
							minDistance = (int)Math.abs(current-averageYValue);
							closestPoint = current;
						}
					}
					System.out.println("what you're looking for: " + ranges.get(closestPoint));
					String[] vals = ranges.get(closestPoint).split(":");
					this.noteValues = vals[0];
					this.octaves = vals[1];
					this.noteHeadYValues = Integer.toString(averageYValue);
					
					if(isSameNote){
						System.out.println("stems are more one and is the same note");
						Line theStem = symbolStems.get(0);
						boolean topCornerLeftPresence = false;
						boolean bottomCornerLeftPresence = false;
						boolean bottomCornerRightPresence = false;
						boolean topCornerRightPresence = false;
						
						Pixel topPixel = theStem.getPixels().get((int)Math.floor(theStem.getPixels().size()*0.3));
						Pixel bottomPixel = theStem.getPixels().get((int)Math.floor(theStem.getPixels().size()*0.9));
						for(int i=1;i<currentScore.spaceHeight;i++){
							currentScore.symbolImage.setRGB(topPixel.getxPixel()+i, topPixel.getyPixel(), Color.PINK.getRGB());
							currentScore.symbolImage.setRGB(topPixel.getxPixel()-i, topPixel.getyPixel(), Color.RED.getRGB());
							currentScore.symbolImage.setRGB(bottomPixel.getxPixel()+i, bottomPixel.getyPixel(), Color.ORANGE.getRGB());
							currentScore.symbolImage.setRGB(bottomPixel.getxPixel()-i, bottomPixel.getyPixel(), Color.GRAY.getRGB());
							if(score[topPixel.getxPixel()+i][topPixel.getyPixel()].isBlack()){
								currentScore.symbolImage.setRGB(topPixel.getxPixel()+i, topPixel.getyPixel(), Color.BLUE.getRGB());
								topCornerRightPresence = true;
							}
							if(score[topPixel.getxPixel()-i][topPixel.getyPixel()].isBlack()){
								currentScore.symbolImage.setRGB(topPixel.getxPixel()-i, topPixel.getyPixel(), Color.BLUE.getRGB());
								topCornerLeftPresence = true;
							}
							if(score[bottomPixel.getxPixel()+i][bottomPixel.getyPixel()].isBlack()){
								currentScore.symbolImage.setRGB(bottomPixel.getxPixel()+i, bottomPixel.getyPixel(), Color.BLUE.getRGB());
								bottomCornerRightPresence = true;
							}
							if(score[bottomPixel.getxPixel()-i][bottomPixel.getyPixel()].isBlack()){
								currentScore.symbolImage.setRGB(bottomPixel.getxPixel()-i, bottomPixel.getyPixel(), Color.BLUE.getRGB());
								bottomCornerLeftPresence = true;
							}
						}
						boolean topPresence = false;
						boolean bottomPresence = false;
						
						if(topCornerRightPresence || topCornerLeftPresence){
							topPresence = true;
						}
						if(bottomCornerRightPresence || bottomCornerLeftPresence){
							bottomPresence = true;
						}
						if(topPresence && bottomPresence){
							this.rhythmNames = "eighth";
							System.out.println("QUAVER:" + this.noteValues);
						}else{
							this.rhythmNames = "quarter";
							this.singleIsDotted = this.hasDot(new Pixel(potentialWholeNotes.get(0).xPixel,averageYValue,true) , theStem,score, currentScore);
							System.out.println("CROTCHET: " + this.noteValues);
						}
					}
		}else if(this.symbolStems.size() > 1){
			System.out.println("multiple notes");
			ArrayList<ArrayList<Pixel>> noteHeadGroups = new ArrayList<ArrayList<Pixel>>();
			int counter = 0;
			noteHeadGroups.add(new ArrayList<Pixel>());
			for(int i=0;i<potentialWholeNotes.size()-1;i++){
				if((int)Math.abs(potentialWholeNotes.get(i).xPixel - potentialWholeNotes.get(i+1).xPixel) < 3){
					noteHeadGroups.get(counter).add(potentialWholeNotes.get(i));
				}else{
					noteHeadGroups.get(counter).add(potentialWholeNotes.get(i));
					counter++;
					noteHeadGroups.add(new ArrayList<Pixel>());
				}
			}
			
			ArrayList<Pixel> centerPixels = new ArrayList<Pixel>();
			int averageYs = 0;
			int averageXs = 0;
			int divider = 0;
			for(int i=0;i<noteHeadGroups.size();i++){
				for(int j=0;j<noteHeadGroups.get(i).size();j++){
					averageYs = averageYs + noteHeadGroups.get(i).get(j).yPixel;
					averageXs = averageXs + noteHeadGroups.get(i).get(j).xPixel;
					divider++;
				}
				averageYs = averageYs / divider;
				averageXs = averageXs / divider;
				centerPixels.add(new Pixel(averageXs, averageYs,true));
				currentScore.testImage.setRGB(averageXs, averageYs, Color.PINK.getRGB());
				divider = 0;
				averageYs = 0;
				averageXs = 0;
			}
			
			System.out.println("number of note heads " + centerPixels.size());
			
			//determine whether the note heads are on top or on bottom
			
			boolean stemsUp = false;
			if((int)Math.abs(this.symbolStems.get(0).getPixels().get(0).yPixel - centerPixels.get(0).yPixel) > (this.symbolStems.get(0).getLineLength()*0.3)){
				stemsUp = true;
			}
			int numberOfBars = 0;
			
			if(!stemsUp){
				for(int i=0;i<centerPixels.size()-1;i++){
					int midDistance = (int)Math.abs(centerPixels.get(i).xPixel - centerPixels.get(i+1).xPixel)/2;
					boolean inblack = false;
					for(int j=0;j<this.symbolStems.get(0).getLineLength()*1.3;j++){
						currentScore.symbolImage.setRGB(centerPixels.get(i).xPixel+midDistance, centerPixels.get(i).yPixel+j, Color.BLUE.getRGB());
						if(score[centerPixels.get(i).xPixel+midDistance][centerPixels.get(i).yPixel+j].isBlack() && !inblack){
							inblack = true;
							numberOfBars++;
							System.out.println("found black here");
						}else if(!score[centerPixels.get(i).xPixel+midDistance][centerPixels.get(i).yPixel+j].isBlack()){
							inblack = false;
						}
					}
				}
			}else{
				for(int i=0;i<centerPixels.size()-1;i++){
					int midDistance = (int)Math.abs(centerPixels.get(i).xPixel - centerPixels.get(i+1).xPixel)/2;
					boolean inblack = false;
					for(int j=0;j<this.symbolStems.get(0).getLineLength()*1.3;j++){
						currentScore.symbolImage.setRGB(centerPixels.get(i).xPixel+midDistance, centerPixels.get(i).yPixel-j, Color.BLUE.getRGB());
						System.out.println("isblack: " + inblack);
						if(score[centerPixels.get(i).xPixel+midDistance][centerPixels.get(i).yPixel-j].isBlack() && !inblack){
							inblack = true;
							numberOfBars++;
							System.out.println("found black here");
						}else if(!score[centerPixels.get(i).xPixel+midDistance][centerPixels.get(i).yPixel-j].isBlack()){
							inblack = false;
						}
					}
				}
			}
			System.out.println("numberOfBars: " + numberOfBars + " centerPixels.size(): " + centerPixels.size());
			if(numberOfBars == 1){
				for(int i=0;i<centerPixels.size();i++){
					this.rhythmNames = this.rhythmNames + "eighth-";
					int closestPoint = 0;
					int minDistance = currentScore.symbolImage.getHeight();
					int YValue = centerPixels.get(i).yPixel;
					for(Integer current : ranges.keySet()){
						if((int)Math.abs(current-YValue) < minDistance){
							minDistance = (int)Math.abs(current-YValue);
							closestPoint = current;
						}
					}
					String[] vals = ranges.get(closestPoint).split(":");
					this.noteValues = this.noteValues +  vals[0]+"-";
					this.octaves = this.octaves + vals[1]+"-";
					if(!stemsUp){
						this.noteHeadYValues = this.noteHeadYValues + this.startY + "-";
					}else{
						this.noteHeadYValues = this.noteHeadYValues + this.endY + "-";
					}
					
				}
				System.out.println("this.noteValues: " + this.noteValues + " this.octaves: " + this.octaves);
			}
	}
	} else if(this.symbolStems.size() == 1){
		System.out.println("In the minum area");
		Line theStem = symbolStems.get(0);
		boolean topCornerLeftPresence = false;
		boolean bottomCornerLeftPresence = false;
		boolean bottomCornerRightPresence = false;
		boolean topCornerRightPresence = false;
		Pixel topPixel = theStem.getPixels().get((int)Math.floor(theStem.getPixels().size()*0.1));
		Pixel bottomPixel = theStem.getPixels().get((int)Math.floor(theStem.getPixels().size()*0.9));
		for(int i=1;i<currentScore.spaceHeight;i++){
			currentScore.symbolImage.setRGB(topPixel.getxPixel()+i, topPixel.getyPixel(), Color.PINK.getRGB());
			currentScore.symbolImage.setRGB(topPixel.getxPixel()-i, topPixel.getyPixel(), Color.RED.getRGB());
			currentScore.symbolImage.setRGB(bottomPixel.getxPixel()+i, bottomPixel.getyPixel(), Color.ORANGE.getRGB());
			currentScore.symbolImage.setRGB(bottomPixel.getxPixel()-i, bottomPixel.getyPixel(), Color.GRAY.getRGB());
			if(score[topPixel.getxPixel()+i][topPixel.getyPixel()].isBlack()){
				currentScore.symbolImage.setRGB(topPixel.getxPixel()+i, topPixel.getyPixel(), Color.BLUE.getRGB());
				topCornerRightPresence = true;
			}
			if(score[topPixel.getxPixel()-i][topPixel.getyPixel()].isBlack()){
				currentScore.symbolImage.setRGB(topPixel.getxPixel()-i, topPixel.getyPixel(), Color.BLUE.getRGB());
				topCornerLeftPresence = true;
			}
			if(score[bottomPixel.getxPixel()+i][bottomPixel.getyPixel()].isBlack()){
				currentScore.symbolImage.setRGB(bottomPixel.getxPixel()+i, bottomPixel.getyPixel(), Color.BLUE.getRGB());
				bottomCornerRightPresence = true;
			}
			if(score[bottomPixel.getxPixel()-i][bottomPixel.getyPixel()].isBlack()){
				currentScore.symbolImage.setRGB(bottomPixel.getxPixel()-i, bottomPixel.getyPixel(), Color.BLUE.getRGB());
				bottomCornerLeftPresence = true;
			}
		}
		boolean topPresence = false;
		boolean bottomPresence = false;
		
		if(topCornerRightPresence || topCornerLeftPresence){
			topPresence = true;
		}
		if(bottomCornerRightPresence || bottomCornerLeftPresence){
			bottomPresence = true;
		}
		if(topPresence && !bottomPresence){
			
			this.rhythmNames = "half";
			int closestPoint = 0;
			int minDistance = currentScore.symbolImage.getHeight();
			int YValue = theStem.getPixels().get(0).yPixel;
			for(Integer current : ranges.keySet()){
				if((int)Math.abs(current-YValue) < minDistance){
					minDistance = (int)Math.abs(current-YValue);
					closestPoint = current;
				}
			}
			String[] vals = ranges.get(closestPoint).split(":");
			this.noteValues = vals[0];
			this.octaves = vals[1];
			this.noteHeadYValues = Integer.toString(YValue);
			System.out.println("minum: note head on top: " + this.noteValues);
		}else if(!topPresence && bottomPresence){
			this.rhythmNames = "half";
			int closestPoint = 0;
			int minDistance = currentScore.symbolImage.getHeight();
			int YValue = theStem.getPixels().get(theStem.getPixels().size()-1).yPixel;
			for(Integer current : ranges.keySet()){
				if((int)Math.abs(current-YValue) < minDistance){
					minDistance = (int)Math.abs(current-YValue);
					closestPoint = current;
				}
			}
			String[] vals = ranges.get(closestPoint).split(":");
			this.noteValues = vals[0];
			this.octaves = vals[1];
			this.noteHeadYValues = Integer.toString(YValue);
			System.out.println("minum: note head on top: " + this.noteValues);
		}else{
			System.out.println("vertical line found and is not a note.");
		}
		
	}
		
}
	private boolean checkForStem(Score currentScore, Pixel[][] score){
		int symbolLength = endX - startX;
		int symbolHeight = endY - startY;
		boolean needToBreak = false;
		ArrayList<Line> stems = new ArrayList<Line>();
		for(int i=0;i<symbolLength;i++){
			if(needToBreak){
				break;
			}
			int currentLine = 0;
			Line currentLineObj = new Line();
			for(int j=0;j<symbolHeight;j++){
				if( (startX + i) < score.length && (startY + j) < score[0].length){
					if(score[startX+i][startY+j].isBlack()){
						currentLineObj.addPixel(score[startX+i][startY+j]);
						currentLine++;
					}
					if(!score[startX+i][startY+j].isBlack() && currentLine != 0){
						break;
					}
				}else{
					needToBreak = true;
				}
			}
			if(currentLine > ((currentScore.spaceHeight*3)*0.6)){
				boolean okToAdd = true;
				for(int k=0;k<stems.size();k++){
					if((int)Math.abs(stems.get(k).getPixels().get(0).xPixel - currentLineObj.getPixels().get(0).getxPixel()) < 3){
						okToAdd = false;
						for(int l=0;l<currentLineObj.getPixels().size();l++){
							currentLineObj.getPixels().get(l).isBlack = false;
						}
						break;
					}
				}
				if(okToAdd){
					stems.add(currentLineObj);
					for(int k=0;k<currentLineObj.getPixels().size();k++){
						currentScore.symbolImage.setRGB(currentLineObj.getPixels().get(k).xPixel, currentLineObj.getPixels().get(k).yPixel, Color.RED.getRGB());
					}
				}
			}
		}
		this.symbolStems = stems;
		if(stems.size() > 0){
			return true;
		}else{
			return false;
		}
	}
	public boolean hasDot(Pixel noteHead, Line stem, Pixel[][] score, Score currentScore){
		boolean headOnTop = false;
		if((int)Math.abs(noteHead.yPixel - stem.getPixels().get(0).getyPixel()) < (stem.getPixels().size()*0.2)){
			headOnTop = true;
		}
		int noteHeadWidth = 0;
		int blackLeft = 0;
		int blackRight = 0;
		int i=0;
		while(score[noteHead.getxPixel() - i][noteHead.getyPixel()].isBlack()){
			//currentScore.symbolImage.setRGB(i, j-movingPixel, Color.ORANGE.getRGB());
			blackLeft++;
			i++;
		}
		i = 0;
		while(score[noteHead.getxPixel() + i][noteHead.getyPixel()].isBlack()){
			//currentScore.symbolImage.setRGB(i, j-movingPixel, Color.ORANGE.getRGB());
			blackRight++;
			i++;
		}
		noteHeadWidth = blackLeft + blackRight;
		int amountAccross = noteHeadWidth;
		int amountUp = (int)Math.floor(noteHeadWidth*1.5);
		int xStarting = (int)Math.floor(noteHeadWidth*0.7);
		int amountOfBlack = 0;
		if(headOnTop){
			for(i=0;i<amountUp;i++){
				for(int j=0;j<amountAccross;j++){
					if(score[noteHead.xPixel+xStarting+j][noteHead.yPixel-i].isBlack()){
						currentScore.symbolImage.setRGB(noteHead.xPixel+xStarting+j, noteHead.yPixel-i, Color.GREEN.getRGB());
						amountOfBlack++;
					}
				}
			}
		}else{
			xStarting = (int)Math.floor(noteHeadWidth*0.9);
			for(i=0;i<amountUp;i++){
				for(int j=0;j<amountAccross;j++){
					if(score[noteHead.xPixel+xStarting+j][noteHead.yPixel-i].isBlack()){
						currentScore.symbolImage.setRGB(noteHead.xPixel+xStarting+j, noteHead.yPixel-i, Color.GREEN.getRGB());
						amountOfBlack++;
					}
				}
			}
		}
			if(amountOfBlack > 2){
				return true;
			}
		return false;
	}
}
/*
 * TODO: DFA here will look through each symbol and return the possibilities. Each accepting
 * state will be associated with a list of the possible symbols that it could represent.
 */
