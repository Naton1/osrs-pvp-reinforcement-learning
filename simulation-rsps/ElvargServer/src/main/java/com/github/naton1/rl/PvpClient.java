package com.github.naton1.rl;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PvpClient implements Closeable {
    private static final Gson gson = new Gson();

    private final String host;
    private final int port;

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public PvpClient(String serverName, int serverPort) {
        this.host = serverName;
        this.port = serverPort;
    }

    public Response sendRequest(Request request) throws IOException {
        if (this.clientSocket == null || this.clientSocket.isClosed()) {
            this.clientSocket = new Socket(this.host, this.port);
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }
        try {
            final String json = gson.toJson(request);
            out.println(json);
            final String responseJson = in.readLine();
            if (responseJson == null) {
                throw new IOException("No response from server");
            }
            return gson.fromJson(responseJson, Response.class);
        } catch (IOException e) {
            try {
                close();
            } catch (IOException e2) {
                e.addSuppressed(e2);
            }
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        if (this.clientSocket != null) {
            out.close();
            in.close();
            clientSocket.close();
            clientSocket = null;
            in = null;
            out = null;
        }
    }

    @Value
    @Builder
    public static class Request {
        private final String model;
        private final List<List<Number>> obs;
        private final List<List<Boolean>> actionMasks;
        private final boolean deterministic;
        private final boolean returnLogProb;
        private final boolean returnEntropy;
        private final boolean returnValue;
        private final boolean returnProbs;
        private final List<String> extensions;
    }

    @Value
    public static class Response {
        private final List<Integer> action;
        private final Double logProb;
        private final List<Double> entropy;
        private final List<Double> values;
        private final List<List<Double>> probs;
        private final List<?> extensionResults;

        @SuppressWarnings("unchecked")
        public <T> T getExtensionResult(int index) {
            return (T) extensionResults.get(index);
        }
    }
}
