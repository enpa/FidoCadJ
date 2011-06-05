package export;

import java.awt.*;
import java.util.*;
import java.io.*;
import globals.*;
import layers.*;
import primitives.*;

/** 
	Export drawing in the Scalable Vector Graphics format.

<pre>
	This file is part of FidoCadJ.

    FidoCadJ is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FidoCadJ is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FidoCadJ.  If not, see <http://www.gnu.org/licenses/>.

	Copyright 2008-2011 by Davide Bucci
</pre>

    
    @author Davide Bucci
*/

public class ExportSVG implements ExportInterface {

	private File fileExp;
	private OutputStreamWriter fstream;
	private BufferedWriter out;
	private Vector layerV;
	private int numberPath;
	private int actualDash;
	
	static final String dash[]={"2.5,5", "1.25,1.25",
		"0.5,0.5", "0.5,1.25", "0.5,1.25,1.25,1.25"};

	public int cLe(double l)
	{
		//return (int)(l*sizeMagnification);
		return (int)l;
	}
	/** Constructor
	
		@param f the File object in which the export should be done.
		
	*/
	
	public ExportSVG (File f) throws IOException
	{
		fileExp=f;
		
		fstream = new OutputStreamWriter(new FileOutputStream(f), 
			Globals.encoding);
    
		
	}
	
	/**	Called at the beginning of the export phase. Ideally, in this routine
		there should be the code to write the header of the file on which
		the drawing should be exported.
			
		@param totalSize the size of the image. Useful to calculate for example
		the	bounding box.
		@param la a vector describing the attributes of each layer.
		@param grid the grid size. This is useful when exporting to another 
			drawing program having some kind of grid concept. You might use
			this value to synchronize FidoCadJ's grid with the one used by
			the target.

	*/
	
	public void exportStart(Dimension totalSize, Vector la, int grid)   
		throws IOException
	{ 
		
		// We need to save layers informations, since we will use them later.
		
		layerV=la;
	    out = new BufferedWriter(fstream);
	    numberPath=0;
	    	    
	    int wi=(int)(totalSize.width);
	    int he=(int)(totalSize.height);
	    
	    // A dumb, basic header of the SVG file
	    
    	out.write("<?xml version=\"1.0\" encoding=\""+Globals.encoding+"\" " + 			
			"standalone=\"no\"?> \n<!DOCTYPE svg PUBLIC"+
			" \"-//W3C//Dtd SVG 1.1//EN\" " + "\"http://www.w3.org/Graphics/SVG/1.1/Dtd/svg11.dtd\">\n"+
			"<svg width=\""+cLe(wi)+"\" height=\""+cLe(he)+"\" version=\"1.1\" " + "xmlns=\"http://www.w3.org/2000/svg\" " +
			"xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n"+
			"<!-- Created by FidoCadJ ver. "+Globals.version+
			", export filter by Davide Bucci -->\n");
	} 
	
	/** Called at the end of the export phase.
	*/
	public void exportEnd() 
		throws IOException
	{ 
		out.write("</svg>");
		out.close();
    
	}

	/** Called when exporting an Advanced Text primitive.
	
		@param x the x position of the beginning of the string to be written
		@param y the y position of the beginning of the string to be written
		@param sizex the x size of the font to be used
		@param sizey the y size of the font to be used
		@param fontname the font to be used
		@param isBold true if the text should be written with a boldface font
		@param isMirrored true if the text should be mirrored
		@param isItalic true if the text should be written with an italic font
		@param orientation angle of orientation (degrees)
		@param layer the layer that should be used
		@param text the text that should be written
	*/
	
