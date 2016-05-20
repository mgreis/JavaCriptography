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

    SSLSocket socket;

    public Dispatcher(SSLSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            socket.startHandshake();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
