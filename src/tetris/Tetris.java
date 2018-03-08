package tetris;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

public class Tetris 
{
	private static final int COLS = 11, ROWS = 26;
	
	public static final int COL_WIDTH = 20,
						     ROW_HEIGHT = 20;
	
	public static final int SCENE_WIDTH = COLS * COL_WIDTH, 
							SCENE_HEIGHT = ROWS * ROW_HEIGHT;
	
	private static final double NORMAL_MOVE_DELAY = 0.1,
								SOFT_MOVE_DELAY = 0.05;

	private Updater updater;
	
	private UI ui;

	private Square[][] grid;
	
	private Piece currPiece, nextPiece;
	
	private Random rand;
	
	private DropMode dropMode;
	
	public enum DropMode {NORMAL, SOFT}
	
	private int score, bestScore;
	
	private double time;
	
	public Tetris()
	{
		updater = new Updater(this);
		
		ui = new UI(this);
		
		grid = new Square[ROWS][COLS];
		
		rand = new Random();
	
		currPiece = randomPiece();
		nextPiece = randomPiece();
	}
	
	private Piece randomPiece()
	{
		int i = rand.nextInt(7);
		
		if(i == 0)
			return new I(0, 4);
		else if(i == 1)
			return new O(0, 4);
		else if(i == 2)
			return new T(0, 4);
		else if(i == 3)
			return new S(0, 4);
		else if(i == 4)
			return new Z(0, 4);
		else if(i == 5)
			return new J(0, 4);
		else
			return new L(0, 4);
	}
	
	public void start()
	{
		updater.start();
		
		ui.show();
		
		Audio.play("sounds/start.wav", false);
		Audio.play("sounds/music.wav", true);
	}
	
	private double elapsed = 0;
	
	public void update(double dt)
	{
		ui.update(dt);
		
		time += dt;
		
		elapsed += dt;
		
		double moveDelay = dropMode == DropMode.NORMAL ? 
						   NORMAL_MOVE_DELAY : SOFT_MOVE_DELAY;
						
		if(elapsed > moveDelay)
		{
			elapsed = 0;
			
			currPiece.moveDown();
			
			if(currPiece.collides())
			{
				if(dropMode == DropMode.NORMAL)
					Audio.play("sounds/slow-hit.wav", false);
				else
					Audio.play("sounds/force-hit.wav", false);
				
				currPiece.moveUp();
				currPiece.split();
				
				currPiece = nextPiece;
				
				nextPiece = randomPiece();
				
				int rowsRemoved = 0;
				
				for(int row = 1; row < ROWS; row++)
				{
					if(filledRow(row))
					{
						rowsRemoved++;
						
						dropRow(row);
						row--;
					}
				}
				
				if(rowsRemoved == 4)
					Audio.play("sounds/line-removal4.wav", false);
				else if(rowsRemoved > 0)
					Audio.play("sounds/line-remove.wav", false);
			}
		}
	}
	
	public void paint(Graphics g)
	{
		g.setColor(new Color(3, 25, 42));
		
		g.fillRect(0, 0, SCENE_WIDTH, SCENE_HEIGHT);
				
		for(int row = 0; row < ROWS; row++)
		{
			for(int col = 0; col < COLS; col++)
			{
				int x = col * COL_WIDTH,
					y = row * ROW_HEIGHT;
				
				if(grid[row][col] != null)
					grid[row][col].paintComponent(g, x, y);
			}
		}
		
		currPiece.paint(g);
		
		g.setColor(Color.BLACK);

		for(int row = 0; row < ROWS; row++)
		{
			for(int col = 0; col < COLS; col++)
			{
				int x = col * COL_WIDTH,
					y = row * ROW_HEIGHT;
			
				g.drawRect(x, y, COL_WIDTH, ROW_HEIGHT);
			}
		}
	}
	
	private boolean filledRow(int row)
	{
		for(int col = 0; col < COLS; col++)
			if(grid[row][col] == null)
				return false;
		
		return true;
	}
	
	private void dropRow(int row)
	{
		for(int r = row; r > 0; r--)
			for(int col = 0; col < COLS; col++)
				grid[r][col] = grid[r - 1][col];
	}
	
