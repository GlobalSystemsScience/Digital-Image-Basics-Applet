package com.LHS.digitalimagebasics;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;
import javax.swing.filechooser.FileNameExtensionFilter;

import sun.awt.image.ToolkitImage;

//import sun.awt.image.ToolkitImage;

public abstract class InteractiveDisplay {
	private static final String CAPTION_NAME = "caption";
	private static final String IMAGE_NAME = "image";
	private final String DOMAIN = "https://sites.google.com";
	private Container body = new Container();
	private  GridBagLayout layout = new GridBagLayout();
	private String imageURL;
	private ImageIcon imageIcon;
	private BufferedImage bufferedImage;
	private JLabel imageLabel;
	private JLabel caption;
	private Container pixelValueContainer;
	private String[] answers;
	private int imageIndex;
	protected JLayeredPane imagePane;
	private BufferedImage image;
	private int resizeDimension = -1;
	private BufferedImage originalBufferedImage;
	
	protected JLabel redValue;
	protected JLabel greenValue;
	protected JLabel blueValue;
	private String[] captions;
	
	//////////////////////DEBUGGER/////////////////////////////
	private boolean debug = false; ////MUST USE IN ORDER TO RUN LOCALLY RATHER THAN FROM THE SPECIFIED DOMAIN ONLINE
	///////////////////////////////////////////////////////////
	
	private JPanel imageContainer;
	
	protected MouseInputAdapter mouseClick = null;
	private GridBagConstraints imageConstraints;
	private boolean absolutePath = false;
	
