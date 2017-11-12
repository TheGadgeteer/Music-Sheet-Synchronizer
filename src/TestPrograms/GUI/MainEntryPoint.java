package TestPrograms.GUI;

import java.awt.BorderLayout;
import java.awt.event.AdjustmentEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JScrollBar;

public class MainEntryPoint {

	public static void main(String[] args) throws Exception {
		startImageScroller();
	}

	public static void startImageScroller() {
		JFrame f = new JFrame("ImageScroller");

		ImageScroller is = new ImageScroller(800, 700);
		try {
			is.addImage(ImageIO.read(new File("alyssa arce.jpg")));
			is.addImage(ImageIO.read(new File("emily.jpg")));
		} catch (IOException e) {}

		JScrollBar scaleScrollBar = new JScrollBar(JScrollBar.HORIZONTAL, 100, 50, 100, 300);
		scaleScrollBar.addAdjustmentListener((AdjustmentEvent e) -> is.scaleImages(e.getValue() / 100f));

		f.add(scaleScrollBar, BorderLayout.NORTH);
		f.add(is);
		f.pack();
		f.setVisible(true);
	}
}
