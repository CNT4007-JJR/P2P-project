package edu.ufl.jjr.peer;

import java.util.*;
//import java.io.FileInputStream;
//import java.io.InputStream;
import java.io.*;

public class Peer{

    //Information specific to peer, based off PeerInfo.cfg file
    public int peerID;
    public String hostName;
    public int portNumber;
    public int containsFile;
    public Hashtable<Integer, Byte> peerManager;


    //Common properties known by all peers, based off Common.cfg file
    public int numOfPreferredNeighbors;
    public int unchokingInterval;
    public int optimisticUnchokingInterval;
    public String downloadFileName;
    public int fileSize;
    public int pieceSize;


    public Peer() throws FileNotFoundException {
        readCommonConfig();
    }

    protected boolean readCommonConfig(){
        Properties prop = new Properties();
        String fileName = "C:\\Users\\joseph\\Desktop\\P2P-project\\Common.cfg";
        InputStream is = null;

        try{
            is = new FileInputStream(fileName);
        } catch (FileNotFoundException ex){

            System.out.println(ex);
            return false;
        }
        try {
            prop.load(is);
        } catch (IOException ex){
            System.out.println(ex);
            return false;
        }

        numOfPreferredNeighbors = Integer.parseInt(prop.getProperty("NumberOfPreferredNeighbors"));
        unchokingInterval = Integer.parseInt(prop.getProperty("UnchokingInterval"));
        optimisticUnchokingInterval = Integer.parseInt(prop.getProperty("OptimisticUnchokingInterval"));
        downloadFileName = prop.getProperty("FileName");
        fileSize = Integer.parseInt(prop.getProperty("FileSize"));
        pieceSize = Integer.parseInt(prop.getProperty("PieceSize"));

        return true;
    }

    public boolean peerInfoConfig(int peerID, String hostName, int portNumber, int containsFile){
        this.peerID = peerID;
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.containsFile = containsFile;
        this.peerManager = new Hashtable<Integer, Byte>();

        return true;
    }

    public void addInitialPeerConnection(int peerID, byte filePieces){
        peerManager.put(peerID, filePieces);
    }


}