	public void setDropMode(DropMode dropMode) {
		this.dropMode = dropMode;
	}
	
	public void hardDrop() 
	{
		elapsed = 0;
		
		while(!currPiece.collides())
			currPiece.moveDown();

		currPiece.moveUp();
		currPiece.split();

		Audio.play("sounds/line-drop.wav", false);
		
		currPiece = nextPiece;
			
		nextPiece = randomPiece();
			
		int rowsRemoved = 0;
		
		for(int row = 1; row < ROWS; row++)
		{
			if(filledRow(row))
			{
				rowsRemoved++;
				
				dropRow(row);
				row--;
			}
		}
		
		if(rowsRemoved == 4)
			Audio.play("sounds/line-removal4.wav", false);
		else if(rowsRemoved > 0)
			Audio.play("sounds/line-remove.wav", false);
	}
	
	public Piece getNextPiece() {
		return nextPiece;
	}

	public void moveCurrentPieceLeft() 
	{
		currPiece.moveLeft();
	
		if(currPiece.collides())
			currPiece.moveRight();
	}

	public void moveCurrentPieceRight() 
	{
		currPiece.moveRight();
		
		if(currPiece.collides())
			currPiece.moveLeft();
	}

	public void rotateCurrentPieceLeft() 
	{
		currPiece.rotateLeft();
		
		if(currPiece.collides())
			currPiece.rotateRight();		
	}

	public void rotateCurrentPieceRight() 
	{
		currPiece.rotateRight();
		
		if(currPiece.collides())
			currPiece.rotateLeft();
	}
	
	public Updater getUpdater() {
		return updater;
	}
	
	public int getScore() {
		return score;
	}
	
	public int getBestScore() {
		return bestScore;
	}
	
	public double getTime() {
		return time;
	}
	
	public UI getUI() {
		return ui;
	}
	
	private class Square
	{
		private Color color;
		
		public Square() {
			color = Color.GRAY;
		}
		
		public Square(Color color) {
			this.color = color;
		}
		
		public void paintComponent(Graphics g, int x, int y)
		{
			g.setColor(color);
			
			g.fillRect(x, y, COL_WIDTH, ROW_HEIGHT);
			
			g.setColor(Color.GRAY);
			
			g.drawRect(x + 3, y + 3, COL_WIDTH - 6, ROW_HEIGHT - 6);
		}
	}
	
	public abstract class Piece
	{
		protected int row, col;
		
		private Color color;
		
		protected int orientation;
		
		public Piece(int row, int col, Color color)
		{
			this.row = row;
			this.col = col;
			
			this.color = color;
		}
		
		public abstract int[][] indices();
		
		public void paint(Graphics g, int x, int y)
		{
			int[][] indices = indices();
			
			Square square = new Square(color);
			
			for(int i = 0; i < indices.length; i++)
			{
				int row = indices[i][1],
					col = indices[i][0];
								
				square.paintComponent(g, x + col * COL_WIDTH, y + row * ROW_HEIGHT);
			}
		}
		
		public void paint(Graphics g)
		{
			int[][] indices = indices();
					
			Square square = new Square(color);
			
			for(int i = 0; i < indices.length; i++)
			{
				int pieceRow = row + indices[i][1],
					pieceCol = col + indices[i][0];
								
				square.paintComponent(g, pieceCol * COL_WIDTH, pieceRow * ROW_HEIGHT);
			}
		}
		
		public void split()
		{
			int[][] indices = indices();

			Square square = new Square(color);
			
			for(int i = 0; i < indices.length; i++)
			{
				int pieceRow = row + indices[i][1],
					pieceCol = col + indices[i][0];

				grid[pieceRow][pieceCol] = square;
			}
		}
		
		public void rotateLeft()
		{
			orientation--;
			if(orientation < 0)
				orientation = 3;
			Audio.play("sounds/block-rotate.wav", false);
		}
		
		public void rotateRight()
		{
			orientation++;
			orientation %= 4;
			Audio.play("sounds/block-rotate.wav", false);
		}
		
		public void moveUp() { row--; }
		
