package de.avpod;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

//sends the file to server
public class UdpClient {
    public static void main(String[] args) {
        byte[] file = readFile(args);
        DatagramSocket outSocket = initOutgoingConnection();
        List<DatagramPacket> packets = packData(file, outSocket);
        int packetNumber = 0;
        for (DatagramPacket packet : packets) {
            try {
                System.out.println("Sending packet number:" + ++packetNumber);
                outSocket.send(packet);
            } catch (IOException e) {
                throw new RuntimeException("Cannot send packet: " + packet, e);
            }
        }
    }

    private static List<DatagramPacket> packData(byte[] file, DatagramSocket socket) {
        List<DatagramPacket> result = new ArrayList<>();
        int packetDataSize = 1024;
        for (int packetNumber = 0; packetNumber <= file.length / packetDataSize; packetNumber++) {
            int msgDataSize;
            if (packetNumber != file.length / packetDataSize) {
                msgDataSize = packetDataSize;
            } else if (file.length % packetDataSize != 0) {
                msgDataSize = file.length % packetDataSize;
            } else {
                break;
            }
            byte[] packetData = new byte[msgDataSize];
            System.arraycopy(file, packetNumber * msgDataSize, packetData, 0, msgDataSize);
            UdpPacketData sendPacketData = new UdpPacketData(file.length, packetNumber, packetData);
            byte[] toSend = sendPacketData.toBytes();
            result.add(new DatagramPacket(toSend, toSend.length, socket.getRemoteSocketAddress()));
        }
        return result;
    }

    private static byte[] readFile(String[] args) {
        if (args.length < 1) {
            throw new RuntimeException("Filepath was not provided");
        }
        Path filePath = Paths.get(args[0]);
        File file = filePath.toFile();
        if (!file.exists())
            throw new RuntimeException("Check the existence of the file:" + filePath);
        try {
            return new FileInputStream(file).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static DatagramSocket initOutgoingConnection() {
        SocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 12345);
        try {
            DatagramSocket socket = new DatagramSocket(null);
            socket.connect(serverAddress);
            return socket;
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

}
