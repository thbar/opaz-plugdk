
package jvst.examples.liquinth;

public interface AudioSource {
	public void set_sampling_rate( int sampling_rate );
	public int[] allocate_mix_buf( int frames );
	public void get_audio( int[] mix_buf, int length );
}
