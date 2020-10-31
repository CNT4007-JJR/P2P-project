import edu.ufl.jjr.client.Client;
import edu.ufl.jjr.peer.Peer;
import edu.ufl.jjr.server.Server;

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
        System.out.println("Peer Process Port Number: " + peers.get(peerID).hostName);
        System.out.println("Peer Process Port Number: " + peers.get(peerID).portNumber);
        System.out.println("Peer Process Port Number: " + peers.get(peerID).containsFile);

        Server server = new Server(peers.get(peerID));
        Thread serverThread = new Thread(server);
        serverThread.start();

        System.out.println("Started Server for Peer" + peerID);

        Iterator peersIterator = peers.entrySet().iterator();

        while(peersIterator.hasNext()){
            Map.Entry peerElement = (Map.Entry)peersIterator.next();
            if((int) peerElement.getKey() < peerID){
                System.out.println((int)(peerElement.getKey()));
                peers.get(peerID).addInitialPeerConnection((int)(peerElement.getKey()), (byte) 0);
                Client client= new Client(peers.get(peerID), (Peer)peerElement.getValue());
                client.link();
                System.out.println("Peer " + peerID + " connected to " + peerElement.getKey());
            }
        }
        System.out.println("Connected Peers: " + peers.get(peerID).peerManager);


    }
}
