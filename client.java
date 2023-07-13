import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import javax.swing.JFileChooser;

public class Client {
  private static final int SERVER_PORT = 4444;
  private static final String SERVER_HOST = "localhost";
  private static final int HEADER_SIZE = 5;
  private static final int MAX_CONTENT_SIZE = 1019;
  private static final int WINDOW_SIZE = 10; // Tamanho da janela deslizante
  private static int baseSequenceNumber = 1; // Número de sequência base da janela deslizante

  private static void sendFile(DatagramSocket socket, byte[] fileByteArray, InetAddress address, int port) throws IOException {
    System.out.println("Sending file");
    int sequenceNumber = 0;
    int contentSize;
    boolean reachedEof = false;

    while (!reachedEof) {
      sequenceNumber = (sequenceNumber + 1) % 65536;

      byte[] message = new byte[1024];
      message[0] = (byte) (sequenceNumber >> 8);
      message[1] = (byte) (sequenceNumber);

      int remainingBytes = fileByteArray.length - (sequenceNumber - 1) * MAX_CONTENT_SIZE;
      contentSize = Math.min(MAX_CONTENT_SIZE, remainingBytes);

      if (remainingBytes <= MAX_CONTENT_SIZE) {
        message[2] = (byte) (1);
        reachedEof = true;
      } else {
        message[2] = (byte) (0);
      }
      message[3] = (byte) (contentSize >> 8);
      message[4] = (byte) (contentSize);

      System.err.println("contentSize: " + contentSize);

      System.arraycopy(fileByteArray, (sequenceNumber - 1) * MAX_CONTENT_SIZE, message, HEADER_SIZE, contentSize);

      DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, port);
      socket.send(sendPacket);
      System.out.println("Sent: Sequence number = " + sequenceNumber);

      if (sequenceNumber == baseSequenceNumber) {
        boolean ackReceived = false;

        while (!ackReceived) {
          byte[] ack = new byte[2];
          DatagramPacket ackPacket = new DatagramPacket(ack, ack.length);

          try {
            socket.setSoTimeout(50);
            socket.receive(ackPacket);
            int ackSequence = ((ack[0] & 0xff) << 8) + (ack[1] & 0xff);

            if (ackSequence >= baseSequenceNumber && ackSequence < baseSequenceNumber + WINDOW_SIZE) {
              baseSequenceNumber = ackSequence + 1; // Atualiza a base da janela deslizante
              ackReceived = true;
              System.out.println("Ack received: Sequence Number = " + ackSequence);
            }
          } catch (SocketTimeoutException e) {
            System.out.println("Socket timed out waiting for ack");
            break;
          }
        }
      }
    }
  }

  private static byte[] readFileToByteArray(File file) {
    byte[] bArray = new byte[(int) file.length()];

    try {
      FileInputStream fileInputStream = new FileInputStream(file);

      fileInputStream.read(bArray);
      fileInputStream.close();
    } catch (IOException ioExp) {
      ioExp.printStackTrace();
    }
    return bArray;
  }

  public static void main(String[] args) {
    System.out.println("Choosing file to send");

    try {
      JFileChooser jfc = new JFileChooser();

      jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      jfc.setMultiSelectionEnabled(false);

      int r = jfc.showOpenDialog(null);

      if (r == JFileChooser.APPROVE_OPTION) {
        File f = jfc.getSelectedFile();
        String fileName = f.getName();

        byte[] fileNameBytes = fileName.getBytes();
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(Client.SERVER_HOST);
        DatagramPacket fileStatPacket = new DatagramPacket(fileNameBytes, fileNameBytes.length, address, Client.SERVER_PORT);

        socket.send(fileStatPacket);

        byte[] fileByteArray = readFileToByteArray(f);

        sendFile(socket, fileByteArray, address, Client.SERVER_PORT);
        socket.close();
      } else {
        System.out.println("No file was selected");
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }
}
