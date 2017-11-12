package de.avpod;

import java.nio.ByteBuffer;

/**
 * Created by apodznoev
 * date 12.11.2017.
 */
class UdpPacketData {
    private final int fileSize;
    private final int packetNumber;
    private final byte[] data;

    UdpPacketData(int fileSize, int packetNumber, byte[] data) {
        this.fileSize = fileSize;
        this.packetNumber = packetNumber;
        this.data = data;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getPacketNumber() {
        return packetNumber;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] toBytes() {
        byte[] packetData = new byte[4 + 4 + data.length];
        byte[] fileSizeInfo = ByteBuffer.allocate(4).putInt(fileSize).array();
        byte[] packetNumberInfo = ByteBuffer.allocate(4).putInt(packetNumber).array();
        System.arraycopy(fileSizeInfo, 0, packetData, 0, 4);
        System.arraycopy(packetNumberInfo, 0, packetData, 4, 4);
        System.arraycopy(data, 0, packetData, 4 + 4, data.length);
        return packetData;
    }

    public static UdpPacketData readBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        int fileSize = byteBuffer.getInt();
        int packetNumber = byteBuffer.getInt();
        byte[] data = new byte[bytes.length - 8];
        byteBuffer.get(data, 0, bytes.length - 4 - 4);
        return new UdpPacketData(fileSize, packetNumber, data);
    }
}