	public void exportAdvText (int x, int y, int sizex, int sizey,
		String fontname, boolean isBold, boolean isMirrored, boolean isItalic,
		int orientation, int layer, String text) 
		throws IOException
	{ 
		LayerDesc l=(LayerDesc)layerV.get(layer);
		Color c=l.getColor();
		String path;
		
		/*  THIS VERSION OF TEXT EXPORT IS NOT COMPLETE! IN PARTICULAR, 
			MIRRORING EFFECTS, ANGLES AND A PRECISE SIZE CONTROL IS NOT
			HANDLED
		*/
		
 /*
		if(isBold)
			outt.write("/F2"+" "+sizey+" Tf\n");       
		else
			outt.write("/F1"+" "+sizey+" Tf\n");       
*/		


		out.write("<g transform=\"translate("+cLe(x)+","+cLe(y)+")");
	
		
		double xscale = (sizex/22.0/sizey*38.0)	;	
		if(orientation !=0) {
			double alpha=(isMirrored?orientation:-orientation);
			out.write(" rotate("+alpha+") ");
		}
		if(isMirrored) {
			xscale=-xscale;
		}
		out.write(" scale("+xscale+",1) ");		
		
		out.write("\">");
		out.write("<text x=\""+0+"\" y=\""+cLe(sizey)+"\" font-family=\""+
			fontname+"\" font-size=\""+cLe(sizey)+"\" font-style=\""+
			(isItalic?"Italic":"")+"\" font-weigth=\""+
			(isBold?"bold":"")+"\" "+
			"fill=\"#"+
				convertToHex2(c.getRed())+
				convertToHex2(c.getGreen())+
				convertToHex2(c.getBlue())+"\""+
			
			">");
		out.write(text);
		out.write("</text>\n");
		out.write("</g>\n");	
		
	}
	
	/** Called when exporting a B�zier primitive.
	
		@param x1 the x position of the first point of the trace
		@param y1 the y position of the first point of the trace
		@param x2 the x position of the second point of the trace
		@param y2 the y position of the second point of the trace
		@param x3 the x position of the third point of the trace
		@param y3 the y position of the third point of the trace
		@param x4 the x position of the fourth point of the trace
		@param y4 the y position of the fourth point of the trace
		@param layer the layer that should be used
		
				// from 0.22.1
		
		@param arrowStart specify if an arrow is present at the first point
		@param arrowEnd specify if an arrow is present at the second point
		@param arrowLength total lenght of arrows (if present)
		@param arrowHalfWidth half width of arrows (if present)
		@param dashStyle dashing style
		
	*/
	public void exportBezier (int x1, int y1,
		int x2, int y2,
		int x3, int y3,
		int x4, int y4,
		int layer,
		boolean arrowStart, 
		boolean arrowEnd, 
		int arrowStyle, 
		int arrowLength, 
		int arrowHalfWidth, 
		int dashStyle,
		double strokeWidth)
		throws IOException	
	{ 
		LayerDesc l=(LayerDesc)layerV.get(layer);
		Color c=l.getColor();
		
		out.write("<path d=\"M "+cLe(x1)+","+cLe(y1)+" C "+
				  cLe(x2)+ ","+cLe(y2)+" "+cLe(x3)+","+cLe(y3)+" "+cLe(x4)+
				  ","+cLe(y4)+"\" ");
				  
		checkColorAndWidth(c, strokeWidth,"fill=\"none\"", dashStyle);
		
		if (arrowStart) exportArrow(x1, y1, x2, y2, arrowLength, 
			arrowHalfWidth, arrowStyle,c, strokeWidth);
		if (arrowEnd) exportArrow(x4, y4, x3, y3, arrowLength, 
			arrowHalfWidth, arrowStyle,c, strokeWidth);
	}
	
