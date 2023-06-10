// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.

import MersenneTwisterPRNG.MersenneTwisterPRNG;

public class Main
{
    public static void main(String[] args)
    {
        long seed = System.currentTimeMillis();
        MersenneTwisterPRNG gen = new MersenneTwisterPRNG(seed);

        int num = gen.nextInt();
        float num_float = gen.nextFloat();

        int num_du = gen.nextInt(0,85);
        float num_float_dp = gen.nextFloat(0,10);

        System.out.println(num);
        System.out.println(num_float);
        System.out.println(num_du);
        System.out.println(num_float_dp);
    }
}