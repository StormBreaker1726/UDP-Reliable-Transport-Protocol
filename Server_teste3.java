 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Random;

public class Server {
    private static final int PORT = 4444;
    private static final int HEADER_SIZE = 5;
    private static final double PACKET_LOSS_PROBABILITY = 0.002; // Probabilidade de perda de pacotes
    private static long totalDataReceived = 0; // Rastreia o tamanho total dos dados recebidos
    private static long seed = System.currentTimeMillis() / 1000;
    private static Random random = new Random(seed); // Gerador de números aleatórios

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(Server.PORT);
            byte[] receiveFileName = new byte[1024];
            DatagramPacket receiveFileNamePacket = new DatagramPacket(receiveFileName, receiveFileName.length);

            socket.receive(receiveFileNamePacket);

            byte[] data = receiveFileNamePacket.getData();
            String fileName = new String(data, 0, receiveFileNamePacket.getLength());

            System.err.println("Receiving file: `" + fileName + "`");
            File f = new File(fileName);
            FileOutputStream outStream = new FileOutputStream(f);

            receiveFile(outStream, socket);

            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void receiveFile(FileOutputStream outStream, DatagramSocket socket) throws IOException {
        int totalPacketsReceived = 0;
        int totalPacketsLost = 0;
long tempoInicial = System.currentTimeMillis();
        while (true) {
            byte[] message = new byte[1024];

            DatagramPacket receivedPacket = new DatagramPacket(message, message.length);
            try {
                socket.receive(receivedPacket);
                message = receivedPacket.getData();

                InetAddress address = receivedPacket.getAddress();
                int port = receivedPacket.getPort();

                int sequenceNumber = ((message[0] & 0xff) << 8) + (message[1] & 0xff);
                boolean reachedEof = (message[2] & 0xff) == 1;
                int contentSize = ((message[3] & 0xff) << 8) + (message[4] & 0xff);

                System.err.println("contentSize: " + contentSize);

                byte[] fileContent = new byte[contentSize];

                if (!simularPerdaPacote()) {
                    outStream.write(message, HEADER_SIZE, contentSize);
                    System.err.println("Received: Sequence number: " + sequenceNumber);
                    totalDataReceived += contentSize;
                } else {
                    System.err.println("Packet loss: Sequence number: " + sequenceNumber);
                    totalPacketsLost++;
                }

                totalPacketsReceived++;
                sendAck(sequenceNumber, socket, address, port);

                if (reachedEof) {
                    break;
                }
            } catch (SocketTimeoutException e) {
                System.err.println("Socket timeout: No packet received.");
                break;
            }
        }
        outStream.close();
long tempoFinal = System.currentTimeMillis();
        double vazaoMediaMbps = (((double) totalDataReceived * 8)) / ((tempoFinal-tempoInicial)/1000.0);
        System.out.println("Vazão média da rede: " + vazaoMediaMbps + " Mbps");

        double lossRate = (double) totalPacketsLost / (totalPacketsReceived + totalPacketsLost);
        System.out.println("Total packets received: " + totalPacketsReceived);
        System.out.println("Total packets lost: " + totalPacketsLost);
        System.out.println("Packet loss rate: " + lossRate);
    }

    private static void sendAck(int lastReceived, DatagramSocket socket, InetAddress address, int port)
            throws IOException {
        byte[] ackPacket = new byte[2];

        ackPacket[0] = (byte) (lastReceived >> 8);
        ackPacket[1] = (byte) (lastReceived);

        DatagramPacket ack = new DatagramPacket(ackPacket, ackPacket.length, address, port);
        socket.send(ack);
    }

    private static boolean simularPerdaPacote() {
        double number = random.nextDouble();
        System.out.print("Number = ");
        System.out.println(number);
        return number < PACKET_LOSS_PROBABILITY;
    }
}
