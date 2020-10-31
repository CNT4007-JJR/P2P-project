package edu.ufl.jjr.writingLog;

import edu.ufl.jjr.peer.Peer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

// https://docs.oracle.com/javase/7/docs/api/java/util/logging/Logger.html
// https://www.codota.com/code/java/classes/java.util.logging.FileHandler
// https://github.com/CNT4007-JJR/P2P-project/blob/3-logging-class/src/edu/ufl/jjr/writingLog/WritingLog.java

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
//        logger.info(msg);
//    }

    public void tcpConnectiontoPeer(String peerID_1, String peerID_2){
    }

    public void connectedFromPeer(String peerID_1, String peerID_2){
    }

    public void changeNeighbors(String peer_ID, String[] neighborIDList){
    }

    public void optimisticUnchoke(String peer_ID, String unchokedNeighborID){
    }

    public void unchokedByNeighbor(String peerID_1, String peerID_2){
    }

    public void chokedByNeighbor(String peerID_1, String peerID_2){
    }

    public void receivesHave(String peerID_1, String peerID_2, int pieceIndex){
    }

    public void receivesInterested(String peerID_1, String peerID_2){
    }

    public void receivesUninterested(String peerID_1, String peerID_2){
    }

    public void finishedDownloadingPiece(String peerID_1, String peerID_2, int pieceIndex, int numPieces){
    }

    public void finishedDownloadComplete(String peerID){
    }



}
