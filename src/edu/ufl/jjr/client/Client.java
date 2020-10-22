package edu.ufl.jjr.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;




public class Client{
    private Peer peer
    private Peer targetPeer
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Client(Peer peer, Peer targetPeer){
        this.peer = peer;
        this.targetPeer = targetPeer;
    }

    //link a peer and a target peer
    public void link(){
        try {
            Socket socket = new Socket(targetPeer.getIp(), targetPeerPeer.getPort());

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            in = new ObjectInputStream(requestSocket.getInputStream());

            //create message controller this will handle dealing with incoming messages as well as sending responses to messages
            //message controller part of peer package
            messageController = new messageController(in, out, peer); //(assuming want peer maybe not needed?)

            //create handshake message (seperate package for messages)?
            //send handshake message

            //run message controller on thread

        }
        catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        }
        catch ( ClassNotFoundException e ) {
            System.err.println("Class not found");
        }
        catch(UnknownHostException unknownHost){
            System.err.println("You are trying to connect to an unknown host!");
        }
        catch(IOException ioException){
            System.err.println("IoException!")
            ioException.printStackTrace();
        }
    }


}