	/** Called when exporting a Connection primitive.
	
		@param x the x position of the position of the connection
		@param y the y position of the position of the connection
		
		@param layer the layer that should be used
	*/
	public void exportConnection (int x, int y, int layer, double node_size) 
		throws IOException
	{ 
		LayerDesc l=(LayerDesc)layerV.get(layer);
		Color c=l.getColor();
		double strokeWidth = cLe(0.33);
		
		out.write("<circle cx=\""+cLe(x)+"\" cy=\""+cLe(y)+"\""+
			" r=\""+cLe(node_size/2.0)+"\" style=\"stroke:#"+
				  convertToHex2(c.getRed())+
				  convertToHex2(c.getGreen())+
				  convertToHex2(c.getBlue())+";stroke-width:"+strokeWidth+
				  "\" fill=\"#"+
				  convertToHex2(c.getRed())+
				  convertToHex2(c.getGreen())+
				  convertToHex2(c.getBlue())+"\"/>\n");
	
	}
		
	/** Called when exporting a Line primitive.
	
		@param x1 the x position of the first point of the segment
		@param y1 the y position of the first point of the segment
		@param x2 the x position of the second point of the segment
		@param y2 the y position of the second point of the segment
		
		@param layer the layer that should be used
		
		// from 0.22.1
		
		@param arrowStart specify if an arrow is present at the first point
		@param arrowEnd specify if an arrow is present at the second point
		@param arrowLength total lenght of arrows (if present)
		@param arrowHalfWidth half width of arrows (if present)
		@param dashStyle dashing style
		
	*/
	public void exportLine (int x1, int y1,
		int x2, int y2,
		int layer,
		boolean arrowStart, 
		boolean arrowEnd, 
		int arrowStyle, 
		int arrowLength, 
		int arrowHalfWidth, 
		int dashStyle,
		double strokeWidth)
		throws IOException
	{ 
		LayerDesc l=(LayerDesc)layerV.get(layer);
		Color c=l.getColor();
		
		out.write("<line x1=\""+cLe(x1)+"\" y1=\""+cLe(y1)+"\" x2=\""+
			cLe(x2)+"\" y2=\""+cLe(y2)+"\" ");
		checkColorAndWidth(c, strokeWidth,"fill=\"none\"", dashStyle);

		if (arrowStart) exportArrow(x1, y1, x2, y2, arrowLength, 
			arrowHalfWidth, arrowStyle, c, strokeWidth);
		if (arrowEnd) exportArrow(x2, y2, x1, y1, arrowLength, 
			arrowHalfWidth, arrowStyle, c, strokeWidth);
		
	}
	
	/** Called when exporting a Macro call.
		This function can just return false, to indicate that the macro should 
		be rendered by means of calling the other primitives. Please note that 
		a macro does not have a reference layer, since it is defined by its
		components.
		
		@param x the x position of the position of the macro
		@param y the y position of the position of the macro
		@param isMirrored true if the macro is mirrored
		@param orientation the macro orientation in degrees
		@param macroName the macro name
		@param macroDesc the macro description, in the FidoCad format
		@param name the shown name
		@param xn coordinate of the shown name
		@param yn coordinate of the shown name
		@param value the shown value
		@param xv coordinate of the shown value
		@param yv coordinate of the shown value
		@param font the used font
		@param fontSize the size of the font to be used
		@param m the library
	*/
	public boolean exportMacro(int x, int y, boolean isMirrored, 
		int orientation, String macroName, String macroDesc,
		String name, int xn, int yn, String value, int xv, int yv, String font,
		int fontSize, Map m)
		throws IOException
	{
		// The macro will be expanded into primitives.
		return false; 
	}
	

	/** Called when exporting an Oval primitive. Specify the bounding box.
			
		@param x1 the x position of the first corner
		@param y1 the y position of the first corner
		@param x2 the x position of the second corner
		@param y2 the y position of the second corner
		@param isFilled it is true if the oval should be filled
		
		@param layer the layer that should be used
		@param dashStyle dashing style

	*/	
	public void exportOval(int x1, int y1, int x2, int y2,
		boolean isFilled, int layer, int dashStyle, double strokeWidth)
		throws IOException
	{ 
		LayerDesc l=(LayerDesc)layerV.get(layer);
		Color c=l.getColor();
		String fill_pattern="";
		
		if(isFilled) {
			fill_pattern="fill=\"#"+
				  convertToHex2(c.getRed())+
				  convertToHex2(c.getGreen())+
				  convertToHex2(c.getBlue())+"\"";
		} else {
			fill_pattern="fill=\"none\"";
		
		}
		 
		
		
		out.write("<ellipse cx=\""+cLe((x1+x2)/2.0)+"\" cy=\""+
				  cLe((y1+y2)/2.0)+
				  "\" rx=\""+cLe(Math.abs(x2-x1)/2.0)+"\" ry=\""+
				  cLe(Math.abs(y2-y1)/2.0)+"\" ");
		checkColorAndWidth(c, strokeWidth,fill_pattern, dashStyle);
	}
		
