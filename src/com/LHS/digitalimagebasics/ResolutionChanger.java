package com.LHS.digitalimagebasics;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import javax.swing.JOptionPane;

public class ResolutionChanger {
	private int oldWidth;
	private BufferedImage image;
	public ResolutionChanger(BufferedImage img) {
		image = img;
		oldWidth = image.getWidth();
	}
	
	public Image decreaseResolution(int newNumPixels) {
		int[] fPixels = new int[newNumPixels*newNumPixels];
		int sizeBox = oldWidth/newNumPixels;
		int index = 0;
		System.out.println(oldWidth + " " + sizeBox);
		int c=0;
        int r=0;
		for (c = 0; c < oldWidth; c+= sizeBox) {
			for (r = 0; r < oldWidth; r+= sizeBox) {
				int[] pixels = new int[sizeBox*sizeBox];
				pixels = getPixels(image, c, r, sizeBox, sizeBox);
				int thisPixel;
				thisPixel = getAveragePixelValue(pixels);
				fPixels[index] = thisPixel;
				index++;
			}
		}
		return getImageFromArray(fPixels, newNumPixels, newNumPixels);
	}
	private static int getAveragePixelValue(int[] pixels) {
		int totalRed = 0, totalGreen = 0, totalBlue = 0;
		int totalPixels = pixels.length;
		for (int k = 0; k < pixels.length; k++) {
			totalRed += ((pixels[k] >> 16) & 255);
			totalGreen += ((pixels[k] >> 8) & 255);
			totalBlue += (pixels[k] & 255);
		}
		return  ((totalRed/totalPixels) <<16)+((totalGreen/totalPixels) <<8) +(totalBlue/totalPixels);
	}
	public static Image getImageFromArray(int[] pixels, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int i = 0;
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                image.setRGB(w, h, pixels[i]);
                i++;
            }
        }
        
        return image;
    }
	private static int[] getPixels(BufferedImage img, int x, int y, int width, int height) {
	    int[] pix = new int[width*height];
	    int i = 0;
	    for (int w = x; w < x + width; w++) {
	        for (int h = y; h < y + height; h++) {
	            pix[i] = img.getRGB(w, h);
	            i++;
	        }
	    }
	    return pix;
	}
}
