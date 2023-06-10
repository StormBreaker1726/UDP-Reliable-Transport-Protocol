package ReliableUDPTP;

public class Package {
    private int sequenceNumber;
    private byte[] data;

    public Package(int sequenceNumber, byte[] data)
    {
        this.sequenceNumber = sequenceNumber;
        this.data = data;
    }

    public int getSequenceNumber()
    {
        return this.sequenceNumber;
    }

    public byte[] getData() {
        return this.data;
    }

    // Implementar aqui os getters e setters necessários
}
