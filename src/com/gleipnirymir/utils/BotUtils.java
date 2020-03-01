package com.gleipnirymir.utils;

import com.gleipnirymir.model.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.json.JSONObject;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.PrivateChannel;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class BotUtils {

    private static final Logger logger = LogManager.getLogger(BotUtils.class);
    public static final String BLANK_EMOJI = "<:blank:534902434712256514>";

    // Constants for use throughout the bot
    public static String BOT_PREFIX = "!";

    // Handles the creation and getting of a IDiscordClient object for a token
    public static IDiscordClient getBuiltDiscordClient(String token) {

        // The ClientBuilder object is where you will attach your params for configuring the instance of your bot.
        // Such as withToken, setDaemon etc
        return new ClientBuilder()
                .withToken(token)
                .withRecommendedShardCount()
                .build();

    }

    // Helper functions to make certain aspects of the bot easier to use.
    public static long sendMessage(IChannel channel, String message) {

        AtomicLong messageId = new AtomicLong(0);

        // This might look weird but it'll be explained in another page.
        RequestBuffer.request(() -> {
            try {
                messageId.set(channel.sendMessage(message).getLongID());
            } catch (DiscordException e) {
                logger.error("Message could not be sent with error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        return messageId.get();
    }

    // Helper functions to make certain aspects of the bot easier to use.
    public static long sendMessage(IChannel channel, EmbedObject embedObject) {

        AtomicLong messageId = new AtomicLong(0);

        // This might look weird but it'll be explained in another page.
        RequestBuffer.request(() -> {
            try {
                messageId.set(channel.sendMessage(embedObject).getLongID());
            } catch (DiscordException e) {
                logger.error("Message could not be sent with error: " + e.getMessage());
                e.printStackTrace();
            }
        }).get();

        return messageId.get();
    }

    public static long sendMessage(IChannel channel, String beforeEmbedText, EmbedObject embedObject) {

        AtomicLong messageId = new AtomicLong(0);

        // This might look weird but it'll be explained in another page.
        RequestBuffer.request(() -> {
            try {
                messageId.set(channel.sendMessage(beforeEmbedText, embedObject).getLongID());
            } catch (DiscordException e) {
                logger.error("Message could not be sent with error: " + e.getMessage());
                e.printStackTrace();
            }
        }).get();

        return messageId.get();
    }

    public static void setTypingStatus(IChannel channel, boolean typingStatus) {
        channel.setTypingStatus(typingStatus);
    }

    /**
     * Gets an author's player tag from the data base.
     *
     * In case it is not found, it returns "".
     *
     * @param author
     * @return player tag or "" if wasn't found.
     */
    public static String getPlayerTag(IUser author) {

//        try {
            Session session = HibernateUtils.getSessionFactory().openSession();
            User user = session.get(User.class, author.getLongID());
            session.close();


//        } catch (JDBCConnectionException e) {
//        }
            if (user == null || user.getCrAccountTag().isEmpty()) return "";

            return user.getCrAccountTag();

    }

    public static Long getUserByTag(String playerTag) {

        Session session = HibernateUtils.getSessionFactory().openSession();

        Query query = session.createQuery("from User U where U.crAccountTag=:playerTag");
        query.setParameter("playerTag", playerTag);
        List usersUsingTag = query.list();
        User user = null;
        if (!usersUsingTag.isEmpty()) {
            user = (User) usersUsingTag.get(0);
        }

        session.close();

        if (user == null) return null;

        return user.getDiscordAccountId();

    }

    /**
     * Validates if the author has a player tag saved.
     *
     * Sends an error message to the author if he hasn't.
     *
     * @param event
     * @return player tag or null if wasn't found.
     */
    public static String validateTag(MessageReceivedEvent event) {
        IUser author = event.getAuthor();
        String playerTag = getPlayerTag(author);

        if (playerTag.isEmpty()) {
            sendMessage(event.getChannel(), author.mention() + ", you need to have a tag associated to your user. Use `" + BOT_PREFIX + "saveTag` command first.");
            return null;
        }
        return playerTag;
    }

    /**
     * Extracts the card name from the parameters of the command called.
     *
     * @param args
     * @return card name
     */
    public static String getCardName(List<String> args) {
        StringBuilder cardName = new StringBuilder();
        for (String arg : args) {
            cardName.append(StringUtils.capitalize(arg.toLowerCase())).append(" ");
        }
        cardName.deleteCharAt(cardName.length() - 1);
        return cardName.toString();
    }

    /**
     * Sends an error message to the channel where the command was called.
     *
     * @param event
     * @param errorMessage
     */
    public static void sendErrorMessage(MessageReceivedEvent event, String errorMessage) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.withTitle("There was an internal problem");
        builder.withDesc(errorMessage);
        builder.withColor(new Color(0x79000E));
        sendMessage(event.getChannel(), builder.build());
    }

    /**
     * Checks if the author is in a clan or not.
     *
     * @param event
     * @param playerInfoJson
     * @return true if player is clanless
     */
    public static boolean isClanless(MessageReceivedEvent event, JSONObject playerInfoJson) {
        IUser author = event.getAuthor();
        if (playerInfoJson.get("clan") == JSONObject.NULL) {
            sendMessage(event.getChannel(), author.mention() + ", you are not a member of any clan.");
            return true;
        }
        return false;
    }

    /**
     * Returns the valid clan list for the command called.
     *
     * @param event
     * @param authorClanTag
     * @param onlyMyClan
     * @return Clan list
     */
    public static List<String> getValidClans(MessageReceivedEvent event, String authorClanTag, boolean onlyMyClan) {
        List<String> clansFromFamily = new ArrayList<>();
        if (!onlyMyClan) {
            clansFromFamily = getClansFromFamily(event.getChannel());
        }
        if (clansFromFamily.isEmpty()) {
            clansFromFamily.add(authorClanTag);
        } else {
            if (!clansFromFamily.contains(authorClanTag)) {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append(event.getAuthor().mention())
                        .append(", seems that your clan `#")
                        .append(authorClanTag)
                        .append("` is not part of the family.\n")
                        .append("This channel is only for the family clans.");
                sendMessage(event.getChannel(), errorMessage.toString());
                clansFromFamily = new ArrayList<>();
            }
        }
        return clansFromFamily;
    }

    /**
     * Checks if the command was called with the "only my clan" (-omc) parameter.
     *
     * @param args
     * @return True if the argument '-omc' was found. False, otherwise.
     */
    public static boolean onlyMyClan(List<String> args) {
        String onlyMyClanArg = "-omc";
        if (args.contains(onlyMyClanArg)) {
            args.remove(onlyMyClanArg);
            return true;
        }
        return false;
    }

    /**
     * Sets the color of the message.
     *
     * @param builder
     * @param rarity
     */
    public static void setRarityColor(EmbedBuilder builder, String rarity) {
        int colorHex = 0x7B8B5F;
        switch (rarity) {
            case "Common":
                colorHex = 0xFFFFFF;
                break;
            case "Rare":
                colorHex = 0xe68304;
                break;
            case "Epic":
                colorHex = 0xb300b3;
                break;
            case "Legendary":
                colorHex = 0x00b0d5;
                break;
        }

        builder.withColor(new Color(colorHex));
    }

    public static boolean isCardMaxed(JSONObject cardInfoJSON) {
        return cardInfoJSON.getBoolean("maxed");
    }

    /**
     * Returns the clan list for the channel specified.
     *
     * @param channel
     * @return
     */
    private static List<String> getClansFromFamily(IChannel channel) {
        String clanFamilyString = "CRTH_clanFamily:[\"";
        List<String> clanList = new ArrayList<>();
        if (!(channel instanceof PrivateChannel)) {
            if (channel.getTopic() != null) {
                String topic = channel.getTopic().toUpperCase();
                int clanFamilyIndex = topic.indexOf(clanFamilyString.toUpperCase());
                if (clanFamilyIndex > -1) {
                    String restOfTopic = topic.substring(clanFamilyIndex + clanFamilyString.length());
                    int endClansTopic = restOfTopic.indexOf("\"]");
                    if (endClansTopic > -1) {
                        String clansString = restOfTopic.substring(0, endClansTopic);
                        String[] clanTags = clansString.split("\",\"");
                        for (String clanTag : clanTags) {
                            clanList.add(clanTag.substring(1));
                        }
                    }
                }
            }
        }
        return clanList;
    }
}