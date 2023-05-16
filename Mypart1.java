import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

/*
CS 576 Spring 2023 – Assignment 1
Author: Junmeng Xu
 */

public class Mypart1{

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage img;
	int width = 512;
	int height = 512;

	// Draws a black line on the given buffered image from the pixel defined by (x1, y1) to (x2, y2)
	public void drawLine(BufferedImage image, int x1, int y1, int x2, int y2) {
		Graphics2D g = image.createGraphics();
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1));
		g.drawLine(x1, y1, x2, y2);
		g.drawImage(image, 0, 0, null);
	}

	public void showIms(String[] args){

		// Read a parameter from command line
		String param0 = args[0];
		System.out.println("The first parameter was: " + param0);

		// Read a parameter from command line
		String param1 = args[1];
		System.out.println("The second parameter was: " + param1);

		// Read a parameter from command line
		String param2 = args[2];
		System.out.println("The third parameter was: " + param2);

		// Initialize a plain white image
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		int ind = 0;
		for(int y = 0; y < height; y++){

			for(int x = 0; x < width; x++){

				// byte a = (byte) 255;
				byte r = (byte) 255;
				byte g = (byte) 255;
				byte b = (byte) 255;

				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);

				//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				img.setRGB(x,y,pix);
				ind++;
			}
		}

		drawLine(img, 0, 0, width-1, 0);				// top edge
		drawLine(img, 0, 0, 0, height-1);				// left edge
		drawLine(img, 0, height-1, width-1, height-1);	// bottom edge
		drawLine(img, width-1, height-1, width-1, 0); 	// right edge

		//	Given a degree 'd' and the center of the image as (x0, y0),
		//	we can find the x, y coordinates of the end of the line using the following formula:
		//	x = x0 + r * Math.cos(Math.toRadians(d))
		//	y = y0 + r * Math.sin(Math.toRadians(d))
		//	The values of x0, y0 and r will depend on the size of the image.
		int paramN = Integer.valueOf(param0);
		double n = (double) paramN;
		double d = 360 / n;
		int x0 = width/2;
		int y0 = height/2;

		double ds = 0;
		for(int i=0; i<n; i++){
			int x = (int) ( x0 + width * Math.cos(Math.toRadians(ds)) );
			int y = (int) ( y0 + height * Math.sin(Math.toRadians(ds)) );
			drawLine(img, x0, y0, x, y);
			ds += d;
		}

		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		JLabel lbText1 = new JLabel("Original image (Left)");
		lbText1.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbText2 = new JLabel("Image after modification (Right)");
		lbText2.setHorizontalAlignment(SwingConstants.CENTER);
		lbIm1 = new JLabel(new ImageIcon(img));

		// get a resample image
		BufferedImage resampleImg = resampleImg(img, param1, param2);
		lbIm2 = new JLabel(new ImageIcon(resampleImg));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		frame.getContentPane().add(lbText1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 0;
		frame.getContentPane().add(lbText2, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		frame.getContentPane().add(lbIm2, c);

		frame.pack();
		frame.setVisible(true);
	}

	// resample function
	public BufferedImage resampleImg(BufferedImage img, String param1, String param2){
		// get new width and height in terms of scale
		double scale = Double.valueOf(param1);
		int newWidth = (int) (width * scale);
		int newHeight = (int) (height * scale);

		// check whether need to do anti-aliasing
		boolean antiAliasing = (Integer.valueOf(param2) == 0) ? false : true;

		BufferedImage resampleimg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

		// Initialize a plain white image
		for(int y = 0; y < newHeight; y++){
			for(int x = 0; x < newWidth; x++){
				int color = 0;
				// anti-aliasing
				if(antiAliasing){
					// find 9 pixels around the current pixel
					// and then use the average color of these pixels
					// as the color of current pixel
					int centerX = (int) Math.round(x / scale);
					int centerY = (int) Math.round(y / scale);
					int r = 0, g = 0, b = 0;
					int[] around = {-1, 0, 1};
					int aroundPix = 0;
					for (int aroundY : around){
						if(centerY + aroundY < 0 || centerY + aroundY >= height){
							continue;
						}
						for (int aroundX : around) {
							if(centerX + aroundX < 0 || centerX + aroundX >= width){
								continue;
							}
							int curColor = img.getRGB(centerX + aroundX, centerY + aroundY);
							r += (curColor >> 16) & 0xff;
							g += (curColor >> 8) & 0xff;
							b += (curColor) & 0xff;
							aroundPix++;
						}
						color = 0xff000000 | (((r / aroundPix) & 0xff) << 16) | (((g / aroundPix) & 0xff) << 8) | ((b / aroundPix) & 0xff);
					}
				// not anti-aliasing
				}else {
					color = img.getRGB((int) Math.round(x / scale), (int) Math.round(y / scale));
				}
				resampleimg.setRGB(x,y,color);
			}
		}

		drawLine(resampleimg, 0, 0, newWidth-1, 0);				// top edge
		drawLine(resampleimg, 0, 0, 0, newHeight-1);				// left edge
		drawLine(resampleimg, 0, newHeight-1, newWidth-1, newHeight-1);	// bottom edge
		drawLine(resampleimg, newWidth-1, newHeight-1, newWidth-1, 0); 	// right edge
		return resampleimg;
	}

	public static void main(String[] args) {
		Mypart1 ren = new Mypart1();
		ren.showIms(args);
	}
}
