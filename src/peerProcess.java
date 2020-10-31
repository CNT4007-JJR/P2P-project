import edu.ufl.jjr.peer.Peer;
import edu.ufl.jjr.writingLog.WritingLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class peerProcess {
    public static void main(String arg[]) throws FileNotFoundException {

        File peerConfig = new File("/Users/jonathanmorales/Desktop/P2P-project/PeerInfo.cfg");
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

        Iterator peersIterator = peers.entrySet().iterator();

        while(peersIterator.hasNext()){
            Map.Entry peerElement = (Map.Entry)peersIterator.next();
            if((int) peerElement.getKey() < peerID){
                System.out.println((int)(peerElement.getKey()));
                peers.get(peerID).addInitialPeerConnection((int)(peerElement.getKey()), (byte) 0);
            }
        }
        System.out.println("Connected Peers: " + peers.get(peerID).peerManager);

        WritingLog first = new WritingLog("1000");
        first.test("this is a test");


    }
}
