package com.elvarg.util.flood;

import com.elvarg.Server;
import com.elvarg.game.GameConstants;
import com.elvarg.net.NetworkConstants;
import com.elvarg.net.login.LoginResponses;
import com.elvarg.net.security.IsaacRandom;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Represents a client which will attempt
 * to connect to the server.
 * <p>
 * This can be used to stresstest the server.
 * <p>
 * Note: Code was copy+pasted from client.
 * I've barely touched it.
 *
 * @author Professor Oak
 */
public class Client {

    private final String username;
    private final String password;
    public boolean loggedIn;
    int pingCounter = 0;
    private Buffer incoming, login;
    private ByteBuffer outgoing;
    private BufferedConnection socketStream;
    private long serverSeed;
    private IsaacRandom encryption;

    public Client(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void attemptLogin() throws Exception {
        login = Buffer.create();
        incoming = Buffer.create();
        outgoing = ByteBuffer.create(5000, false, null);
        socketStream = new BufferedConnection(openSocket(NetworkConstants.GAME_PORT));

        outgoing.putByte(14); //REQUEST
        socketStream.queueBytes(1, outgoing.getBuffer());

        int response = socketStream.read();

        //Our encryption for outgoing messages for this player's session
        IsaacRandom cipher = null;

        if (response == 0) {
            socketStream.flushInputStream(incoming.payload, 8);
            incoming.currentPosition = 0;
            serverSeed = incoming.readLong(); // aka server session key
            int seed[] = new int[4];
            seed[0] = (int) (Math.random() * 99999999D);
            seed[1] = (int) (Math.random() * 99999999D);
            seed[2] = (int) (serverSeed >> 32);
            seed[3] = (int) serverSeed;
            outgoing.resetPosition();
            outgoing.putByte(10);
            outgoing.putInt(seed[0]);
            outgoing.putInt(seed[1]);
            outgoing.putInt(seed[2]);
            outgoing.putInt(seed[3]);
            outgoing.putInt(GameConstants.CLIENT_UID);
            outgoing.putString(username);
            outgoing.putString(password);
            outgoing.encryptRSAContent();

            login.currentPosition = 0;
            login.writeByte(16); //18 if reconnecting, we aren't though
            login.writeByte(outgoing.getPosition() + 2); // size of the
            // login block
            login.writeByte(255);
            login.writeByte(0); // low mem
            login.writeBytes(outgoing.getBuffer(), outgoing.getPosition(), 0);
            cipher = new IsaacRandom(seed);
            for (int index = 0; index < 4; index++)
                seed[index] += 50;

            encryption = new IsaacRandom(seed);
            socketStream.queueBytes(login.currentPosition, login.payload);
            response = socketStream.read();
        }

        if (response == LoginResponses.LOGIN_SUCCESSFUL) {
            Server.getFlooder().clients.put(username, this);
            int rights = socketStream.read();
            loggedIn = true;
            outgoing = ByteBuffer.create(5000, false, cipher);
            incoming.currentPosition = 0;
        }
    }

    public void process() throws Exception {
        if (loggedIn) {
            /*for(int i = 0; i < 5; i++) {
				if(!readPacket())
					break;
			}*/
            if (pingCounter++ >= 25) {
                outgoing.resetPosition();
                //Basic packet ping to keep connection alive
                outgoing.putOpcode(0);
                if (socketStream != null) {
                    socketStream.queueBytes(outgoing.bufferLength(), outgoing.getBuffer());
                }
                pingCounter = 0;
            }
        }
    }

    private boolean readPacket() throws Exception {
        if (socketStream == null) {
            return false;
        }

        int available = socketStream.available();
        if (available < 2) {
            return false;
        }

        int opcode = -1;
        int packetSize = -1;

        //First we read opcode...
        if (opcode == -1) {

            socketStream.flushInputStream(incoming.payload, 1);

            opcode = incoming.payload[0] & 0xff;

            if (encryption != null) {
                opcode = opcode - encryption.nextInt() & 0xff;
            }

            //Now attempt to read packet size..
            socketStream.flushInputStream(incoming.payload, 2);
            packetSize = ((incoming.payload[0] & 0xff) << 8)
                    + (incoming.payload[1] & 0xff);

        }

        if (!(opcode >= 0 && opcode < 256)) {
            opcode = -1;
            return false;
        }

        incoming.currentPosition = 0;
        socketStream.flushInputStream(incoming.payload, packetSize);

        switch (opcode) {

        }
        return false;
    }

    private Socket openSocket(int port) throws IOException {
        return new Socket(InetAddress.getByName("localhost"), port);
    }
}