	//Instructions at 0,0
	//Image list at 0,1
	//Specifics at 0,2
	//Image at 1,0 to 1,4
	//Caption at 1,4
	//Help text at 0,4
	//Pixel shit at 0,3
	
	
	public InteractiveDisplay() {
		imagePane = new JLayeredPane();
		
		body.setLayout(layout);
		body.setBounds(0, 0, 1000, 700);
		
	}
	protected void generateInstructions(String text) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		
		JLabel instructions = new JLabel(text);
		body.add(instructions, constraints);
	}
	protected void generateImageList(String[] labels, final String[] relURLS, final String[] captions, String[] answers) {
		generateImageList(labels, relURLS, captions);
		this.answers = answers;
	}
	protected void generateImageList(String[] labels, final String[] relURLS, final String[] captions) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridheight = 1;
		this.captions = captions;
		JComboBox list = new JComboBox(labels);
		final int specialIndex = list.getItemCount();
		//list.addItem("Your Picture");
		System.out.println("CAPTIONS: " + captions.length);
		list.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JComboBox target = (JComboBox)arg0.getSource();
				
				String newRelURL = null;
				String newCaption = null;
				System.out.println("SPECIAL: " + specialIndex + "  " + target.getSelectedIndex());
				if (specialIndex == target.getSelectedIndex()) {	//Upload own picture
					JFileChooser fc = new JFileChooser();
					int returnVal = fc.showOpenDialog(target);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
		                File file = fc.getSelectedFile();
		                System.out.println(file.getAbsolutePath());
		                //FileWriter fw = new FileWriter(file);
		                String ext = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".")+1);
		                //File destFile = new File(getClass().getResource("/images/user" + ext).getFile());
		                /*try {
							BufferedImage img = createResizedCopy(ImageIO.read(file), 512, 512, true);
							//ImageIO.write(img, ext, destFile);
						} catch (IOException e1) {
							e1.printStackTrace();
							JOptionPane.showMessageDialog(null, e1.toString() + " generateImageList Interactive Display");
						}*/
		                
		                System.out.println(ext);
		                try {
                            bufferedImage = ImageIO.read(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
		                absolutePath = true;
		                imageIcon = new ImageIcon(bufferedImage);
		                newRelURL = file.getAbsolutePath(); //"/images/user" + ext;
		                newCaption = "Your Picture";
					} else {
						target.setSelectedIndex(InteractiveDisplay.this.imageIndex);
						return;
					}
				} else {
				    absolutePath = false;
					newRelURL = relURLS[target.getSelectedIndex()];
					newCaption = captions[target.getSelectedIndex()];
				}
				((JComboBox)arg0.getSource()).getParent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				InteractiveDisplay.this.imageIndex = target.getSelectedIndex();
				InteractiveDisplay.this.imageURL = newRelURL;
				
				changePicture(InteractiveDisplay.this.imageURL);
				changeCaption(newCaption);
				((JComboBox)arg0.getSource()).getParent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});
		body.add(list, constraints);
		
	}
	private void changeCaption(String newCaption) {
		caption.setText(newCaption);
		
		body.repaint();
	}
	/**
	 * This should only be called once. It instantiates and adds the image to the display
	 * @param relURL relative URL of the image to add
	 */
	protected void addImage(String relURL) {
		
		this.imageURL = relURL;
		this.imageConstraints = new GridBagConstraints();
		imageConstraints.gridx = 1;
		imageConstraints.gridy = 0;
		imageConstraints.gridheight = 4;
		this.imageIndex = 0;
		imageIcon = new ImageIcon(getClass().getResource(imageURL));
		try {
            bufferedImage = ImageIO.read(getClass().getResource(imageURL));
            this.originalBufferedImage = bufferedImage;
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.toString() + " addImage InteractiveDisplay");
        }
		imageLabel = new JLabel(imageIcon);
		imageLabel.setName(IMAGE_NAME);

		
		if (mouseClick != null) {
			imageLabel.addMouseListener(mouseClick);
			imageLabel.addMouseMotionListener(mouseClick);
	
		} else {		
			imageLabel.addMouseMotionListener(new MouseMotionListener() {
				@Override
				public void mouseDragged(MouseEvent arg0) {
					//Nothing	
				}
				@Override
				public void mouseMoved(MouseEvent arg0) {
					Point target = arg0.getPoint();
					int pixel = getBufferedImage().getRGB(target.x, target.y);
					System.out.println(getBufferedImage().getRGB(target.x, target.y));
					handlePixelValue(pixel);
				}
			});
		}
		
		imagePane.setPreferredSize(new Dimension(imageIcon.getIconWidth(), imageIcon.getIconHeight()));
		imageLabel.setMinimumSize(new Dimension(imageIcon.getIconWidth(), imageIcon.getIconHeight()));
		imageLabel.setBounds(0, 0, imageIcon.getIconWidth(), imageIcon.getIconHeight());
		if (this.imageIndex == this.captions.length-1) {
			System.out.println("RESIZING");
			imageLabel.setBounds(0,0,512,512);
		}
		body.add(imageLabel, imageConstraints);
		imagePane.add(imageLabel, new Integer(1));

		imagePane.moveToFront(imageLabel);

		body.add(imagePane, imageConstraints);		
	}
	protected void addCaption(String captionText) {
		GridBagConstraints consts = new GridBagConstraints();
		consts.gridx=1;
		consts.gridy=4;
		caption = new JLabel(captionText);
		caption.setName(CAPTION_NAME);
		
		body.add(caption, consts);
	}
	private BufferedImage getImage(String relURL) {
		BufferedImage img = null;
		try {
		    if (absolutePath) {
		        img = ImageIO.read(new File(this.imageURL));
		    } else {
    		    String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    	        String decodedPath;
    	        decodedPath = URLDecoder.decode(path, "UTF-8");
    	        decodedPath = decodedPath.substring(0, decodedPath.lastIndexOf("/"));
    	        if (debug) {
    	            img = ImageIO.read(new File(decodedPath + this.imageURL));
    	        } else {
    	            img = ImageIO.read(new URL(DOMAIN + decodedPath + this.imageURL));
    	        }
    			//img = ImageIO.read(new File(getClass().getResource(relURL).getFile()));
		    }
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.toString() + " getImage Interactive Display");
		}
		return img;
	}
	protected void changePicture(String newRelURL) {
		BufferedImage img = getImage(newRelURL);
		bufferedImage = img;
		originalBufferedImage = bufferedImage;
//		image = img;
		if (this.imageIndex == this.captions.length-1) {
			System.out.println("RESIZING");
			

		} else {
			//imageLabel.setBounds(0,0,imageIcon.getIconWidth(), imageIcon.getIconHeight());
		}
		if (resizeDimension != -1) {
			img = InteractiveDisplay.createResizedCopy(img, resizeDimension, resizeDimension, true);
		}
		//ToolkitImage newImage = Toolkit.getDefaultToolkit().createImage(img.getSource());
		//Image newImage = ImageIO.read(img.getSource());
		imageIcon.setImage(img);
		body.repaint();
		this.changePictureHandler();

	}
	private static BufferedImage createResizedCopy(Image originalImage, 
            int scaledWidth, int scaledHeight, boolean preserveAlpha) {
	    System.out.println("resizing...");
	    int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
	    BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
	    Graphics2D g = scaledBI.createGraphics();
	    if (preserveAlpha) {
	            g.setComposite(AlphaComposite.Src);
	    }
	    g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null); 
	    g.dispose();
	    return scaledBI;
	}
	protected void changePicture(Image newImage) {
	    bufferedImage = myCreateImage(newImage);
		if (resizeDimension != -1) {
			newImage = InteractiveDisplay.createResizedCopy(newImage, resizeDimension, resizeDimension, true);
		}
		imageIcon.setImage(newImage);
		System.out.println("Changing: " + newImage);
		
//		try {
//			image = (BufferedImage) newImage;
//		} catch (Exception e) {
//			ToolkitImage img = (ToolkitImage)newImage;
//			System.out.println("MESSED UP: " + img);
//			System.out.println(img.getBufferedImage());
//		}
//		imageIcon.setImage(;)
//		System.out.println("Picture changed: " + image);
		this.revalidateContainer();
	}
	protected void addHelpText(boolean allowOriginalImage) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 4;
		
		Container temp = new Container();
		temp.setLayout(new GridLayout(2,1));
		
		JLabel label = new JLabel("<html>Intensities of colors range from 0%,<br>" +
				"meaning none of the color is present, to<br>" +
				"100%, when maximum color is present");
		temp.add(label);
		
		if (allowOriginalImage) {
			JButton originalImage = new JButton("Show Original Picture");
			originalImage.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					JFrame popup = new JFrame("Original Image");
					ImageIcon original = new ImageIcon(getClass().getResource(imageURL));
					popup.setBounds(0,0, original.getIconWidth(), original.getIconHeight());
					JLabel originalLabel = new JLabel(original);
					popup.add(originalLabel);
					popup.setVisible(true);
				}
			});
			temp.add(originalImage);
		}
		
		body.add(temp, constraints);
		
	}
	protected void addToBody(Component comp, GridBagConstraints constraints) {
		body.add(comp, constraints);
	}
	public Container getContainer() {return body;}
	
	protected BufferedImage getBufferedImage() {
		/*try {
			return ((ToolktImage)imageIcon.getImage()).getBufferedImage();
		} catch (Exception e) {
			return (BufferedImage)imageIcon.getImage();
		}*/
	    return bufferedImage;
	}
	protected BufferedImage getOriginalBufferedImage() throws IOException {
	    System.out.println("getOriginalBufferedImage: " + imageURL);
	    /**if (absolutePath) {
	        return ImageIO.read(new File(this.imageURL));
	    } else {
    	    String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String decodedPath;
            decodedPath = URLDecoder.decode(path, "UTF-8");
            decodedPath = decodedPath.substring(0, decodedPath.lastIndexOf("/"));
            if (debug) {
                return ImageIO.read(new File(decodedPath + this.imageURL));
            } else {
                return ImageIO.read(new URL(DOMAIN + decodedPath + this.imageURL));
            }
	    }*/
	    return originalBufferedImage;
	}
	protected void setUpPixelValue(String label1, String label2, String label3) {
		pixelValueContainer = new Container();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridheight = 1;
		
		GridLayout layout = new GridLayout(4, 2);
		pixelValueContainer.setLayout(layout);
		
		JLabel header1 = new JLabel("Color");
		JLabel header2 = new JLabel("Displayed Intensity");
		
		JLabel red = new JLabel(label1);
		redValue = new JLabel("");
		
		JLabel green = new JLabel(label2);
		greenValue = new JLabel("");
		
		JLabel blue = new JLabel(label3);
		blueValue = new JLabel("");
		
		pixelValueContainer.add(header1);
		pixelValueContainer.add(header2);
		
		pixelValueContainer.add(red);
		pixelValueContainer.add(redValue);
		
		pixelValueContainer.add(green);
		pixelValueContainer.add(greenValue);
		
		pixelValueContainer.add(blue);
		pixelValueContainer.add(blueValue);
		
		body.add(pixelValueContainer, constraints);
	}
	protected void handlePixelValue(int rgb) {
		int red = (rgb >> 16) & 0x000000FF;
		int green = (rgb >>8 ) & 0x000000FF;
		int blue = (rgb) & 0x000000FF;
		red = (int)(((double)red / 255)*100);
		green = (int)(((double)green / 255)*100);
		blue = (int)(((double)blue / 255)*100);
		
		redValue.setText(red + "");
		greenValue.setText(green + "");
		blueValue.setText(blue + "");
		
		pixelValueContainer.repaint();
	}
	protected void repaintPixels() {
		pixelValueContainer.repaint();
	}
	protected void revalidateContainer() {
		this.body.validate();
		this.body.repaint();
	}
	protected String getRelURL() {
		return this.imageURL;
	}
	protected void displayAnswer() {
		this.changeCaption(answers[this.imageIndex]);
	}
	protected void hideAnswer() {
		this.changeCaption(this.captions[this.imageIndex]);
	}
	protected Rectangle getImageBounds() {
		System.out.println("IMAGE BOUNDS: " + image);
		return imageLabel.getBounds();
//		return new Rectangle(image.getMinX(), image.getMinY(), image.getWidth(), image.getHeight());
	}
	protected abstract void changePictureHandler();
	protected void addToLayeredPane(JComponent comp, Integer layer, int position) {
		imagePane.add(comp, layer, position);
	}
	protected void paintImage() {
		Graphics g = imageContainer.getGraphics();
		g.drawImage(image, 0, 0, null);
	}
	protected void drawImage(BufferedImage img) {
		System.out.println("Drawing image");
		this.imageIcon.setImage(img);
		this.revalidateContainer();
//		Graphics g = img.getGraphics();
//		g.drawImage(img, img.getMinX(), img.getMinY(), img.getWidth(), img.getHeight(), null);
		
	}
	protected void bringToFront(JComponent comp) {
		imagePane.moveToFront(comp);
	}
	
	protected void setPixelValue(int pixel) {
		this.redValue.setText(Math.round(((pixel & 0xFF0000) >> 16)/(double)255*100) + "");
		this.greenValue.setText(Math.round(((pixel & 0x00FF00) >> 8)/(double)255*100) + "");
		this.blueValue.setText(Math.round((pixel & 0x0000FF)/(double)255*100) + "");
		pixelValueContainer.repaint();
	}
	public void savePicture() {
		BufferedImage img = this.getBufferedImage();
		JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter fnef = new FileNameExtensionFilter("Images", "png");
		fc.setFileFilter(fnef);
		int returnVal = fc.showSaveDialog(body);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			int index = file.getAbsolutePath().lastIndexOf(".");
			if (index == -1 || !file.getAbsolutePath().substring(index+1).equals("png")) {
				file = new File(file.getAbsolutePath() + ".png"); 
			}
			try {
				ImageIO.write(img, "png", file);
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.toString() + " savePicture Interactive Display");
			}
		}
	}
	protected void setResize(int num) {
		this.resizeDimension = num;
	}
