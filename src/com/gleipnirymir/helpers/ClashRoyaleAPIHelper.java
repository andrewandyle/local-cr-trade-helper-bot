package com.gleipnirymir.helpers;

import com.gleipnirymir.cache.CacheManager;
import com.gleipnirymir.exceptions.CrthException;
import com.gleipnirymir.exceptions.CrthExceptionFactory;
import com.gleipnirymir.utils.CardUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;

public class ClashRoyaleAPIHelper {

    private static final Logger logger = LogManager.getLogger(ClashRoyaleAPIHelper.class);

    private static final String apiBaseUrl = "https://api.royaleapi.com/";
    private static String apiToken = "";

    private static Instant requestsAvailableAt = Instant.now();
    private static CacheManager cacheManager = CacheManager.getInstance();

    public static String getCardByName(String cardName) throws IOException {

        String cardNameId = CardUtils.getCardKey(cardName);
        String cards = getCards();
        JSONArray cardsJson = new JSONArray(cards);
        Iterator<Object> iterator = cardsJson.iterator();
        JSONObject card = new JSONObject();
        boolean cardFound = false;
        while (iterator.hasNext() && !cardFound) {
            card = (JSONObject) iterator.next();
            cardFound = cardNameId.equals(card.getString("key"));
        }

        return cardFound ? card.toString() : "";
    }

    public static String getCards() throws IOException {
        String method = "getCards";
        String cacheResponse = cacheManager.retrieve(method, "");
        if (!cacheResponse.isEmpty()) {
            return cacheResponse;
        }

        String fullURL = "https://api.github.com/repos/RoyaleAPI/cr-api-data/contents/json/cards.json";

        URL urlForGetRequest = new URL(fullURL);
        String readLine;
        HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3.raw");

        logger.info("trying to GET " + fullURL);

        int responseCode = connection.getResponseCode();
        StringBuffer response = new StringBuffer();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((readLine = in.readLine()) != null) {
                response.append(readLine);
            }
            in.close();
            cacheManager.save(method, "", response.toString(), 1440);
        } else {
            logger.error("There was a problem getting " + fullURL);
            logger.error("[" + responseCode + "] " + connection.getResponseMessage());
        }
        connection.disconnect();
        return response.toString();
    }

    public static String getPlayerInfo(String playerTag) throws IOException, CrthException {
        String method = "getPlayerInfo";
        String cacheResponse = cacheManager.retrieve(method, playerTag);
        if (!cacheResponse.isEmpty()) {
            return cacheResponse;
        }

        sleepUntilCanContinue(playerTag);

        String fullURL = apiBaseUrl + "player/" + playerTag;

        URL urlForGetRequest = new URL(fullURL);
        String readLine;
        HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("auth", apiToken);

        logger.info("trying to GET " + fullURL);

        int responseCode = connection.getResponseCode();
        StringBuffer response = new StringBuffer();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((readLine = in.readLine()) != null) {
                response.append(readLine);
            }
            in.close();
            cacheManager.save(method, playerTag, response.toString());

        } else {
            logger.error("There was a problem getting " + fullURL);
            String errorMessage = "[" + responseCode + "] " + connection.getResponseMessage();
            logger.error(errorMessage);

            if (responseCode == 429) {
                String timeToWait = connection.getHeaderField("X-Ratelimit-Retry-After");
                Instant newRequestsAvailableAt = Instant.now().plusMillis(Long.valueOf(timeToWait));

                if (newRequestsAvailableAt.isAfter(requestsAvailableAt)) {
                    requestsAvailableAt = newRequestsAvailableAt;
                }
                return getPlayerInfo(playerTag);
            }

            throw CrthExceptionFactory.responseException(errorMessage);
        }

        return response.toString();
    }

    private static void sleepUntilCanContinue(String playerTag) {
        long millisToWait = Instant.now().until(requestsAvailableAt, ChronoUnit.MILLIS);
        if (millisToWait > 0) {
            try {
                logger.info("Waiting " + millisToWait + " milliseconds. (" + playerTag + ")");
                Thread.sleep(millisToWait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sleepUntilCanContinue(playerTag);
        }
    }

    public static String getPlayerCardByName(String playerTag, String cardName) throws IOException, CrthException {
        String cardNameId = CardUtils.getCardKey(cardName);
        String playerInfo = getPlayerInfo(playerTag);
        JSONObject playerInfoJson = new JSONObject(playerInfo);
        return getPlayerCardInfo(cardNameId, playerInfoJson);
    }

    public static String getPlayerCardInfo(String cardKey, JSONObject playerInfoJson) {
        JSONArray cardsJson = playerInfoJson.getJSONArray("cards");
        Iterator<Object> iterator = cardsJson.iterator();
        JSONObject card = new JSONObject();
        boolean cardFound = false;
        while (iterator.hasNext() && !cardFound) {
            card = (JSONObject) iterator.next();
            cardFound = cardKey.equals(card.getString("key"));
        }

        return cardFound ? card.toString() : "";
    }

    public static String getClanInfo(String clanTag) throws IOException, CrthException {
        String method = "getClanInfo";
        String cacheResponse = cacheManager.retrieve(method, clanTag);
        if (!cacheResponse.isEmpty()) {
            return cacheResponse;
        }

        String fullURL = apiBaseUrl + "clan/" + clanTag;

        URL urlForGetRequest = new URL(fullURL);
        String readLine = null;
        HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("auth", apiToken);

        logger.info("trying to GET " + fullURL);

        int responseCode = connection.getResponseCode();
        StringBuffer response = new StringBuffer();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((readLine = in.readLine()) != null) {
                response.append(readLine);
            }
            in.close();
            cacheManager.save(method, clanTag, response.toString(), 120);
        } else {
            logger.error("There was a problem getting " + fullURL);
            String errorMessage = "[" + responseCode + "] " + connection.getResponseMessage();
            logger.error(errorMessage);
            throw CrthExceptionFactory.responseException(errorMessage);
        }

        return response.toString();
    }

    public static String getClanMembers(String clanTag) throws IOException, CrthException {

        String clanInfo = getClanInfo(clanTag);
        JSONObject clanInfoJson = new JSONObject(clanInfo);
        JSONArray membersJson = clanInfoJson.getJSONArray("members");

        return membersJson.toString();
    }

    public static void setApiToken(String token) {
        apiToken = token;
    }
}
