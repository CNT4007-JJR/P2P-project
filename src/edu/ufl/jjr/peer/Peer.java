package edu.ufl.jjr.peer;
import edu.ufl.jjr.writingLog.WritingLog;

import edu.ufl.jjr.MessageCreator.MessageCreator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

//import java.io.FileInputStream;
//import java.io.InputStream;

public class Peer{

    //Information specific to peer, based off PeerInfo.cfg file
    public int peerID;
    public String hostName;
    public int portNumber;
    public int containsFile;
    public Hashtable<Integer, Peer> peerManager;
    public ObjectOutputStream out;
    private MessageCreator creator;
    public List<Integer> interestedPeers;
    public Hashtable<Integer, BitSet> interestingPieces;
    public byte[][] file;
    public int optimisticallyUnchockedPeer;
    public int downloadedBytes;
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
    public int numPiecesDownloaded;
    public boolean hasFile;
    public WritingLog logger;


    public Peer() throws FileNotFoundException {
        this.optimisticallyUnchockedPeer = 0;
        this.unchokedPeers = new ArrayList<>();
        readCommonConfig();
    }

    protected boolean readCommonConfig() throws FileNotFoundException{
        File commonConfig = new File("Common.cfg");
        List <String> configVariables = new ArrayList<>();

        Scanner commonScan = new Scanner(commonConfig);

        while(commonScan.hasNextLine()){
            String line = commonScan.nextLine();
            String [] variables = line.split(" ");
            configVariables.add(variables[1]);
        }

        commonScan.close();

        numOfPreferredNeighbors = Integer.parseInt(configVariables.get(0));
        unchokingInterval = Integer.parseInt(configVariables.get(1)) * 1000;
        optimisticUnchokingInterval = Integer.parseInt(configVariables.get(2)) * 1000;
        downloadFileName = configVariables.get(3);
        fileSize = Integer.parseInt(configVariables.get(4));
        pieceSize = Integer.parseInt(configVariables.get(5));
        numPieces = (int) Math.ceil((double)fileSize/pieceSize);

        return true;
    }

    public boolean peerInfoConfig(int peerID, String hostName, int portNumber, int containsFile){
        this.peerID = peerID;
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.containsFile = containsFile;
        this.peerManager = new Hashtable<Integer, Peer>();
        this.interestingPieces = new Hashtable<Integer, BitSet>();
        this.interestedPeers = new ArrayList<>();
        this.bitfield = new BitSet(numPieces);
        this.creator = new MessageCreator();
        this.downloadedBytes = 0;
        this.numPiecesDownloaded = 0;

        if(containsFile == 1){
            this.bitfield.set(0,numPieces, true);
            this.numPiecesDownloaded = numPieces;
            this.hasFile = true;
        }else{
            this.bitfield.clear(0,numPieces);
            this.hasFile = false;
        }

        file = new byte[numPieces][];

        return true;
    }

    public int getRequestIndex(int remotePeerId){
        BitSet validPieces =  (BitSet)bitfield.clone();
        validPieces.flip(0,numPieces);
        validPieces.and(interestingPieces.get(remotePeerId));

        System.out.println(validPieces);

        List<Integer> validPieceIndex = new ArrayList<>();

        for(int i = 0; i<validPieces.length(); i++){
            if(validPieces.get(i) == true) validPieceIndex.add(i);
        }

        Collections.shuffle(validPieceIndex, new Random());

        return validPieceIndex.get(0);

    }

    public void setOut(ObjectOutputStream out){
        this.out = out;
    }

    public void incrementNumPiecesDownloaded(){
        this.numPiecesDownloaded += 1;
    }

    public void addInterestedPeer(int peerID) {
        if(interestedPeers.contains(peerID)){
            //System.out.println("Peer labeled as interested.");
        }
        else{
            interestedPeers.add(peerID);
        }
    }
    public void removeInterestedPeer(int peerID) {
        if(interestedPeers.contains(peerID)){
            interestedPeers.remove(new Integer(peerID));
        }
        else{
            //System.out.println("Peer has been labeled as not interested.");
        }
    }

