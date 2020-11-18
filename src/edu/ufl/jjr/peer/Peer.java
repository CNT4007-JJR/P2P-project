package edu.ufl.jjr.peer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.Properties;

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

