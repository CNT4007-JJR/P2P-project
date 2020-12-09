package edu.ufl.jjr.peer;

import edu.ufl.jjr.MessageCreator.MessageCreator;
import edu.ufl.jjr.writingLog.WritingLog;
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
    private WritingLog logger;

    public MessageHandler(ObjectInputStream in, ObjectOutputStream out, Peer peer, Socket socket){
        this.in = in;
        this.out = out;
        this.peer = peer;
        this.socket = socket;
        creator = new MessageCreator();
        logger = new WritingLog(peer);
    }

    public void run(){
        byte[] handshake = new byte[0];
        try {
            handshake = creator.handshakeMessage(peer.peerID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Send initial handshake message
        peer.send(handshake, out, remotePeerId);
        while(true){
            if(peer.hasFile && peer.neighborsHaveFile()) System.exit(0);
            try {
                byte [] message = (byte[])in.readObject();

                if(message[4] == 5){
                    logger.receivedBitfieldFromPeer(peer.peerID, remotePeerId);

                    /*Test Print Statements:
                    * System.out.println("Peer "+ peer.peerID +" received bitfield message from Peer " + remotePeerId);
                    * System.out.println("Received bitfield value: " + receivedBitfield);
                    */

                    byte[] messagePayload = new byte[message.length-5];
                    System.arraycopy(message, 5, messagePayload, 0, message.length-5);

                    BitSet receivedBitfield  = BitSet.valueOf(messagePayload);

                    peer.peerManager.get(remotePeerId).bitfield = (BitSet)receivedBitfield.clone();

                    //Checking for equality between received bitfield and peer's bitfield, send not interested if equal
                    if(peer.bitfield.equals(receivedBitfield)){
                        logger.sentNotInterestedMessage(peer.peerID, remotePeerId);
                        /*Test Print Statement:
                        *  System.out.println("Sending not interested message to peer " + remotePeerId);*/

                        peer.send(creator.notInterestedMessage(), out, remotePeerId);
                    }
                    //Checking whether peer is empty, and received bitfield is not, send interested message if so.
                    else if(peer.bitfield.isEmpty() && !receivedBitfield.isEmpty()){

                        /*Test Print Statement:
                        * System.out.println("Original peer bitfield: " +peer.bitfield);
                        * System.out.println("Remote peer bitfield: " + receivedBitfield );
                        * System.out.println("Interesting Pieces after or: " + interestingPieces);
                        * System.out.println("Sending interested message to peer " + remotePeerId);*/

                        BitSet interestingPieces = (BitSet) peer.bitfield.clone();
                        interestingPieces.or(receivedBitfield);

                        peer.updateInterestingPieces(remotePeerId, interestingPieces);

                        logger.sentInterestedMessage(peer.peerID, remotePeerId);
                        peer.send(creator.interestedMessage(), out, remotePeerId);
                    }
                    /* Checking whether both received and peer bitfield are empty, send not interested message if so */
                    else if(peer.bitfield.isEmpty() && receivedBitfield.isEmpty()){

                        /*Test Print Statements:
                        * System.out.println("Original peer bitfield: " + peer.bitfield);
                        * System.out.println("Remote peer bitfield: " + receivedBitfield );
                        * System.out.println("Sending not interested message to peer " + remotePeerId); */

                        logger.sentNotInterestedMessage(peer.peerID, remotePeerId);
                        peer.send(creator.notInterestedMessage(), out, remotePeerId);

                    }
                    //If both the peer and received bitfield contain pieces, obtain the differences between the two and send interested message
                    else{
                        /*Test Print Statements:  (OR statement must be added below corresponding bit manipulation)
                         * System.out.println("Original peer bitfield: " + peer.bitfield);
                         * System.out.println("Remote peer bitfield: " + updatedBitfield );
                         * System.out.println("Interesting Pieces after or: " + interestingPieces);
                         * System.out.println("Sending interested message to peer " + remotePeerId); */

                        BitSet interestingPieces = (BitSet) peer.bitfield.clone();
                        interestingPieces.or(receivedBitfield);

                        interestingPieces.xor(peer.bitfield);

                        peer.updateInterestingPieces(remotePeerId, interestingPieces);

                        if(interestingPieces.isEmpty()){
                            /*Test Print Statement:
                            * System.out.println("Sending not interested message to peer: " + remotePeerId); */

                            logger.sentNotInterestedMessage(peer.peerID, remotePeerId);
                            peer.send(creator.notInterestedMessage(), out, remotePeerId);
                        }
                        else{
                            /*Test Print Statement:
                            * System.out.println("Sending interested message to peer: " + remotePeerId); */

                            logger.sentInterestedMessage(peer.peerID, remotePeerId);
                            peer.send(creator.interestedMessage(), out, remotePeerId);
                        }

                    }

                }
                else if(message[4] == 0){
                    /*Test Print Statement:
                     * System.out.println("Received choke message from " + remotePeerId); */

                    logger.chokedByNeighbor(peer.peerID, remotePeerId);
                }
                else if(message[4] == 1){
                    /*Test Print Statement:
                    * System.out.println("Received unchoke message from " + remotePeerId); */

                    logger.unchokedByNeighbor(peer.peerID, remotePeerId);
                    if(!peer.hasFile) {

                        //Select the next piece to request
                        int requestPiece = peer.getRequestIndex(remotePeerId);

                        /*Test Print Statements:
                         * System.out.println("Requesting Piece " + requestPiece); */

                        logger.requestedPieceFrom(peer.peerID, remotePeerId, requestPiece);

                        //Create and send a request message for the piece we want
                        peer.send(creator.requestMessage(requestPiece), out, remotePeerId);
                    }
                }
                else if(message[4] == 2){
                    /*Test Print Statements:
                     * System.out.println("Sending interested message to peer " + remotePeerId); */

                    logger.receivesInterested(peer.peerID, remotePeerId);
                    peer.addInterestedPeer(remotePeerId);

                }
                else if(message[4] == 3){
                    /*Test Print Statements:
                     * System.out.println("Sending not interested message to peer " + remotePeerId); */

                    logger.receivesUninterested(peer.peerID, remotePeerId);
                    peer.removeInterestedPeer(remotePeerId);
                }
                else if(message[4] == 4){
                    /*Test Print Statements:
                    * System.out.println("Peer "+ remotePeerId + " now has piece: "+ pieceIndex);
                    * System.out.println("Received have message from " + remotePeerId); */

                    int pieceIndex = ByteBuffer.wrap(Arrays.copyOfRange(message, 5, 9)).order(ByteOrder.BIG_ENDIAN).getInt();

                    logger.receivesHave(peer.peerID, remotePeerId, pieceIndex);

                    peer.peerManager.get(remotePeerId).updatePeerBitfield(pieceIndex);
                    BitSet updatedBitfield = peer.peerManager.get(remotePeerId).bitfield;

                    //Checking for equality between received bitfield and peer's bitfield, send not interested if equal
                    if(peer.bitfield.equals(updatedBitfield)){

                        /*Test Print Statements:
                        * System.out.println("Sending not interested message to peer " + remotePeerId); */

                        logger.sentNotInterestedMessage(peer.peerID, remotePeerId);
                        peer.send(creator.notInterestedMessage(), out, remotePeerId);
                    }
                    //Checking whether peer is empty, and received bitfield is not, send interested message if so.
                    else if(peer.bitfield.isEmpty() && !updatedBitfield.isEmpty()){

                        /*Test Print Statements:  (OR statement must be added below corresponding bit manipulation)
                        * System.out.println("Original peer bitfield: " + peer.bitfield);
                        * System.out.println("Remote peer bitfield: " + updatedBitfield );
                        * System.out.println("Interesting Pieces after or: " + interestingPieces);
                        * System.out.println("Sending interested message to peer " + remotePeerId); */

                        BitSet interestingPieces = (BitSet) peer.bitfield.clone();
                        interestingPieces.or(updatedBitfield);


                        peer.updateInterestingPieces(remotePeerId, interestingPieces);


                        logger.sentInterestedMessage(peer.peerID, remotePeerId);
                        peer.send(creator.interestedMessage(), out, remotePeerId);
                    }
                    /* Checking whether both received and peer bitfield are empty, send not interested message if so */
                    else if(peer.bitfield.isEmpty() && updatedBitfield.isEmpty()){

                        /*Test Print Statements:
                        * System.out.println("Original peer bitfield: " + peer.bitfield);
                        * System.out.println("Remote peer bitfield: " + updatedBitfield );
                        * System.out.println("Sending not interested message to peer " + remotePeerId); * */

                        logger.sentNotInterestedMessage(peer.peerID, remotePeerId);
                        peer.send(creator.notInterestedMessage(), out, remotePeerId);

                    }
                    //If both the peer and received bitfield contain pieces, obtain the differences between the two and send interested message
                    else{

                        /*Test Print Statements: (Must be added below their corresponding sections)
                        * System.out.println("Original peer bitfield: " + peer.bitfield);
                        * System.out.println("Remote peer bitfield: " + updatedBitfield );
                        * System.out.println("Interesting Pieces after or: " + interestingPieces);
                        * System.out.println("Interesting Pieces after xor: " + interestingPieces); */

                        BitSet interestingPieces = (BitSet) peer.bitfield.clone();
                        interestingPieces.or(updatedBitfield);

                        interestingPieces.xor(peer.bitfield);

                        peer.updateInterestingPieces(remotePeerId, interestingPieces);

                        if(interestingPieces.isEmpty()){

                            /*Test Print Statement:
                            * System.out.println("Sending not interested message to peer: " + remotePeerId); */

                            logger.sentNotInterestedMessage(peer.peerID, remotePeerId);
                            peer.send(creator.notInterestedMessage(), out, remotePeerId);
                        }
                        else{
                            /*Test Print Statement:
                            * System.out.println("Sending interested message to peer: " + remotePeerId); */

                            logger.sentInterestedMessage(peer.peerID, remotePeerId);
                            peer.send(creator.interestedMessage(), out, remotePeerId);
                        }

                    }

                }
                else if(message[4] == 6){
                    int pieceIndex = ByteBuffer.wrap(Arrays.copyOfRange(message, 5, 9)).order(ByteOrder.BIG_ENDIAN).getInt();
                    logger.receivedRequestMessage(peer.peerID, remotePeerId, pieceIndex);

                    /*Test Print Statements:
                    * System.out.println("Received request message from " + remotePeerId);
                    * System.out.println("Requested piece index is " + pieceIndex); */

                    if(peer.unchokedPeers.contains(remotePeerId)) {
                        byte[] data = peer.file[pieceIndex].clone();

                        /*Test Print Statement:
                        * System.out.println("Sending the data " + new String(data)); */

                        logger.sentPieceMessage(peer.peerID, remotePeerId, pieceIndex);
                        peer.send(creator.pieceMessage(pieceIndex, data), out, remotePeerId);
                    }else{
                        /*Test Print Statement:
                        * System.out.println("This remote peer " + remotePeerId + " is choked"); */
                    }
                }
                else if(message[4] == 7){
                    
                    int messagePayloadLength = message.length - 5;
                    byte[] pieceIndex = new byte[4];
                    byte[] piece = new byte[messagePayloadLength-4];

                    System.arraycopy(message, 5,pieceIndex,0,pieceIndex.length);
                    System.arraycopy(message, 9, piece, 0, piece.length);

                    int pieceIndexInt = ByteBuffer.wrap(pieceIndex).getInt();

                    /*Test Print Statement:
                    *  System.out.println("Received piece message from " + remotePeerId + "for index " + pieceIndexInt); */

                    logger.receivedPieceMessage(peer.peerID, remotePeerId, pieceIndexInt);

                    //Download the received file piece
                    peer.file[pieceIndexInt] = piece;
                    peer.incrementNumPiecesDownloaded();

                    logger.finishedDownloadingPiece(peer.peerID, remotePeerId, pieceIndexInt, peer.numPiecesDownloaded);

                    //Set bitfield to indicate we now have this piece ( we will not request this piece)
                    peer.peerManager.get(remotePeerId).updatePeerDownloadedBytes(piece.length);
                    peer.updatePeerBitfield(pieceIndexInt);

                    peer.peerManager.forEach((k,v) ->{
                        if(k != peer.peerID){
                            try {
                                logger.sentHaveMessage(peer.peerID, k, pieceIndexInt);
                                peer.send(creator.haveMessage(pieceIndexInt), out, k );
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    if(!peer.hasFile) {
                        int requestPiece = peer.getRequestIndex(remotePeerId);

                        /* Test Print Statement:
                        * System.out.println("Requesting Piece after receiving piece request piece is " + requestPiece); */

                        logger.requestedPieceFrom(peer.peerID, remotePeerId, requestPiece);

                        //Create and send request message for the piece we want
                        peer.send(creator.requestMessage(requestPiece), out, remotePeerId);
                    }else{
                        logger.finishedDownloadComplete(peer.peerID);
                        peer.saveFileToDisk();
                    }

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

                    /*Test Print Statement:
                    * System.out.println("Peer " + peer.peerID + " received the handshake message from Peer " + peerIdInt); */

                    remotePeerId = peerIdInt;
                    logger.connectedFromPeer(peer.peerID, remotePeerId);
                    logger.receivedHandshakeFrom(peer.peerID, remotePeerId);
                    peer.send(creator.bitFieldMessage(peer.bitfield), out, remotePeerId);
                    }
                    peer.peerManager.get(remotePeerId).out = out;
                }

            } catch (IOException e) {
                System.exit(0);
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
