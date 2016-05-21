/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package joao.chat.client;

import javax.net.ssl.SSLSocket;
import java.io.IOException;

/**
 *
 * @author Mário
 */
public class Dispatcher extends Thread {
    String pickedCipher[] ={"TLS_RSA_WITH_AES_128_CBC_SHA"};
    SSLSocket socket;

    public Dispatcher(SSLSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            socket.setEnabledCipherSuites(pickedCipher);
            socket.startHandshake();
            System.out.println("Done scheduling!");
        } catch (IOException e) {
            System.err.println("Error while scheduling!");
            e.printStackTrace();
        }

    }

}
