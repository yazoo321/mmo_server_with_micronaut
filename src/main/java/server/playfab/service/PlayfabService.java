package server.playfab.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import server.playfab.model.PlayFabAuthResponse;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Singleton
public class PlayfabService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DEVELOPER_SECRET_KEY = System.getenv("PLAYFAB_SECRET_KEY");
    private static final String TITLE_ID = System.getenv("TITLE_ID");
//    private static final String DEVELOPER_SECRET_KEY = "Y57A5QYISBY3F5KMNCHKP6ESB91U4R83IED1M1Z8O9B7UEWWXS";
//    private static final String TITLE_ID = "A63F0";


    public PlayFabAuthResponse validateSessionTicket(String sessionTicket) throws Exception {
        String urlString = "https://" + TITLE_ID + ".playfabapi.com/Server/AuthenticateSessionTicket";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-SecretKey", DEVELOPER_SECRET_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Serialize request body using Jackson
        String jsonInputString = objectMapper.writeValueAsString(new SessionTicketRequest(sessionTicket));

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int code = conn.getResponseCode();
        if (code == 200) {
            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                String responseBody = scanner.useDelimiter("\\A").next();
                return extractPlayFabId(responseBody);
            }
        } else {
            throw new Exception("Failed to authenticate session ticket. HTTP code: " + code);
        }
    }

    private static PlayFabAuthResponse extractPlayFabId(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String playFabId = root.path("data").path("UserInfo").path("PlayFabId").asText(null);

        if (playFabId == null) {
            throw new Exception("Invalid response format: PlayFabId not found");
        }

        return new PlayFabAuthResponse(playFabId);
    }

    private static class SessionTicketRequest {
        public String SessionTicket;

        public SessionTicketRequest(String sessionTicket) {
            this.SessionTicket = sessionTicket;
        }
    }

}