		public void moveDown() { row++; }
		
		public void moveLeft() { col--; }
		
		public void moveRight() { col++; }
		
		public boolean collides()
		{
			int[][] indices = indices();
						
			for(int i = 0; i < indices.length; i++)
			{
				int pieceRow = row + indices[i][1],
					pieceCol = col + indices[i][0];
				
				if(pieceRow < 0)
					continue;
				else if(pieceCol < 0 || pieceCol >= COLS ||
						pieceRow >= ROWS || grid[pieceRow][pieceCol] != null)
					return true;
			}
			
			return false;
		}
	}
	
	private class I extends Piece
	{
		public I(int row, int col) {
			super(row, col, Color.CYAN);
		}

		@Override
		public int[][] indices() 
		{
			int[][] indices = new int[5][2];
			
			indices[0][0] = 0; indices[0][1] = 0;
			
			if(orientation == 0)
			{
				indices[1][0] = -1; indices[1][1] = 0;
				indices[2][0] = 1; indices[2][1] = 0;
				indices[3][0] = 2; indices[3][1] = 0;
			}
			else if(orientation == 3)
			{
				indices[1][0] = 0; indices[1][1] = -1;
				indices[2][0] = 0; indices[2][1] = 1;
				indices[3][0] = 0; indices[3][1] = 2;
			}
			else if(orientation == 2)
			{
				indices[1][0] = -2; indices[1][1] = 0;
				indices[2][0] = -1; indices[2][1] = 0;
				indices[3][0] = 1; indices[3][1] = 0;
			}
			else
			{
				indices[1][0] = 0; indices[1][1] = -2;
				indices[2][0] = 0; indices[2][1] = -1;
				indices[3][0] = 0; indices[3][1] = 1;
			}
			
			return indices;
		}
	}

	private class O extends Piece
	{
		public O(int row, int col) {
			super(row, col, Color.YELLOW);
		}
	
		@Override
		public int[][] indices() 
		{
			int[][] indices = new int[5][2];
			
			indices[0][0] = 0; indices[0][1] = 0;
			indices[1][0] = 1; indices[1][1] = 0;
			indices[2][0] = 0; indices[2][1] = 1;
			indices[3][0] = 1; indices[3][1] = 1;
			
			return indices;
		}
	}
	
	private class T extends Piece
	{
		public T(int row, int col) {
			super(row, col, Color.MAGENTA);
		}

		@Override
		public int[][] indices() 
		{
			int[][] indices = new int[5][2];
			
			indices[0][0] = 0; indices[0][1] = 0;
			
			if(orientation == 0)
			{
				indices[1][0] = -1; indices[1][1] = 0;
				indices[2][0] = 1; indices[2][1] = 0;
				indices[3][0] = 0; indices[3][1] = 1;
			}
			else if(orientation == 1)
			{
				indices[1][0] = 0; indices[1][1] = -1;
				indices[2][0] = 0; indices[2][1] = 1;
				indices[3][0] = 1; indices[3][1] = 0;
			}
			else if(orientation == 2)
			{
				indices[1][0] = -1; indices[1][1] = 0;
				indices[2][0] = 1; indices[2][1] = 0;
				indices[3][0] = 0; indices[3][1] = -1;
			}
			else
			{
				indices[1][0] = 0; indices[1][1] = -1;
				indices[2][0] = 0; indices[2][1] = 1;
				indices[3][0] = -1; indices[3][1] = 0;
			}
			
			return indices;
		}
	}

	private class S extends Piece
	{
		public S(int row, int col) {
			super(row, col, Color.GREEN);
		}

