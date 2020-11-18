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

        System.out.println("Peer Process ID: " + arg[0]);
        int peerID = Integer.parseInt(arg[0]);

        peers.get(peerID).readFile();

        System.out.println("Peer Process Host Name: " + peers.get(peerID).hostName);
        System.out.println("Peer Process Port Number: " + peers.get(peerID).portNumber);
        System.out.println("Peer Process Contains File: " + peers.get(peerID).containsFile);
        System.out.println("Peer Process Number of Pieces: " + peers.get(peerID).numPieces);
        System.out.println("Peer Process Bitfield: " + peers.get(peerID).bitfield);

        WritingLog logger = new WritingLog(peers.get(peerID));

        Server server = new Server(peers.get(peerID));
        Thread serverThread = new Thread(server);
        serverThread.start();

        System.out.println("Started Server for Peer" + peerID);

        Iterator peersIterator = peers.entrySet().iterator();

        while(peersIterator.hasNext()){
            Map.Entry peerElement = (Map.Entry)peersIterator.next();
            if((int) peerElement.getKey() < peerID){
                peers.get(peerID).addInitialPeerConnection((int)(peerElement.getKey()), peers.get((int)(peerElement.getKey())).bitfield );
                System.out.println((int)(peerElement.getKey()));
                Client client= new Client(peers.get(peerID), (Peer)peerElement.getValue());
                client.link();
                logger.tcpConnectiontoPeer(peerID, (int)peerElement.getKey());
                //System.out.println("Peer " + peerID + " connected to " + peerElement.getKey());
            }
        }
        System.out.println("Connected Peers: " + peers.get(peerID).peerManager);

    }
}
