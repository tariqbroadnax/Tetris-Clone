package tetris;

public class Updater implements Runnable
{
	private final static int TARGET_FPS = 60;
	
	private Tetris tetris;
	
	private boolean running;
	
	private Thread thread;
	
	private int fps, ticks;
	
	public Updater(Tetris tetris)
	{
		this.tetris = tetris;
		
		thread = new Thread(this);
	}
	
	public void start() 
	{
		if(!running)
		{
			running = true;
			
			thread.start();
		}
	}
	
	public void stop()
	{
		if(running)
			running = false;
	}
	
	public void run()
	{
		long start, end = System.nanoTime(),
			 period = (long)(1.0e9 / TARGET_FPS),
			 elapsed = 0;
		
		int frames = 0;

		while(running)
		{
			start = System.nanoTime();
		
			ticks++; frames++;
			
			elapsed += (start - end);
			
			if(elapsed > 1.0e9)
			{
				fps = frames;
				elapsed = frames = 0;
			}
			
			tetris.update((start - end) / 1.0e9);
		
			end = System.nanoTime();
			
			try 
			{
				long sleep = period - (end - start);
				
				if(sleep > 0)
					Thread.sleep((long)(sleep / 1e6));
			} 
			catch (InterruptedException e) {}
		}
	}

	public int getFPS() {
		return fps;
	}

	public int getTicks() {
		return ticks;
	}
}