//	public void printPicture() {
//		PrinterJob pj = PrinterJob.getPrinterJob();
//		if (pj.printDialog()) {
//			try {
////				PageFormat pf = new PageFormat();
////				Paper p = new Paper();
////				//p.setImageableArea(this.imagePane.getX(), this.imagePane.getY(), this.imagePane.getWidth(), imagePane.getHeight());
////				pf.setPaper(p);
////				pj.pageDialog(pf);
//				
//				pj.print();
//				System.out.println("Printed");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			System.out.println("Printed");
//		}
//		
//		
//		PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
//		 pras.add(new Copies(1));
//		 PrintService pss[] = PrintServiceLookup.lookupPrintServices(DocFlavor.INPUT_STREAM.GIF, pras);
//		 if (pss.length == 0)
//		      throw new RuntimeException("No printer services available.");
//		 PrintService ps = pss[0];
//		 System.out.println("Printing to " + ps);
//		 DocPrintJob job = ps.createPrintJob();
//		 FileInputStream fin;
//		try {
//			fin = new FileInputStream("YOurImageFileName.PNG");
//		
//			Doc doc = new SimpleDoc(fin, DocFlavor.INPUT_STREAM.GIF, null);
//		 
//			job.print(doc, pras);
//		
//			fin.close();
//		} catch (PrintException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (FileNotFoundException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	public BufferedImage myCreateImage(Image image) {  
	    int width = image.getWidth(null);  // or, = image.getWidth(this);  
	    int height = image.getHeight(null);  
	    // Create a buffered image in which to draw  
	    BufferedImage bufferedImage = new BufferedImage(width, height,  
	                                          BufferedImage.TYPE_INT_RGB);  
	    // Draw image into bufferedImage.  
	    Graphics2D g2 = bufferedImage.createGraphics();  
	    g2.drawImage(image, 0, 0, null);  
	    g2.dispose();  
	    return bufferedImage;  
	}  
}
