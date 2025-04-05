package org.example.dssm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.search.SearchQuery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DSSMClient {
    private static final String DSSM_API = "http://localhost:8000/scores";

    public Map<Integer, Float> getDSSMScores(SearchQuery query) throws IOException {

        URL url = new URL(DSSM_API);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        String jsonInputString = "{\"query\": \"" + query.toString() + "\"}";
        try(OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());

            ObjectMapper objectMapper = new ObjectMapper();
            try {
                DSSMResponse res = objectMapper.readValue(response.toString(), DSSMResponse.class);
                float[] scores = res.getScores();
                int[] ids = res.getIds();
                System.out.println(res.toString());
                Map<Integer, Float> dssmScores = new HashMap<>();
                for(int i = 0; i < ids.length; i++) {
                    dssmScores.put(ids[i], scores[i]);
                }
                return dssmScores;
            } catch (Exception ignored) {
                System.out.println("ignored");
            }
        }

        return new HashMap<>();
    }
}
