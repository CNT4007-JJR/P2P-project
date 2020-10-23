package edu.ufl.jjr.server;

import java.net.*;
import java.io.*;

class Server implements Runnable{

    private Peer peer;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Server(Peer peer){
        this.peer = peer;
    }

    public void run() {
        ServerSocket listener = new ServerSocket(peer.getPort());
        try {
            while (true) {
                socket = listener.accept();

                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();

                in = new ObjectInputStream(socket.getInputStream());

                //create message controller this will handle dealing with incoming messages as well as sending responses to messages
                //message controller part of peer package
                messageController = new messageController(in, out, peer, socket); //(assuming we want peer and socket maybe not needed?)

                //create handshake message (seperate package for messages)?
                //send handshake message

                //run message controller on thread
            }
            
        } catch (IOException e) {
           System.err.println("IO Error with server!");
            e.printStackTrace();
        } finally {
            try {
                listener.close();
            } catch (IOException e) {
                System.err.println("IO Error with closing server!");
                e.printStackTrace();
            }
        }

    }
}