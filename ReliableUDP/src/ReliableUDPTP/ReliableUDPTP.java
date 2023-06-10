package ReliableUDPTP;

import java.net.*;
import java.io.*;

public class ReliableUDPTP {
    private static final int PACKET_SIZE = 1024;
    private static final int WINDOW_SIZE = 10;
    private static final int TIMEOUT = 2000;
    private static final int MAX_SEQUENCE_NUM = WINDOW_SIZE * 2;

    private DatagramSocket senderSocket;
    private DatagramSocket receiverSocket;
    private InetAddress receiverAddress;
    private int receiverPort;

    public ReliableUDPTP(String receiverIP, int receiverPort) throws Exception {
        this.receiverAddress = InetAddress.getByName(receiverIP);
        this.receiverPort = receiverPort;

        senderSocket = new DatagramSocket();
        receiverSocket = new DatagramSocket(receiverPort);
    }

    // Método para enviar pacotes
    private void sendPacket(Package packet) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(packet);
        byte[] sendData = outputStream.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receiverAddress, receiverPort);
        senderSocket.send(sendPacket);
    }

    // Método para receber pacotes
    private Package receivePacket() throws IOException, ClassNotFoundException {
        byte[] receiveData = new byte[PACKET_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        receiverSocket.receive(receivePacket);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(receiveData);
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        return (Package) objectInputStream.readObject();
    }

    // Método principal para enviar dados
    public void sendData(byte[] data) throws Exception {
        // Implemente aqui a lógica de controle de fluxo e controle de congestionamento
        // utilizando uma janela deslizante, números de sequência, ACKs, etc.

        // Divida os dados em pacotes e envie-os
        // Gerencie a confirmação acumulativa (ACK acumulativo) do destinatário

        // Implemente também o tratamento de timeouts para reenviar pacotes não confirmados

        // Avalie a entrega ordenada dos pacotes baseada no número de sequência

        // Lembre-se de lidar com perdas de pacotes e implementar sua lógica de controle de congestionamento

        // Aqui está apenas um exemplo simplificado sem as funcionalidades completas
        int sequenceNumber = 0;
        for (int i = 0; i < data.length; i += PACKET_SIZE) {
            int length = Math.min(PACKET_SIZE, data.length - i);
            byte[] packetData = new byte[length];
            System.arraycopy(data, i, packetData, 0, length);
            Package packet = new Package(sequenceNumber, packetData);
            sendPacket(packet);
            sequenceNumber = (sequenceNumber + 1) % MAX_SEQUENCE_NUM;
        }
    }

    // Método principal para receber dados
    public byte[] receiveData() throws Exception {
        // Implemente aqui a lógica para receber e montar os pacotes em ordem
        // Gerencie a janela deslizante, confirmando os pacotes recebidos

        // Implemente também o controle de fluxo para informar ao remetente o tamanho da janela

        // Lembre-se de lidar com perdas de pacotes e implementar sua lógica de controle de congestionamento

        // Aqui está apenas um exemplo simplificado sem as funcionalidades completas
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while (true) {
            Package packet = receivePacket();
            outputStream.write(packet.getData());
            if (packet.getSequenceNumber() == 0) {
                break;
            }
        }
        return outputStream.toByteArray();
    }
}
