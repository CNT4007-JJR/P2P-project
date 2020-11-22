package edu.ufl.jjr.MessageCreator;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

public class MessageCreator {

    public MessageCreator(){
    };

    public byte[] handshakeMessage(int peerID) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );

        String handshakeString = "P2PFILESHARINGPROJ";
        outputStream.write(handshakeString.getBytes());

        byte [] zeroBits = new byte[10];
        outputStream.write(zeroBits);

        ByteBuffer peerIDBuffer = ByteBuffer.allocate(4);
        peerIDBuffer.putInt(peerID);
        byte[] peerIDByte = peerIDBuffer.array();
        outputStream.write(peerIDByte);

        outputStream.close();

        return outputStream.toByteArray();
    }

    public byte[] bitFieldMessage(BitSet bitfield) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int lengthCount = 0;

        byte[] messageLength;

        byte[] messageType = new byte[1];
        lengthCount += messageType.length;
        messageType[0] = 5;

        byte[] messagePayload = bitfield.toByteArray();
        lengthCount += messagePayload.length;

        ByteBuffer messageLengthBuffer = ByteBuffer.allocate(4);
        messageLengthBuffer.putInt(lengthCount);
        messageLength = messageLengthBuffer.array();

        outputStream.write(messageLength);
        outputStream.write(messageType);
        outputStream.write(messagePayload);

        outputStream.close();

        return outputStream.toByteArray();
    }

    public byte[] chokeMessage() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int lengthCount = 0;
        byte[] messageLength;

        byte[] messageType = new byte[1];
        lengthCount += messageType.length;
        messageType[0] = 0;

        ByteBuffer messageLengthBuffer = ByteBuffer.allocate(4);
        messageLengthBuffer.putInt(lengthCount);
        messageLength = messageLengthBuffer.array();

        outputStream.write(messageLength);
        outputStream.write(messageType);

        outputStream.close();

        return outputStream.toByteArray();
    }

    public byte[] unchokeMessage() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int lengthCount = 0;
        byte[] messageLength;

        byte[] messageType = new byte[1];
        lengthCount += messageType.length;
        messageType[0] = 1;

        ByteBuffer messageLengthBuffer = ByteBuffer.allocate(4);
        messageLengthBuffer.putInt(lengthCount);
        messageLength = messageLengthBuffer.array();

        outputStream.write(messageLength);
        outputStream.write(messageType);

        outputStream.close();

        return outputStream.toByteArray();
    }

    public byte[] interestedMessage() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int lengthCount = 0;
        byte[] messageLength;

        byte[] messageType = new byte[1];
        lengthCount += messageType.length;
        messageType[0] = 2;

        ByteBuffer messageLengthBuffer = ByteBuffer.allocate(4);
        messageLengthBuffer.putInt(lengthCount);
        messageLength = messageLengthBuffer.array();

        outputStream.write(messageLength);
        outputStream.write(messageType);

        outputStream.close();

        return outputStream.toByteArray();
    }

    public byte[] notInterestedMessage() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int lengthCount = 0;
        byte[] messageLength;

        byte[] messageType = new byte[1];
        lengthCount += messageType.length;
        messageType[0] = 3;

        ByteBuffer messageLengthBuffer = ByteBuffer.allocate(4);
        messageLengthBuffer.putInt(lengthCount);
        messageLength = messageLengthBuffer.array();

        outputStream.write(messageLength);
        outputStream.write(messageType);

        outputStream.close();

        return outputStream.toByteArray();
    }


    public static void main(String arg[]) throws IOException {
        MessageCreator messageCreator = new MessageCreator();
        byte[] message = messageCreator.handshakeMessage(1001);
        ByteBuffer buffer =  ByteBuffer.wrap(message);

        byte[] handshakeHeader = new byte[18];
        byte[] zeroBits = new byte[10];
        byte[] peerIDBytes = new byte[4];

        buffer.get(handshakeHeader, 0, handshakeHeader.length);
        buffer.get(zeroBits, 0, zeroBits.length);
        buffer.get(peerIDBytes, 0, peerIDBytes.length);

        String handShakeString = new String(handshakeHeader, StandardCharsets.UTF_8);

        System.out.println("Handshake Message");
        System.out.println("Message Size: "+message.length);
        System.out.println("Handshake Header String: " + handShakeString);
        System.out.println("Peer ID: " + ByteBuffer.wrap(peerIDBytes).getInt());
        System.out.println("Zero Bits Value: " + ByteBuffer.wrap(zeroBits).getInt());
        System.out.println();

        BitSet bitfield = new BitSet(16);
        bitfield.set(0,15,true);

        byte[] bitfieldMessage = messageCreator.bitFieldMessage(bitfield);
        ByteBuffer bitfieldBuffer =  ByteBuffer.wrap(bitfieldMessage);

        byte[] messageLength = new byte [4];
        byte[] messageType = new byte[1];
        byte[] messagePayload = new byte[bitfieldMessage.length-5];

        bitfieldBuffer.get(messageLength, 0, messageLength.length);
        bitfieldBuffer.get(messageType, 0, messageType.length);
        bitfieldBuffer.get(messagePayload, 0, messagePayload.length);

        System.out.println("Bitfield Message");
        System.out.println("Message Size: " + bitfieldMessage.length);
        System.out.println("Message Length passed from bit array: " + ByteBuffer.wrap(messageLength).getInt());
        System.out.println("Message Type: " + messageType[0]);

        StringBuilder sb = new StringBuilder();
        for (byte b : messagePayload) {
            sb.append(String.format("%02X ", b));
        }
        System.out.println("Message Payload: " +sb.toString());

    }
}
