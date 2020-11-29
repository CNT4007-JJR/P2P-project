package edu.ufl.jjr.peer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

//import java.io.FileInputStream;
//import java.io.InputStream;

public class Peer{

    //Information specific to peer, based off PeerInfo.cfg file
    public int peerID;
    public String hostName;
    public int portNumber;
    public int containsFile;
    public Hashtable<Integer, BitSet> peerManager;
    public byte[][] file;
    public int optimisticallyUnchockedPeer;
    public List<Integer> unchokedPeers;


    //Common properties known by all peers, based off Common.cfg file
    public int numOfPreferredNeighbors;
    public int unchokingInterval;
    public int optimisticUnchokingInterval;
    public String downloadFileName;
    public int fileSize;
    public int pieceSize;
    public int numPieces;
    public BitSet bitfield;


    public Peer() throws FileNotFoundException {
        this.optimisticallyUnchockedPeer = 0;
        this.unchokedPeers = new ArrayList<>();
        readCommonConfig();
    }

    protected boolean readCommonConfig(){
        Properties prop = new Properties();
      
        String fileName = "Common.cfg";

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
        numPieces = (int) Math.ceil(fileSize/pieceSize);

        return true;
    }

    public boolean peerInfoConfig(int peerID, String hostName, int portNumber, int containsFile){
        this.peerID = peerID;
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.containsFile = containsFile;
        this.peerManager = new Hashtable<Integer, BitSet>();
        this.bitfield = new BitSet(numPieces-1);

        if(containsFile == 1){
            this.bitfield.set(0,numPieces, true);
        }

        return true;
    }

    public void startOptimisticallyUnchokingPeer() {
        Peer peer = this;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    peer.optimisticallyUnchokePeer(peer.getInterestedPeers());
                    try {
                        Thread.sleep(optimisticUnchokingInterval);
                    } catch (InterruptedException interruptedException) {
                        System.out.println("Thread to optimistically unchoke neighbor interrupted while trying to sleep.");
                        interruptedException.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    private void optimisticallyUnchokePeer(List<Integer> IntersetedPeers) {
        List<Integer> candidatePeers = new ArrayList<>();
        for(int interestedPeerId : interestedPeers) { //interested Peers is List<integer> / Array<integer> of interested peers
            if(unchokedPeers.contains(interestedPeerId)) {
                candidatePeers.add(interestedPeerId);
            }
        }
        if(!candidatePeers.isEmpty()) {
            Collections.shuffle(candidatePeers);
            int optimisticallyUnchokedPeerId = candidatePeers.get(0);
            byte[] message = UnchokeMessage.getMessage();
            send(mapWithwhatweneedtoSend.get(optimisticallyUnchokedPeerId).outputStream, message); //do we need to move send method to peer and pass in required variables for sending have map that stores said variables for each peer
            this.optimisticallyUnchockedPeer = optimisticallyUnchokedPeerId;
        }
    }

    public void addInitialPeerConnection(int peerID, BitSet filePieces){
        peerManager.put(peerID, filePieces);
    }

    public void readFile(){
        file = new byte[numPieces+1][];
        if(containsFile == 1) {
            try {
                byte[] allBytes = Files.readAllBytes(Paths.get(downloadFileName));
                for (int i = 0, j = 0; i < allBytes.length; i += pieceSize, j++) {
                    byte bytes[] = Arrays.copyOfRange(allBytes, i, i + pieceSize);
                    file[j] = bytes;
                }
            } catch (IOException ioException) {
                System.out.println("Exception while reading file");
                ioException.printStackTrace();
            }
        }
        System.out.println("TheFile.dat byte array: " + Arrays.deepToString(file));
    }

}

