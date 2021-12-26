package com.runescape;

import javax.swing.*;
import java.awt.*;

public final class GameFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private GameApplet appletInstance;

	public GameFrame(GameApplet gameApplet, int width, int height, boolean resizable, boolean full) {
		appletInstance = gameApplet;

		this.setTitle(Configuration.CLIENT_NAME);
		this.setUndecorated(full);
		this.setResizable(resizable);
		this.setFocusTraversalKeysEnabled(false);
		this.setBackground(Color.BLACK);

		this.setVisible(true);
		Insets insets = this.getInsets();
		this.setSize(width + insets.left + insets.right, height + insets.top + insets.bottom);
		if (resizable) {
			setMinimumSize(new Dimension(800 + insets.left + insets.right, 600 + insets.top + insets.bottom));
		}
		this.requestFocus();
		this.toFront();
	}

	@Override
	public Graphics getGraphics() {
		Graphics g = super.getGraphics();
		return g;
	}

	@Override
	public void update(Graphics g) {
		appletInstance.update(g);
	}

	@Override
	public void paint(Graphics g) {
		appletInstance.paint(g);
	}

	public int getFrameWidth() {
		Insets insets = this.getInsets();
		return getWidth() - (insets.left + insets.right);
	}

	public int getFrameHeight() {
		Insets insets = this.getInsets();
		return getHeight() - (insets.top + insets.bottom);
	}

}