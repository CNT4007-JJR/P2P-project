package edu.ufl.jjr.writingLog;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class WritingLog {

    private String peerIP;
    private Logger logger;
    private FileHandler fh;

    public WritingLog(String peerIP) {
        this.peerIP = peerIP;
        this.logger = Logger.getLogger(WritingLog.class.getName());

        // this path needs to be abstracted, right now it is hard coded
        String dir = "/Users/jonathanmorales/Desktop/P2P-project/project/log_peer_" + peerIP.toString() + ".log";
        try {
            fh = new FileHandler(dir);
            this.logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
