package examples;

import java.io.*;
import java.security.Security;
import java.security.PrivilegedActionException;

import javax.net.ssl.*;
import com.sun.net.ssl.*;
import com.sun.net.ssl.internal.ssl.Provider;

/**
 * @author Joe Prasanna Kumar
 * This program simulates an SSL Server listening on a specific port for client requests
 *
 * Algorithm:
 * 1. Register the JSSE provider
 * 2. Set System property for keystore by specifying the keystore which contains the server certificate
 * 3. Set System property for the password of the keystore which contains the server certificate
 * 4. Create an instance of SSLServerSocketFactory
 * 5. Create an instance of SSLServerSocket by specifying the port to which the SSL Server socket needs to bind with
 * 6. Initialize an object of SSLSocket
 * 7. Create InputStream object to read data sent by clients
 * 8. Create an OutputStream object to write data back to clients.
 *
 */


public class SSLServer {

    /**
     * @param args
     */

    public static void main(String[] args) throws Exception{

        int intSSLport = 4443; // Port where the SSL Server needs to listen for new requests from the client

        {
            // Registering the JSSE provider
            Security.addProvider(new Provider());

            //Specifying the Keystore details
            System.setProperty("javax.net.ssl.keyStore","server.ks");
            System.setProperty("javax.net.ssl.keyStorePassword","JsEkey@4");

            // Enable debugging to view the handshake and communication which happens between the SSLClient and the SSLServer
            // System.setProperty("javax.net.debug","all");
        }

        try {
            // Initialize the Server Socket
            SSLServerSocketFactory sslServerSocketfactory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
            SSLServerSocket sslServerSocket = (SSLServerSocket)sslServerSocketfactory.createServerSocket(intSSLport);
            SSLSocket sslSocket = (SSLSocket)sslServerSocket.accept();

            // Create Input / Output Streams for communication with the client
            while(true)
            {
                PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                sslSocket.getInputStream()));
                String inputLine, outputLine;

                while ((inputLine = in.readLine()) != null) {
                    out.println(inputLine);
                    System.out.println(inputLine);
                }

                // Close the streams and the socket
                out.close();
                in.close();
                sslSocket.close();
                sslServerSocket.close();

            }
        }


        catch(Exception exp)
        {
            PrivilegedActionException priexp = new PrivilegedActionException(exp);
            System.out.println(" Priv exp --- " + priexp.getMessage());

            System.out.println(" Exception occurred .... " +exp);
            exp.printStackTrace();
        }

    }

}
