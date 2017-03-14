package openCVImpl;


import org.jfugue.integration.MusicXmlParserListener;
import org.jfugue.parser.Parser;
import org.jfugue.pattern.Pattern;
import org.jfugue.player.Player;
import org.staccato.StaccatoParserListener;

import nu.xom.ParsingException;

import org.jfugue.integration.MusicXmlParser;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;


public class Score {
	BufferedImage symbolImage; 
	BufferedImage testImage; 
	int spaceHeight;
	String story = "";
	String xml = "";
	public Score() {
		try {
			symbolImage = ImageIO.read(new File("simple-test.bmp"));
			testImage = ImageIO.read(new File("simple-test.bmp"));
			buildSymbols();
			Pixel[][] scoreSymbols = this.buildArrayFromBitmap(symbolImage);
			boolean flag2 = ImageIO.write(symbolImage, "png", new File("note-heads.png"));
			boolean flag1 = ImageIO.write(testImage, "png", new File("showing.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void buildSymbols(){
		try {
			
			BufferedImage image = ImageIO.read(new File("simple-test.bmp"));
			Pixel[][] array2D = this.buildArrayFromBitmap(image);
		    
		    BufferedImage image2 = ImageIO.read(new File("simple-test.bmp"));
		    int maxLine = 0;
		    ArrayList<Line> lines = new ArrayList<Line>();
		    Line currentLine = new Line();
		    for (int yPixel = 0; yPixel < image.getHeight(); yPixel++)
	        {
		    	currentLine = new Line();
	            for (int xPixel = 0; xPixel < image.getWidth(); xPixel++)
	            {
	            	boolean isBlackCurrent = array2D[xPixel][yPixel].isBlack();
	            	
	            	if(isBlackCurrent == true){
	            		currentLine.addPixel(array2D[xPixel][yPixel]);
	            	}else{
	            
	            		if(currentLine.getLineLength() != 0){
	            			lines.add(currentLine);
	            			currentLine = new Line();
	            		}
	            	}
	            	if(xPixel == image.getWidth()-1){
	            		if(currentLine.getLineLength() != 0){
	            			lines.add(currentLine);
	            			currentLine = new Line();
	            		}
	            	}
	            }
	        }
		    for(int k=0;k<lines.size();k++){
				   if(lines.get(k).getLineLength() > maxLine){
					   maxLine = lines.get(k).getLineLength();
				   }
			   }
		    System.out.println("number of lines: " + lines.size());
			   ArrayList<Line> staffLines = new ArrayList<Line>();
			   for(int k=0;k<lines.size();k++){
			    	if(lines.get(k).getLineLength() > maxLine * 0.9){
			    		//set which pixels belong to barlines.
			    		for(int l=0;l<lines.get(k).getPixels().size();l++){
			    			lines.get(k).getPixels().get(l).setIsBarLine(true);
			    		}
			    		if(staffLines.size() != 0){ 
				    		if((lines.get(k).getInitialY() - staffLines.get(staffLines.size()-1).getInitialY()) > 1){
				    			staffLines.add(lines.get(k));
				    		}
				    	}else{
				    		staffLines.add(lines.get(k));
				    	}
				    }
			    }
			   
			   /*
			    * 	Group the Lines into staves
			    */
			   
			   ArrayList<Stave> staves = new ArrayList<Stave>();
			   
			   for(int k=0;k<staffLines.size();k=k+5){
				   Stave current = new Stave();
				   current.staffLines.add(staffLines.get(k));
				   current.staffLines.add(staffLines.get(k+1));
				   current.staffLines.add(staffLines.get(k+2));
				   current.staffLines.add(staffLines.get(k+3));
				   current.staffLines.add(staffLines.get(k+4));
				   staves.add(current);
			   }
			   System.out.println("staves.size(): " + staves.size());
			   /*
			    * Find the distance to black to the left of each line
			    */
			   
			   for(int k=0;k<staffLines.size();k++){
				  int count = 0;
				  int blackLength = 0;
				  int startX = staffLines.get(k).pixels.get(0).xPixel-2	;
				  int startY = staffLines.get(k).pixels.get(0).yPixel;
		
				  while(startX-count > 0){
						image2.setRGB(startX-count, startY, Color.RED.getRGB());

					  if(array2D[startX-count][startY].isBlack()){ 
						  staffLines.get(k).blackLineLength = blackLength; 
						  break;
					  }else{
						  blackLength++;
					  }
					  count++;
				  }
			   }
			   boolean flag1 = ImageIO.write(image2, "png", new File("testing.png"));
			   /*
			    * Attempt to group them into choir
			    */
			   ArrayList<Line> choirLines = new ArrayList<Line>();
			   int currentLength = staffLines.get(0).blackLineLength;
			   ArrayList<ArrayList<Line>> groupHolder = new ArrayList<ArrayList<Line>>();
			   ArrayList<Line> currentGroup = new ArrayList<Line>();
			   currentGroup.add(staffLines.get(0));
			   for(int k=1;k<staffLines.size();k++){
					  if(staffLines.get(k).blackLineLength == currentLength){
						  currentGroup.add(staffLines.get(k));
						  //System.out.println("size: " + currentGroup.size());
					  }else{
						 // System.out.println("difference between one being looked at: " + staffLines.get(k).blackLineLength + " and currentLength: " + currentLength);
						  currentLength = staffLines.get(k).blackLineLength;
						 // System.out.println("making new group at line " + k);
						  testImage.setRGB(staffLines.get(k).pixels.get(0).xPixel, staffLines.get(k).pixels.get(0).yPixel, Color.YELLOW.getRGB());
						  groupHolder.add(currentGroup);
						  currentGroup = new ArrayList<Line>();
						  currentGroup.add(staffLines.get(k));  
			   		}
					  
			   }
			   if(groupHolder.size() == 0){
				   groupHolder.add(currentGroup);
			   }
			   /*
			    * Error correction on the choir part.
			    */
			   ArrayList<Stave> choirStaves = new ArrayList<Stave>();
			   ArrayList<SystemGroup> choirSystems = new ArrayList<SystemGroup>();
			   for(int k=0;k<groupHolder.size();k++){
				   if(groupHolder.get(k).size() >= 5){ //size of the group is bigger than one stave
					   Line topOfGroup = groupHolder.get(k).get(0);
					   Line bottomOfGroup = groupHolder.get(k).get(groupHolder.get(k).size()-1);
					   int topStaveIndex = 0;
					   int bottomStaveIndex = 0;
					   //now find the indexes in staves of the top and bottom staves
					   //top
					   for(int m=0;m<staves.size();m++){
						   if(staves.get(m).checkIfLineContained(topOfGroup)){
							   topStaveIndex = m;
						   }
						   if(staves.get(m).checkIfLineContained(bottomOfGroup)){
							   bottomStaveIndex = m;
						   }
					   }
					   // we can assume that all of the staves in the middle of the top and bottom will be also choir staves
					   //System.out.println("topIndex: " + topStaveIndex);
					   //System.out.println("bottomIndex: " + bottomStaveIndex);
					   SystemGroup currentSystem = new SystemGroup(array2D);
					   for(int count=topStaveIndex;count<=bottomStaveIndex;count++){
						   choirStaves.add(staves.get(count));
						   currentSystem.staves.add(staves.get(count));
					   }
					   choirSystems.add(currentSystem);
				   }
			   }
			   /*
			    * Find the clefs in the systems.
			    */
			   for(int k=0;k<choirSystems.size();k++){
				   choirSystems.get(k).findClef();
			   }
			   for(int k=0;k<choirSystems.size();k++){
				   choirSystems.get(k).findBars(array2D);
			   }
			   /*
			    * Make the distances needed to read symbols 
			    */
			   
			   //Distance between choir systems
			   SystemGroup firstSystem = choirSystems.get(0);
			   Stave bottomStave = firstSystem.staves.get(firstSystem.staves.size()-1);
			   Line bottomStaffLine = bottomStave.staffLines.get(bottomStave.staffLines.size()-1);
			   
			   Line topStaffLine;
			   if(choirSystems.size() > 1){
			   SystemGroup secondSystem = choirSystems.get(1);
			   Stave topStave = secondSystem.staves.get(0);
			   topStaffLine = topStave.staffLines.get(0);
			   int systemDistance = topStaffLine.getInitialY() - bottomStaffLine.getInitialY();
			   }
			   //distance between staves
			   Stave firstStave = firstSystem.staves.get(0);
			   Stave secondStave = firstSystem.staves.get(1);
			   
			   bottomStaffLine = firstStave.staffLines.get(firstStave.staffLines.size()-1);
			   topStaffLine = secondStave.staffLines.get(0);
			   
			   int staveDistance = topStaffLine.getInitialY() - bottomStaffLine.getInitialY();
			   
			  
			   
			   //distance between spaces
			   Line firstLine = firstStave.staffLines.get(0);
			   Line secondLine = firstStave.staffLines.get(1);
			   
			   int spaceDistance = secondLine.getInitialY() - firstLine.getInitialY();
			   spaceHeight = spaceDistance;
			  	/*
			  	 * Finding symbols and writing them to the symbolImage file
			  	 */
			   
				for(int k=0;k<symbolImage.getWidth();k++){
					for(int l=0;l<symbolImage.getHeight();l++){
						symbolImage.setRGB(k, l, Color.WHITE.getRGB());
					}
				}
				
			   for(int k=0;k<choirSystems.size();k++){
				   System.out.println("\n******** System " + k + " ********\n");
				   choirSystems.get(k).findSymbols(array2D, staveDistance, spaceDistance, this);
			   }
			   int systemSize = choirSystems.get(0).staves.size();
			   ArrayList<Part> parts = new ArrayList<Part>();
			   String[] partNames = {"Soprano", "Alto", "Tenor", "Bass"};
			   for(int k=0;k<systemSize;k++){
				   parts.add(new Part(partNames[k],"P"+(k+1), (k+1)));
				   xml = xml + parts.get(k).buildPartListXML();
			   }
			   xml = xml + "<part-group type=\"stop\" number=\"1\"/>\n";
			   xml = xml + "</part-list>";
			  
			   for(int k=0;k<choirSystems.size();k++){
				   for(int z=0;z<choirSystems.get(k).staves.size();z++){
					   parts.get(z).stavesInPart.add(choirSystems.get(k).staves.get(z));
					   
				   }
			   }
			   for(int k=0;k<parts.size();k++){
				   xml = xml + "<part id=\"P" +(k+1) + "\">\n";
				   xml = xml + parts.get(k).buildBarsXML();
				   xml = xml + "</part>\n";
			   }
			   xml = xml + "</score-partwise>\n";
			   try
			   {
			       String filename= "xml-template.xml";
			       FileWriter fw = new FileWriter(filename,true); //the true will append the new data
			       fw.write(xml);//appends the string to the file
			       fw.close();
			   }
			   catch(IOException ioe)
			   {
			       System.err.println("IOException: " + ioe.getMessage());
			   }
			   System.out.println("Writing to file");
			   
			   
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public Pixel[][] buildArrayFromBitmap(BufferedImage image){
		int i = 0, j = 0;
		int minColor = 0;
		int maxColor = -16777216;
		Pixel[][] array2D = new Pixel[image.getWidth()][image.getHeight()];
		for (int yPixel = 0; yPixel < image.getHeight(); yPixel++)
        {
            for (int xPixel = 0; xPixel < image.getWidth(); xPixel++)
            {
                int color = image.getRGB(xPixel, yPixel);
                if(color < maxColor){
                	maxColor = color;
                }
                if(color > minColor){
                	minColor = color;
                }
            }
        }
    
    int topOfRange = maxColor;
    int bottomOfRange = (int)((double)maxColor + Math.floor(((double)minColor - (double)maxColor) / 1.01));
    
    i=0;j=0;
    for (int yPixel = 0; yPixel < image.getHeight(); yPixel++)
    {
        for (int xPixel = 0; xPixel < image.getWidth(); xPixel++)
        {
        	int color = image.getRGB(xPixel, yPixel);
        if(color >= topOfRange && color <= bottomOfRange){
        		i++;
        		Pixel currentPixel = new Pixel(xPixel,yPixel,true);
        		array2D[xPixel][yPixel] = currentPixel;
     
        	}else{
        		j++;
        		Pixel currentPixel = new Pixel(xPixel,yPixel,false);
        		array2D[xPixel][yPixel] = currentPixel;
        	}
        }
    }
		return array2D;
	}
	
	public static void main(String[] args){
		Score score = new Score();
	}
	
}
