
package jvst.examples.liquinth;

import javax.sound.sampled.*;

/*
	BUF_FRAMES determines the latency.
	Default value of 1024 gives 21.3ms at 48000hz.
*/
class Player implements Runnable {
	public static final int SAMPLING_RATE = 48000;

	private static final int BUF_FRAMES = 1024;
	private static final int BUF_BYTES = BUF_FRAMES * 2;

	private final AudioSource audio_source;
	private final AudioFormat audio_format;
	private final SourceDataLine.Info line_info;
	private Mixer audio_mixer;

	private boolean play, running;

	public Player( AudioSource source ) {
		audio_source = source;
		audio_format = new AudioFormat( SAMPLING_RATE, 16, 1, true, false );
		line_info = new DataLine.Info( SourceDataLine.class, audio_format, BUF_BYTES );
	}

	public void set_mixer( Mixer mixer ) {
		audio_mixer = mixer;
	}		

	public boolean is_running() {
		return running;
	}

	public void run() {
		int mix_idx, out_idx, out;
		int[] mix_buf;
		byte[] out_buf;
		SourceDataLine audio_line;
		if( play ) {
			stop();
		}
		play = true;
		mix_buf = audio_source.allocate_mix_buf( BUF_FRAMES );
		out_buf = new byte[ BUF_BYTES ];
		try {
			audio_line = ( SourceDataLine ) audio_mixer.getLine( line_info );
			audio_line.open( audio_format, BUF_BYTES );
		} catch( Exception e ) {
			System.out.println( "Player.run(): Unable to open audio output!" );
			return;
		}
		audio_line.start();
		running = true;
		while( play ) {
			out_idx = 0;
			audio_source.get_audio( mix_buf, BUF_FRAMES );
			for( mix_idx = 0; mix_idx < BUF_FRAMES; mix_idx++ ) {
				out = mix_buf[ mix_idx ];
				out_buf[ out_idx     ] = ( byte ) ( out & 0xFF );
				out_buf[ out_idx + 1 ] = ( byte ) ( out >> 8 );
				out_idx += 2;
			}
			audio_line.write( out_buf, 0, BUF_BYTES );
		}
		audio_line.drain();
		audio_line.close();
		running = false;
	}

	public void stop() {
		while( running ) {
			play = false;
			try {
				Thread.sleep( 20 );
			} catch( InterruptedException ignored) {
			}
		}
	}
}
