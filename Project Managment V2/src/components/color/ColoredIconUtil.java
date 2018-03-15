package components.color;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

public class ColoredIconUtil {
	public static ImageIcon prepImage(Image image, Color color) { return prepImage(image, color, 0); }
	public static ImageIcon prepImage(Image image, Color color, Color fill) { return prepImage(image, color, 0, fill); }
	public static ImageIcon prepImage(Image image, Color color, double rot) { return prepImage(image, color, rot, null); }
	public static ImageIcon prepImage(Image image, Color color, double rotation, Color fill) {
		return prepImage(image, color, 16, rotation, fill); }
		
	public static ImageIcon prepImage(Image image, Color color, int size, double rotation, Color fill) {
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2d = img.createGraphics();
		g2d.rotate(rotation, size / 2, size / 2);
		g2d.drawImage(image, 0, 0, img.getWidth(), img.getHeight(), null);
		g2d.dispose();
		
		float alpha = color.getAlpha() / 255f;
		float red   = color.getRed()   / 255f;
		float green = color.getGreen() / 255f;
		float blue  = color.getBlue()  / 255f;
		
		for(int x = 0; x < img.getWidth(); x ++) {
		for(int y = 0; y < img.getHeight(); y ++) {
			int RGB = img.getRGB(x, y);
			
			img.setRGB(x, y, 
				(int) ((RGB >> 24 & 0xFF) * alpha) << 24 |
				(int) ((RGB >> 16 & 0xFF) * red  ) << 16 |
				(int) ((RGB >>  8 & 0xFF) * green) <<  8 |
				(int) ((RGB >>  0 & 0xFF) * blue ) <<  0
			);
		}}

		if(fill != null) {
			BufferedImage imgFill = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
			g2d = imgFill.createGraphics();
			g2d.setColor(fill); g2d.fillRect(0, 0, size, size);
			g2d.drawImage(img, 0, 0, size, size, null); 
			g2d.dispose(); img = imgFill;
		}
		
		return new ImageIcon(img);
	}
	
	public static ImageIcon emptyImage() {
		return new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
	}
}
