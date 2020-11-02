package edu.ufl.jjr.writingLog;

import edu.ufl.jjr.peer.Peer;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
        String dir = "/Users/jonathanmorales/Desktop/P2P-project/project/log_peer_" + Integer.toString(peerID) + ".log";

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

    public void tcpConnectiontoPeer(String peerID_1, String peerID_2){
        logger.info(time() + "Peer " + peerID_1 + " makes a connection to Peer " + peerID_2 + ".");
    }

    public void connectedFromPeer(String peerID_1, String peerID_2){
        logger.info(time() + "Peer " + peerID_1 + " is connected from Peer " + peerID_2 + ".");
    }

    public void changeNeighbors(String peer_ID, String[] neighborIDList){
        logger.info(time() + "Peer " + peer_ID + " has the preferred neighbors " + Arrays.toString(neighborIDList) + ".");
    }

    public void optimisticUnchoke(String peer_ID, String unchokedNeighborID){
        logger.info(time() + "Peer " + peer_ID + " has the optimistically unchoked neighbor "
                + unchokedNeighborID + ".");
    }

    public void unchokedByNeighbor(String peerID_1, String peerID_2){
        logger.info(time() + "Peer " + peerID_1 + " is unchoked by " + peerID_2 + ".");
    }

    public void chokedByNeighbor(String peerID_1, String peerID_2){
        logger.info(time() + "Peer " + peerID_1 + " is choked by " + peerID_2 + ".");
    }

    public void receivesHave(String peerID_1, String peerID_2, int pieceIndex){
        logger.info(time() + "Peer " + peerID_1 + " received the ‘have’ message from" + peerID_2 +
                " for the piece " + pieceIndex + ".");
    }

    public void receivesInterested(String peerID_1, String peerID_2){
        logger.info(time() + "Peer " + peerID_1 + " received the ‘interested’ message from " + peerID_2 + ".");
    }

    public void receivesUninterested(String peerID_1, String peerID_2){
        logger.info(time() + "Peer " + peerID_1 + " received the ‘not interested’ message from " + peerID_2 + ".");
    }

    public void finishedDownloadingPiece(String peerID_1, String peerID_2, int pieceIndex, int numPieces){
        logger.info(time() + "Peer " + peerID_1 + " has downloaded the piece " + pieceIndex + " from " + peerID_2
                + ". Now the number of pieces it has is " + numPieces + ".");
    }

    public void finishedDownloadComplete(String peerID_1){
        logger.info(time() + "Peer " + peerID_1 + " has downloaded the complete file.");
    }

    public String time(){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date) + ": ";
    }
}
