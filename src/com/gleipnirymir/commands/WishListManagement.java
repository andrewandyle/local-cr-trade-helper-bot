package com.gleipnirymir.commands;

import com.gleipnirymir.exceptions.CrthException;
import com.gleipnirymir.helpers.CardEmojis;
import com.gleipnirymir.helpers.ClashRoyaleAPIHelper;
import com.gleipnirymir.helpers.WisherManager;
import com.gleipnirymir.model.Wish;
import com.gleipnirymir.utils.BotUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.gleipnirymir.utils.CardUtils.Rarity.*;

/**
 * This class has all the commands related to the wish lists.
 */
public class WishListManagement {

    private static final Logger logger = LogManager.getLogger(WishListManagement.class);
    private static final String CARD_NAME_KEY = "name";
    private static final String LEFT_TO_UPGRADE_KEY = "leftToUpgrade";
    private static final String DISPLAY_LEVEL_KEY = "displayLevel";

    private static WishListManagement instance = null;

    private WishListManagement() {
    }

    public static synchronized WishListManagement getInstance() {
        if (instance == null) {
            instance = new WishListManagement();
        }
        return instance;
    }

    private static String buildWishChunk(List<String> wishes) {
        StringBuilder chunk = new StringBuilder();
        for (String wish : wishes) {
            chunk.append(wish + "\n");
        }
        return chunk.toString();
    }
    
    // +* Andrew Le - commented out code involved using the MySQL database, which isn't used in the local version.

