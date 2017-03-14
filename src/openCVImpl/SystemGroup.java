package openCVImpl;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


import javax.imageio.ImageIO;

public class SystemGroup {
	ArrayList<Stave> staves = new ArrayList<Stave>();
	Pixel[][] score = null;
	
	public SystemGroup(Pixel[][] scorePar){
		score = scorePar;
	}
	/*
	 * Check the first 5% of the stave for black above the top staffline and black below the bottokm staffline.
	 */
	public void findSymbols(Pixel[][] score, int staveDistance, int spaceDistance, Score currentScore){
		for(int i=0;i<staves.size();i++){
			System.out.println("\n------ Stave " + i + " Clef: " + staves.get(i).clef +" ------");
			staves.get(i).findSymbols(score, staveDistance, spaceDistance, currentScore);
		}
	}
	public void findClef(){
		try {
			BufferedImage image2 = ImageIO.read(new File("simple-test.bmp"));
		
		for(int i=0;i<staves.size();i++){
			boolean topHasBlack = false;
			boolean bottomHasBlack = false;
			Line topLine = staves.get(i).staffLines.get(0);
			Line bottomLine = staves.get(i).staffLines.get(staves.get(i).staffLines.size()-1);
			
			for(int j=(int) Math.floor(topLine.pixels.size() * 0.01);j<topLine.pixels.size() * 0.05;j++){
				image2.setRGB(topLine.pixels.get(0).xPixel + j, topLine.pixels.get(0).yPixel-4, Color.GREEN.getRGB());
				if(score[topLine.pixels.get(0).xPixel + j][topLine.pixels.get(0).yPixel-2].isBlack){
					topHasBlack = true;
					break;
				}
			}
			for(int j=(int) Math.floor(bottomLine.pixels.size() * 0.01);j<bottomLine.pixels.size() * 0.05;j++){
				image2.setRGB(bottomLine.pixels.get(0).xPixel + j, bottomLine.pixels.get(0).yPixel+4, Color.BLUE.getRGB());
				if(score[bottomLine.pixels.get(0).xPixel + j][bottomLine.pixels.get(0).yPixel-2].isBlack){
					bottomHasBlack = true;
					break;
				}
			}
			if(topHasBlack && bottomHasBlack){
				staves.get(i).clef = "treble";
				staves.get(i).notes[0] = "B";
				staves.get(i).notes[1] = "C";
				staves.get(i).notes[2] = "D";
				staves.get(i).notes[3] = "E";
				staves.get(i).notes[4] = "F";
				staves.get(i).notes[5] = "G";
				staves.get(i).notes[6] = "A";
				System.out.println("found treble clef on stave " + i);
			}else{
				staves.get(i).clef = "bass";
				staves.get(i).notes[0] = "D";
				staves.get(i).notes[1] = "E";
				staves.get(i).notes[2] = "F";
				staves.get(i).notes[3] = "G";
				staves.get(i).notes[4] = "A";
				staves.get(i).notes[5] = "B";
				staves.get(i).notes[6] = "C";
				System.out.println("found bass clef on stave " + i);
			}
		}
		
		 boolean flag2 = ImageIO.write(image2, "png", new File("clefs.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void findBars(Pixel[][] score){
		for(int i=0;i<staves.size();i++){
			staves.get(i).findBars(score);
		}
	}
	
}
