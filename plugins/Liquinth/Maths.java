
package jvst.examples.liquinth;

/*
	All these numbers can become a bit confusing -
	... so we use Maths to make sense of it all.
*/
class Maths {
	public static final int FP_SHIFT = 15;
	public static final int FP_ONE = 1 << FP_SHIFT;
	public static final int FP_TWO = 2 << FP_SHIFT;
	public static final int FP_MASK = FP_ONE - 1;

	private static final int TABLE_ACCURACY = 4;

	private static final int[] log2_table = {
		    0,  2866,  5568,  8124, 10549, 12855, 15055, 17156, 19168,
		21098, 22952, 24736, 26455, 28114, 29717, 31267, 32768, 32768
	};

	private static final int[] exp2_table = {
		32768, 34219, 35734, 37316, 38968, 40693, 42495, 44376, 46341,
		48393, 50535, 52773, 55109, 57549, 60097, 62757, 65536, 65536
	};

	private static final int[] sine_table = {
		    0,  6393, 12540, 18205, 23170, 27246, 30274, 32138, 32768,
		32138, 30274, 27246, 23170, 18205, 12540,  6393,     0,     0
	};

	/*
		Calculate base-2 log of x (fixed-point).
	*/
	public static int log2( int x ) {
		int shift;
		/* Scale x to range 1.0 <= x < 2.0 */
		shift = 0;
		while( x < FP_ONE ) {
			x <<= 1;
			shift--;
		}
		while( x >= FP_TWO ) {
			x >>= 1;
			shift++;
		}
		return ( FP_ONE * shift ) + interpolate_table( log2_table, x - FP_ONE );
	}

	/*
		Raise 2 to the power of x (fixed-point).
	*/
	public static int exp2( int x ) {
		int y;
		y = interpolate_table( exp2_table, x & FP_MASK ) << FP_SHIFT;
		return y >> FP_SHIFT - ( x >> FP_SHIFT );
	}

	/*
		Calculate sin( x * PI ), fixed point x.
	*/
	public static int sine( int x ) {
		int y;
		y = interpolate_table( sine_table, x & FP_MASK );
		if( ( x & FP_ONE ) != 0 ) {
			y = -y;
		}
		return y;
	}

	/*
		Convert a linear scale from 0 -> FP_ONE
		to an exponential scale from FP_ONE/2^n -> FP_ONE,
		which doubles every FP_ONE/n.
	*/
	public static int exp_scale( int x, int n ) {
		return exp2( x * n ) >> n;
	}

	private static int interpolate_table( int[] table, int x ) {
		int tab_idx, c, m;
		tab_idx = x >> FP_SHIFT - TABLE_ACCURACY;
		c = table[ tab_idx ];
		m = table[ tab_idx + 1 ] - c;
		m = m * ( x & ( FP_MASK >> TABLE_ACCURACY ) );
		return c + ( m >> FP_SHIFT - TABLE_ACCURACY );
	}
}

