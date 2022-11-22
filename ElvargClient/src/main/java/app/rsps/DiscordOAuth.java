package app.rsps;

/**
 * Basic singleton web server that listens on localhost:8080.
 *
 * @author shogun <shogunrsps@gmail.com>
 */

import com.runescape.Configuration.DiscordConfiguration;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DiscordOAuth {

    private static DiscordOAuth instance;

    private DiscordOAuthListener httpd;
    private Thread runner;
    private Function<String, Void> callback;

    public static DiscordOAuth getInstance() {
        return instance;
    }

    public static String getOAuthUrl() {
        return "https://discord.com/api/oauth2/authorize?client_id=" + DiscordConfiguration.CLIENT_ID + "&redirect_uri=http%3A%2F%2Flocalhost%3A8080&response_type=code&scope=identify";
    }

    /**
     * Lightweight wrapper around the web framework. Intended to isolate the actual HTTP
     * processing of receiving the Discord callback
     */
    class DiscordOAuthListener extends NanoHTTPD {
        private DiscordOAuth parent;
        public DiscordOAuthListener(DiscordOAuth parent) {
            super(8080);
            this.parent = parent;
        }

        @Override
        public Response serve(IHTTPSession session) {
            if (parent.callback == null) return null;

            String msg = "<html><head><meta http-equiv=\"refresh\" content=\"0; URL=" + DiscordConfiguration.REDIRECT_URL + "\" /></head></html>";

            Map<String, List<String>> params = session.getParameters();
            if (params.containsKey("code")) {
                String code = params.get("code").get(0);
                this.parent.callback(code);
            }

            return NanoHTTPD.newFixedLengthResponse(msg);
        }
    }

    public DiscordOAuth() {
        httpd = new DiscordOAuthListener(this);

        runner = new Thread(() -> {
            try {
                httpd.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            } catch (IOException ioe) {
                System.exit(-1);
            }
        });
        runner.start();
    }

    public void setCallback(Function<String, Void> callback) {
        this.callback = callback;
    }

    public void callback(String token) {
        this.callback.apply(token);
        this.callback = null;
    }

    static {
        instance = new DiscordOAuth();
    }
}
