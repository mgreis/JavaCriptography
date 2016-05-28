package joao.chat.server;

import joao.chat.commonPackage.Message;
import javax.net.ssl.*;
import java.net.*;
import java.io.*;
import java.security.KeyStore;
import java.security.SecureRandom;
import joao.chat.client.Scheduler;

public class ServerSide implements Runnable { // Thread de Aceitação de Sockets!

    private ServerSideThread clients[] = new ServerSideThread[20];
    private SSLServerSocket server_socket = null;
    private Thread thread = null;
    private int clientCount = 0;
    String pickedCipher[] = {"TLS_RSA_WITH_AES_128_CBC_SHA"};

    static final int SESSION_CACHE_SIZE = 4;    // Time sessions out after 15 minutes. 
    static final int SESSION_TIMEOUT = 15 * 60; // 15m
    Scheduler scheduler = null;
    private static SSLServerSocketFactory getServerSocketFactory(String type, String pass) {
        if (type.equals("TLS")) {
            SSLServerSocketFactory ssf = null;
            char[] passphrase = pass.toCharArray();
            try {
                // set up key manager to do server authentication
                SSLContext ctx = SSLContext.getInstance("TLSV1.2");

                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                KeyStore ks = KeyStore.getInstance("JKS");
                KeyStore kt = KeyStore.getInstance("JKS");

                ks.load(new FileInputStream("certs/server.jks"), passphrase);
                kt.load(new FileInputStream("certs/servercerts.jks"), passphrase);
                kmf.init(ks, passphrase);
                tmf.init(kt);
                
                ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

                ssf = ctx.getServerSocketFactory();
                return ssf;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        }
        return null;
    }

    public ServerSide(int port, String pass) {
        //System.setProperty("javax.net.debug", "ssl");
        System.out.println("TROL");
        try {
            // Binds to port and starts server
            System.out.println("Binding to port " + port);
            SSLServerSocketFactory ssf
                    = this.getServerSocketFactory("TLS", pass);
            server_socket = (SSLServerSocket) ssf.createServerSocket(port);
            server_socket.setEnabledCipherSuites(pickedCipher); //server_socket.setEnabledCipherSuites(server_socket.getSupportedCipherSuites());
            
            //System.out.println("here!\n"+Arrays.toString(server_socket.getSupportedCipherSuites())+ "ereH!\n");
            server_socket.setWantClientAuth(true);
            server_socket.setNeedClientAuth(true);

            System.out.println("Server started: " + server_socket);
            start();
        } catch (IOException ioexception) {
            // Error binding to port
            System.out.println("Binding error (port=" + port + "): " + ioexception.getMessage());
        }
    }

    public void run() {
        while (thread != null) {
            try {
                // Adds new thread for new client
                System.out.println("Waiting for a client ...");
                SSLSocket clientSocket = (SSLSocket) server_socket.accept();
                clientSocket.setEnabledCipherSuites(pickedCipher);
                clientSocket.startHandshake();
                scheduler = new Scheduler(clientSocket);
                addThread(clientSocket);
            } catch (IOException ioexception) {
                System.out.println("Accept error: " + ioexception);
                stop();
            }
        }
    }

    public void start() {
        if (thread == null) {
            // Starts new thread for client
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            // Stops running thread for client
            thread.stop();
            thread = null;
        }
    }

    private int findClient(int ID) {
        // Returns client from id
        for (int i = 0; i < clientCount; i++) {
            if (clients[i].getID() == ID) {
                return i;
            }
        }
        return -1;
    }

    @Deprecated
    public synchronized void handle(int ID, String input) {
        if (input.equals(".quit")) {
            int leaving_id = findClient(ID);
            // Client exits
            clients[leaving_id].send(".quit");
            // Notify remaing users
            for (int i = 0; i < clientCount; i++) {
                if (i != leaving_id) {
                    clients[i].send("Client " + ID + " exits..");
                }
            }
            remove(ID);
        } else // Brodcast message for every other client online
        {
            for (int i = 0; i < clientCount; i++) {
                clients[i].send(ID + ": " + input);
            }
        }
    }

    public synchronized void handle(int ID, Message m) {
        if (m.getMessage().equals(".quit")) {
            int leaving_id = findClient(ID);
            // Client exits
            clients[leaving_id].send(".quit", null);
            // Notify remaing users
            for (int i = 0; i < clientCount; i++) {
                if (i != leaving_id) {
                    clients[i].send("Client " + m.getUsername() + " exits..", null);
                }
            }
            remove(ID);
        } else // Brodcast message for every other client online
        {
            for (int i = 0; i < clientCount; i++) {
                clients[i].send(m.getUsername() + ": " + m.getMessage(), null);
            }
        }
    }

    public synchronized void remove(int ID) {
        int pos = findClient(ID);

        if (pos >= 0) {
            // Removes thread for exiting client
            ServerSideThread toTerminate = clients[pos];
            System.out.println("Removing client thread " + ID + " at " + pos);
            if (pos < clientCount - 1) {
                for (int i = pos + 1; i < clientCount; i++) {
                    clients[i - 1] = clients[i];
                }
            }
            clientCount--;

            try {
                toTerminate.close();
            } catch (IOException ioe) {
                System.out.println("Error closing thread: " + ioe);
            }

            toTerminate.stop();
        }
    }

    private void addThread(SSLSocket socket) {
        if (clientCount < clients.length) {
            // Adds thread for new accepted client
            System.out.println("Client accepted: " + socket);
            clients[clientCount] = new ServerSideThread(this, socket);

            try {
                clients[clientCount].open();
                clients[clientCount].start();
                clientCount++;
            } catch (IOException ioe) {
                System.out.println("Error opening thread: " + ioe);
            }
        } else {
            System.out.println("Client refused: maximum " + clients.length + " reached.");
        }
    }

    public static void main(String args[]) {
        ServerSide server = null;

        if (args.length != 2) // Displays correct usage for server
        {
            System.out.println("Usage: java ServerSide port pass");
        } else // Calls new server
        {
            server = new ServerSide(Integer.parseInt(args[0]), args[1]);
        }
    }

    class ServerSideThread extends Thread {

        private ServerSide server = null;
        private Socket socket = null;
        private int ID = -1;
        private ObjectInputStream streamIn = null;      //private DataInputStream streamIn = null;
        private ObjectOutputStream streamOut = null;    //private DataOutputStream streamOut = null;

        public ServerSideThread(ServerSide _server, Socket _socket) {
            super();
            server = _server;
            socket = _socket;
            ID = socket.getPort();
        }

        @Deprecated
        // Sends message to client
        public void send(String msg) {
            try {
                streamOut.writeUTF(msg);
                streamOut.flush();
            } catch (IOException ioexception) {
                System.out.println(ID + " ERROR sending message: " + ioexception.getMessage());
                server.remove(ID);
                stop();
            }
        }

        public void send(String msg, String username) {
            try {
                Message m = new Message(msg, username);
                streamOut.writeObject(m);
                streamOut.flush();
            } catch (IOException ioexception) {
                System.out.println(ID + " ERROR sending message: " + ioexception.getMessage());
                server.remove(ID);
                stop();
            }
        }

        // Gets id for client
        public int getID() {
            return ID;
        }

        // Runs thread
        public void run() {
            System.out.println("Server Thread " + ID + " running.");

            while (true) {
                try {
                    //server.handle(ID, streamIn.readUTF());
                    server.handle(ID, (Message) streamIn.readObject());
                } catch (IOException ioe) {
                    System.out.println(ID + " ERROR reading: " + ioe.getMessage());
                    server.remove(ID);
                    stop();
                } catch (ClassNotFoundException cnfe) {
                    System.err.println("Wrong class Received: " + cnfe);
                }
            }
        }

        // Opens thread
        public void open() throws IOException {
            //streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            streamIn = new ObjectInputStream(socket.getInputStream());
            //streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            streamOut = new ObjectOutputStream(socket.getOutputStream());
        }

        // Closes thread
        public void close() throws IOException {
            if (socket != null) {
                socket.close();
            }
            if (streamIn != null) {
                streamIn.close();
            }
            if (streamOut != null) {
                streamOut.close();
            }
        }
    }

}
