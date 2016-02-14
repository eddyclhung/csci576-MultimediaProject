
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;


public class imageReader {

	static int counter = 0; /* count how many files in a folder */
	public static ArrayList<Data> dataSet = new ArrayList<Data>();

	public static void main(String[] args) {
		
		final File dir = new File(args[0]);
		int width = 352;
		int height = 288;
		int row = 0;
		final imageReader imr = new imageReader();

		/* array of supported extensions */
		final String[] EXTENSIONS = new String[] { 
				"rgb" /* and other formats if needed */
		};

		/* filter to identify images based on their extensions */
		final FilenameFilter IMAGE_FILTER = new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				for (final String ext : EXTENSIONS) {
					if (name.endsWith("." + ext)) {
						return (true);
					}
				}
				return (false);
			}
		};

		if (dir.isDirectory()) { /* count how many files are there in the folder */
			for (final File file : dir.listFiles(IMAGE_FILTER)) {
				counter++;
			}
			//System.out.println("total file in this folder : " + counter);
		}

		if((counter % 10) == 0) /* use counter to control gridlayout format*/
			row = counter/10; 
		else
			row = (counter/10) + 1; 
		
		JFrame frame = new JFrame("Image Collage"); /* new a frame to display the files in the folder */
		JPanel panel = new JPanel(new GridLayout( row , 10)); /* add a panel to the frame (using gridlayout) */
		
		BufferedImage[] imgF = new BufferedImage[counter];
		
		int c = 0;
		for (final File file : dir.listFiles(IMAGE_FILTER)) { /*forloop: the total number of files */

			try {
				InputStream is = new FileInputStream(file);

				long len = file.length(); /* Calculate input file size */
				// System.out.println("File length : " + len); /* Print input file size */
				double framelen = (len / (width * height * 3)); /* Calculate inputfile's frames */
				// System.out.println("Numbers of frames : " + framelen); /* Print input file's frames */
				final BufferedImage frameA[] = new BufferedImage[(int) framelen]; /* Set the frame array */

				byte[] bytes = new byte[(int) len];

				int offset = 0;
				int numRead = 0;
				while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
					offset += numRead;
				}

				for (int i = 0; i < framelen; i++) {
					BufferedImage img = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
					int ind = 0;
					for (int y = 0; y < height; y++) {
						for (int x = 0; x < width; x++) {

							byte a = 0;
							byte r = bytes[i * width * height * 3 + ind];
							byte g = bytes[i * width * height * 3 + ind + height * width];
							byte b = bytes[i * width * height * 3 + ind + height * width * 2];

							int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
							img.setRGB(x, y, pix);
							ind++;
						}
					}
					frameA[i] = img;
					
					/* Obtain each frame into frame array */
					imgF[c] = img;
					//System.out.println(" width " + i + " : " + frameA[i].getWidth());
					//System.out.println(" height" + i + " : " + frameA[i].getHeight());
				}
				
				//The counter is used for imgF array  
				c++;

				// System.out.println("image: " + file.getName());
				// System.out.println(" size  : " + file.length());
				// imr.histogram(frameA[0]);
				
				
				if (frameA.length > 1){ /* if the file is a video, create its first frame as button in Image Collage display */
					JButton frameVideobutton = new JButton(new StretchIcon(frameA[0]));
					frameVideobutton.setBorder(BorderFactory.createEmptyBorder());
					frameVideobutton.setContentAreaFilled(true);
					panel.add(frameVideobutton);
					frame.getContentPane().add(panel, BorderLayout.CENTER);
					frame.setSize(800,700);
					frame.setVisible(true);
					
					frameVideobutton.addActionListener(new ActionListener() { /* if button click, call display function to play the clicked video*/
						@Override
						public void actionPerformed(ActionEvent e) {
							// TODO Auto-generated method stub
							imr.display(frameA);
						}
			        }); 
				}else{
					/* if the file is an image, create it as a button in Image Collage display */ 
					
					JButton framebutton = new JButton(new StretchIcon(frameA[0]));
					framebutton.setBorder(BorderFactory.createEmptyBorder());
					framebutton.setContentAreaFilled(true);
					panel.add(framebutton);
					frame.getContentPane().add(panel, BorderLayout.CENTER);
					frame.setSize(800, 700);
					frame.setVisible(true);

					framebutton.addActionListener(new ActionListener() { /* if button click, call display function to display the clicked original image*/
						@Override
						public void actionPerformed(ActionEvent e) {
							imr.display(frameA);	//--------------------->Display in a single collage
						}
				});
					
				}
				
				
			} catch (final IOException e) {
				/* handle errors here */
			}
		}
		
		/* THIS SECTION IS FOR DOING CLUSTERING. */
		/*Obtain each frame's histogram*/
		int[][][] frameHtg = new int[imgF.length][256][3]; 
		for(int i = 0; i < imgF.length; i++){
			frameHtg[i] = imr.histogram(imgF[i]);
			System.out.println("Caluculating histogram number with frame :"+i);
		}
		
		/*Transfer frameHtg from 3-way into 2-way array with value y. */
		int[][] frameHtgT = new int[imgF.length][256];
		for(int i=0; i < imgF.length; i++){
			for(int j=0; j <256; j++){
				frameHtgT[i][j] = frameHtg[i][j][0];
			}
		}
		
		/*Transmit data array after histogram to do cluster*/
		Kmeans km = new Kmeans(frameHtgT,2);
		dataSet = km.start();
		int a=0, b=0;
		 for (int j = 0; j < dataSet.size(); j++) {
             if (dataSet.get(j).cluster() == 0) { 
               a++;
             }else{
               b++;
             }
         }

		final BufferedImage[] frameGC1 = new BufferedImage[a];
		final BufferedImage[] frameGC2 = new BufferedImage[b];
		
		//int NUM_CLUSTERS = 2;
		int m=0, n=0;
         for (int j = 0; j < dataSet.size(); j++) {
             if (dataSet.get(j).cluster() == 0) { 
               frameGC1[m]=imgF[j];
               m++;
             }else{
               frameGC2[n]=imgF[j];
               n++;
             }
         }
         System.out.println("Cluster 1 with total number of frames :"+a);
         System.out.println("Cluster 2 with total number of frames :"+b);
         
         /* DISPLAY CLUSTER 1 */
 		JFrame frame1 = new JFrame("Image Collage Cluster1"); /* new a frame to display the files in the folder */
 		JPanel panel1 = new JPanel(new GridLayout( row , 10)); /* add a panel to the frame (using gridlayout) */
        
 		for(int i = 0; i < frameGC1.length; i++){
        	JButton framebutton = new JButton(new StretchIcon(frameGC1[i]));
			framebutton.setBorder(BorderFactory.createEmptyBorder());
			framebutton.setContentAreaFilled(true);
			panel1.add(framebutton);
			frame1.getContentPane().add(panel1, BorderLayout.CENTER);
			frame1.setSize(500, 700);
			frame1.setLocation(0, 0);
			frame1.setVisible(true);

			framebutton.addActionListener(new ActionListener() { /* if button click, call display function to display the clicked original image*/
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					imr.display(frameGC1);	//Display in a single collage cluster1
				}
			});
         }
         
         /* DISPLAY CLUSTER 2 */
 		JFrame frame2 = new JFrame("Image Collage Cluster2"); /* new a frame to display the files in the folder */
 		JPanel panel2 = new JPanel(new GridLayout( row , 10)); /* add a panel to the frame (using gridlayout) */
		for(int i = 0; i < frameGC2.length; i++){
			JButton framebutton = new JButton(new StretchIcon(frameGC2[i]));
			framebutton.setBorder(BorderFactory.createEmptyBorder());
			framebutton.setContentAreaFilled(true);
			panel2.add(framebutton);
			frame2.getContentPane().add(panel2, BorderLayout.CENTER);
			frame2.setSize(500, 700);
			frame2.setLocation(500, 0);
			frame2.setVisible(true);

			framebutton.addActionListener(new ActionListener() { /* if button click, call display function to display the clicked original image*/
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					imr.display(frameGC2);	//Display in a single collage cluster2
				}
			});
		}
	}
	/* Function display the single image*/
	public void displayS(BufferedImage img) {
		/* Use a label to display the image */
		
		JFrame frame = new JFrame();
		frame.setTitle(" Original Image ");
		frame.setVisible(true);
		
		JLabel label = new JLabel(new ImageIcon(img));
		frame.getContentPane().add(label, BorderLayout.CENTER);
		frame.pack();
	}
	
	
	/* Function display the image array*/
	public void display(BufferedImage[] img) {
		/* Use a label to display the image */
		
		int frameRate = 30; /* frameRate is 30 */
		JFrame frame = new JFrame();
		frame.setTitle(" Original Video ");
		frame.setVisible(true);
		
		for (int i = 0; i < img.length; i++) {
			System.out.println(i);
			JLabel label = new JLabel(new ImageIcon(img[i]));
			frame.getContentPane().add(label, BorderLayout.CENTER);
			frame.pack();

			/* Determine the frame rate */
			try {
				Thread.sleep(1000 / frameRate);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/* Determine not to remove last image */
			
			if (i < img.length - 1) {
				frame.getContentPane().removeAll();
			}
			
		}
	}

	// Function Extract Pixel's each rgb
	public int[] getrgb(BufferedImage img, int x, int y) {

		int loc_x = x;
		int loc_y = y;
		BufferedImage src_img = img;

		int rgb[] = new int[3];

		rgb[0] = (src_img.getRGB(loc_x, loc_y) >> 16) & 0xFF; // handle Red
		rgb[1] = (src_img.getRGB(loc_x, loc_y) >> 8) & 0xFF; // handle Green
		rgb[2] = (src_img.getRGB(loc_x, loc_y)) & 0xFF; // handle Blue

		return rgb;
	}

	// Function Histogram
	public int[][] histogram(BufferedImage image) {

		int[][] hgm = new int[256][3]; // stands for hgm[x axis][r,g,b]
		int[][] yuv = new int[256][3]; // stands for yuv[x axis][y,u,v]
		imageReader imr = new imageReader();

		//Calculate with RGB format
		/*
		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 0; j < image.getWidth(); j++) {
				for (int k = 0; k < 256; k++) {
					if (imr.getrgb(image, j, i)[0] == k) {
						hgm[k][0]++;
					}
					if (imr.getrgb(image, j, i)[1] == k) {
						hgm[k][1]++;
					}
					if (imr.getrgb(image, j, i)[2] == k) {
						hgm[k][2]++;
					}
				}
			}
		}
		*/
		
		/*//Check output with rgb format
		for (int i = 0; i < 256; i++) {
			System.out.println("hgmR" + i + ":" + hgm[i][0]);
			System.out.println("hgmG" + i + ":" + hgm[i][1]);
			System.out.println("hgmB" + i + ":" + hgm[i][2]);
		}
		*/
		
		//Calculate with YUV
		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 0; j < image.getWidth(); j++) {
				for (int k = 0; k < 256; k++) {
					int y = (int)((imr.getrgb(image, j, i)[0]*0.299)+imr.getrgb(image, j, i)[1]*0.587+imr.getrgb(image, j, i)[2]*0.114);
					int u = (int)((imr.getrgb(image, j, i)[0]*(-0.147))+imr.getrgb(image, j, i)[1]*(-0.289)+imr.getrgb(image, j, i)[2]*0.436);
					int v = (int)((imr.getrgb(image, j, i)[0]*0.615)+imr.getrgb(image, j, i)[1]*(-0.515)+imr.getrgb(image, j, i)[2]*(-0.100));
					if (y == k) {
						yuv[k][0]++;
					}
					if (u == k) {
						yuv[k][1]++;
					}
					if (v == k) {
						yuv[k][2]++;
					}
				}
			}
		}
		
		/*//Check output with yuv format 
		for (int i = 0; i < 256; i++) {
			System.out.println("yuvY" + i + ":" + yuv[i][0]);
			System.out.println("yuvU" + i + ":" + yuv[i][1]);
			System.out.println("yuvV" + i + ":" + yuv[i][2]);
		}
		*/
		
		return yuv;
	}

}