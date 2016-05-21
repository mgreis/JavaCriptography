package joao.chat.client;

import joao.chat.commonPackage.Message;
import javax.net.ssl.*;
import java.net.*;
import java.io.*;
import java.security.*;

public class ClientSideOld implements Runnable {
    
    private String username = null;     // Identficador do Cliente
    private char[] passphrase = null;   // Identficador do Cliente
    private SSLSocket socket = null;
    private Thread thread = null;
    private BufferedReader console = null;
    private ObjectOutputStream streamOut = null; //private DataOutputStream streamOut = null;
    private ClientSideThreadOld client = null;
    private SSLSessionContext clientSessionContext = null;
    static final int SESSION_CACHE_SIZE = 4;
    // Time sessions out after 15 minutes.
    static final int SESSION_TIMEOUT = 15 * 60; // 15m
    Scheduler scheduler = null;

    public ClientSideOld(String serverName, int serverPort, String username, String pass) {

        System.out.println("Establishing connection to server...");
        this.username = username;
        this.passphrase = pass.toCharArray();
        try {
            // Establishes connection with server (name and port)

            SSLSocketFactory factory = null;
            try {
                SSLContext ctx = SSLContext.getInstance("TLSV1.2");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

                KeyStore ks = KeyStore.getInstance("JKS");
                KeyStore kt = KeyStore.getInstance("JKS");

                ks.load(new FileInputStream("certs/"+username+".jks"), passphrase);
                kt.load(new FileInputStream("certs/"+username+"certs.jks"), passphrase);

                kmf.init(ks, passphrase);
                tmf.init(kt);
                ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

                factory = ctx.getSocketFactory();

            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }

            socket = (SSLSocket) factory.createSocket(serverName, serverPort);

            socket.startHandshake();
            scheduler = new Scheduler(socket);

            System.out.println("Connected to server: " + socket);
            start();
        } catch (UnknownHostException uhe) {
            // Host unknown
            System.out.println("Error establishing connection - host unknown: " + uhe.getMessage());
        } catch (IOException ioexception) {
            // Other error establishing connection
            System.out.println("Error establishing connection - unexpected exception: " + ioexception.getMessage());
        }

    }

    public void run() {
        while (thread != null) {
            try {
                // Sends message from console to server
                //streamOut.writeUTF(console.readLine());
                streamOut.writeObject(new Message(console.readLine(), username));
                streamOut.flush();
            } catch (IOException ioexception) {
                System.out.println("Error sending string to server: " + ioexception.getMessage());
                stop();
            }
        }
    }
    @Deprecated
    public void handle(String msg) {
        // Receives message from server
        if (msg.equals(".quit")) {
            // Leaving, quit command
            System.out.println("Exiting...Please press RETURN to exit ...");
            stop();
        } else // else, writes message received from server to console
        {
            System.out.println(msg);
        }
    }

     public void handle(Message msg) {
        // Receives message from server
        if (msg.getMessage().equals(".quit")) {
            // Leaving, quit command
            System.out.println("Exiting...Please press RETURN to exit ...");
            stop();
        } else // else, writes message received from server to console
        {
            System.out.println(msg);
        }
    }
    
    // Inits new client thread
    public void start() throws IOException {
        console = new BufferedReader(new InputStreamReader(System.in));
        //streamOut = new DataOutputStream(socket.getOutputStream());
        streamOut = new ObjectOutputStream(socket.getOutputStream());
        if (thread == null) {
            client = new ClientSideThreadOld(this, socket);
            thread = new Thread(this);
            thread.start();
        }
    }

    // Stops client thread
    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
        try {
            if (console != null) {
                console.close();
            }
            if (streamOut != null) {
                streamOut.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ioe) {
            System.out.println("Error closing thread...");
        }
        client.close();
        client.stop();
    }

    public static void main(String args[]) {
        ClientSideOld client = null;
        if (args.length != 4) // Displays correct usage syntax on stdout
        {
            System.out.println("Usage: java ChatClient host port username pass");
        } else // Calls new client
        {
            client = new ClientSideOld(args[0], Integer.parseInt(args[1]), args[2], args[3]);
        }
    }

    class ClientSideThreadOld extends Thread {

        private Socket socket = null;
        private ClientSideOld client = null;
        private ObjectInputStream streamIn = null; //private DataInputStream streamIn = null;

        public ClientSideThreadOld(ClientSideOld _client, Socket _socket) {
            client = _client;
            socket = _socket;
            open();
            start();
        }

        public void open() {
            try {
                //streamIn = new DataInputStream(socket.getInputStream());
                streamIn = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ioe) {
                System.out.println("Error getting input stream: " + ioe);
                client.stop();
            }
        }

        public void close() {
            try {
                if (streamIn != null) {
                    streamIn.close();
                }
            } catch (IOException ioe) {
                System.out.println("Error closing input stream: " + ioe);
            }
        }

        public void run() {
            while (true) {
                try {
                    client.handle((Message) streamIn.readObject());
                } catch (IOException ioe) {
                    System.out.println("Listening error: " + ioe.getMessage());
                    client.stop();
                } catch (ClassNotFoundException cnfe) {
                    System.err.println("Wrong Object: " + cnfe.getMessage());
                }
            }
        }
    }

}
