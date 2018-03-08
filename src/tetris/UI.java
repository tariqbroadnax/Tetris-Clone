package tetris;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class UI 
{	
	private Tetris tetris;
	
	private JFrame frame;
	
//	private Container cont;
	
	private BufferedImage offscr;
	
	private GridPane gridPane;
	
	private NextPane nextPane;
	
	private ScorePane scorePane;
	
	private BestPane bestPane;
	
	private TimePane timePane;
	
	public UI(Tetris tetris)
	{
		this.tetris = tetris;
		
		frame = new JFrame();
		
		offscr = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		
		gridPane = new GridPane();
		
		nextPane = new NextPane();
		
		scorePane = new ScorePane();
		
		bestPane = new BestPane();
		
		timePane = new TimePane();
	
//		cont = new JPanel();
		
		Container cont = frame.getContentPane();
		
		cont.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
	
		c.gridx = 0; c.gridy = 0;
		c.gridwidth = 1; c.gridheight = 4;
		c.fill = c.BOTH;
		c.weightx = 0.7; c.weighty = 1; 
		cont.add(gridPane, c);
		
		c.gridx = 1; c.gridy = 0;
		c.gridwidth = 1; c.gridheight = 1;
		c.fill = c.BOTH;
		c.weightx = 0.4; c.weighty = 1; 
		cont.add(nextPane, c);
		
		c.gridx = 1; c.gridy = 1;
		c.gridwidth = 1; c.gridheight = 1;
		c.fill = c.BOTH;
		c.weightx = 0.4; c.weighty = 1; 
		cont.add(scorePane, c);
		
		c.gridx = 1; c.gridy = 2;
		c.gridwidth = 1; c.gridheight = 1;
		c.fill = c.BOTH;
		c.weightx = 0.4; c.weighty = 1;
		cont.add(bestPane, c);
		
		c.gridx = 1; c.gridy = 3;
		c.gridwidth = 1; c.gridheight = 1;
		c.fill = c.BOTH;
		c.weightx = 0.4; c.weighty = 1;
		cont.add(timePane, c);
	
		cont.setIgnoreRepaint(true);
		
		frame.setSize(450, 650);
		frame.setResizable(false);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//	
		installFont();
	}
	
	private void installFont()
	{
		try {
		     GraphicsEnvironment ge = 
		         GraphicsEnvironment.getLocalGraphicsEnvironment();
		     ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("CHMCPixel.ttf")));
		} catch (IOException|FontFormatException e) {
		}
	}
	
	private double elapsed;
	
	public void update(double dt)
	{
		gridPane.update(dt);
		
		elapsed += dt;
		
		if(elapsed > 1)
		{
			elapsed = 0;
			
			Updater updater = tetris.getUpdater();
		
			frame.setTitle("Tetris Clone | FPS: " + updater.getFPS() + " | Ticks: " + updater.getTicks());
		}
		
		Container cont = frame.getContentPane();
		
		Dimension contSize = cont.getSize(),
				  offscrSize = new Dimension(offscr.getWidth(), offscr.getHeight());
		
		if(contSize.width == 0 || contSize.height == 0)
			return;
		
		if(!contSize.equals(offscrSize))
			offscr = new BufferedImage(contSize.width, contSize.height, BufferedImage.TYPE_INT_ARGB);
			
		Graphics g = offscr.getGraphics();
		
		cont.paint(g);
		
		cont.getGraphics()
			.drawImage(offscr, 0, 0, null);
	}
	
	public void show() 
	{
		frame.setVisible(true);
		while(!frame.isVisible()) {}
	}
	
