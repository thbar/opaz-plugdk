
//factor out computation-intensive stuff to java
public class HybridGainTools {
    private HybridGainTools() {}

    public static void applyGain(float[] input, float[] output, int sampleFrames, float gain) {
        for (int i = 0x0; i < sampleFrames; i++) {
            output[i] = gain * input[i];
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
