package edu.ufl.jjr.peer;

import edu.ufl.jjr.MessageCreator.MessageCreator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;

public class MessageHandler implements Runnable {
    private Peer peer;
    private int remotePeerId;
    private ObjectInputStream in;
    private ObjectOutputStream out;
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
        peer.send(handshake, out, remotePeerId);
        while(true){
            try {
                byte [] message = (byte[])in.readObject();

                if(message[4] == 5){
                    System.out.println("Peer "+ peer.peerID +" received bitfield message from Peer " + remotePeerId);
                    System.out.println();

                    byte[] messagePayload = new byte[message.length-5];
                    System.arraycopy(message, 5, messagePayload, 0, message.length-5);

                    BitSet receivedBitfield  = BitSet.valueOf(messagePayload);

                    System.out.println("Received bitfield value: " + receivedBitfield);
                    System.out.println();

                    //Checking for equality between received bitfield and peer's bitfield, send not interested if equal
                    if(peer.bitfield.equals(receivedBitfield)){
                        System.out.println("Sending not interested message to peer " + remotePeerId);
                        System.out.println();
                        peer.send(creator.notInterestedMessage(), out, remotePeerId);
                    }
                    //Checking whether peer is empty, and received bitfield is not, send interested message if so.
                    else if(peer.bitfield.isEmpty() && !receivedBitfield.isEmpty()){
                        System.out.println("Original peer bitfield: " +peer.bitfield);
                        System.out.println("Remote peer bitfield: " + receivedBitfield );

                        BitSet interestingPieces = (BitSet) peer.bitfield.clone();
                        interestingPieces.or(receivedBitfield);

                        System.out.println("Interesting Pieces after or: " + interestingPieces);
                        peer.updateInterestingPieces(remotePeerId, interestingPieces);

                        System.out.println("Sending interested message to peer " + remotePeerId);
                        System.out.println();
                        peer.send(creator.interestedMessage(), out, remotePeerId);
                    }
                    /* Checking whether both received and peer bitfield are empty, send not interested message if so */
                    else if(peer.bitfield.isEmpty() && receivedBitfield.isEmpty()){
                        System.out.println("Original peer bitfield: " + peer.bitfield);
                        System.out.println("Remote peer bitfield: " + receivedBitfield );

                        System.out.println("Sending not interested message to peer " + remotePeerId);
                        peer.send(creator.notInterestedMessage(), out, remotePeerId);

                    }
                    //If both the peer and received bitfield contain pieces, obtain the differences between the two and send interested message
                    else{
                        System.out.println("Original peer bitfield: " +peer.bitfield);
                        System.out.println("Remote peer bitfield: " + receivedBitfield );

                        BitSet interestingPieces = (BitSet) peer.bitfield.clone();
                        interestingPieces.or(receivedBitfield);

                        System.out.println("Interesting Pieces after or: " + interestingPieces);
                        interestingPieces.xor(peer.bitfield);

                        System.out.println("Interesting Pieces after xor: " + interestingPieces);
                        peer.updateInterestingPieces(remotePeerId, interestingPieces);

                        if(interestingPieces.isEmpty()){
                            System.out.println("Sending not interested message to peer: " + remotePeerId);
                            peer.send(creator.notInterestedMessage(), out, remotePeerId);
                        }
                        else{
                            System.out.println("Sending interested message to peer: " + remotePeerId);
                            peer.send(creator.interestedMessage(), out, remotePeerId);
                        }

                    }

                }
                else if(message[4] == 0){
                    System.out.println("Received choke message from " + remotePeerId);
                    System.out.println();
                }
                else if(message[4] == 1){
                    System.out.println("Received unchoke message from " + remotePeerId);
                    //any logic needed for unchoking (reset timers etc)
                    //calculate piece we want
                    int requestPiece = peer.getRequestIndex(remotePeerId);
                    System.out.println("Requesting Piece " + requestPiece);
                    //create request message for piece we want
                    //send request message
                    peer.send(creator.requestMessage(requestPiece),out,remotePeerId);
                    System.out.println();
                }
                else if(message[4] == 2){
                    System.out.println("Received interested message from " + remotePeerId);
                    System.out.println();

                    peer.addInterestedPeer(remotePeerId);

                }
                else if(message[4] == 3){
                    System.out.println("Received not interested message from " + remotePeerId);
                    System.out.println();

                    peer.removeInterestedPeer(remotePeerId);
                }
                else if(message[4] == 4){
                    System.out.println("Received have message from " + remotePeerId);
                    System.out.println();

                    int pieceIndex = ByteBuffer.wrap(Arrays.copyOfRange(message, 5, 9)).order(ByteOrder.BIG_ENDIAN).getInt();
                    System.out.println("Peer "+ remotePeerId + " now has piece: "+ pieceIndex);

                    peer.peerManager.get(remotePeerId).updatePeerBitfield(pieceIndex);

                    BitSet updatedBitfield = peer.peerManager.get(remotePeerId).bitfield;

                    //Checking for equality between received bitfield and peer's bitfield, send not interested if equal
                    if(peer.bitfield.equals(updatedBitfield)){
                        System.out.println("Sending not interested message to peer " + remotePeerId);
                        System.out.println();
                        peer.send(creator.notInterestedMessage(), out, remotePeerId);
                    }
                    //Checking whether peer is empty, and received bitfield is not, send interested message if so.
                    else if(peer.bitfield.isEmpty() && !updatedBitfield.isEmpty()){
                        System.out.println("Original peer bitfield: " + peer.bitfield);
                        System.out.println("Remote peer bitfield: " + updatedBitfield );

                        BitSet interestingPieces = (BitSet) peer.bitfield.clone();
                        interestingPieces.or(updatedBitfield);

                        System.out.println("Interesting Pieces after or: " + interestingPieces);
                        peer.updateInterestingPieces(remotePeerId, interestingPieces);

                        System.out.println("Sending interested message to peer " + remotePeerId);
                        System.out.println();
                        peer.send(creator.interestedMessage(), out, remotePeerId);
                    }
                    /* Checking whether both received and peer bitfield are empty, send not interested message if so */
                    else if(peer.bitfield.isEmpty() && updatedBitfield.isEmpty()){
                        System.out.println("Original peer bitfield: " + peer.bitfield);
                        System.out.println("Remote peer bitfield: " + updatedBitfield );

                        System.out.println("Sending not interested message to peer " + remotePeerId);
                        peer.send(creator.notInterestedMessage(), out, remotePeerId);

                    }
                    //If both the peer and received bitfield contain pieces, obtain the differences between the two and send interested message
                    else{
                        System.out.println("Original peer bitfield: " + peer.bitfield);
                        System.out.println("Remote peer bitfield: " + updatedBitfield );

                        BitSet interestingPieces = (BitSet) peer.bitfield.clone();
                        interestingPieces.or(updatedBitfield);

                        System.out.println("Interesting Pieces after or: " + interestingPieces);
                        interestingPieces.xor(peer.bitfield);

                        System.out.println("Interesting Pieces after xor: " + interestingPieces);
                        peer.updateInterestingPieces(remotePeerId, interestingPieces);

                        if(interestingPieces.isEmpty()){
                            System.out.println("Sending not interested message to peer: " + remotePeerId);
                            peer.send(creator.notInterestedMessage(), out, remotePeerId);
                        }
                        else{
                            System.out.println("Sending interested message to peer: " + remotePeerId);
                            peer.send(creator.interestedMessage(), out, remotePeerId);
                        }

                    }

                }
                else if(message[4] == 6){
                    System.out.println("Received request message from " + remotePeerId);
                    int pieceIndex = ByteBuffer.wrap(Arrays.copyOfRange(message, 5, 9)).order(ByteOrder.BIG_ENDIAN).getInt();
                    System.out.println("Requested piece index is " + pieceIndex);
                    if(peer.unchokedPeers.contains(remotePeerId)) {
                        byte[] data = peer.file[pieceIndex].clone();
                        System.out.println("Sending the data " + new String(data));
                        peer.send(creator.pieceMessage(pieceIndex, data), out, remotePeerId);
                    }else{
                        System.out.println("This remote peer " + remotePeerId + " is choked");
                    }
                    System.out.println();
                }
                else if(message[4] == 7){
                    
                    byte[] messageLength = new byte[4];
                    int messagePayloadLength = message.length - 5;
                    byte[] pieceIndex = new byte[4];
                    byte[] piece = new byte[messagePayloadLength-4];

                    System.arraycopy(message, 5,pieceIndex,0,pieceIndex.length);
                    System.arraycopy(message, 9, piece, 0, piece.length);

                    int pieceIndexInt = ByteBuffer.wrap(pieceIndex).getInt();
                    System.out.println("Received piece message from " + remotePeerId + "for index " + pieceIndexInt);

                    //"download" file piece
                    peer.file[pieceIndexInt] = piece;
                    //set bitfield to indicate we now have this piece ( we will not request this piece)

                    peer.peerManager.get(peer.peerID).updatePeerDownloadedBytes(piece.length);

                    //Send have message?
                    peer.peerManager.forEach((k,v) ->{
                        if(k != peer.peerID){
                            try {
                                peer.send(creator.haveMessage(pieceIndexInt), out, k );
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    peer.updatePeerBitfield(pieceIndexInt);
                    if(!peer.hasFile) {
                        int requestPiece = peer.getRequestIndex(remotePeerId);
                        System.out.println("Requesting Piece after receiving piece request piece is " + requestPiece);
                        //create request message for piece we want
                        //send request message
                        peer.send(creator.requestMessage(requestPiece), out, remotePeerId);
                    }

                    System.out.println();
                }
                else{
                ByteBuffer buffer =  ByteBuffer.wrap(message);
                byte[] header = new byte[18];
                buffer.get(header,0,18);
                String handShakeString = new String(header, StandardCharsets.UTF_8);

                if(handShakeString.equals("P2PFILESHARINGPROJ")) {
                    byte[] peerId = new byte[4];
                    System.arraycopy(message, 28, peerId, 0, 4);
                    int peerIdInt = ByteBuffer.wrap(peerId).getInt();
                    System.out.println("Peer " + peer.peerID + " received the handshake message from Peer " + peerIdInt);
                    remotePeerId = peerIdInt;
                    peer.send(creator.bitFieldMessage(peer.bitfield), out, remotePeerId);
                    }
                    peer.peerManager.get(remotePeerId).out = out;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