	/** Called when exporting a PCBLine primitive.
	
		@param x1 the x position of the first point of the segment
		@param y1 the y position of the first point of the segment
		@param x2 the x position of the second point of the segment
		@param y2 the y position of the second point of the segment
		@param width the width ot the line
		@param layer the layer that should be used
	*/
	public void exportPCBLine(int x1, int y1, int x2, int y2, int width, 
		int layer) 
		throws IOException
	{ 
		LayerDesc l=(LayerDesc)layerV.get(layer);
		Color c=l.getColor();
		
		out.write("<line x1=\""+cLe(x1)+"\" y1=\""+cLe(y1)+"\" x2=\""+
			cLe(x2)+"\" y2=\""+cLe(y2)+"\" style=\"stroke:#"+
				  convertToHex2(c.getRed())+
				  convertToHex2(c.getGreen())+
				  convertToHex2(c.getBlue())+
				  ";stroke-linejoin:round;stroke-linecap:round"+
				  ";stroke-width:"+width+
				  "\"/>\n");
		

	}
		
	
	/** Called when exporting a PCBPad primitive.
	
		@param x the x position of the pad 
		@param y the y position of the pad
		@param style the style of the pad (0: oval, 1: square, 2: rounded 
			square)
		@param six the x size of the pad
		@param siy the y size of the pad
		@param indiam the hole internal diameter
		@param layer the layer that should be used
	*/
	
	public void exportPCBPad(int x, int y, int style, int six, int siy, 
		int indiam, int layer, boolean onlyHole) 
		throws IOException
	{ 
		double xdd;
		double ydd;
		
		double strokeWidth=0.33;
		
		LayerDesc l=(LayerDesc)layerV.get(layer);
		Color c=l.getColor();
		
		// At first, draw the pad...
		if(!onlyHole) {
			switch (style) {
				default:
				case 0: // Oval pad
					out.write("<ellipse cx=\""+cLe(x)+"\" cy=\""+cLe(y)+"\""+
				  		" rx=\""+cLe(six/2.0)+"\" ry=\""+cLe(siy/2.0)+
				  		"\" style=\"stroke:#"+
				  		convertToHex2(c.getRed())+
					  	convertToHex2(c.getGreen())+
					  	convertToHex2(c.getBlue())+";stroke-width:"+
					  	cLe(strokeWidth)+
					  	"\" fill=\"#"+
					  	convertToHex2(c.getRed())+
					  	convertToHex2(c.getGreen())+
					  	convertToHex2(c.getBlue())+"\"/>\n");
					 break;
				case 1:	// Square pad
						xdd=cLe((double)x-six/2.0);
						ydd=cLe((double)y-siy/2.0);
					
						out.write("<rect x=\""+xdd+"\" y=\""+
					  		ydd+	"\" rx=\"0\" ry=\"0\" "+
					  		"width=\""+cLe(six)+"\" height=\""+
					  		cLe(siy)+"\" style=\"stroke:#"+
					  		convertToHex2(c.getRed())+
					  		convertToHex2(c.getGreen())+
					  		convertToHex2(c.getBlue())+
					  		";stroke-width:"+strokeWidth+"\" fill=\"#"+
					 		convertToHex2(c.getRed())+
					  		convertToHex2(c.getGreen())+
					  		convertToHex2(c.getBlue())+"\"/>\n");
				
					break;
				case 2:	// Rounded pad
						xdd=cLe((double)x-six/2.0);
						ydd=cLe((double)y-siy/2.0);
						double rd = cLe(2.5);
						out.write("<rect x=\""+xdd+"\" y=\""+ydd+
					  		"\" rx=\""+rd+"\" ry=\""+rd+"\" "+
					  		"width=\""+cLe(six)+"\" height=\""+
					  		cLe(siy)+"\" style=\"stroke:#"+
					  		convertToHex2(c.getRed())+
					  		convertToHex2(c.getGreen())+
					  		convertToHex2(c.getBlue())+
					  		";stroke-width:"+strokeWidth+"\" fill=\"#"+
					 		convertToHex2(c.getRed())+
					  		convertToHex2(c.getGreen())+
					  		convertToHex2(c.getBlue())+"\"/>\n");
					break;
			}
		}
		// ... then, drill the hole!
		out.write("<circle cx=\""+cLe(x)+"\" cy=\""+cLe(y)+"\""+
			" r=\""+cLe(indiam/2.0)+"\" style=\"stroke:white;stroke-width:"+strokeWidth+
				  	"\" fill=\"white\"/>\n");
		
	}
	
