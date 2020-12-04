package edu.ufl.jjr.server;

import edu.ufl.jjr.peer.Peer;
import edu.ufl.jjr.peer.MessageHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{

    private Peer peer;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Server(Peer peer){
        this.peer = peer;
    }

    public void run() {
        ServerSocket listener = null;
        try {
            listener = new ServerSocket(peer.portNumber);
        } catch (IOException e) {
            System.err.println("Problem starting server");
            e.printStackTrace();
        }
        try {
            while (true) {
                socket = listener.accept();

                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();

                in = new ObjectInputStream(socket.getInputStream());

                //create message handler this will handle dealing with incoming messages as well as sending responses to messages
                MessageHandler handler = new MessageHandler(in, out, peer, socket); //(assuming we want peer and socket maybe not needed?)
//                peer.setOut(out);

                //start handler on thread
                Thread serverThread = new Thread(handler);
                serverThread.start();


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