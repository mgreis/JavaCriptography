package chat.server;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.net.*;
import java.io.*;
import java.security.KeyStore;


public class ServerSide implements Runnable
{
    private ServerSideThread clients[] = new ServerSideThread[20];
    private ServerSocket server_socket = null;
    private Thread thread = null;
    private int clientCount = 0;

    public ServerSide(int port)
    {
        try
        {
            // Binds to port and starts server
            System.out.println("Binding to port " + port);
            ServerSocketFactory ssf =
                    this.getServerSocketFactory("TLS");
            server_socket = ssf.createServerSocket(port);
            ((SSLServerSocket)server_socket).setNeedClientAuth(true);
            System.out.println("Server started: " + server_socket);
            start();
        }
        catch(IOException ioexception)
        {
            // Error binding to port
            System.out.println("Binding error (port=" + port + "): " + ioexception.getMessage());
        }
    }

    public void run()
    {
        while (thread != null)
        {
            try
            {
                // Adds new thread for new client
                System.out.println("Waiting for a client ...");

                addThread(server_socket.accept());
            }
            catch(IOException ioexception)
            {
                System.out.println("Accept error: " + ioexception); stop();
            }
        }
    }

    public void start()
    {
        if (thread == null)
        {
            // Starts new thread for client
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop()
    {
        if (thread != null)
        {
            // Stops running thread for client
            thread.stop();
            thread = null;
        }
    }

    private int findClient(int ID)
    {
        // Returns client from id
        for (int i = 0; i < clientCount; i++)
            if (clients[i].getID() == ID)
                return i;
        return -1;
    }

    public synchronized void handle(int ID, String input)
    {
        if (input.equals(".quit"))
        {
            int leaving_id = findClient(ID);
            // Client exits
            clients[leaving_id].send(".quit");
            // Notify remaing users
            for (int i = 0; i < clientCount; i++)
                if (i!=leaving_id)
                    clients[i].send("Client " +ID + " exits..");
            remove(ID);
        }
        else
            // Brodcast message for every other client online
            for (int i = 0; i < clientCount; i++)
                clients[i].send(ID + ": " + input);
    }

    public synchronized void remove(int ID)
    {
        int pos = findClient(ID);

        if (pos >= 0)
        {
            // Removes thread for exiting client
            ServerSideThread toTerminate = clients[pos];
            System.out.println("Removing client thread " + ID + " at " + pos);
            if (pos < clientCount-1)
                for (int i = pos+1; i < clientCount; i++)
                    clients[i-1] = clients[i];
            clientCount--;

            try
            {
                toTerminate.close();
            }

            catch(IOException ioe)
            {
                System.out.println("Error closing thread: " + ioe);
            }

            toTerminate.stop();
        }
    }

    private void addThread(Socket socket)
    {
        if (clientCount < clients.length)
        {
            // Adds thread for new accepted client
            System.out.println("Client accepted: " + socket);
            clients[clientCount] = new ServerSideThread(this, socket);

            try
            {
                clients[clientCount].open();
                clients[clientCount].start();
                clientCount++;
            }
            catch(IOException ioe)
            {
                System.out.println("Error opening thread: " + ioe);
            }
        }
        else
            System.out.println("Client refused: maximum " + clients.length + " reached.");
    }


    public static void main(String args[])
    {
        ServerSide server = null;

        if (args.length != 1)
            // Displays correct usage for server
            System.out.println("Usage: java ServerSide port");
        else
            // Calls new server
            server = new ServerSide (Integer.parseInt(args[0]));
    }

    private static ServerSocketFactory getServerSocketFactory(String type) {
        if (type.equals("TLS")) {
            SSLServerSocketFactory ssf = null;
            try {
                // set up key manager to do server authentication
                SSLContext ctx = SSLContext.getInstance("TLS");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                KeyStore ks = KeyStore.getInstance("JKS");
                char[] passphrase = "123456".toCharArray();

                ks.load(new FileInputStream("C:\\Keys\\DebKeyStore.jks"), passphrase);
                kmf.init(ks, passphrase);
                ctx.init(kmf.getKeyManagers(), null, null);

                ssf = ctx.getServerSocketFactory();
                return ssf;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return ServerSocketFactory.getDefault();
        }
        return null;
    }


}