	/**	Called when exporting a Polygon primitive
	
		@param vertices array containing the position of each vertex
		@param nVertices number of vertices
		@param isFilled true if the polygon is filled
		@param layer the layer that should be used
		@param dashStyle dashing style

	
	*/
	public void exportPolygon(Point[] vertices, int nVertices, 
		boolean isFilled, int layer, int dashStyle, double strokeWidth)
		throws IOException
	{ 
		LayerDesc l=(LayerDesc)layerV.get(layer);
		Color c=l.getColor();
		String fill_pattern="";
		
		if(isFilled) {
			fill_pattern="fill=\"#"+
				  convertToHex2(c.getRed())+
				  convertToHex2(c.getGreen())+
				  convertToHex2(c.getBlue())+"\"";
		} else {
			fill_pattern="fill=\"none\"";
		
		}
		int i;
		
		//LayerDesc l=(LayerDesc)layerV.get(layer);
		out.write("<polygon points=\"");
		for (i=0; i<nVertices; ++i) {
			out.write(""+cLe(vertices[i].x)+","+cLe(vertices[i].y)+" ");
		
		}
		out.write("\" ");
		checkColorAndWidth(c, strokeWidth,fill_pattern, dashStyle);

		
	}
		
	/** Called when exporting a Rectangle primitive.
			
		@param x1 the x position of the first corner
		@param y1 the y position of the first corner
		@param x2 the x position of the second corner
		@param y2 the y position of the second corner
		@param isFilled it is true if the rectangle should be filled
		
		@param layer the layer that should be used
		@param dashStyle dashing style

	*/
	public void exportRectangle(int x1, int y1, int x2, int y2,
		boolean isFilled, int layer, int dashStyle, double strokeWidth)
		throws IOException
	{ 
		
		LayerDesc l=(LayerDesc)layerV.get(layer);
		Color c=l.getColor();
		String fill_pattern="";
		
		if(isFilled) {
			fill_pattern="fill=\"#"+
				  convertToHex2(c.getRed())+
				  convertToHex2(c.getGreen())+
				  convertToHex2(c.getBlue())+"\"";
		} else {
			fill_pattern="fill=\"none\"";
		
		}
		
		out.write("<rect x=\""+cLe(Math.min(x1,x2))+"\" y=\""+
				  cLe(Math.min(y1,y2))+
				  "\" rx=\"0\" ry=\"0\" "+
				  "width=\""+cLe(Math.abs(x2-x1))+"\" height=\""+
				  cLe(Math.abs(y2-y1))+"\" ");
		checkColorAndWidth(c, strokeWidth,fill_pattern, dashStyle);
	
	}
	
