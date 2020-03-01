package com.gleipnirymir.commands;

import com.gleipnirymir.helpers.ClashRoyaleAPIHelper;
import com.gleipnirymir.utils.BotUtils;
import com.gleipnirymir.utils.CardUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class has the "help commands".
 */
public class HelpCommands {

    private static final Logger logger = LogManager.getLogger(HelpCommands.class);
    private static final int COLOR = 0x7B8B5F;
    private static final String CODE_SNIPPET = "```";

    /**
     * Prints the list of the cards available for the game.
     *
     * @param event
     * @param args
     */
    public static void printCardNames(MessageReceivedEvent event, List<String> args) {
        try {
            String cardResponse = ClashRoyaleAPIHelper.getCards();
            JSONArray cardsInfo = new JSONArray(cardResponse);
            Iterator<Object> iterator = cardsInfo.iterator();
            StringBuilder cardsList = new StringBuilder();
//            StringBuilder cardsList2 = new StringBuilder();
            cardsList.append("--- Cards names ---\n");
            while (iterator.hasNext()) {
                JSONObject card = (JSONObject) iterator.next();
                cardsList.append(card.get("name") + "\n");
//                cardsList2.append("\\"+":"+card.getString("key").replaceAll("-","") + ":");
            }
            BotUtils.sendMessage(event.getChannel(), CODE_SNIPPET + cardsList + CODE_SNIPPET);
//            BotUtils.sendMessage(event.getChannel(), cardsList2.toString() );
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Prints the help command.
     *
     * @param event
     * @param onInvokedChannel
     */
    public static void printHelp(MessageReceivedEvent event, boolean onInvokedChannel) {
        String prefix = BotUtils.BOT_PREFIX;
        IUser botUser = event.getClient().getOurUser();

        EmbedBuilder builder = new EmbedBuilder();
        builder.withAuthorName(botUser.getName()).withAuthorIcon(botUser.getAvatarURL());
        builder.withColor(new Color(COLOR));
        builder.withDesc("A helper bot for your trading needs.");

        builder.appendField(BotUtils.BLANK_EMOJI, "__**Configuration**__", false);
        builder.appendField(prefix + "saveTag <#Clash Royale Tag>", "Saves the CR tag for your user.", false);
        builder.appendField(BotUtils.BLANK_EMOJI, "__**Wish list management**__", false);
        builder.appendField(prefix + "addToWishList <card name>", "Adds the card to your wishlist.\n*Also can use `" + prefix + "awl`.*", false);
        builder.appendField(prefix + "deleteFromWishList <card name>", "Deletes the card from your wish list.\n*Also can use `" + prefix + "dwl` or `" + prefix + "rwl`.*", false);
        builder.appendField(prefix + "wishList [@mention]", "Prints your current wish list. If it is followed by a mention player it will print his wish list.\n*Also can use `" + prefix + "wl`.*", false);
        builder.appendField(BotUtils.BLANK_EMOJI, "__**Clan trade explorer**__", false);
        builder.appendField(prefix + "whoHas <card name>", "Prints the clan members who have that card.", false);
        builder.appendField(prefix + "whoWants <card name>", "Prints the clan members who wants that card.", false);
        builder.appendField(prefix + "whoTakes <card name> for <card name>", "Prints the clan members who could find the deal appealing.", false);
        builder.appendField(prefix + "findMeTrades [rarity]", "Matches clan members that can give you the cards you want and can receive the ones you have.\n*Also can use `" + prefix + "fmt`.*", false);
        builder.appendField(prefix + "maxedCards [rarity]", "Prints all the maxed cards the clan have and who have them maxed.\n*Also can use `" + prefix + "mc`.*", false);
        builder.appendField(prefix + "myTradableCards [rarity] [@mention]", "Prints all the cards that you can trade (you have enough quantity and it is not in your wish list). If it is followed by a mention player it will print his tradable cards.\n*Also can use `" + prefix + "mtc`.*", false);
        builder.appendField(BotUtils.BLANK_EMOJI, "__**Management**__", false);
        builder.appendField(prefix + "checkMembers", "Lists the clan members displaying who is registered with the bot.\n*Also can use `" + prefix + "cm`.*", false);
        builder.appendField("Clan Family", "To use the commands family wise you have to add the following to the channel topic:\n`CRTH_clanFamily:[\"#CLAN_TAG-1\",\"#CLAN_TAG-2\",\"#CLAN_TAG-3\",\"#CLAN_TAG-N\"]`\n*Note: The channel topic can have other text, but the before structure has to be present somewhere exactly as it is showed.*\nIf you have a channel configured like this but you still want to use the commands only for your clan you can add the argument `-omc` to the command desire.", false);
        builder.appendField(BotUtils.BLANK_EMOJI, "__**Other**__", false);
        builder.appendField(prefix + "card <card name>", "Retrieves the card information.", false);
        builder.appendField(prefix + "cardAliases", "Prints each card with it aliases.", false);
        builder.appendField(prefix + "help [-cc]", "Prints this help. If the argument `-cc` is present it will print the help on the invoked channel.\n<:blank:534902434712256514>", false);
        builder.appendField("Join my", "[Discord!](https://discord.gg/j6S4Jpd)", true);
        builder.appendField("Invite me!", "[link](https://discordapp.com/oauth2/authorize?&client_id=534149301580988475&scope=bot&permissions=10240)", true);
        builder.appendField("Made by", "Eitri Ymir#1453", true);

        builder.withFooterText("Version 1.6.4");

        if (onInvokedChannel) {
            BotUtils.sendMessage(event.getChannel(), builder.build());
        } else {
            BotUtils.sendMessage(event.getAuthor().getOrCreatePMChannel(), builder.build());
        }
    }

    /**
     * Prints the card aliases list.
     *
     * @param event
     */
    public static void printCardAliasList(MessageReceivedEvent event) {
        Map<Set<String>, String> aliasesMap = CardUtils.ALIASES_MAP;
        StringBuffer stringBuffer = new StringBuffer();
        StringBuilder cardName = new StringBuilder();
        List<String> cardList = new ArrayList<>();

        for (Map.Entry<Set<String>, String> alias : aliasesMap.entrySet()) {
            for (String cardNamePart : alias.getValue().split("-")) {
                cardName.append(StringUtils.capitalize(cardNamePart)).append(" ");
            }
            stringBuffer.append("**")
                    .append(cardName.toString().trim())
                    .append("**: ")
                    .append(alias.getKey().stream().collect(Collectors.joining(", ")))
                    .append("\n");
            cardList.add(stringBuffer.toString());
            stringBuffer.setLength(0);
            cardName.setLength(0);
        }

        cardList.sort(String.CASE_INSENSITIVE_ORDER);

        EmbedBuilder builder = new EmbedBuilder();
        builder.withColor(new Color(COLOR));
        builder.withTitle("Card aliases");

        Iterator<String> iterator = cardList.iterator();
        int cardCounter = 0;
        while (iterator.hasNext()) {
            cardCounter++;
            stringBuffer.append(iterator.next());
            if (cardCounter == 20) {
                cardCounter = 0;
                builder.appendField(BotUtils.BLANK_EMOJI, stringBuffer.toString(), false);
                stringBuffer.setLength(0);
            }
        }

        if (cardCounter > 0) {
            builder.appendField(BotUtils.BLANK_EMOJI, stringBuffer.toString(), false);
        }

        BotUtils.sendMessage(event.getChannel(), builder.build());
    }

    /**
     * Prints the card information of the card passed by parameter.
     *
     * @param event
     * @param args
     */
    public static void getCardInfo(MessageReceivedEvent event, List<String> args) {
        if (args.size() > 0) {
            try {
                String cardName = BotUtils.getCardName(args);
                String cardResponse = ClashRoyaleAPIHelper.getCardByName(cardName);
                if (cardResponse.isEmpty()) {
                    BotUtils.sendMessage(event.getChannel(), event.getAuthor().mention() + ", the card `" + cardName + "` doesn't exists.");
                    return;
                }

                JSONObject cardInfo = new JSONObject(cardResponse);
                printCardInfo(cardInfo, event);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            BotUtils.sendMessage(event.getChannel(), event.getAuthor().mention() + ", missing card name.");
        }
    }

    /**
     * Prints the card information of the card passed by parameter.
     *
     * @param cardInfo
     * @param event
     */
    private static void printCardInfo(JSONObject cardInfo, MessageReceivedEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.withTitle(cardInfo.getString("name"));
        builder.withDesc(cardInfo.getString("description"));
        //builder.withAuthorName(author.getName()).withAuthorIcon(author.getAvatarURL());
        String rarity = cardInfo.getString("rarity");
        builder.appendField("Rarity", rarity, true);
        builder.appendField("Elixir cost", Integer.toString(cardInfo.getInt("elixir")), true);
        builder.appendField("Type", cardInfo.getString("type"), true);

        BotUtils.setRarityColor(builder, rarity);

        String idName = cardInfo.getString("key");
        builder.withThumbnail("https://raw.githubusercontent.com/RoyaleAPI/cr-api-assets/master/cards/" + idName + ".png");
        BotUtils.sendMessage(event.getChannel(), builder.build());
    }
}
