import edu.ufl.jjr.peer.Peer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Scanner;

public class peerProcess {
    public static void main(String arg[]) throws FileNotFoundException {

        File peerConfig = new File("C:/Users/rober/Desktop/Networking-Project/P2P-project/PeerInfo.cfg");
        Hashtable<Integer, Peer> peers = new Hashtable<Integer, Peer>();

        Scanner scnr = new Scanner(peerConfig);

        while(scnr.hasNextLine()){
            String line = scnr.nextLine();
            String [] variables = line.split(" ");
            Peer peer = new Peer();
            peer.peerInfoConfig(Integer.parseInt(variables[0]), variables[1], Integer.parseInt(variables[2]), Integer.parseInt(variables[3]));
            peers.put(peer.peerID, peer);
        }

        System.out.println(peers.get(1006).hostName);
    }
}
