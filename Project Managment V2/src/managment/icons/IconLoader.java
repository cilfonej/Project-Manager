package managment.icons;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class IconLoader {
	private IconLoader() { }
	
	public static Image loadImage(String fileName) {
		return new ImageIcon(IconLoader.class.getResource(fileName)).getImage();
	}
	
	public static Icon loadIcon(String fileName) {
		return new ImageIcon(IconLoader.class.getResource(fileName));
	}
	
	public static Icon loadIcon(String fileName, int size) {
		Image img = new ImageIcon(IconLoader.class.getResource(fileName)).getImage();
		BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics g = scaled.createGraphics(); g.drawImage(img, 0, 0, size, size, null); g.dispose();

		return new ImageIcon(scaled); 
	}
	
	public static Icon loadIcon(String fileName, int size, Color color) {
		Image img = new ImageIcon(IconLoader.class.getResource(fileName)).getImage();
		BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics g = scaled.createGraphics(); g.drawImage(img, 0, 0, size, size, null); g.dispose();
		
		float alpha = color.getAlpha() / 255f;
		float red   = color.getRed()   / 255f;
		float green = color.getGreen() / 255f;
		float blue  = color.getBlue()  / 255f;
		
		for(int x = 0; x < scaled.getWidth(); x ++) {
		for(int y = 0; y < scaled.getHeight(); y ++) {
			int RGB = scaled.getRGB(x, y);
			
			scaled.setRGB(x, y, 
				(int) ((RGB >> 24 & 0xFF) * alpha) << 24 |
				(int) ((RGB >> 16 & 0xFF) * red  ) << 16 |
				(int) ((RGB >>  8 & 0xFF) * green) <<  8 |
				(int) ((RGB >>  0 & 0xFF) * blue ) <<  0
			);
		}}

		return new ImageIcon(scaled); 
	}
}
