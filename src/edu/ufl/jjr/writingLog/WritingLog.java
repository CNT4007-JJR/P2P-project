package edu.ufl.jjr.writingLog;

import edu.ufl.jjr.peer.Peer;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

// https://docs.oracle.com/javase/7/docs/api/java/util/logging/Logger.html
// https://www.codota.com/code/java/classes/java.util.logging.FileHandler
// https://github.com/CNT4007-JJR/P2P-project/blob/3-logging-class/src/edu/ufl/jjr/writingLog/WritingLog.java
// https://stackabuse.com/how-to-get-current-date-and-time-in-java/

public class WritingLog {

    private Peer peer;
    private int peerID;
    private Logger logger;
    private FileHandler fh;

    public WritingLog(Peer peer) {
        this.peer = peer;
        this.peerID = peer.peerID;
        this.logger = Logger.getLogger(Integer.toString(peerID));

        // this path needs to be abstracted, right now it is hard coded
        String dir = "log_peer_" + Integer.toString(peerID) + ".log";

        try {
            fh = new FileHandler(dir);
            this.logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // testing purposes only
//    public void test(String msg){
//        logger.info(msg + " " + time());
//    }

    public void tcpConnectiontoPeer(int peerID_1, int peerID_2){
        logger.info(time() + "Peer " + peerID_1 + " makes a connection to Peer " + peerID_2 + ".");
    }

    public void receivedRequestMessage(int peerID_1, int peerID_2, int pieceIndex){
        logger.info(time() + "Peer "+ peerID_1 + " has received the `request` message for piece "
                + pieceIndex + " from Peer "+ peerID_2 + ".");
    }

    public void sentNotInterestedMessage(int peerID_1, int peerID_2){
        logger.info(time()+ "Peer "+ peerID_1 + " sent the `not interested` message to Peer "+ peerID_2 + ".");
    }

    public void sentInterestedMessage(int peerID_1, int peerID_2){
        logger.info(time()+ "Peer "+ peerID_1 + " sent the `interested` message to Peer "+ peerID_2 + ".");
    }

    public void sentPieceMessage(int peerID_1, int peerID_2, int pieceIndex){
        logger.info(time() + "Peer "+ peerID_1 + " sent piece "+ pieceIndex+ " to Peer "+ peerID_2 + ".");
    }

    public void receivedPieceMessage(int peerID_1, int peerID_2, int pieceIndex){
        logger.info(time()+"Peer "+ peerID_1 + " received piece " + pieceIndex +" from Peer " + peerID_2 + ".");
    }

    public void receivedBitfieldFromPeer(int peerID_1, int peerID_2){
        logger.info(time() + "Peer "+ peerID_1 + " received the `bitfield` message from Peer "
                + peerID_2 + ".");
    }

    public void connectedFromPeer(int peerID_1, int peerID_2){
        logger.info(time() + "Peer " + peerID_1 + " is connected from Peer " + peerID_2 + ".");
    }

    public void changeNeighbors(int peer_ID, int[] neighborIDList){
        logger.info(time() + "Peer " + peer_ID + " has the preferred neighbors " +
                Arrays.toString(neighborIDList) + ".");
    }

    public void optimisticUnchoke(int peer_ID, int unchokedNeighborID){
        logger.info(time() + "Peer " + peer_ID + " has the optimistically unchoked neighbor "
                + unchokedNeighborID + ".");
    }

    public void unchokedByNeighbor(int peerID_1, int peerID_2){
        logger.info(time() + "Peer " + peerID_1 + " is unchoked by " + peerID_2 + ".");
    }

    public void requestedPieceFrom(int peerID_1, int peerID_2, int pieceIndex){
        logger.info(time() + "Peer "+ peerID_1 + " has requested piece " + pieceIndex + " from Peer "+ peerID_2);
    }

    public void chokedByNeighbor(int peerID_1, int peerID_2){
        logger.info(time() + "Peer " + peerID_1 + " is choked by " + peerID_2 + ".");
    }

    public void receivesHave(int peerID_1, int peerID_2, int pieceIndex){
        logger.info(time() + "Peer " + peerID_1 + " received the `have` message from" + peerID_2 +
                " for the piece " + pieceIndex + ".");
    }

    public void receivesInterested(int peerID_1, int peerID_2){
        logger.info(time() + "Peer " + peerID_1 + " received the `interested` message from " + peerID_2 + ".");
    }

    public void receivesUninterested(int peerID_1, int peerID_2){
        logger.info(time() + "Peer " + peerID_1 + " received the `not interested` message from " + peerID_2 + ".");
    }

    public void finishedDownloadingPiece(int peerID_1, int peerID_2, int pieceIndex, int numPieces){
        logger.info(time() + "Peer " + peerID_1 + " has downloaded the piece " + pieceIndex + " from " + peerID_2
                + ". Now the number of pieces it has is " + numPieces + ".");
    }

    public void sentHaveMessage(int peerID_1, int peerID_2, int pieceIndex){
        logger.info(time() + "Peer " + peerID_1 + " sent a `have` message to Peer " + peerID_2 + " with piece " + pieceIndex +".");
    }

    public void finishedDownloadComplete(int peerID_1){
        logger.info(time() + "Peer " + peerID_1 + " has downloaded the complete file.");
    }

    public void receivedHandshakeFrom(int peerID_1, int peerID_2){
        logger.info(time() + "Peer " + peerID_1 + " has received a handshake message from Peer " + peerID_2 + ".");
    }

    public void setInitialVariables(int peerID, BitSet bitfield, String hostName, int portNumber, int containsFile){
        logger.info(time() + "Peer "+ peerID + " has started its server and contains the following initial variables: ");
        logger.info("Peer bitfield: " + bitfield.toString());
        logger.info("Peer hostname: "+ hostName);
        logger.info("Peer port number: " + portNumber);
        logger.info("Peer contains file: "+ containsFile);

    }

    public String time(){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date) + ": ";
    }
}
