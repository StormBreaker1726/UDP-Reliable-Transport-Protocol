/*
* This file contains my own implementation of Mersenne Twister Pseudo-random Number Generator
* All the theory behind this PRNG can be found in <http://www.math.sci.hiroshima-u.ac.jp/m-mat/MT/emt.html>
* */

package MersenneTwisterPRNG;

public class MersenneTwisterPRNG
{
    private static final int N = 624;
    private static final int M = 397;
    private static final int MATRIX_A = 0x9908B0DF;
    private static final int UPPER_MASK = 0x80000000;
    private static final int LOWER_MASK = 0x7FFFFFFF;

    private int[] mt;
    private int index;

    public MersenneTwisterPRNG(long seed)
    {
        mt = new int[N];
        mt[0] = (int)seed;

        for(int i = 1; i < N; i++)
        {
            mt[i] = (1812433253 * (mt[i - 1] ^ (mt[i - 1] >> 30)) + i);
        }
    }

    private void generateNumbers() {
        for (int i = 0; i < N; i++) {
            int y = (mt[i] & UPPER_MASK) | (mt[(i + 1) % N] & LOWER_MASK);
            mt[i] = mt[(i + M) % N] ^ (y >>> 1);

            if (y % 2 != 0) {
                mt[i] ^= MATRIX_A;
            }
        }
    }

    public int nextInt() {
        if (index == 0) {
            generateNumbers();
        }

        int y = mt[index];
        y ^= (y >>> 11);
        y ^= (y << 7) & 0x9D2C5680;
        y ^= (y << 15) & 0xEFC60000;
        y ^= (y >>> 18);

        index = (index + 1) % N;
        return y;
    }

    public float nextFloat() {
        int y;

        if (index == 0) {
            generateNumbers();
        }

        y = mt[index];
        y ^= (y >>> 11);
        y ^= (y << 7) & 0x9D2C5680;
        y ^= (y << 15) & 0xEFC60000;
        y ^= (y >>> 18);

        index = (index + 1) % N;
        return (y >>> 8) / ((float) (1 << 24));
    }

    public float nextFloat(float lower, float upper) {
        if (lower >= upper) {
            throw new IllegalArgumentException("Lower bound must be less than upper bound.");
        }

        float range = upper - lower;
        return (nextFloat() * range) + lower;
    }

    public int nextInt(int lower, int upper) {
        if (lower >= upper) {
            throw new IllegalArgumentException("Lower bound must be less than upper bound.");
        }

        int range = upper - lower;
        return (int) (nextFloat() * range) + lower;
    }
}
