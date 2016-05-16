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
public class SessionManagedSSLClient implements HandshakeCompletedListener {

    // Session management parameters. 
    // In a practical implementation these would be defined 
    // externally, e.g. in a properties file. 
    // Cache up to ten sessions
    static final int SESSION_CACHE_SIZE = 10;
    // Time sessions out after 1 hour. 
    static final int SESSION_TIMEOUT = 60 * 60; // 1h 
    private SSLContext sslContext;
    private SSLSocketFactory socketFactory;
    private SSLSocket socket;

    static // initializer 
    { // as required // 
        System.setProperty("javax.net.debug", "ssl");
    }

    /**
     * Create new SessionManagedSSLClient
     *
     * @param host target host
     * @param port target port
     * @exception IOException creating socket
     * @exception GeneralSecurityException initializing SSL
     */

    public SessionManagedSSLClient(String host, int port)
            throws IOException, GeneralSecurityException {
        if (sslContext == null) {
            this.sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
            // Configure client session context: JDK 1.4-specific 
            SSLSessionContext clientSessionContext = sslContext.getClientSessionContext();
            clientSessionContext.setSessionCacheSize(SESSION_CACHE_SIZE);
            clientSessionContext.setSessionTimeout(SESSION_TIMEOUT);
            this.socketFactory = sslContext.getSocketFactory();
        }
        this.socket = (SSLSocket) socketFactory.createSocket(host, port);
        socket.addHandshakeCompletedListener(this);
    }

    /**
     * Handle conversation
     */
    public void handleConversation() {
        try {
            InputStream in = new BufferedInputStream(socket.getInputStream());
            OutputStream out = new BufferedOutputStream(socket.getOutputStream(), 8192);
            // … 
        } catch (SSLException exc) {
            // Treat this as a possible security attack … 
        } catch (IOException exc) {
            // Treat this as a network failure … 
        } finally {
            try {
                socket.close();
            } catch (SSLException exc) {
                // Handle possible truncation attack, not shown … 
            } catch (IOException exc) {
                // …  
            }
            socket = null;
        } // finally 
    } // handleConversation()

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
            // Verify the distinguished name (DN) 
            // of the zeroth certificate. 
            Principal principal = peerCert.getSubjectDN();
            // check principal.getName() &c against expectation, 
            // not shown … 
        } catch (SSLPeerUnverifiedException exc) {
            // Handle this as required … 
        }
    } // handshakeCompleted() 
} // class SessionManagedSSLClient
