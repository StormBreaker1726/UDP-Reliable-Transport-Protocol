// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.

import MersenneTwisterPRNG.MersenneTwisterPRNG;
import ReliableUDPTP.ReliableUDPTP;

public class Main
{
    public static void TesteRLDBUDPTP()
    {
        try {
            ReliableUDPTP client = new ReliableUDPTP("127.0.0.1", 1243);
            ReliableUDPTP server = new ReliableUDPTP("127.0.0.1", 1243);

            // Exemplo de envio de dados
            byte[] data = "Exemplo de dados para envio.".getBytes();
            client.sendData(data);

            // Exemplo de recebimento de dados
            byte[] receivedData = server.receiveData();
            System.out.println(new String(receivedData));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
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

        TesteRLDBUDPTP();
    }
}