package edu.ufl.jjr.peer;

import edu.ufl.jjr.MessageCreator.MessageCreator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

public class MessageHandler implements Runnable {
    private Peer peer;
    private int remotePeerId;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    private MessageCreator creator;

    public MessageHandler(ObjectInputStream in, ObjectOutputStream out, Peer peer, Socket socket){
        this.in = in;
        this.out = out;
        this.peer = peer;
        this.socket = socket;
        creator = new MessageCreator();
    }

    public void run(){
        //String handshake = "handshake message";
        byte[] handshake = new byte[0];
        try {
            handshake = creator.handshakeMessage(peer.peerID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //send handshake message
        send(handshake);
        while(true){
            try {
                byte [] message = (byte[])in.readObject();

                if(message[4] == 5){
                    System.out.println("recieved bitfield message" + remotePeerId);

                    //Checking for bitfield values and sending interested/not interested messages
                    if(peer.bitfield.equals(peer.peerManager.get(remotePeerId))){
                        System.out.println("Sending not interested message to peer " + remotePeerId);
                        send(creator.notInterestedMessage());
                    }
                    else if(peer.bitfield.isEmpty() && !peer.peerManager.get(remotePeerId).isEmpty()){
                        System.out.println("Original peer bitfield: " +peer.bitfield);
                        System.out.println("Remote peer bitfield: " + peer.peerManager.get(remotePeerId) );

                        BitSet interestingPieces = (BitSet) peer.bitfield.clone();
                        interestingPieces.or(peer.peerManager.get(remotePeerId));

                        System.out.println("Interesting Pieces after or: " + interestingPieces);
                        peer.updateInterestingPieces(remotePeerId, interestingPieces);

                        System.out.println("Sending interested message to peer " + remotePeerId);
                        send(creator.interestedMessage());
                    }
                    else{
                        System.out.println("Original peer bitfield: " +peer.bitfield);
                        System.out.println("Remote peer bitfield: " + peer.peerManager.get(remotePeerId) );

                        BitSet interestingPieces = (BitSet) peer.bitfield.clone();
                        interestingPieces.or(peer.peerManager.get(remotePeerId));

                        System.out.println("Interesting Pieces after or: " + interestingPieces);
                        interestingPieces.xor(interestingPieces);

                        System.out.println("Interesting Pieces after xor: " + interestingPieces);
                        peer.updateInterestingPieces(remotePeerId, interestingPieces);

                        System.out.println("Sending interested message to peer " + remotePeerId);
                        send(creator.interestedMessage());
                    }

                }else{
                ByteBuffer buffer =  ByteBuffer.wrap(message);
                byte[] header = new byte[18];
                buffer.get(header,0,18);
                String handShakeString = new String(header, StandardCharsets.UTF_8);

                if(handShakeString.equals("P2PFILESHARINGPROJ")) {
                    byte[] peerId = new byte[4];
                    System.arraycopy(message, 28, peerId, 0, 4);
                    int peerIdInt = ByteBuffer.wrap(peerId).getInt();
                    System.out.println(peer.peerID + " received the handshake message from " + peerIdInt);
                    remotePeerId = peerIdInt;
                    send(creator.bitFieldMessage(peer.bitfield));
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(byte [] msg){ //will need to be changed to byte [] msg
        try{
            out.writeObject(msg);
            out.flush();
            System.out.println("Sending message: " + msg + " from Peer " + peer.peerID);
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
}
