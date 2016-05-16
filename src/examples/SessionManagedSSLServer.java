/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examples;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import javax.net.*;
import javax.net.ssl.*;

/**
 *
 * @author Silvinha
 */
// SessionManagedSSLServer.java
public class SessionManagedSSLServer implements HandshakeCompletedListener, Runnable {

    // Session management parameters. 
    // In a practical implementation these would be defined 
    // externally, e.g. in a properties file. 
    // Cache up to four sessions 
    static final int SESSION_CACHE_SIZE = 4;
    // Time sessions out after 15 minutes. 
    static final int SESSION_TIMEOUT = 15 * 60; // 15m
    private SSLContext sslContext;
    private SSLServerSocketFactory serverSocketFactory;
    private SSLServerSocket serverSocket;
    // Replace with your own local values … 
    static final File keysFile = new File("...", "testkeys");
    static final String passPhrase = "passphrase";

    static // initialization 
    { // Alter as required // 
        //System.setProperty("javax.net.debug", "ssl");
    }

    /**
     * Create new SessionManagedSSLServer
     *
     * @param port Port to listen at
     * @exception IOException creating socket
     * @exception GeneralSecurityException initializing SSL
     */
    public SessionManagedSSLServer(int port) throws IOException, GeneralSecurityException {
        if (sslContext == null) {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            KeyStore ks = KeyStore.getInstance("JKS");
            File keysFile = new File("...");
            String passphrase = "";
            ks.load(new FileInputStream(keysFile), passphrase.toCharArray());
            kmf.init(ks, passphrase.toCharArray());
            this.sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);
            // Configure the server session context (1.4-specific) 
            SSLSessionContext serverSessionContext = sslContext.getServerSessionContext();
            serverSessionContext.setSessionCacheSize(SESSION_CACHE_SIZE);
            serverSessionContext.setSessionTimeout(SESSION_TIMEOUT);
            this.serverSocketFactory = sslContext.getServerSocketFactory();

        }
        this.serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port);
    }

    /**
     * handshakeCompleted callback. Called whenever a handshake completes
     * successfully. Handshaking is usually asynchronous, but no I/O is done on
     * the socket until a handshake completes successfully, so there is no need
     * to synchronize anything with the completion of this method.
     */
    @Override
    public void handshakeCompleted(HandshakeCompletedEvent event) {
        String cipherSuite = event.getCipherSuite();
        // Ensure cipher suite is strong enough, not shown …
        try {
            // (JDK 1.4) 
            java.security.cert.Certificate[] peerCerts = event.getPeerCertificates();
            X509Certificate peerCert = (X509Certificate) peerCerts[0];
            // Verify distinguished name of zeroth certificate. 
            Principal principal = peerCert.getSubjectDN();
            // check principal.getName() &c against expectations, 
            // not shown …
        } catch (SSLPeerUnverifiedException exc) {
            // do whatever is required, e.g. close the socket
        }
    } // handshakeCompleted() 

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        for (;;) {
            try {
                SSLSocket socket = (SSLSocket) serverSocket.accept();
                socket.addHandshakeCompletedListener(this);
                new ConnectionThread(socket).start();
            } catch (IOException exc) { // … 
            }
        } // for (;;) 
    } // run() 

    class ConnectionThread extends Thread {

        SSLSocket socket;

        ConnectionThread(SSLSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // Choose whichever of these suits your situation ... 
                // socket.setWantClientAuth(true); 
                // socket.setNeedClientAuth(true); 
                InputStream in = new BufferedInputStream(socket.getInputStream(), 8192);
                OutputStream out = new BufferedOutputStream(socket.getOutputStream(), 8192); // Handle the conversation 
            } catch (SSLException exc) { // Treat this as a possible security attack … 
            } catch (IOException exc) { // Treat this as a network failure … 
            } finally {
                try {
                    socket.close();
                } catch (SSLException exc) { // Handle possible truncation attack, not shown ... 
                } catch (IOException exc) {
                }
            } // finally 
        } // run() 
    } // class ConnectionThread 
} // class SessionManagedSSLServer

