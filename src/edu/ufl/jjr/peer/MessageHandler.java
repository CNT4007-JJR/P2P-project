package edu.ufl.jjr.peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MessageHandler implements Runnable {
    private Peer peer;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;

    public MessageHandler(ObjectInputStream in, ObjectOutputStream out, Peer peer, Socket socket){
        this.in = in;
        this.out = out;
        this.peer = peer;
        this.socket = socket;
    }

    public void run(){
        while(true){
            try {
                String message = (String)in.readObject();
                System.out.println(peer.peerID + " received this message " + message);
                send("message received");

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(String msg){ //will need to be changed to byte [] msg
        try{
            out.writeObject(msg);
            out.flush();
            System.out.println("Send message: " + msg + " from " + peer.peerID);
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
}
