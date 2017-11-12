package de.avpod;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;

//sends the file to server
public class UdpServer {
    public static void main(String[] args) {
        if(args.length < 1){
            throw new RuntimeException("Specify filepath where to save data");
        }

        DatagramSocket inSocket = initIncomingConnection();
        byte[] buffer = new byte[1024 + 8];
        FileAssembler assembler = new FileAssembler();
        int packetNumber = 0;
        while (assembler.building()) {
            DatagramPacket packet = new DatagramPacket(buffer, 1024 + 8);
            try {
                inSocket.receive(packet);

                System.out.println("Received packet:" + ++packetNumber);
            } catch (IOException e) {
                throw new RuntimeException("Cannot receive packet", e);
            }
            UdpPacketData packetData = UdpPacketData.readBytes(packet.getData());
            assembler.append(packetData);
        }
        assembler.assemble(args[0]);
    }


    private static DatagramSocket initIncomingConnection() {
        SocketAddress serverAddress = new InetSocketAddress("localhost", 12345);
        try {
            return new DatagramSocket(serverAddress);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private static class FileAssembler {
        private Integer fileSize;
        private byte[][] segments;
        private boolean allSegmentsReceived;

        public boolean building() {
            return !allSegmentsReceived;
        }

        public void append(UdpPacketData packetData) {
            if (fileSize == null) {
                System.out.println("Received first packet, expected file of size:" + packetData.getFileSize() + " bytes");
                fileSize = packetData.getFileSize();
//                todo last segment will cause problems if will be received first
                segments = new byte[fileSize / packetData.getData().length + (fileSize % packetData.getData().length == 0 ? 0 : 1) ][];
            }
            System.out.println("Received part #" + packetData.getPacketNumber() + " of size:" + packetData.getData().length + "bytes");
            segments[packetData.getPacketNumber()] = packetData.getData();
            for (int segmentNumber = 0; segmentNumber < segments.length; segmentNumber++) {
                byte[] dataSegment = segments[segmentNumber];
                if (dataSegment == null) {
                    System.out.println("Segment number: " + segmentNumber + " is missing");
                    return;
                }
            }
            System.out.println("All file segments were received");
            allSegmentsReceived = true;
        }

        public void assemble(String filePath) {
            try(FileOutputStream fos = new FileOutputStream(filePath)) {
                for(byte[] segment : segments){
                    fos.write(segment);
                };
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
