package openCVImpl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;


public class Bar {
	int beginningXPixel;
	int endingXPixel;
	ArrayList<Line> lines;
	int topLineYPixel;
	int bottomLineYPixel;
	ArrayList<Symbol> symbols = new ArrayList<Symbol>();
	
	public Bar(int start, int end, ArrayList<Line> staffLines){
				
		beginningXPixel = start + 1; //account for barline itself
		endingXPixel = end;
		lines = staffLines;
		topLineYPixel = lines.get(0).getInitialY();
		bottomLineYPixel = lines.get(lines.size()-1).getInitialY();
		
	}
	public void findSymbols(Pixel[][] score, int staveDistance, int spaceDistance, Score currentScore, HashMap<Integer,String> ranges){
		//currentScore.symbolImage.setRGB(beginningXPixel, topLineYPixel, Color.ORANGE.getRGB());
		int distanceToCheck = (int) Math.floor(staveDistance/2);
		int barLength = endingXPixel - beginningXPixel;
		int startTopY = topLineYPixel - 1;
		int startBottomY = bottomLineYPixel + 1;
		
		for(int i=0;i<barLength;i++){
			for(int j=0;j<distanceToCheck;j++){
				if(score[beginningXPixel+i][startTopY-j].isBlack()){
					currentScore.symbolImage.setRGB(beginningXPixel+i, startTopY-j, Color.BLACK.getRGB());
				}
				if(score[beginningXPixel+i][startBottomY+j].isBlack()){
					currentScore.symbolImage.setRGB(beginningXPixel+i, startBottomY+j, Color.BLACK.getRGB());
				}
			}
		}
		//if it's black and it's not a barline take it - if it's black and it is part of a barline 
		//then check the pixel above and below it and if they are both black then it is part of a symbol
		
		/*
		 * Check inside the stave itself
		 */
		
		distanceToCheck = (bottomLineYPixel - topLineYPixel) + 1; //check the barline itself also
		
		
		for(int i=0;i<barLength;i++){
			for(int j=0;j<distanceToCheck;j++){
				if(score[beginningXPixel+i][topLineYPixel+j].isBlack() && !score[beginningXPixel+i][topLineYPixel+j].isBarline){
					currentScore.symbolImage.setRGB(beginningXPixel+i, topLineYPixel+j, Color.BLACK.getRGB());
				}
				if(score[beginningXPixel+i][topLineYPixel+j].isBlack() && score[beginningXPixel+i][topLineYPixel+j].isBarline){
					if(score[beginningXPixel+i][topLineYPixel+j-1].isBlack() || score[beginningXPixel+i][topLineYPixel+j+1].isBlack()){
						currentScore.symbolImage.setRGB(beginningXPixel+i, topLineYPixel+j, Color.BLACK.getRGB());
					}
				}
			}
		}
		this.removeText(currentScore, staveDistance, ranges);
		
	}
		
	/*
	 * Function to create note symbols
	 */
	private void removeText( Score currentScore, int staveDistance, HashMap<Integer,String> ranges){
		Pixel[][] score = currentScore.buildArrayFromBitmap(currentScore.symbolImage);
		int barLength = endingXPixel - beginningXPixel;
		int distanceToCheck = (int) Math.floor(staveDistance/2) + (staveDistance/6);
		Double distanceOfWords = (distanceToCheck / 1.7);
		int distanceIntOfWords = distanceOfWords.intValue();
		for(int i=0;i<barLength;i++){
			for(int j=0;j<distanceIntOfWords;j++){
				if(score[beginningXPixel+i][bottomLineYPixel+distanceToCheck-j].isBlack()){
					currentScore.symbolImage.setRGB(beginningXPixel+i, bottomLineYPixel+distanceToCheck-j, Color.WHITE.getRGB());
				}
			}
		}
		Double doubleStartY = topLineYPixel - (distanceToCheck - (distanceIntOfWords*0.9));
		int startY = doubleStartY.intValue();
		int endY = bottomLineYPixel + (distanceToCheck - distanceIntOfWords);
		this.makeSymbols(currentScore, startY, endY, barLength, ranges);
	}
	private void makeSymbols(Score currentScore, int startY, int endY, int barLength, HashMap<Integer,String> ranges){
		Pixel[][] score = currentScore.buildArrayFromBitmap(currentScore.symbolImage);
		int distanceToScan = endY - startY;
		boolean hasHitNote = false;
		int whiteColumnCount = 0;
		int count = 0;
		int currentStartX = beginningXPixel; //starts at start of bar looking for columns
		for(int i=0;i<barLength;i++){
			boolean isWhiteColumn = true;
			for(int j=0;j<distanceToScan;j++){
				if(score[beginningXPixel + i][startY + j].isBlack()){
					isWhiteColumn = false;
					hasHitNote = true;
				}
			}
			if(isWhiteColumn){
				whiteColumnCount++;
				if(hasHitNote == true){
					if(Math.abs(((beginningXPixel + i) - currentStartX)) > (currentScore.spaceHeight * 0.5)){
						this.symbols.add(new Symbol(currentStartX, beginningXPixel + i, startY, endY, barLength, currentScore, ranges )); //makes new symbol encircling that symbol.
						currentScore.symbolImage.setRGB(currentStartX, startY, Color.ORANGE.getRGB());
						whiteColumnCount = 0;
						hasHitNote = false;
						currentStartX = beginningXPixel + i;
						count ++; //testing
					}
				}else{
					whiteColumnCount++;
				}
			}
		}
		if(Math.abs(((beginningXPixel+(barLength-1)) - currentStartX)) > (currentScore.spaceHeight * 0.5)){
			this.symbols.add(new Symbol(beginningXPixel+barLength-1, startY, startY, endY, barLength, currentScore, ranges)); //makes new symbol encircling that symbol.
			currentScore.symbolImage.setRGB(beginningXPixel+barLength-1,startY, Color.ORANGE.getRGB());
			count++;
		}
		System.out.println("number of symbols: " + count);
	}
	
}