		@Override
		public int[][] indices() 
		{
			int[][] indices = new int[5][2];
			
			indices[0][0] = 0; indices[0][1] = 0;
			
			if(orientation == 0)
			{
				indices[1][0] = -1; indices[1][1] = 0;
				indices[2][0] = 0; indices[2][1] = 1;
				indices[3][0] = 1; indices[3][1] = 1;
			}
			else if(orientation == 1)
			{
				indices[1][0] = 0; indices[1][1] = 1;
				indices[2][0] = 1; indices[2][1] = 0;
				indices[3][0] = 1; indices[3][1] = -1;
			}
			else if(orientation == 2)
			{
				indices[1][0] = 1; indices[1][1] = 0;
				indices[2][0] = 0; indices[2][1] = -1;
				indices[3][0] = -1; indices[3][1] = -1;
			}
			else
			{
				indices[1][0] = -1; indices[1][1] = 0;
				indices[2][0] = -1; indices[2][1] = 1;
				indices[3][0] = 0; indices[3][1] = -1;
			}
			
			return indices;
		}
	}
	
	private class Z extends Piece
	{
		public Z(int row, int col) {
			super(row, col, Color.RED);
		}

		@Override
		public int[][] indices() 
		{
			int[][] indices = new int[5][2];
			
			indices[0][0] = 0; indices[0][1] = 0;
			
			if(orientation == 0)
			{
				indices[1][0] = 0; indices[1][1] = 1;
				indices[2][0] = -1; indices[2][1] = 1;
				indices[3][0] = 1; indices[3][1] = 0;
			}
			else if(orientation == 1)
			{
				indices[1][0] = 0; indices[1][1] = -1;
				indices[2][0] = 1; indices[2][1] = 0;
				indices[3][0] = 1; indices[3][1] = 1;
			}
			else if(orientation == 2)
			{
				indices[1][0] = -1; indices[1][1] = 0;
				indices[2][0] = 0; indices[2][1] = -1;
				indices[3][0] = 1; indices[3][1] = -1;
			}
			else
			{
				indices[1][0] = 0; indices[1][1] = 1;
				indices[2][0] = -1; indices[2][1] = 0;
				indices[3][0] = -1; indices[3][1] = -1;
			}
			
			return indices;
		}
	}
	
	private class J extends Piece
	{
		public J(int row, int col) {
			super(row, col, Color.BLUE);
		}

		@Override
		public int[][] indices() 
		{
			int[][] indices = new int[5][2];
			
			indices[0][0] = 0; indices[0][1] = 0;
			
			if(orientation == 0)
			{
				indices[1][0] = -1; indices[1][1] = 0;
				indices[2][0] = -1; indices[2][1] = 1;
				indices[3][0] = 1; indices[3][1] = 0;
			}
			else if(orientation == 1)
			{
				indices[1][0] = 0; indices[1][1] = -1;
				indices[2][0] = 0; indices[2][1] = 1;
				indices[3][0] = 1; indices[3][1] = 1;
			}
			else if(orientation == 2)
			{
				indices[1][0] = -1; indices[1][1] = 0;
				indices[2][0] = 1; indices[2][1] = 0;
				indices[3][0] = 1; indices[3][1] = -1;
			}
			else
			{
				indices[1][0] = 0; indices[1][1] = -1;
				indices[2][0] = 0; indices[2][1] = 1;
				indices[3][0] = -1; indices[3][1] = -1;
			}
			
			return indices;
		}
	}
	
	private class L extends Piece
	{
		public L(int row, int col) {
			super(row, col, Color.ORANGE);
		}

		@Override
		public int[][] indices() 
		{
			int[][] indices = new int[5][2];
			
			indices[0][0] = 0; indices[0][1] = 0;
			
			if(orientation == 0)
			{
				indices[1][0] = -1; indices[1][1] = 0;
				indices[2][0] = 1; indices[2][1] = 0;
				indices[3][0] = 1; indices[3][1] = 1;
			}
			else if(orientation == 1)
			{
				indices[1][0] = 0; indices[1][1] = -1;
				indices[2][0] = 0; indices[2][1] = 1;
				indices[3][0] = 1; indices[3][1] = -1;
			}
			else if(orientation == 2)
			{
				indices[1][0] = -1; indices[1][1] = 0;
				indices[2][0] = 1; indices[2][1] = 0;
				indices[3][0] = -1; indices[3][1] = -1;
			}
			else
			{
				indices[1][0] = 0; indices[1][1] = -1;
				indices[2][0] = 0; indices[2][1] = 1;
				indices[3][0] = -1; indices[3][1] = 1;
			}
			
			return indices;
		}
	}
}