//	public Container getContent() {
//		return cont;
//	}
	
	private class GridPane extends JPanel implements KeyListener
	{
		private static final long serialVersionUID = 1L;
		
		private List<KeyEvent> pressedKeyEvents,
							   releasedKeyEvents;
		
		public GridPane()
		{
			pressedKeyEvents = Collections.synchronizedList(new ArrayList<KeyEvent>());
			
			releasedKeyEvents = Collections.synchronizedList(new ArrayList<KeyEvent>());

			setBorder(BorderFactory.createLineBorder(Color.black));
		
			addKeyListener(this);
		}
		
		public void addNotify()
		{
			super.addNotify();
			
			requestFocusInWindow();
		}
		
		public void update(double dt)
		{
			synchronized(pressedKeyEvents)
			{
				for(KeyEvent e : pressedKeyEvents)
				{
					int code = e.getKeyCode();
					
					if(code == KeyEvent.VK_LEFT)
						tetris.moveCurrentPieceLeft();
					else if(code == KeyEvent.VK_RIGHT)
						tetris.moveCurrentPieceRight();
					else if(code == KeyEvent.VK_UP)
						tetris.rotateCurrentPieceLeft();
					else if(code == KeyEvent.VK_DOWN)
						tetris.setDropMode(Tetris.DropMode.SOFT);
					else if(code == KeyEvent.VK_SPACE)
						tetris.hardDrop();
				}
				
				pressedKeyEvents.clear();
			}
			
			synchronized(releasedKeyEvents)
			{
				for(KeyEvent e : pressedKeyEvents)
				{
					int code = e.getKeyCode();
					
					if(code == KeyEvent.VK_DOWN)
						tetris.setDropMode(Tetris.DropMode.NORMAL);
				}
				
				releasedKeyEvents.clear();
			}
		}
		
		public void paintComponent(Graphics g)
		{
			Graphics2D g2d = (Graphics2D) g;

			Dimension size = getSize();
	
			AffineTransform transform = g2d.getTransform();
			
			g2d.scale(size.width * 1.0 / Tetris.SCENE_WIDTH,
					  size.height * 1.0 / Tetris.SCENE_HEIGHT);
			
			tetris.paint(g);
			
			g2d.setTransform(transform);
		}

		@Override
		public void keyPressed(KeyEvent e) {
			pressedKeyEvents.add(e);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			releasedKeyEvents.add(e);
		}

		@Override
		public void keyTyped(KeyEvent e) {}
	}
	
	private class NextPane extends JPanel
	{
		private JLabel nextLabel;
		
		private JPanel nextPiecePanel;
		
		public NextPane()
		{
			nextLabel = new JLabel("NEXT", SwingConstants.CENTER);
			
			nextPiecePanel = new JPanel() 
			{
				public void paintComponent(Graphics g)
				{
					Tetris.Piece piece = tetris.getNextPiece();
					
					Dimension size = getSize();
					
					double x = size.getWidth()/2 - Tetris.COL_WIDTH/2,
						   y = size.getHeight()/2 - Tetris.ROW_HEIGHT/2;
					
					piece.paint(g, (int) x, (int) y);
				}
			};
			
			nextLabel.setFont(new Font("CHMCPixel", Font.BOLD, 30));
			nextLabel.setForeground(Color.white);
			
			setBackground(new Color(0, 31, 73));
			
			setLayout(new BorderLayout());
			
			add(nextLabel, BorderLayout.NORTH);
			add(nextPiecePanel, BorderLayout.CENTER);
			
			setBorder(BorderFactory.createLineBorder(Color.blue, 5));
		}
	}
	
	private class ScorePane extends JPanel
	{
		private JLabel titleLabel,
					   scoreLabel;
		
		public ScorePane()
		{
			titleLabel = new JLabel("SCORE");
			
			scoreLabel = new JLabel() {
				public String getText() {
					return "" + tetris.getScore();
				}
			};
			
			titleLabel.setFont(new Font("CHMCPixel", Font.BOLD, 30));
			titleLabel.setForeground(Color.white);
			titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
			titleLabel.setVerticalAlignment(SwingConstants.CENTER);
			
			scoreLabel.setFont(new Font("CHMCPixel", Font.BOLD, 30));
			scoreLabel.setForeground(Color.white);
			scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
			scoreLabel.setVerticalAlignment(SwingConstants.CENTER);
			
			scoreLabel.setIgnoreRepaint(true);
			
			setLayout(new BorderLayout());
			
			add(titleLabel, BorderLayout.NORTH);
			add(scoreLabel, BorderLayout.CENTER);
			
			setBackground(new Color(0, 31, 73));
			setBorder(BorderFactory.createLineBorder(Color.blue, 5));
		}
	}
	
	private class BestPane extends JPanel
	{
		private JLabel titleLabel,
		   			   scoreLabel;

		public BestPane()
		{
			titleLabel = new JLabel("BEST");
			
			scoreLabel = new JLabel()
			{
				public String getText() {
					return "" + tetris.getBestScore();
				}
			};
			
			titleLabel.setFont(new Font("CHMCPixel", Font.BOLD, 30));
			titleLabel.setForeground(Color.white);
			titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
			titleLabel.setVerticalAlignment(SwingConstants.CENTER);
			
			scoreLabel.setFont(new Font("CHMCPixel", Font.BOLD, 30));
			scoreLabel.setForeground(Color.white);
			scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
			scoreLabel.setVerticalAlignment(SwingConstants.CENTER);
			
			setLayout(new BorderLayout());
			
			add(titleLabel, BorderLayout.NORTH);
			add(scoreLabel, BorderLayout.CENTER);
			
			setBackground(new Color(0, 31, 73));
			setBorder(BorderFactory.createLineBorder(Color.blue, 5));
		}
	}
	
	private class TimePane extends JPanel
	{
		private JLabel titleLabel,
		   			   timeLabel;

		public TimePane()
		{
			titleLabel = new JLabel("TIME");
			
			timeLabel = new JLabel()
			{
				public String getText() 
				{
					double time = tetris.getTime();
					
					int minutes = (int)(time / 60),
						seconds = (int)(time - minutes * 60);
					
					if(minutes > 99) minutes = 99;
					
					String str = String.format("%02d:%02d", minutes, seconds);
				
					return str;
				}
			};
			
			titleLabel.setFont(new Font("CHMCPixel", Font.BOLD, 30));
			titleLabel.setForeground(Color.white);
			titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
			titleLabel.setVerticalAlignment(SwingConstants.CENTER);
			
			timeLabel.setFont(new Font("CHMCPixel", Font.BOLD, 30));
			timeLabel.setForeground(Color.white);
			timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
			timeLabel.setVerticalAlignment(SwingConstants.CENTER);
			
			setLayout(new BorderLayout());
			
			add(titleLabel, BorderLayout.NORTH);
			add(timeLabel, BorderLayout.CENTER);
			
			setBackground(new Color(0, 31, 73));
			setBorder(BorderFactory.createLineBorder(Color.blue, 5));
		}
	}
}
