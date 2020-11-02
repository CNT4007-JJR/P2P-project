import edu.ufl.jjr.client.Client;
import edu.ufl.jjr.peer.Peer;
import edu.ufl.jjr.MessageCreator.MessageCreator;
import edu.ufl.jjr.writingLog.WritingLog;
import edu.ufl.jjr.server.Server;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.io.InputStreamReader;
import java.util.*;


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

        System.out.println("Peer Process Host Name: " + peers.get(peerID).hostName);
        System.out.println("Peer Process Port Number: " + peers.get(peerID).portNumber);
        System.out.println("Peer Process Contains File: " + peers.get(peerID).containsFile);
        System.out.println("Peer Process Number of Pieces: " + peers.get(peerID).numPieces);
        System.out.println("Peer Process Bitfield: " + peers.get(peerID).bitfield);

        Server server = new Server(peers.get(peerID));
        Thread serverThread = new Thread(server);
        serverThread.start();

        System.out.println("Started Server for Peer" + peerID);

        Iterator peersIterator = peers.entrySet().iterator();

        while(peersIterator.hasNext()){
            Map.Entry peerElement = (Map.Entry)peersIterator.next();
            if((int) peerElement.getKey() < peerID){
                System.out.println((int)(peerElement.getKey()));
                peers.get(peerID).addInitialPeerConnection((int)(peerElement.getKey()), peers.get((int)(peerElement.getKey())).bitfield );
                Client client= new Client(peers.get(peerID), (Peer)peerElement.getValue());
                client.link();
                System.out.println("Peer " + peerID + " connected to " + peerElement.getKey());
            }
        }
        System.out.println("Connected Peers: " + peers.get(peerID).peerManager);


        // logging testing

//      Peer peer_1 = new Peer();
//      peer_1.peerID = 1000;
//
//      Peer peer_2 = new Peer();
//      peer_2.peerID = 1001;
//
//
//      WritingLog test_1 = new WritingLog(peer_1);
//      test_1.test("attempt 1");
//      test_1.test("attempt 2");
//
//      WritingLog test_2 = new WritingLog(peer_2);
//      test_2.test("attempt 1");
//      test_2.test("attempt 2");
//


    }
}