    /**
     * Adds a card to the wish list of the author's (player tag) wish list.
     *
     * @param event
     * @param args
     * @param playerTag
     */
    public void add(MessageReceivedEvent event, List<String> args, String playerTag) {
        IUser author = event.getAuthor();
//        String playerTag = BotUtils.validateTag(event);
//        if (playerTag == null) return;

        if (args.isEmpty()) {
            BotUtils.sendMessage(event.getChannel(), event.getAuthor().mention() + ", you need to add the card you want to add.");
            return;
        }

        String cardName = BotUtils.getCardName(args);

        String cardResponse = "";
        try {
            cardResponse = ClashRoyaleAPIHelper.getCardByName(cardName);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        if (cardResponse.isEmpty()) {
            BotUtils.sendMessage(event.getChannel(), author.mention() + ", the card `" + cardName + "` doesn't exist.");
            return;
        }
        cardName = new JSONObject(cardResponse).getString(CARD_NAME_KEY);
        if (WisherManager.existsWishForAuthorByCardName(playerTag, cardName)) {
            BotUtils.sendMessage(event.getChannel(), author.mention() + ", the card " + cardName + " is already in your wish list.");
            return;
        }
        try {
            String playerCardByName = ClashRoyaleAPIHelper.getPlayerCardByName(playerTag, cardName);
            // The player didn't unlock that card, yet.
            if (!playerCardByName.isEmpty()) {
                JSONObject playerCardInfo = new JSONObject(playerCardByName);

                int cardLevel = playerCardInfo.getInt(DISPLAY_LEVEL_KEY);
                boolean isToBeMaxed = cardLevel == 12 && (!playerCardInfo.has(LEFT_TO_UPGRADE_KEY) || playerCardInfo.getInt(LEFT_TO_UPGRADE_KEY) <= 0);
                if (BotUtils.isCardMaxed(playerCardInfo) || isToBeMaxed) {
                    BotUtils.sendMessage(event.getChannel(), author.mention() + ", your card " + cardName + " is already maxed.");
                    return;
                }
            }
        } catch (CrthException e) {
            logger.error(e.getMessage(), e);
            BotUtils.sendErrorMessage(event, e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        } finally {
            BotUtils.setTypingStatus(event.getChannel(), false);
        }

        WisherManager.addWish(playerTag, cardName);
        BotUtils.sendMessage(event.getChannel(), author.mention() + ", the card " + cardName + " was successfully added to your wish list.");
    }

    /**
     * Deletes a wish from the author (playerTag) wish list.
     *
     * @param event
     * @param args
     * @param playerTag
     */
    public void delete(MessageReceivedEvent event, List<String> args, String playerTag) {
        IUser author = event.getAuthor();

//        String playerTag = BotUtils.validateTag(event);
//        if (playerTag == null) return;

        if (args.isEmpty()) {
            BotUtils.sendMessage(event.getChannel(), event.getAuthor().mention() + ", you need to add the card you want to remove.");
            return;
        }

        String cardName = BotUtils.getCardName(args);

        String cardResponse = "";
        try {
            cardResponse = ClashRoyaleAPIHelper.getCardByName(cardName);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        if (cardResponse.isEmpty()) {
            BotUtils.sendMessage(event.getChannel(), author.mention() + ", the card `" + cardName + "` doesn't exists.");
            return;
        }
        cardName = new JSONObject(cardResponse).getString(CARD_NAME_KEY);
        if (!WisherManager.existsWishForAuthorByCardName(playerTag, cardName)) {
            BotUtils.sendMessage(event.getChannel(), author.mention() + ", the card " + cardName + " is not in your wish list.");
            return;
        }
        if (WisherManager.deleteWish(playerTag, cardName)) {
            BotUtils.sendMessage(event.getChannel(), author.mention() + ", the card " + cardName + " was successfully removed from your wish list.");
        } else {
            BotUtils.sendMessage(event.getChannel(), author.mention() + ", there was a problem removing the card " + cardName + " from your wish list.");
        }
    }

    /**
     * Prints author (playerTag) wish list.
     *
     * @param event
     * @param args
     * @param playerTag
     */
    public void print(MessageReceivedEvent event, List<String> args, String playerTag) {
        IUser author = event.getAuthor();
//        String playerTag = BotUtils.validateTag(event);
//        IUser wishListOwner = author;
//        if (playerTag == null) return;

//        if (args.size() > 0) {
//            String playerToCheck = args.get(0);
//
//            if (playerToCheck.startsWith("<@")) {
//                wishListOwner = event.getMessage().getMentions().get(0);
//                playerTag = BotUtils.getPlayerTag(wishListOwner);
//
//                if (playerTag.isEmpty()) {
//                    BotUtils.sendMessage(event.getChannel(), author.mention() + ", you can't see that player wish list because doesn't have a tag associated to the user.");
//                    return;
//                }
//
//            }
//        }

        List<Wish> wishes = WisherManager.getWishes(playerTag);
        if (wishes.isEmpty()) {
            BotUtils.sendMessage(event.getChannel(), author.mention() + ", your wish list is empty. Use `" + BotUtils.BOT_PREFIX + "help` to learn how to add cards to it.");
        } else {
            String beforeEmbedContent = author.mention() + ", this is the wish list you asked for:";
            EmbedBuilder builder = new EmbedBuilder();
            List<String> commonWishes = new ArrayList<>();
            List<String> rareWishes = new ArrayList<>();
            List<String> epicWishes = new ArrayList<>();
            List<String> legendaryWishes = new ArrayList<>();
            
            // +* Andrew Le - any wishes that are already maxed out are added here.
            // They are all removed from the wishlist after iteration, preventing concurrency issues.
            List<String> cardsToRemove = new ArrayList<>();

            String authorName = author.getName();

            builder.withAuthorName(authorName + "'s wishes").withAuthorIcon(author.getAvatarURL());
            builder.withColor(new Color(0x8B8B8B));
            //builder.withTitle("Wish list");
            for (Wish wish : wishes) {

                try {
                    String cardResponse = ClashRoyaleAPIHelper.getPlayerCardByName(playerTag, wish.getCardName());
                    JSONObject cardInfo;
                    String levelDisplayString;
                    boolean cardIsMaxed = false;
                    // The played didn't unlock the card, yet.
                    if (cardResponse.isEmpty()) {
                        String cardResponseInfo = ClashRoyaleAPIHelper.getCardByName(wish.getCardName());
                        cardInfo = new JSONObject(cardResponseInfo);
                        levelDisplayString = "Not unlocked";
                    } else {
                        cardInfo = new JSONObject(cardResponse);

                        int cardLevel = cardInfo.getInt(DISPLAY_LEVEL_KEY);
                        int cardQuantity = cardInfo.getInt("count");
                        String cardRequiredForNextLevel = String.valueOf(cardInfo.get("requiredForUpgrade"));
                        if (BotUtils.isCardMaxed(cardInfo)) {
                            cardRequiredForNextLevel = "Maxed";
                            cardIsMaxed = true;
                        }
                        levelDisplayString = String.format("Level %d (%d/%s)", cardLevel, cardQuantity, cardRequiredForNextLevel);
                    }
                    String cardDisplayName = cardInfo.getString(CARD_NAME_KEY);
                    // +* Andrew Le - add maxed wishes to this list, so we can remove them all later
                    if (cardIsMaxed) {
                    	cardsToRemove.add(cardDisplayName);
                    }
                    String cardKey = cardInfo.getString("key");
                    String wishRow;
                    // +* Andrew Le - if a card is prioritized, put * next to its display on the wishlist as indication
                	if (WisherManager.cardIsPriority(playerTag, cardDisplayName)) {
                	    wishRow = String.format("%s ***%s** `%s`", CardEmojis.getEmoji(cardKey), cardDisplayName, levelDisplayString); // extra asterisk
                	} else {
                	    wishRow = String.format("%s **%s** `%s`", CardEmojis.getEmoji(cardKey), cardDisplayName, levelDisplayString);
                	}
                    String rarity = cardInfo.getString("rarity");

                    if (COMMON.toString().equals(rarity) && !cardIsMaxed) {
                        commonWishes.add(wishRow);
                    }
                    if (RARE.toString().equals(rarity) && !cardIsMaxed) {
                        rareWishes.add(wishRow);
                    }
                    if (EPIC.toString().equals(rarity) && !cardIsMaxed) {
                        epicWishes.add(wishRow);
                    }
                    if (LEGENDARY.toString().equals(rarity) && !cardIsMaxed) {
                        legendaryWishes.add(wishRow);
                    }

                } catch (CrthException e) {
                    logger.error(e.getMessage(), e);
                    BotUtils.sendErrorMessage(event, e.getMessage());
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    e.printStackTrace();
                }

            }
            
            // +* Andrew Le - remove all the maxed wishes at once
            for (String cardToRemove : cardsToRemove) {
            	WisherManager.deleteWish(playerTag, cardToRemove);
            }

            if (!commonWishes.isEmpty()) {
                String commonChunk = buildWishChunk(commonWishes);
                builder.appendField("Commons", commonChunk, false);
            }
            if (!rareWishes.isEmpty()) {
                String rareChunk = buildWishChunk(rareWishes);
                builder.appendField("Rares", rareChunk, false);
            }
            if (!epicWishes.isEmpty()) {
                String epicChunk = buildWishChunk(epicWishes);
                builder.appendField("Epics", epicChunk, false);
            }
            if (!legendaryWishes.isEmpty()) {
                String legendaryChunk = buildWishChunk(legendaryWishes);
                builder.appendField("Legendaries", legendaryChunk, false);
            }

            BotUtils.sendMessage(event.getChannel(), beforeEmbedContent, builder.build());
        }
    }
    
    /**
     * +* Andrew Le - Makes a wish currently in the wish list a prioritized wish. (one per rarity)
     *
     * @param event
     * @param args
     * @param playerTag
     */
    public void prioritize(MessageReceivedEvent event, List<String> args, String playerTag) {
    	IUser author = event.getAuthor();

    	if (args.isEmpty()) {
    		BotUtils.sendMessage(event.getChannel(), event.getAuthor().mention() + ", you need to add the card you want to prioritize.");
    		return;
    	}

    	String cardName = BotUtils.getCardName(args);

    	String cardResponse = "";
    	try {
    		cardResponse = ClashRoyaleAPIHelper.getCardByName(cardName);
    	} catch (IOException e) {
    		logger.error(e.getMessage(), e);
    	}
    	if (cardResponse.isEmpty()) {
    		BotUtils.sendMessage(event.getChannel(), author.mention() + ", the card `" + cardName + "` doesn't exist.");
    		return;
    	}
    	cardName = new JSONObject(cardResponse).getString("name");
    	// Check that the card we want to prioritize is in the wishlist
    	if (!WisherManager.existsWishForAuthorByCardName(playerTag, cardName)) {
    		BotUtils.sendMessage(event.getChannel(), author.mention() + ", the card " + cardName + " is not in your wish list.");
    		return;
    	}
    	// Check that the card we want to prioritize isn't already a priority
    	if (WisherManager.cardIsPriority(playerTag, cardName)) {
    		BotUtils.sendMessage(event.getChannel(), author.mention() + ", the card " + cardName + " is already prioritized.");
    		return;
    	}

    	JSONObject cardInfo = new JSONObject(cardResponse);
    	String rarity = cardInfo.getString("rarity");
    	
    	// We only allow one priority per rarity, so check if a priority of that rarity already exists
    	List<Wish> priorities = WisherManager.getPlayerPriorities(playerTag);
    	for (Wish priority : priorities) {
    		String otherCardName = priority.getCardName();
    		try {
    			String otherCardResponse = ClashRoyaleAPIHelper.getCardByName(otherCardName);
    			JSONObject otherCardInfo = new JSONObject(otherCardResponse);
    			String otherRarity = otherCardInfo.getString("rarity");
    			// If a priority of that rarity already exists, delete it. We're replacing the priority for that rarity
    			if (rarity.equals(otherRarity)) {
    				WisherManager.deletePriority(playerTag, otherCardName);
    			}
    		} catch (IOException e) {
    			logger.error(e.getMessage(), e);
    		}
    	}
    	// Finally, prioritize the wish (change its priority field to true)
    	WisherManager.addPriority(playerTag, cardName);
    	BotUtils.sendMessage(event.getChannel(), author.mention() + ", the card " + cardName + " was successfully prioritized.");
    }
    
    /**
     * +* Andrew Le - Makes a currently prioritized card no longer a priority.
     *
     * @param event
     * @param args
     * @param playerTag
     */
    public void deletePriority(MessageReceivedEvent event, List<String> args, String playerTag) {
    	IUser author = event.getAuthor();

    	if (args.isEmpty()) {
    		BotUtils.sendMessage(event.getChannel(), event.getAuthor().mention() + ", you need to add the card that you want to deprioritize.");
    		return;
    	}
    	String cardName = BotUtils.getCardName(args);

    	String cardResponse = "";
    	try {
    		cardResponse = ClashRoyaleAPIHelper.getCardByName(cardName);
    	} catch (IOException e) {
    		logger.error(e.getMessage(), e);
    	}
    	if (cardResponse.isEmpty()) {
    		BotUtils.sendMessage(event.getChannel(), author.mention() + ", the card `" + cardName + "` doesn't exist.");
    		return;
    	}
    	// Check that the card we want to deprioritize is in the wishlist
    	cardName = new JSONObject(cardResponse).getString("name");
    	if (!WisherManager.existsWishForAuthorByCardName(playerTag, cardName)) {
    		BotUtils.sendMessage(event.getChannel(), author.mention() + ", the card " + cardName + " is not in your wish list.");
    		return;
    	}
    	// Check that the card we want to deprioritize is a priority
    	if (!WisherManager.cardIsPriority(playerTag, cardName)) {
    		BotUtils.sendMessage(event.getChannel(), author.mention() + ", the card " + cardName + " is not a priority.");
    		return;
    	}
    	// Delete the priority (set priority field to false)
    	if (WisherManager.deletePriority(playerTag, cardName)) {
    		BotUtils.sendMessage(event.getChannel(), author.mention() + ", the card " + cardName + " was succesfully deprioritized.");
    	} else {
    		BotUtils.sendMessage(event.getChannel(), author.mention() + ", there was a problem removing " + cardName + " as a priority.");
    	}
    }

}
