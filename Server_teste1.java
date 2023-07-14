import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Server {
  private static final int PORT = 4444;
  private static final int HEADER_SIZE = 5;
  private static final int WINDOW_SIZE = 10; // Tamanho da janela deslizante
  private static int expectedSequenceNumber = 1; // Número de sequência esperado
  private static boolean reachedEof = false; // Flag para indicar o final do arquivo

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
    } catch (Exception ex){
      ex.printStackTrace();
      System.exit(1);
    }
  }

  private static void receiveFile(FileOutputStream outStream, DatagramSocket socket) throws IOException {
    while (!reachedEof) {
      byte[] message = new byte[1024];

      DatagramPacket receivedPacket = new DatagramPacket(message, message.length);
      socket.receive(receivedPacket);
      message = receivedPacket.getData();

      InetAddress address = receivedPacket.getAddress();
      int port = receivedPacket.getPort();

      int sequenceNumber = ((message[0] & 0xff) << 8) + (message[1] & 0xff);
      reachedEof = (message[2] & 0xff) == 1;
      int contentSize = ((message[3] & 0xff) << 8) + (message[4] & 0xff);

      System.err.println("contentSize: " + contentSize);

      byte[] fileContent = new byte[contentSize];

      if (sequenceNumber >= expectedSequenceNumber && sequenceNumber < expectedSequenceNumber + WINDOW_SIZE) {
        if (sequenceNumber == expectedSequenceNumber) {
          outStream.write(message, HEADER_SIZE, contentSize);
          System.err.println("Received: Sequence number: " + sequenceNumber);

          expectedSequenceNumber++;

          // Verifica se há pacotes subsequentes disponíveis na janela deslizante
          while (expectedSequenceNumber <= sequenceNumber + WINDOW_SIZE) {
            byte[] nextPacket = new byte[1024];
            DatagramPacket nextPacketPacket = new DatagramPacket(nextPacket, nextPacket.length);
            socket.receive(nextPacketPacket);
            nextPacket = nextPacketPacket.getData();

            int nextSequenceNumber = ((nextPacket[0] & 0xff) << 8) + (nextPacket[1] & 0xff);
            int nextContentSize = ((nextPacket[3] & 0xff) << 8) + (nextPacket[4] & 0xff);

            if (nextSequenceNumber == expectedSequenceNumber && (nextPacket[2] & 0xff) == 1) {
              outStream.write(nextPacket, HEADER_SIZE, nextContentSize);
              System.err.println("Received: Sequence number: " + nextSequenceNumber);
              expectedSequenceNumber++;
              reachedEof = true; // Define reachedEof como true se o próximo pacote for o último
            } else {
              break;
            }
          }
        }
      }

      sendAck(expectedSequenceNumber - 1, socket, address, port);
    }
    outStream.close();
  }

  private static void sendAck(int lastReceived, DatagramSocket socket, InetAddress address, int port) throws IOException {
    byte[] ackPacket = new byte[2];

    ackPacket[0] = (byte) (lastReceived >> 8);
    ackPacket[1] = (byte) (lastReceived);

    DatagramPacket ack = new DatagramPacket(ackPacket, ackPacket.length, address, port);
    socket.send(ack);
  }
}