    public void setPeerManager(Hashtable<Integer, Peer> peerManager){
        this.peerManager = peerManager;
        this.logger = new WritingLog(peerManager.get(peerID));

    }

    public void addUnchokedPeer(int peerID){
        unchokedPeers.add(peerID);
    }

    public void removeUnchokedPeer(int peerID){
        if(unchokedPeers.contains(peerID)){
            unchokedPeers.remove(new Integer(peerID));
        }
    }

    public List<Integer> getInterestedPeers(){
        return interestedPeers;
    }

    public int getDownloadedBytes() {
        return downloadedBytes;
    }

    public void updatePeerDownloadedBytes(int bytes){
        this.downloadedBytes += bytes;
    }

    public void updatePeerBitfield(int index) {
        this.bitfield.set(index, true);
        if(this.bitfield.nextClearBit(0) == numPieces){
            hasFile = true;
        }
    }

    public boolean neighborsHaveFile(){
        for(int id : peerManager.keySet()){
            if(peerManager.get(id).bitfield.nextClearBit(0) != numPieces){
                return false;
            }
        }

        return true;
    }

    public void resetPeerDownloadedBytes() { this.downloadedBytes = 0;}

    public void peerChokeTracker() {
        Peer peer = this;
        final Instant[] start = {Instant.now()};

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        preferredPeersSelection(interestedPeers, start[0]);
                        start[0] = Instant.now();
                        Thread.sleep(unchokingInterval);
                    } catch (InterruptedException | IOException e) {
                        System.out.println("Thread to unchoke neighbor interrupted while trying to sleep.");
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    //Helper function for preferredPeerSelection, returns random peer id based on download rate value passed in
    public static <K, V> K getKey(HashMap<K, V> map, V value) {
        return map.keySet()
                .stream()
                .filter(key -> value.equals(map.get(key)))
                .findAny().get();
    }

    //Method used to select the new set of preferred peers
    public void preferredPeersSelection(List <Integer> interestedPeers, Instant startTime) throws IOException {
        HashMap<Integer, Double> candidatePeers = new HashMap<Integer, Double>();

        int[] preferredNeighbors = new int[numOfPreferredNeighbors];
        List<Integer> peersToChoke = new ArrayList<>();

        Instant finish = Instant.now();

        //For each peer (neighbor) in peerManager, except for itself, calculate the download rate
        peerManager.forEach((id,peerValues) -> {
            if(id != peerID){
                int timeElapsed = Duration.between(startTime, finish).getNano();
                double downloadRate = ((double) peerValues.getDownloadedBytes()/timeElapsed);
                candidatePeers.put(id, downloadRate);
            }
        });

        resetPeerDownloadedBytes();

        //Create a list of all download rates, sort in ascending order
        Collection<Double> values = candidatePeers.values();
        ArrayList<Double> listOfValues = new ArrayList<Double>(values);
        Collections.sort(listOfValues);

        if(interestedPeers.size() != 0){
            //If the number of interested peers is less than the preferred neighbor limit, just select all interested peers
            if(interestedPeers.size() <= preferredNeighbors.length){
                //System.out.println("All interested peers fit within preferredNeighbors requirement!");
                //System.out.println("Size of interested peers: "+ interestedPeers.size());

                for(int i = 0; i < interestedPeers.size(); i++){
                    preferredNeighbors[i] = interestedPeers.get(i);
                    //System.out.println("Peer: "+ preferredNeighbors[i] +" selected as preferred neighbor!");
                }

                logger.changeNeighbors(peerID, preferredNeighbors);

                for(int j = 0; j < preferredNeighbors.length; j++){

                    //Check whether the preferredNeighbor ID is part of the unchoked peers already, add if not
                    if(!unchokedPeers.contains(preferredNeighbors[j]) && preferredNeighbors[j] != 0){
                        addUnchokedPeer(preferredNeighbors[j]);

                        //Send unchoke message
                        send(creator.unchokeMessage(), peerManager.get(preferredNeighbors[j]).out, preferredNeighbors[j]);
                    }
                }

                //Remove any peer from the unchokedPeers list if they are not part of the preferred peers array, send choke message
                for (int peer: unchokedPeers) {
                    //System.out.println("Unchoked peer: " + peer);
                    boolean included = false;
                    for (int neighbor: preferredNeighbors) {
                        if (neighbor == peer) {
                            included = true;
                            break;
                        }
                    }

                    //System.out.println("Peer inclusion: " + included);
                    if(!included && peer!=0){
                        //System.out.println("Peer "+ peer + " has been choked!");
                        
                        //Send choke message to the removed peer
                        send(creator.chokeMessage(), peerManager.get(peer).out, peer);
                        peersToChoke.add(peer);
                    }
                }

                for (int peer: peersToChoke) {
                    removeUnchokedPeer(peer);
                }

                System.out.println();

            }
            else if(containsFile == 1){
                //System.out.println("Peer contains full file, randomly select peers.");
                //System.out.println();
                //Randomly select preferred peers based on those that are interested
                Collections.shuffle(interestedPeers, new Random());

                //System.out.println();
                for(int i = 0; i < preferredNeighbors.length; i++){
                    preferredNeighbors[i] = interestedPeers.get(i);
                    //System.out.println("Peer: "+ preferredNeighbors[i] +" selected as preferred neighbor!");
                }

                logger.changeNeighbors(peerID, preferredNeighbors);

                for(int j = 0; j < preferredNeighbors.length; j++){

                    //Check whether the preferredNeighbor ID is part of the unchoked peers already, add if not
                    if(!unchokedPeers.contains(preferredNeighbors[j]) && preferredNeighbors[j] != 0){
                        addUnchokedPeer(preferredNeighbors[j]);

                        //Send unchoke message
                        send(creator.unchokeMessage(), peerManager.get(preferredNeighbors[j]).out, preferredNeighbors[j]);
                    }
                }

                //Remove any peer from the unchokedPeers list if they are not part of the preferred peers array, send choke message
                for (int peer: unchokedPeers) {
                    //System.out.println(unchokedPeers.size());
                    boolean included = false;
                    for (int neighbor: preferredNeighbors) {
                        if (neighbor == peer) {
                            included = true;
                            break;
                        }
                    }

                    if(!included && peer!=0){
                        //System.out.println("Peer "+ peer + " has been choked!");

                        //Send choke message to the removed peer
                        send(creator.chokeMessage(), peerManager.get(peer).out, peer);
                        peersToChoke.add(peer);
                    }
                }

                for (int peer: peersToChoke) {
                    removeUnchokedPeer(peer);
                }

                //System.out.println();

            } else  {
                for(int i = 0; i < preferredNeighbors.length; i++){
                    if(interestedPeers.contains(getKey(candidatePeers, listOfValues.get(listOfValues.size()-1)))){
                        System.out.println("Max Download Rate Value: "+ listOfValues.get(listOfValues.size()-1));
                        preferredNeighbors[i] = getKey(candidatePeers, listOfValues.get(listOfValues.size()-1));
                        System.out.println("Peer ID that matches value: " + preferredNeighbors[i]);
                        System.out.println("Peer: "+ preferredNeighbors[i] +" selected as preferred neighbor!");
                    }
                    listOfValues.remove(listOfValues.size()-1);
                }

                logger.changeNeighbors(peerID, preferredNeighbors);

                //Check whether the preferredNeighbor ID is part of the unchoked peers already, add if not
                for(int j = 0; j < preferredNeighbors.length; j++){
                    if(!unchokedPeers.contains(preferredNeighbors[j]) && preferredNeighbors[j] != 0){
                        addUnchokedPeer(preferredNeighbors[j]);

                        //Send unchoke message
                        send(creator.unchokeMessage(),  peerManager.get(preferredNeighbors[j]).out, preferredNeighbors[j]);
                    }
                }

                //Remove any peer from the unchokedPeers list if they are not part of the preferred peers array, send choke message
                for (int peer: unchokedPeers) {
                    boolean included = false;
                    for (int neighbor: preferredNeighbors) {
                        if (neighbor == peer) {
                            included = true;
                            break;
                        }
                    }

                    if(!included && peer!=0){
                        //System.out.println("Peer "+ peer + " has been choked!");

                        //Send choke message to the removed peer
                        send(creator.chokeMessage(), peerManager.get(peer).out, peer);
                        peersToChoke.add(peer);
                    }
                }

                for (int peer: peersToChoke) {
                    removeUnchokedPeer(peer);
                }

            }
        }

    }

    public void startOptimisticallyUnchokingPeer() {
        Peer peer = this;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                //We are gonna have to check when all the peers have completed their download to stop this thread
                while(true) {
                    try {
                        peer.optimisticallyUnchokePeer(peer.getInterestedPeers());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

    private void optimisticallyUnchokePeer(List<Integer> InterestedPeers) throws IOException {
        List<Integer> candidatePeers = new ArrayList<>();
        for(int interestedPeerId : InterestedPeers) {
            if(!unchokedPeers.contains(interestedPeerId)) {
                candidatePeers.add(interestedPeerId);
            }
        }
        if(!candidatePeers.isEmpty()) {
            Collections.shuffle(candidatePeers, new Random());
            int optimisticallyUnchokedPeerId = candidatePeers.get(0);
            logger.optimisticUnchoke(peerID, optimisticallyUnchokedPeerId);
            send(creator.unchokeMessage(), peerManager.get(optimisticallyUnchokedPeerId).out, optimisticallyUnchokedPeerId);
            this.optimisticallyUnchockedPeer = optimisticallyUnchokedPeerId;
        }
    }

    public void updateInterestingPieces(int peerID, BitSet pieces) {

        if(interestingPieces.containsKey(peerID)){
            if(pieces.isEmpty()){
                interestingPieces.remove(peerID);
            }
            else{
                interestingPieces.replace(peerID, interestingPieces.get(peerID), pieces);
            }
        }
        else{
            interestingPieces.put(peerID, pieces);
        }
    }

    public void readFile(){
        if(containsFile == 1) {
            try {
                byte[] allBytes = Files.readAllBytes(Paths.get("./peer_"+peerID+"/"+downloadFileName));
                for (int i = 0, j = 0; i < allBytes.length; i += pieceSize, j++) {
                    byte bytes[] = Arrays.copyOfRange(allBytes, i, i + pieceSize);
                    file[j] = bytes;
                }
            } catch (IOException ioException) {
                System.out.println("Exception while reading file");
                ioException.printStackTrace();
            }
        }
        //System.out.println("TheFile.dat byte array: " + Arrays.deepToString(file));
    }

    public void send(byte [] msg, ObjectOutputStream outputStream, int remotePeerId){
        try{
            outputStream.writeObject(msg);
            outputStream.flush();
            if(remotePeerId == 0){
                //System.out.println("Sending initial handshake message: " + msg + " from Peer " + peerID);
            }
            else{
                //System.out.println("Sending message: " + msg + " from Peer " + peerID +" to Peer " + remotePeerId);
            }
            System.out.println();
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }

    public void saveFileToDisk() {
        FileOutputStream fileOutputStream = null;
        try {
            File dir = new File("./peer_"+ peerID);
            dir.mkdirs();
            File fileLocation = new File(dir, downloadFileName);
            fileLocation.createNewFile();
            fileOutputStream = new FileOutputStream(fileLocation);

            for(int i = 0; i<numPieces; i++){
                fileOutputStream.write(file[i]);
            }
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("FileNotFoundException while saving downloaded file to disk.");
            fileNotFoundException.printStackTrace();
        } catch (IOException ioException) {
            System.out.println("IOException while writing pieces to file.");
            ioException.printStackTrace();
        } finally {
            if(fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch(IOException ioException) {
                    System.out.println("IOException while closing file output stream");
                    ioException.printStackTrace();
                }
            }
        }
    }
}

