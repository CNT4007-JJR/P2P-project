import edu.ufl.jjr.client.Client;
import edu.ufl.jjr.peer.Peer;
import edu.ufl.jjr.server.Server;
import edu.ufl.jjr.writingLog.WritingLog;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;


public class peerProcess {
    public static void main(String arg[]) throws FileNotFoundException {
      
        File peerConfig = new File("PeerInfo.cfg");

        Hashtable<Integer, Peer> peers = new Hashtable<Integer, Peer>();

        Scanner scnr = new Scanner(peerConfig);

        while(scnr.hasNextLine()){
            String line = scnr.nextLine();
            String [] variables = line.split(" ");
            Peer peer = new Peer();
            peer.peerInfoConfig(Integer.parseInt(variables[0]), variables[1], Integer.parseInt(variables[2]), Integer.parseInt(variables[3]));
            peers.put(peer.peerID, peer);
        }

        scnr.close();

        int peerID = Integer.parseInt(arg[0]);

        peers.get(peerID).setPeerManager(peers);
        peers.get(peerID).readFile();

        /*Test Print Statements:
        * System.out.println("Peer Process ID: " + arg[0]);
        * System.out.println("Peer Process Host Name: " + peers.get(peerID).hostName);
        * System.out.println("Peer Process Port Number: " + peers.get(peerID).portNumber);
        * System.out.println("Peer Process Contains File: " + peers.get(peerID).containsFile);
        * System.out.println("Peer Process Number of Pieces: " + peers.get(peerID).numPieces);
        * System.out.println("Peer Process Bitfield: " + peers.get(peerID).bitfield);
        * */

        System.out.println();

        WritingLog logger = new WritingLog(peers.get(peerID));

        Server server = new Server(peers.get(peerID));
        Thread serverThread = new Thread(server);
        serverThread.start();

        //System.out.println("Started Server for Peer" + peerID);
        logger.setInitialVariables(
                peerID,
                peers.get(peerID).bitfield,
                peers.get(peerID).hostName,
                peers.get(peerID).portNumber,
                peers.get(peerID).containsFile);

        logger.setCommonVariables(
                peers.get(peerID).numOfPreferredNeighbors,
                peers.get(peerID).unchokingInterval,
                peers.get(peerID).optimisticUnchokingInterval,
                peers.get(peerID).downloadFileName,
                peers.get(peerID).fileSize,
                peers.get(peerID).pieceSize,
                peers.get(peerID).numPieces
        );

        Iterator peersIterator = peers.entrySet().iterator();

        while(peersIterator.hasNext()){
            Map.Entry peerElement = (Map.Entry)peersIterator.next();
            if((int) peerElement.getKey() < peerID){
                Client client= new Client(peers.get(peerID), (Peer)peerElement.getValue());
                client.link();
                logger.tcpConnectiontoPeer(peerID, (int)peerElement.getKey());
            }
        }
        peers.get(peerID).peerChokeTracker();
        peers.get(peerID).startOptimisticallyUnchokingPeer();

        /*Test Print Statement:
        * System.out.println("Peers List: " + peers.get(peerID).peerManager);*/


    }
}