	/** Just be sure that the HEX values are given with two digits...
		NOT a speed sensitive context.
	*/
	private String convertToHex2(int v)
	{
		String s=Integer.toHexString(v);
		if (s.length()==1)
			s="0"+s;
			
		return s;
	}

	private Color oc;
	private double owl;
	private String ofp;
	private int ods;

	
	/** This routine ensures that the following items will be drawn with the
		correct stroke pattern and color.
	
	*/
	private void checkColorAndWidth(Color c, double wl, String fill_pattern,
		int dashStyle)
		throws IOException
	{
		
		// Write only if necessary, to save space. 
		// It does not work...
		
		//if(oc!=c || owl!=wl || !fill_pattern.equals(ofp) || ods!=dashStyle) {
		if(true) {
			out.write("style=\"stroke:#"+
				  convertToHex2(c.getRed())+
				  convertToHex2(c.getGreen())+
				  convertToHex2(c.getBlue()));
		
			if (dashStyle>0)
				out.write(";stroke-dasharray: "+dash[dashStyle]);

			out.write(";stroke-width:"+wl+
				  ";fill-rule: evenodd;\" " + fill_pattern + "/>\n");
		
			// Saving old values.
			oc=c;
			owl=wl;
			ofp=fill_pattern;
			ods=dashStyle;
		
		} else {
			out.write("/>\n");
		}
	}
	private String roundTo(double n)
	{
		int ch = 2;
		return ""+ (((int)(n*Math.pow(10,ch)))/Math.pow(10,ch));
	}
	
	private void exportArrow(int x, int y, int xc, int yc, int l, int h, 
		int style, Color c, double strokeWidth)
		throws IOException
	{
		double s;
		double alpha;
		double x0;
		double y0;
		double x1;
		double y1;
		double x2;
		double y2;
		
		// At first we need the angle giving the direction of the arrow
		// a little bit of trigonometry :-)
		
		if (x!=xc)
			alpha = Math.atan((double)(y-yc)/(double)(x-xc));
		else
			alpha = Math.PI/2.0+((y-yc<0)?0:Math.PI);
		
		alpha += (x-xc>0)?0:Math.PI;
		String fill_pattern;		
		
	
		// Then, we calculate the points for the polygon
		x0 = x - l*Math.cos(alpha);
		y0 = y - l*Math.sin(alpha);
		
		x1 = x0 - h*Math.sin(alpha);
		y1 = y0 + h*Math.cos(alpha);
		
		x2 = x0 + h*Math.sin(alpha);
		y2 = y0 - h*Math.cos(alpha);
		
		out.write("<polygon points=\"");
			
     	out.write(""+roundTo(x)+","
     		+roundTo(y)+" ");
      	out.write(""+roundTo(x1)+","
      		+roundTo(y1)+" ");
      	out.write(""+roundTo(x2)+","
      		+roundTo(y2)+"\" ");
      	
        if ((style & Arrow.flagEmpty) == 0)
			fill_pattern="fill=\"#"+
				  convertToHex2(c.getRed())+
				  convertToHex2(c.getGreen())+
				  convertToHex2(c.getBlue())+"\"";
 		else
			fill_pattern="fill=\"none\"";
 
		checkColorAndWidth(c, strokeWidth,fill_pattern,0);

 		if ((style & Arrow.flagLimiter) != 0) {
 			double x3;
			double y3;
			double x4;
			double y4;
			x3 = x - h*Math.sin(alpha);
			y3 = y + h*Math.cos(alpha);
		
			x4 = x + h*Math.sin(alpha);
			y4 = y - h*Math.cos(alpha);
			out.write("<line x1=\""+cLe(x3)+"\" y1=\""+cLe(y3)+"\" x2=\""+
				cLe(x4)+"\" y2=\""+cLe(y4)+"\" ");
			checkColorAndWidth(c, strokeWidth,"fill=\"none\"", 0);
 		}
 		
	}	


}