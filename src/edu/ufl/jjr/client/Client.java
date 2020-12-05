package edu.ufl.jjr.client;

import edu.ufl.jjr.peer.MessageHandler;
import edu.ufl.jjr.peer.Peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client{
    private Peer peer;
    private Peer targetPeer;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    //takes host peer and target peer
    public Client(Peer peer, Peer targetPeer){
        this.peer = peer;
        this.targetPeer = targetPeer;
    }

    //link a peer and a target peer
    public void link(){
        try {
            Socket socket = new Socket(targetPeer.hostName, targetPeer.portNumber);

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            in = new ObjectInputStream(socket.getInputStream());

            //create message handler this will handle dealing with incoming messages as well as sending responses to messages
            MessageHandler handler = new MessageHandler(in, out, peer, socket); //(assuming we want peer and socket maybe not needed?)
//            peer.setOut(out);

            //start handler on own thread
            Thread serverThread = new Thread(handler);
            serverThread.start();

        }
        catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        }
        catch(UnknownHostException unknownHost){
            System.err.println("You are trying to connect to an unknown host!");
        }
        catch(IOException ioException){
            System.err.println("IoException!");
            ioException.printStackTrace();
        }
    }


}