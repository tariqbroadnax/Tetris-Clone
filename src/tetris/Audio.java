package tetris;
import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Audio 
{
	private static ExecutorService threadPool = Executors.newCachedThreadPool();
	
	public static void play(String url, boolean loop)
	{
		threadPool.execute(new AudioThread(url, loop));
	}
	
	private static class AudioThread implements Runnable
	{
		private String url;
		
		private boolean loop;
		
		public AudioThread(String url, boolean loop)
		{
			this.url = url;
			this.loop = loop;
		}
		
		public void run()
		{
			 try {
		         // Open an audio input stream.
		         URL url = new File(this.url).toURI().toURL();
				         
		         AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
		         // Get a sound clip resource.
		         Clip clip = AudioSystem.getClip();
		         // Open audio clip and load samples from the audio input stream.
		         clip.open(audioIn);
		         
		         if(loop)
		        	 clip.loop(Clip.LOOP_CONTINUOUSLY);
		         
		         clip.start();
			 }
			 catch(Exception e) {
				 e.printStackTrace();
			 }
		}
	}
}
