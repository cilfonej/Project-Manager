package components;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class SplashScreen extends JDialog {
	private static final long serialVersionUID = 1755473444710386928L;
	private static final ImageIcon SPLASH = prepImage(
			new ImageIcon(SplashScreen.class.getResource("AssignmentSplash.png")).getImage()
		);

	public static ImageIcon prepImage(Image image) {
		BufferedImage img = new BufferedImage(image.getWidth(null) * 2/3, image.getHeight(null) * 2/3, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2d = img.createGraphics();
		g2d.drawImage(image, 0, 0, img.getWidth(), img.getHeight(), null);
		g2d.dispose();
		
		return new ImageIcon(img);
	}
	
	public SplashScreen() {
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(new JLabel(SPLASH));
		
		setUndecorated(true);
		setSize(SPLASH.getIconWidth(), SPLASH.getIconHeight());
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
