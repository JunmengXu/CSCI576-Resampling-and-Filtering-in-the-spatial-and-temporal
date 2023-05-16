import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

/*
CS 576 Spring 2023 – Assignment 1
Author: Junmeng Xu
 */

public class MyExtraCredit{

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage img;
	int width = 512;
	int height = 512;
	// Set the FPS for the left side video
	double originalFPS = 60.0;

	// Make a panel to simulate video by using timer to repaint image every specific time
	class VideoPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private BufferedImage[] images;
		private int NUM_IMAGES;
		private int currentImage;
		private Timer timer;

		public VideoPanel(BufferedImage[] images, double fps) {
			this.images = images;
			this.NUM_IMAGES = images.length;
			this.currentImage = 0;
			setPreferredSize(new Dimension(width, height));

			// set frequency of the panel fresh
			int delay = (int) (1000 / fps);
			// assume that originalFPS is the standard FPS
			// and the frames of the original input video match the originalFPS
			// then standard step would be 1
			// other fps will result different steps, which means output video get specific frames from the input frames
			double step = originalFPS / fps;
			timer = new Timer(delay, e -> {
				currentImage = (int)(currentImage + step) % NUM_IMAGES;
				repaint();
			});
			timer.start();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(images[currentImage], 0, 0, null);
		}
	}

	// Draws a black line on the given buffered image from the pixel defined by (x1, y1) to (x2, y2)
	public void drawLine(BufferedImage image, int x1, int y1, int x2, int y2) {
		Graphics2D g = image.createGraphics();
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1));
		g.drawLine(x1, y1, x2, y2);
		g.drawImage(image, 0, 0, null);
	}

	// Draws a red line on the given buffered image from the pixel defined by (x1, y1) to (x2, y2)
	public void drawLineRed(BufferedImage image, int x1, int y1, int x2, int y2) {
		Graphics2D g = image.createGraphics();
		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(1));
		g.drawLine(x1, y1, x2, y2);
		g.drawImage(image, 0, 0, null);
	}

	// Create a image based on n and the start degree of the first line
	public BufferedImage createImages(String param0, double startDegree){
		// Initialize a plain white image
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

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

		double ds = startDegree % 360;
//		boolean start = false;
		for(int i=0; i<n; i++){
			int x = (int) ( x0 + width * Math.cos(Math.toRadians(ds)) );
			int y = (int) ( y0 + height * Math.sin(Math.toRadians(ds)) );
//			if(!start){
//				drawLineRed(img, x0, y0, x, y);
//				start = true;
//			}else{
//				drawLine(img, x0, y0, x, y);
//			}
			drawLine(img, x0, y0, x, y);
			ds += d;
		}

		return img;
	}

	// create scaled videos of your input at different frame rates
	// and simultaneously minimize any aliasing effects due to resampling temporarily and spatially.
	public BufferedImage[] resampleImgs(BufferedImage[] imgs, String param3, String param4, String param0){
		// get new width and height in terms of scale
		double scale = Double.valueOf(param4);
		if(scale > 1){
			scale = 1/scale;
		}
		int newWidth = (int) (width * scale);
		int newHeight = (int) (height * scale);

		// check whether need to do anti-aliasing
		boolean antiAliasing = (Integer.valueOf(param3) == 0) ? false : true;

		BufferedImage[] resampleimgs = new BufferedImage[imgs.length];

		for(int i=0; i<imgs.length; i++){
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
								int curColor = imgs[i].getRGB(centerX + aroundX, centerY + aroundY);
								r += (curColor >> 16) & 0xff;
								g += (curColor >> 8) & 0xff;
								b += (curColor) & 0xff;
								aroundPix++;
							}
							color = 0xff000000 | (((r / aroundPix) & 0xff) << 16) | (((g / aroundPix) & 0xff) << 8) | ((b / aroundPix) & 0xff);
						}
					// not anti-aliasing
					}else {
						color = imgs[i].getRGB((int) Math.round(x / scale), (int) Math.round(y / scale));
					}
					resampleimg.setRGB(x,y,color);
				}
			}

			drawLine(resampleimg, 0, 0, newWidth-1, 0);				// top edge
			drawLine(resampleimg, 0, 0, 0, newHeight-1);				// left edge
			drawLine(resampleimg, 0, newHeight-1, newWidth-1, newHeight-1);	// bottom edge
			drawLine(resampleimg, newWidth-1, newHeight-1, newWidth-1, 0); 	// right edge

			resampleimgs[i] = resampleimg;
		}
		// resampling temporarily
		if(antiAliasing){
			BufferedImage[] antialiasingimgs = getAntiAliasedFrames(resampleimgs, param0);
			return antialiasingimgs;
		}else{
			return resampleimgs;
		}
	}

	// minimize any aliasing effects due to resampling temporarily
	// by using multiple samples in time to compute each frame, which can help to smooth out fast-moving objects.
	public BufferedImage[] getAntiAliasedFrames(BufferedImage[] imgs, String param0) {
		int totalFrames = imgs.length;
		int n = Integer.valueOf(param0);
		// number of frames to use for anti-aliasing
		int numSamples = 2;
		// creating a new frames set
		BufferedImage[] antialiasingimgs = new BufferedImage[totalFrames];

		// creating a new frame that is a blend of multiple frames from the input images
		for(int frameIndex=0; frameIndex<totalFrames; frameIndex++){
			BufferedImage blendedFrame = new BufferedImage(imgs[0].getWidth(), imgs[0].getHeight(), BufferedImage.TYPE_INT_RGB);
			float sumWeights = 0;

			// calculate weights
			for (int i = 0; i < numSamples; i++) {
				int sampleIndex = (frameIndex + i - (numSamples / 2)) % totalFrames;
				if (sampleIndex < 0) {
					sampleIndex = totalFrames + sampleIndex;
				}

				BufferedImage sample = imgs[sampleIndex];
				// float weight = (float) (numSamples - Math.abs(i - numSamples / 2)) / numSamples;
				float weight = (float) Math.cos((i - numSamples / 2) * Math.PI / numSamples) + 1f;
				weight /= 2 * numSamples;
				sumWeights += weight;
			}

			// first extract frames from the input images and then blend them together
			// blend the frames using a weighted average, where the weights are determined by the relative time of each frame
			for (int x = 0; x < blendedFrame.getWidth(); x++) {
				for (int y = 0; y < blendedFrame.getHeight(); y++) {
					int red = 0;
					int green = 0;
					int blue = 0;
					for (int i = 0; i < numSamples; i++) {
						int sampleIndex = (frameIndex + i - (numSamples / 2)) % totalFrames;
						if (sampleIndex < 0) {
							sampleIndex = totalFrames + sampleIndex;
						}

						BufferedImage sample = imgs[sampleIndex];
						// float weight = (float) (numSamples - Math.abs(i - numSamples / 2)) / numSamples;
						float weight = (float) Math.cos((i - numSamples / 2) * Math.PI / numSamples) + 1f;
						weight /= 2 * numSamples;
						weight = weight / sumWeights;

						int samplePixel = sample.getRGB(x, y);
						red += (int) (((samplePixel >> 16) & 0xff) * weight);
						green += (int) (((samplePixel >> 8) & 0xff) * weight);
						blue += (int) ((samplePixel & 0xff) * weight);
					}
					// blend the sample into the blended frame using a weighted average
					blendedFrame.setRGB(x, y, (red << 16) | (green << 8) | blue);
				}
			}
			antialiasingimgs[frameIndex] = blendedFrame;
		}

		return antialiasingimgs;
	}

	// Get all parameters and show videos
	public void showVideos(String[] args){

		// Read a parameter from command line
		String param0 = args[0];
		System.out.println("The first parameter was: " + param0);

		// Read a parameter from command line
		String param1 = args[1];
		System.out.println("The second parameter was: " + param1);

		// Read a parameter from command line
		String param2 = args[2];
		System.out.println("The third parameter was: " + param2);

		// Read a parameter from command line
		String param3 = args[3];
		System.out.println("The forth parameter was: " + param3);

		// Read a parameter from command line
		String param4 = args[4];
		System.out.println("The fifth parameter was: " + param4);

		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		JLabel lbText1 = new JLabel("Original video (Left)");
		lbText1.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbText2 = new JLabel("Video after processed (Right)");
		lbText2.setHorizontalAlignment(SwingConstants.CENTER);

		double speed = Double.valueOf(param1);
		double fps = Double.valueOf(param2);

		// find the sum rotation degree of its speed
		// and calculate how much degree each step will have, based on origianl fps
		// then try to find the least number of steps that makes the rotation degree
		// is an integral multiple of 360.
		// rotationSteps equals to the number of how many different images in the process of rotation
		// because of different start degrees
		double totalDegree = speed * 360;
		double degreeStep = totalDegree / originalFPS;
		int rotationSteps = 0;
		while( rotationSteps == 0 || (rotationSteps * degreeStep) % 360 != 0 ){
			rotationSteps++;
		}
		// create all different images with different start degree
		BufferedImage[] images1 = new BufferedImage[rotationSteps];
		for (int i = 0; i < rotationSteps; i++) {
			images1[i] = createImages(param0, degreeStep*i);
		}

		// let left side panel show the video
		VideoPanel panel1 = new VideoPanel(images1, originalFPS);

		// let right side panel show another video
		// the images set could be resample by some parameters
		BufferedImage[] images2 = resampleImgs(images1, param3, param4, param0);
		VideoPanel panel2 = new VideoPanel(images2, fps);

		// put the videos together
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
		frame.getContentPane().add(panel1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		frame.getContentPane().add(panel2, c);

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		MyExtraCredit ren = new MyExtraCredit();
		ren.showVideos(args);
	}
}
