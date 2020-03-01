package com.gleipnirymir.commands;

import com.gleipnirymir.exceptions.CrthException;
import com.gleipnirymir.helpers.CardEmojis;
import com.gleipnirymir.helpers.ClashRoyaleAPIHelper;
import com.gleipnirymir.helpers.MemberCardInfoBuilder;
import com.gleipnirymir.helpers.WisherManager;
import com.gleipnirymir.model.MemberCardInfo;
import com.gleipnirymir.model.Wish;
import com.gleipnirymir.utils.BotUtils;
import com.gleipnirymir.utils.CardUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.gleipnirymir.utils.CardUtils.Rarity.*;
import static java.util.stream.Collectors.*;

public class TradeCommands {

    private static final Logger logger = LogManager.getLogger(TradeCommands.class);

    private static final int MAXED_CARD_QUANTITY = 999999;
    private static final int NUMBER_THREADS = 20;
    private static final String CODE_SNIPPET = "```";
    private static final String ALL_RARITIES = "ALL";

    /**
     * Prints the players that have the card specified.
     *
     * @param event
     * @param args
     * @param showAll
     */
    public static void getPlayersByCard(MessageReceivedEvent event, List<String> args, boolean showAll, String playerTag) {
    	Instant start = Instant.now();
    	//        boolean onlyMyClan = BotUtils.onlyMyClan(args);

    	if (args.isEmpty()) {
    		BotUtils.sendMessage(event.getChannel(), event.getAuthor().mention() + ", you need to add the card you are looking for.");
    		return;
    	}

    	IUser author = event.getAuthor();
    	//        String playerTag = BotUtils.validateTag(event);
    	//        if (playerTag == null) return;

    	String cardName = BotUtils.getCardName(args);

    	try {
    		String cardResponse = ClashRoyaleAPIHelper.getCardByName(cardName);
    		if (cardResponse.isEmpty()) {
    			BotUtils.sendMessage(event.getChannel(), author.mention() + ", the card `" + cardName + "` doesn't exists.");
    			return;
    		}
    		JSONObject cardInfo = new JSONObject(cardResponse);
    		cardName = cardInfo.getString("name");

    		String playerInfo = ClashRoyaleAPIHelper.getPlayerInfo(playerTag);
    		JSONObject playerInfoJson = new JSONObject(playerInfo);
    		if (BotUtils.isClanless(event, playerInfoJson)) return;

    		JSONObject clanJson = playerInfoJson.getJSONObject("clan");
    		String authorClanTag = clanJson.getString("tag");
    		AtomicInteger clanMembersQuantity = new AtomicInteger();

    		String cardRarity = cardInfo.getString("rarity");
    		String cardKey = cardInfo.getString("key");
    		String cardDisplayName = cardInfo.getString("name");

    		Integer minTradeQuantity = getTradeQuantity(cardRarity);

    		//            List<String> clansFromFamily = BotUtils.getValidClans(event, authorClanTag, onlyMyClan);
    		// TODO: Make it multithreading.


    		String clanInfo = ClashRoyaleAPIHelper.getClanInfo(authorClanTag);
    		JSONObject clanInfoJson = new JSONObject(clanInfo);
    		String clanTag = clanInfoJson.getString("tag");
    		String clanName = clanInfoJson.getString("name");
    		String clanIcon = clanInfoJson.getJSONObject("badge").getString("image");

    		EmbedBuilder builder = new EmbedBuilder();
    		builder.withAuthorName(clanName + " [#" + clanTag + "]");
    		builder.withAuthorIcon(clanIcon);
    		builder.withAuthorUrl("https://royaleapi.com/clan/" + clanTag);
    		builder.withTitle("Who can trade " + cardDisplayName + "?");
    		builder.withDesc("Displaying clan members of " + clanName);
    		builder.appendField("Searching for members...", "Please wait.", false);
    		builder.withFooterText("Be patient. This can take a bit.");

    		BotUtils.setRarityColor(builder, cardRarity);
    		builder.withThumbnail("https://raw.githubusercontent.com/RoyaleAPI/cr-api-assets/master/cards/" + cardKey + ".png");
    		long messageId = BotUtils.sendMessage(event.getChannel(), builder.build());
    		BotUtils.setTypingStatus(event.getChannel(), true);

    		String clanMembers = ClashRoyaleAPIHelper.getClanMembers(clanTag);
    		JSONArray clanMembersJson = new JSONArray(clanMembers);
    		List<MemberCardInfo> membersCardTradeInfoList = new ArrayList<>();

    		String finalCardName = cardName;
    		final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_THREADS);
    		final List<Future<?>> futures = new ArrayList<>();
    		for (Object clanMemberJson : clanMembersJson) {
    			JSONObject memberJson = (JSONObject) clanMemberJson;
    			String memberTag = memberJson.getString("tag");
    			Future<?> future = executor.submit(() -> {
    				try {
    					String memberInfo = ClashRoyaleAPIHelper.getPlayerInfo(memberTag);
    					JSONObject memberInfoJson = new JSONObject(memberInfo);
    					String memberName = memberInfoJson.getString("name");
    					String playerCardInfo = ClashRoyaleAPIHelper.getPlayerCardInfo(cardKey, memberInfoJson);
    					int cardQuantity = 0;
    					if (!playerCardInfo.isEmpty()) {
    						JSONObject memberCardInfoJson = new JSONObject(playerCardInfo);
    						cardQuantity = memberCardInfoJson.getInt("count");
    						if (cardRarity.equals("Legendary") && memberCardInfoJson.getInt("level") == 1) {
    							cardQuantity--;
    						}
    						if (BotUtils.isCardMaxed(memberCardInfoJson)) {
    							if (WisherManager.existsWishForAuthorByCardName(memberTag, cardDisplayName)) {
    								System.out.println(cardDisplayName + " REMOVED in whoHas");
    								WisherManager.deleteWish(memberTag, cardDisplayName);
    							}
    							cardQuantity = MAXED_CARD_QUANTITY;
    						}
    					}
    					if (showAll || cardQuantity == MAXED_CARD_QUANTITY || cardQuantity >= minTradeQuantity) {
    						membersCardTradeInfoList.add(new MemberCardInfoBuilder()
    								.setMemberName(memberName)
    								.setMemberTag(memberTag)
    								.setCardName(finalCardName)
    								.setCardQuantity(cardQuantity)
    								.build());
    					}
    					clanMembersQuantity.getAndIncrement();

    				} catch (IOException e) {
    					logger.error(e.getMessage(), e);
    				}
    				return memberTag;
    			});
    			futures.add(future);

    		}
    		try {
    			for (Future<?> future : futures) {
    				String memberTagString = (String) future.get();
    				logger.info(memberTagString + " done.");
    			}
    		} catch (InterruptedException | ExecutionException e) {
    			logger.error(e.getMessage(), e);
    		} finally {
    			shutdown(executor);
    		}

    		// TODO: Seems that I have concurrency problems here. Try to do multiple request fast to reproduce.
    		membersCardTradeInfoList.sort(Comparator.comparingInt(MemberCardInfo::getCardQuantity).reversed().thenComparing(MemberCardInfo::getMemberName, String.CASE_INSENSITIVE_ORDER));

    		StringBuilder membersDisplayStringPart1 = new StringBuilder();
    		appendTitleLine(membersDisplayStringPart1);

    		StringBuilder membersDisplayStringPart2 = new StringBuilder();
    		appendTitleLine(membersDisplayStringPart2);

    		String okChar = "+\u2714 ";   // ✔
    		String noChar = "-\u2716 ";   // ✖

    		int listSize = membersCardTradeInfoList.size();
    		boolean hasPageBreak = listSize > 20;
    		int pageBreak = hasPageBreak ? listSize / 2 : listSize;

    		Iterator<MemberCardInfo> memberCardTradeInfoIterator = membersCardTradeInfoList.iterator();
    		int canTrade = 0;
    		while (memberCardTradeInfoIterator.hasNext()) {
    			MemberCardInfo mcti = memberCardTradeInfoIterator.next();

    			boolean okCondition = (mcti.getCardQuantity() == MAXED_CARD_QUANTITY || mcti.getCardQuantity() >= minTradeQuantity) && !WisherManager.existsWishForAuthorByCardName(mcti.getMemberTag(), cardName);
    			String cardQuantityString = mcti.getCardQuantity() == MAXED_CARD_QUANTITY ? "MAXED" : String.valueOf(mcti.getCardQuantity());
    			if (pageBreak > 0) {
    				if (okCondition) {
    					canTrade++;
    					membersDisplayStringPart1.append(okChar);
    				} else {
    					membersDisplayStringPart1.append(noChar);
    				}

    				membersDisplayStringPart1.append(StringUtils.leftPad(cardQuantityString, 5))
    				.append(" ");
    				membersDisplayStringPart1.append(StringUtils.rightPad(mcti.getMemberName(), 17));
    				membersDisplayStringPart1.append("\n");
    				pageBreak--;

    			} else {
    				if (okCondition) {
    					canTrade++;
    					membersDisplayStringPart2.append(okChar);
    				} else {
    					membersDisplayStringPart2.append(noChar);
    				}

    				membersDisplayStringPart2.append(StringUtils.leftPad(cardQuantityString, 5))
    				.append(" ");
    				membersDisplayStringPart2.append(StringUtils.rightPad(mcti.getMemberName(), 17));
    				membersDisplayStringPart2.append("\n");

    			}

    		}

    		membersDisplayStringPart1.append(CODE_SNIPPET);
    		membersDisplayStringPart2.append(CODE_SNIPPET);

    		//event.getChannel().getMessageByID(messageId).delete();

    		builder.clearFields();
    		builder.appendField(String.valueOf(canTrade) + " of " + String.valueOf(clanMembersQuantity) + " members found", membersDisplayStringPart1.toString(), false);
    		if (hasPageBreak) {
    			builder.appendField("Continue...", membersDisplayStringPart2.toString(), false);
    		}
    		builder.withFooterText("Green (" + okChar.trim() + ") has enough to trade. Red (" + noChar.trim() + ") hasn't enough or has it in the wish list.");
    		//BotUtils.sendMessage(event.getChannel(), builder.build());

    		clanMembersQuantity.set(0);

    		// TODO: Seems that when is too fast it can't edit the original message due to a Discord limitation.
    		// Not the best solution but maybe if the time elapsed is less than 1 second can hold until that to edit the message.
    		long timeToWait = 1000 - Duration.between(start, Instant.now()).toMillis();
    		if (timeToWait > 0) {
    			Thread.sleep(timeToWait);
    		}
    		event.getChannel().getMessageByID(messageId).edit(builder.build());


    	} catch (CrthException e) {
    		logger.error(e.getMessage(), e);
    		BotUtils.sendErrorMessage(event, e.getMessage());
    	} catch (IOException | InterruptedException e) {
    		logger.error(e.getMessage(), e);
    	} finally {
    		BotUtils.setTypingStatus(event.getChannel(), false);
    	}

    	Instant finish = Instant.now();
    	long timeElapsed = Duration.between(start, finish).toMillis();
    	logger.info("Time elapsed: " + timeElapsed + " ms (" + timeElapsed / 1000 + " s)");

    }

    /**
     * Prints the players that wants (have it on their wish list) the card specified.
     *
     * @param event
     * @param args
     */
    public static void getPlayersWishByCard(MessageReceivedEvent event, List<String> args, String playerTag) {

    	Instant start = Instant.now();

    	//        boolean onlyMyClan = BotUtils.onlyMyClan(args);

    	if (args.isEmpty()) {
    		BotUtils.sendMessage(event.getChannel(), event.getAuthor().mention() + ", you need to add the card you want to know about.");
    		return;
    	}
    	
    	String rarity = getRarity(args);
    	boolean getByRarity = !rarity.equals(ALL_RARITIES);
    	if (getByRarity) {
    		getPlayersWishByRarity(event, args, playerTag, rarity);
    		return;
    	}
    	

    	IUser author = event.getAuthor();
    	//        String playerTag = BotUtils.validateTag(event);
    	//        if (playerTag == null) return;

    	String cardName = BotUtils.getCardName(args);

    	try {

    		String cardResponse = ClashRoyaleAPIHelper.getCardByName(cardName);
    		if (cardResponse.isEmpty()) {
    			BotUtils.sendMessage(event.getChannel(), author.mention() + ", the card `" + cardName + "` doesn't exists.");
    			return;
    		}
    		JSONObject cardInfo = new JSONObject(cardResponse);
    		cardName = cardInfo.getString("name");

    		String playerInfo = ClashRoyaleAPIHelper.getPlayerInfo(playerTag);
    		JSONObject playerInfoJson = new JSONObject(playerInfo);
    		if (BotUtils.isClanless(event, playerInfoJson)) return;
    		JSONObject clanJson = playerInfoJson.getJSONObject("clan");
    		String authorClanTag = clanJson.getString("tag");

    		AtomicInteger clanMembersQuantity = new AtomicInteger();

    		String cardRarity = cardInfo.getString("rarity");
    		String cardKey = cardInfo.getString("key");
    		String cardDisplayName = cardInfo.getString("name");

    		//            List<String> clansFromFamily = BotUtils.getValidClans(event, authorClanTag, onlyMyClan);
    		// TODO: Make it multithreading.


    		String clanInfo = ClashRoyaleAPIHelper.getClanInfo(authorClanTag);
    		JSONObject clanInfoJson = new JSONObject(clanInfo);
    		String clanTag = clanInfoJson.getString("tag");
    		String clanName = clanInfoJson.getString("name");
    		String clanIcon = clanInfoJson.getJSONObject("badge").getString("image");

    		EmbedBuilder builder = new EmbedBuilder();
    		builder.withAuthorName(clanName + " [#" + clanTag + "]");
    		builder.withAuthorIcon(clanIcon);
    		builder.withAuthorUrl("https://royaleapi.com/clan/" + clanTag);
    		builder.withTitle("Who wants " + cardDisplayName + "?");
    		builder.withDesc("Displaying clan members of " + clanName);
    		builder.appendField("Searching for members...", "Please wait.", false);
    		builder.withFooterText("Be patient. This can take a bit.");

    		BotUtils.setRarityColor(builder, cardRarity);
    		builder.withThumbnail("https://raw.githubusercontent.com/RoyaleAPI/cr-api-assets/master/cards/" + cardKey + ".png");
    		long messageId = BotUtils.sendMessage(event.getChannel(), builder.build());
    		BotUtils.setTypingStatus(event.getChannel(), true);

    		List<MemberCardInfo> membersWhoWantCardList = getClanMembersWhoWants(cardName, clanTag, clanMembersQuantity, cardKey);

    		membersWhoWantCardList.sort(Comparator.comparingInt(MemberCardInfo::getCardLevel).thenComparingInt(MemberCardInfo::getCardQuantity).reversed().thenComparing(MemberCardInfo::getMemberName, String.CASE_INSENSITIVE_ORDER));

    		StringBuilder membersDisplayStringPart1 = new StringBuilder();
    		StringBuilder membersDisplayStringPart2 = new StringBuilder();

    		int listSize = membersWhoWantCardList.size();
    		boolean hasPageBreak = listSize > 20;
    		int pageBreak = hasPageBreak ? listSize / 2 : listSize;

    		Iterator<MemberCardInfo> memberCardInfoIterator = membersWhoWantCardList.iterator();
    		while (memberCardInfoIterator.hasNext()) {
    			MemberCardInfo mci = memberCardInfoIterator.next();

    			if (pageBreak > 0) {

    				membersDisplayStringPart1.append("**" + mci.getMemberName() + "**  ");
    				membersDisplayStringPart1.append(String.format("`Level %d (%d)`", mci.getCardLevel(), mci.getCardQuantity()));
    				membersDisplayStringPart1.append("\n");
    				pageBreak--;

    			} else {

    				membersDisplayStringPart2.append("**" + mci.getMemberName() + "**  ");
    				membersDisplayStringPart2.append(String.format("`Level %d (%d)`", mci.getCardLevel(), mci.getCardQuantity()));
    				membersDisplayStringPart2.append("\n");

    			}

    		}

    		//event.getChannel().getMessageByID(messageId).delete();

    		builder.clearFields();
    		if (listSize > 0) {
    			builder.appendField(String.valueOf(listSize) + " of " + String.valueOf(clanMembersQuantity) + " members found", membersDisplayStringPart1.toString(), false);
    			if (hasPageBreak) {
    				builder.appendField("Continue...", membersDisplayStringPart2.toString(), false);
    			}
    		} else {
    			builder.appendField("Nobody was found", "Sadly nobody wants this card.", false);
    		}
    		builder.withFooterText("Only displaying those that have the card in the wish list.");
    		//BotUtils.sendMessage(event.getChannel(), builder.build());

    		clanMembersQuantity.set(0);

    		// TODO: Seems that when is too fast it can't edit the original message due to a Discord limitation.
    		// Not the best solution but maybe if the time elapsed is less than 1 second can hold until that to edit the message.
    		long timeToWait = 1000 - Duration.between(start, Instant.now()).toMillis();
    		if (timeToWait > 0) {
    			Thread.sleep(timeToWait);
    		}
    		event.getChannel().getMessageByID(messageId).edit(builder.build());


    	} catch (CrthException e) {
    		logger.error(e.getMessage(), e);
    		BotUtils.sendErrorMessage(event, e.getMessage());
    	} catch (IOException | InterruptedException e) {
    		logger.error(e.getMessage(), e);
    	} finally {
    		BotUtils.setTypingStatus(event.getChannel(), false);
    	}

    	Instant finish = Instant.now();
    	long timeElapsed = Duration.between(start, finish).toMillis();
    	logger.info("Time elapsed: " + timeElapsed + " ms (" + timeElapsed / 1000 + " s)");

    }

    /**
     * Simulates a trade and prints the players that would take it based on the wish lists and cards the players have.
     *
     * @param event
     * @param args
     */
    public static void getPlayersForTrade(MessageReceivedEvent event, List<String> args, String playerTag) {
    	Instant start = Instant.now();

    	if (args.isEmpty() || !args.contains("for")) {
    		BotUtils.sendMessage(event.getChannel(), event.getAuthor().mention() + ", you need to add the card you want to give, then the word `for` and then the card you want to receive.");
    		return;
    	}

    	//        boolean onlyMyClan = BotUtils.onlyMyClan(args);

    	IUser author = event.getAuthor();
    	//        String playerTag = BotUtils.validateTag(event);
    	//        if (playerTag == null) return;

    	String bothCards = BotUtils.getCardName(args);
    	String[] bothCardsArray = bothCards.split(" For ");
    	String cardToGive = bothCardsArray[0];
    	String cardToReceive = bothCardsArray[1];

    	try {
    		String cardToGiveResponse = ClashRoyaleAPIHelper.getCardByName(cardToGive);
    		if (cardToGiveResponse.isEmpty()) {
    			BotUtils.sendMessage(event.getChannel(), author.mention() + ", the card `" + cardToGive + "` doesn't exists.");
    			return;
    		}
    		String cardToReceiveResponse = ClashRoyaleAPIHelper.getCardByName(cardToReceive);
    		if (cardToReceiveResponse.isEmpty()) {
    			BotUtils.sendMessage(event.getChannel(), author.mention() + ", the card `" + cardToReceive + "` doesn't exists.");
    			return;
    		}

    		JSONObject cardToGiveInfo = new JSONObject(cardToGiveResponse);
    		JSONObject cardToReceiveInfo = new JSONObject(cardToReceiveResponse);

    		String cardToGiveRarity = cardToGiveInfo.getString("rarity");
    		String cardToReceiveRarity = cardToReceiveInfo.getString("rarity");

    		cardToGive = cardToGiveInfo.getString("name");
    		cardToReceive = cardToReceiveInfo.getString("name");

    		if (!cardToGiveRarity.equals(cardToReceiveRarity)) {
    			BotUtils.sendMessage(event.getChannel(), author.mention() + ", you can't trade the cards `" + cardToGive + "` and `" + cardToReceive + "` since them are of different rarity.");
    			return;
    		}

    		Integer minTradeQuantity = getTradeQuantity(cardToGiveRarity);

    		// TODO: List them with mention (for that I will need to get the user ID, I will need to re do the saveTag function)
    		// event.getGuild().getUserByID()

    		String playerInfo = ClashRoyaleAPIHelper.getPlayerInfo(playerTag);
    		JSONObject playerInfoJson = new JSONObject(playerInfo);
    		if (BotUtils.isClanless(event, playerInfoJson)) return;
    		JSONObject clanJson = playerInfoJson.getJSONObject("clan");
    		String authorClanTag = clanJson.getString("tag");

    		AtomicInteger clanMembersQuantity = new AtomicInteger();

    		String cardToGiveKey = cardToGiveInfo.getString("key");
    		String cardToGiveDisplayName = cardToGiveInfo.getString("name");
    		String cardToReceiveKey = cardToReceiveInfo.getString("key");
    		String cardToReceiveDisplayName = cardToReceiveInfo.getString("name");

    		// Checking that the author have enough cards of the one that wants to give to do a trade.
    		String playerCardToGiveInfo = ClashRoyaleAPIHelper.getPlayerCardByName(playerTag, cardToGive);
    		JSONObject playerCardToGiveInfoJson = new JSONObject(playerCardToGiveInfo);
    		int cardToGiveQuantity = playerCardToGiveInfoJson.getInt("count");

    		if (!BotUtils.isCardMaxed(playerCardToGiveInfoJson) && cardToGiveQuantity < minTradeQuantity) {
    			BotUtils.sendMessage(event.getChannel(), author.mention() + ", you don't have enough cards of `" + cardToGive + "` to do trade. You have " + cardToGiveQuantity + " and need at least " + minTradeQuantity);
    			return;
    		}

    		//            List<String> clansFromFamily = BotUtils.getValidClans(event, authorClanTag, onlyMyClan);
    		// TODO: Make it multithreading.


    		String clanInfo = ClashRoyaleAPIHelper.getClanInfo(authorClanTag);
    		JSONObject clanInfoJson = new JSONObject(clanInfo);
    		String clanTag = clanInfoJson.getString("tag");
    		String clanName = clanInfoJson.getString("name");
    		String clanIcon = clanInfoJson.getJSONObject("badge").getString("image");

    		// Waiting message, bot processing.
    		EmbedBuilder builder = new EmbedBuilder();
    		builder.withAuthorName(clanName + " [#" + clanTag + "]");
    		builder.withAuthorIcon(clanIcon);
    		builder.withAuthorUrl("https://royaleapi.com/clan/" + clanTag);
    		builder.withTitle("Who can trade...");
    		builder.withDesc(CardEmojis.getEmoji(cardToGiveKey) + " " + cardToGiveDisplayName + " for " + CardEmojis.getEmoji(cardToReceiveKey) + " " + cardToReceiveDisplayName + " ?");
    		builder.appendField("Searching for members...", "Please wait.", false);
    		builder.withFooterText("Be patient. This can take a bit.");

    		BotUtils.setRarityColor(builder, cardToGiveRarity);
    		builder.withThumbnail("https://raw.githubusercontent.com/RoyaleAPI/cr-api-assets/master/trade-tokens/tt-" + cardToGiveRarity.toLowerCase() + ".png");

    		long messageId = BotUtils.sendMessage(event.getChannel(), builder.build());
    		BotUtils.setTypingStatus(event.getChannel(), true);

    		List<MemberCardInfo> membersWhoWantCardList = getClanMembersWhoWants(cardToGive, clanTag, clanMembersQuantity, cardToGiveKey);

    		// TODO: Delete from the list the author, if exists? Maybe it could be good info for other member? Think more.

    		List<MemberCardInfo> membersWhoWantCardListAux = new ArrayList<>(membersWhoWantCardList);

    		String finalCardToReceive = cardToReceive;
    		final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_THREADS);
    		final List<Future<?>> futures = new ArrayList<>();
    		for (MemberCardInfo memberWhoWantCard : membersWhoWantCardListAux) {
    			String memberTag = memberWhoWantCard.getMemberTag();
    			Future<?> future = executor.submit(() -> {
    				try {
    					String memberInfo = ClashRoyaleAPIHelper.getPlayerInfo(memberTag);
    					JSONObject memberInfoJson = new JSONObject(memberInfo);
    					//                        String memberName = memberInfoJson.getString("name");
    					String playerCardInfo = ClashRoyaleAPIHelper.getPlayerCardInfo(cardToReceiveKey, memberInfoJson);
    					int cardQuantity = 0;
    					if (!playerCardInfo.isEmpty()) {
    						JSONObject memberCardInfoJson = new JSONObject(playerCardInfo);
    						cardQuantity = memberCardInfoJson.getInt("count");
    						if (cardToReceiveRarity.equals("Legendary") && memberCardInfoJson.getInt("level") == 1) {
    							cardQuantity--;
    						}
    						if (BotUtils.isCardMaxed(memberCardInfoJson)) {
    							cardQuantity = MAXED_CARD_QUANTITY;
    						}
    					}
    					if (!(cardQuantity == MAXED_CARD_QUANTITY || cardQuantity >= minTradeQuantity) || WisherManager.existsWishForAuthorByCardName(memberTag, finalCardToReceive)) {
    						membersWhoWantCardList.remove(memberWhoWantCard);
    					}

    				} catch (IOException e) {
    					logger.error(e.getMessage(), e);
    				}
    				return memberTag;
    			});
    			futures.add(future);

    		}
    		try {
    			for (Future<?> future : futures) {
    				String memberTagString = (String) future.get();
    				logger.info(memberTagString + " done.");
    			}
    		} catch (InterruptedException | ExecutionException e) {
    			logger.error(e.getMessage(), e);
    		} finally {
    			shutdown(executor);
    		}
    		logger.trace("Sorting info.");
    		membersWhoWantCardList.sort(Comparator.comparingInt(MemberCardInfo::getCardLevel).thenComparingInt(MemberCardInfo::getCardQuantity).reversed().thenComparing(MemberCardInfo::getMemberName, String.CASE_INSENSITIVE_ORDER));

    		StringBuilder membersDisplayStringPart1 = new StringBuilder();
    		StringBuilder membersDisplayStringPart2 = new StringBuilder();

    		int listSize = membersWhoWantCardList.size();
    		boolean hasPageBreak = listSize > 20;
    		int pageBreak = hasPageBreak ? listSize / 2 : listSize;

    		Iterator<MemberCardInfo> memberCardInfoIterator = membersWhoWantCardList.iterator();
    		while (memberCardInfoIterator.hasNext()) {
    			MemberCardInfo mci = memberCardInfoIterator.next();

    			if (pageBreak > 0) {

    				membersDisplayStringPart1.append("**" + mci.getMemberName() + "**  ");
    				membersDisplayStringPart1.append(String.format("`Level %d (%d)`", mci.getCardLevel(), mci.getCardQuantity()));
    				membersDisplayStringPart1.append("\n");
    				pageBreak--;

    			} else {

    				membersDisplayStringPart2.append("**" + mci.getMemberName() + "**  ");
    				membersDisplayStringPart2.append(String.format("`Level %d (%d)`", mci.getCardLevel(), mci.getCardQuantity()));
    				membersDisplayStringPart2.append("\n");

    			}

    		}
    		logger.trace("Building message.");
    		builder.clearFields();
    		if (listSize > 0) {
    			builder.appendField(String.valueOf(listSize) + " of " + String.valueOf(clanMembersQuantity) + " members found", membersDisplayStringPart1.toString(), false);
    			if (hasPageBreak) {
    				builder.appendField("Continue...", membersDisplayStringPart2.toString(), false);
    			}
    		} else {
    			builder.appendField("Nobody was found", "Sadly nobody can accept this deal.", false);
    		}
    		builder.withFooterText("Only displaying those that could love the deal.");

    		long timeToWait = 1000 - Duration.between(start, Instant.now()).toMillis();
    		if (timeToWait > 0) {
    			Thread.sleep(timeToWait);
    		}
    		logger.trace("I am here, about to write the final message.");
    		event.getChannel().getMessageByID(messageId).edit(builder.build());

    		clanMembersQuantity.set(0);

    	} catch (CrthException e) {
    		logger.error(e.getMessage(), e);
    		BotUtils.sendErrorMessage(event, e.getMessage());
    	} catch (IOException | InterruptedException e) {
    		logger.error(e.getMessage(), e);
    	} finally {
    		BotUtils.setTypingStatus(event.getChannel(), false);
    	}

    }

    /**
     * Finds the players that matches the needs of the command caller.
     * <p>
     * It checks the author wishlist and tries to find the best candidates for trading based on the wishlists and cards the players have.
     *
     * @param event
     * @param args
     */
    public static void getSmartTrades(MessageReceivedEvent event, List<String> args, boolean byFreq, boolean maxOnly, String playerTag) {
    	Instant start = Instant.now();

    	//        boolean onlyMyClan = BotUtils.onlyMyClan(args);

    	String rarity = getRarity(args);

    	IUser author = event.getAuthor();
    	//        String playerTag = BotUtils.validateTag(event);
    	//        if (playerTag == null) return;

    	try {

    		List<Wish> wishes = WisherManager.getWishes(playerTag);
    		if (wishes.isEmpty()) {
    			BotUtils.sendMessage(event.getChannel(), author.mention() + ", your wish list is empty. Use `" + BotUtils.BOT_PREFIX + "help` to learn how to add cards to it.");
    			return;
    		}

    		String playerInfo = ClashRoyaleAPIHelper.getPlayerInfo(playerTag);
    		JSONObject playerInfoJson = new JSONObject(playerInfo);
    		if (BotUtils.isClanless(event, playerInfoJson)) return;
    		JSONObject clanJson = playerInfoJson.getJSONObject("clan");
    		String authorClanTag = clanJson.getString("tag");

    		//            List<String> clansFromFamily = BotUtils.getValidClans(event, authorClanTag, onlyMyClan);
    		// TODO: Make it multithreading.
    		// FIX: Optimization. I am going by the wishes for each clan on the list.


    		String clanInfo = ClashRoyaleAPIHelper.getClanInfo(authorClanTag);
    		JSONObject clanInfoJson = new JSONObject(clanInfo);
    		String clanTag = clanInfoJson.getString("tag");
    		String clanName = clanInfoJson.getString("name");
    		String clanIcon = clanInfoJson.getJSONObject("badge").getString("image");


    		// Waiting message, bot processing.
    		EmbedBuilder builder = new EmbedBuilder();
    		builder.withAuthorName(clanName + " [#" + clanTag + "]");
    		builder.withAuthorIcon(clanIcon);
    		builder.withAuthorUrl("https://royaleapi.com/clan/" + clanTag);
    		if (byFreq) {
    			builder.withTitle("Which cards should I offer...");
    		} else {
    			builder.withTitle("Who would trade with me...");
    		}
    		
    		builder.withDesc("?");
    		builder.appendField("Searching for members...", "Please wait.", false);
    		builder.withFooterText("Be patient. This can take a bit.");

    		BotUtils.setRarityColor(builder, rarity);
    		builder.withThumbnail("https://raw.githubusercontent.com/RoyaleAPI/cr-api-assets/master/trade-tokens/tt-" + rarity.toLowerCase() + ".png");

    		long messageId = BotUtils.sendMessage(event.getChannel(), builder.build());
    		BotUtils.setTypingStatus(event.getChannel(), true);

    		List<String> commonWishes = new ArrayList<>();
    		List<String> rareWishes = new ArrayList<>();
    		List<String> epicWishes = new ArrayList<>();
    		List<String> legendaryWishes = new ArrayList<>();
    		
    		List<String> cardsToRemove = new ArrayList<>();

    		Map<String, String> cardTitles = new HashMap<>();

    		for (Wish wish : wishes) {
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

    				int cardLevel = cardInfo.getInt("displayLevel");
    				if (cardLevel == 13) {
    					cardIsMaxed = true;
    					cardsToRemove.add(wish.getCardName());
    				}
    				int cardQuantity = cardInfo.getInt("count");
    				String cardRequiredForNextLevel = String.valueOf(cardInfo.get("requiredForUpgrade"));

    				levelDisplayString = String.format("Level %d (%d/%s)", cardLevel, cardQuantity, cardRequiredForNextLevel);
    			}
    			String cardDisplayName = cardInfo.getString("name");
    			String cardKey = cardInfo.getString("key");
    			String wishRow = String.format("%s **%s** `%s`", CardEmojis.getEmoji(cardKey), cardDisplayName, levelDisplayString);
    			cardTitles.put(cardDisplayName, wishRow);
    			String cardRarity = cardInfo.getString("rarity");
    			if (!cardIsMaxed && checkPriorities(playerTag, cardDisplayName)) {
    				switch (cardRarity) {
    				case "Common":
    					commonWishes.add(cardKey);
    					break;
    				case "Rare":
    					rareWishes.add(cardKey);
    					break;
    				case "Epic":
    					epicWishes.add(cardKey);
    					break;
    				case "Legendary":
    					legendaryWishes.add(cardKey);
    					break;
    				}
    			}
    		}
    		for (String cardToRemove : cardsToRemove) {
    			System.out.println(cardToRemove + " REMOVED in fmt");
    			WisherManager.deleteWish(playerTag, cardToRemove);
    		}

    		List<String> wishesToIterate = new ArrayList<>();
    		switch (rarity) {
    		case "Common":
    			wishesToIterate = commonWishes;
    			break;
    		case "Rare":
    			wishesToIterate = rareWishes;
    			break;
    		case "Epic":
    			wishesToIterate = epicWishes;
    			break;
    		case "Legendary":
    			wishesToIterate = legendaryWishes;
    			break;
    		default:
    			wishesToIterate.addAll(commonWishes);
    			wishesToIterate.addAll(rareWishes);
    			wishesToIterate.addAll(epicWishes);
    			wishesToIterate.addAll(legendaryWishes);
    			break;
    		}

    		String clanMembers = ClashRoyaleAPIHelper.getClanMembers(clanTag);
    		JSONArray clanMembersJson = new JSONArray(clanMembers);

    		Map<String, List<MemberCardInfo>> membersCardsByRarity = new HashMap<>();
    		membersCardsByRarity.put(COMMON.toString(), new ArrayList<>());
    		membersCardsByRarity.put(RARE.toString(), new ArrayList<>());
    		membersCardsByRarity.put(EPIC.toString(), new ArrayList<>());
    		membersCardsByRarity.put(LEGENDARY.toString(), new ArrayList<>());
    		for (String card : wishesToIterate) {

    			final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_THREADS);
    			final List<Future<?>> futures = new ArrayList<>();
    			for (Object clanMemberJson : clanMembersJson) {
    				JSONObject memberJson = (JSONObject) clanMemberJson;
    				String memberTag = memberJson.getString("tag");
    				Future<?> future = executor.submit(() -> {
    					try {
    						String memberInfo = ClashRoyaleAPIHelper.getPlayerInfo(memberTag);
    						JSONObject memberInfoJson = new JSONObject(memberInfo);
    						String memberName = memberInfoJson.getString("name");
    						String playerCardInfo = ClashRoyaleAPIHelper.getPlayerCardInfo(card, memberInfoJson);
    						if (!playerCardInfo.isEmpty()) {
    							JSONObject memberCardInfoJson = new JSONObject(playerCardInfo);
    							int cardQuantity = memberCardInfoJson.getInt("count");
    							String cardRarity = memberCardInfoJson.getString("rarity");
    							if (cardRarity.equals("Legendary") && memberCardInfoJson.getInt("level") == 1) {
    								cardQuantity--;
    							}
    							if (BotUtils.isCardMaxed(memberCardInfoJson)) {
    								cardQuantity = MAXED_CARD_QUANTITY;
    							}
    							
    							String cardNameDisplay = memberCardInfoJson.getString("name");

    							if (!WisherManager.existsWishForAuthorByCardName(memberTag, cardNameDisplay) && (cardQuantity == MAXED_CARD_QUANTITY || cardQuantity >= getTradeQuantity(cardRarity))) {
    								List<MemberCardInfo> memberCardTradeInfos = membersCardsByRarity.get(cardRarity);
    								memberCardTradeInfos.add(new MemberCardInfoBuilder()
    										.setMemberName(memberName)
    										.setMemberTag(memberTag)
    										.setCardName(cardNameDisplay)
    										.setCardQuantity(cardQuantity)
    										.build()
    										);
    							}
    						}

    					} catch (IOException e) {
    						logger.error(e.getMessage(), e);
    					}
    					return memberTag;
    				});
    				futures.add(future);

    			}
    			try {
    				for (Future<?> future : futures) {
    					String memberTagString = (String) future.get();
    					logger.info(memberTagString + " done.");
    				}
    			} catch (InterruptedException | ExecutionException e) {
    				logger.error(e.getMessage(), e);
    			} finally {
    				shutdown(executor);
    			}

    		}

    		Comparator<MemberCardInfo> comparator = Comparator.comparing(MemberCardInfo::getCardName)
    				.thenComparing(Comparator.comparingInt(MemberCardInfo::getCardQuantity).reversed())
    				.thenComparing(MemberCardInfo::getMemberName, String.CASE_INSENSITIVE_ORDER);

    		membersCardsByRarity.get(COMMON.toString()).sort(comparator);
    		membersCardsByRarity.get(RARE.toString()).sort(comparator);
    		membersCardsByRarity.get(EPIC.toString()).sort(comparator);
    		membersCardsByRarity.get(LEGENDARY.toString()).sort(comparator);

    		builder.clearFields();
    		if (byFreq) {
    			builder.withFooterText("Trade options sorted by frequency, from highest to least demand.");
    		} else {
    			builder.withFooterText("Trades found based on your wish list and the others.");
    		}
    		
    		String[] rarities = getRaritiesAsString();
    		if (!ALL_RARITIES.equals(rarity)) {
    			rarities = new String[]{rarity};
    		}
    		for (String rarityStr : rarities) {

    			BotUtils.setRarityColor(builder, rarityStr);
    			builder.withThumbnail("https://raw.githubusercontent.com/RoyaleAPI/cr-api-assets/master/trade-tokens/tt-" + rarityStr.toLowerCase() + ".png");

    			switch (rarityStr) {
    			case "Common":
    				//                        builder.appendField("<:blank:534902434712256514>", "__**Commons**__" ,false);
    				builder.withDesc("...Commons ?");
    				break;
    			case "Rare":
    				//                        builder.appendField("<:blank:534902434712256514>", "__**Rares**__" ,false);
    				builder.withDesc("...Rares ?");
    				break;
    			case "Epic":
    				//                        builder.appendField("<:blank:534902434712256514>", "__**Epics**__" ,false);
    				builder.withDesc("...Epics ?");
    				break;
    			case "Legendary":
    				//                        builder.appendField("<:blank:534902434712256514>", "__**Legendaries**__" ,false);
    				builder.withDesc("...Legendaries ?");
    				break;
    			}

    			StringBuilder displayInfo = new StringBuilder();
    			StringBuilder cardList = new StringBuilder();
    			Map<String, Integer> cardsByFreq = new HashMap<String, Integer>(); // new frequency display
    			String cardName = "";
    			boolean forceBreak = false;
    			int cardsPrinted = 0;
    			boolean printedSomething = false;
    			for (MemberCardInfo mcti : membersCardsByRarity.get(rarityStr)) {
    				cardList.setLength(0);
    				if (!cardName.equals(mcti.getCardName()) || forceBreak) {
    					if (!byFreq && !cardName.isEmpty() && displayInfo.length() > 0) {
    						cardsPrinted++;
    						printedSomething = true;
    						builder.appendField(cardTitles.get(cardName), displayInfo.toString(), false);
    					}
    					if (byFreq && !cardName.isEmpty()) {
    						cardsPrinted++;
    						printedSomething = true;
    	    				StringBuilder freqView = new StringBuilder();
    						Map<String, Integer> sortedFreq = cardsByFreq.entrySet()
    						.stream()
    						.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
    						.collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
    										LinkedHashMap::new));
    						int optionsPrinted = 0;
    						for (String cardToPrint : sortedFreq.keySet()) {
    							System.out.println(sortedFreq.get(cardToPrint) + " people want " + cardToPrint);
    							freqView.append(cardToPrint);
    							optionsPrinted++;
    		                    if (optionsPrinted % 8 == 0) {
    		                        freqView.append("\n");
    		                    }
    						}
    						builder.appendField(cardTitles.get(cardName), freqView.toString(), false);
    						cardsByFreq.clear();
    	    			}
    					cardName = mcti.getCardName();
    					displayInfo.setLength(0);
    					forceBreak = false;
    				}
    				if (cardsPrinted > 3) {
    					cardsPrinted = 0;
    					BotUtils.sendMessage(event.getChannel(), builder.build());
    					builder.clearFields();
    				}
    				String memberTag = mcti.getMemberTag();
    				List<Wish> memberWishes = WisherManager.getWishes(mcti.getMemberTag());
    				List<String> memberCardsToRemove = new ArrayList<>();
    				if (!wishes.isEmpty()) {
    					for (Wish memberWish : memberWishes) {
    						String cardKey = CardUtils.getCardKey(memberWish.getCardName());
    						
    						boolean memberHasCardMaxed = false;
    						String memberInfo = ClashRoyaleAPIHelper.getPlayerInfo(memberTag);
    						JSONObject memberInfoJson = new JSONObject(memberInfo);
    						String memberCardInfo = ClashRoyaleAPIHelper.getPlayerCardInfo(cardKey, memberInfoJson);
    						if (!memberCardInfo.isEmpty()) {
    							JSONObject memberCardInfoJson = new JSONObject(memberCardInfo);
    							if (BotUtils.isCardMaxed(memberCardInfoJson)) {
									cardsToRemove.add(memberWish.getCardName());
									memberHasCardMaxed = true;
								}
    						}
    						
    						String authorCardInfo = ClashRoyaleAPIHelper.getPlayerCardInfo(cardKey, playerInfoJson);
    						if (!authorCardInfo.isEmpty()) {
    							JSONObject authorCardInfoJson = new JSONObject(authorCardInfo);
    							int autorCardQuantity = authorCardInfoJson.getInt("count");
    							String authorCardRarity = authorCardInfoJson.getString("rarity");
    							if (authorCardRarity.equals(rarityStr)) {
    								if (authorCardRarity.equals("Legendary") && authorCardInfoJson.getInt("level") == 1) {
    									autorCardQuantity--;
    								}
    								
    								if (BotUtils.isCardMaxed(authorCardInfoJson)) {
    									autorCardQuantity = MAXED_CARD_QUANTITY;
    								}
    								if (!memberHasCardMaxed && !WisherManager.existsWishForAuthorByCardName(playerTag, memberWish.getCardName()) && (autorCardQuantity == MAXED_CARD_QUANTITY || (!maxOnly && autorCardQuantity >= getTradeQuantity(rarityStr)))) {
    									if (checkPriorities(memberTag, memberWish.getCardName())) {
    										String cardEmoji = CardEmojis.getEmoji(cardKey) + " ";

    										if (byFreq) {
    											int freq = 1;
    											if (cardsByFreq.containsKey(cardEmoji)) {
    												freq = cardsByFreq.get(cardEmoji) + 1;
    											}
    											cardsByFreq.put(cardEmoji, freq);
    										} else {
    											cardList.append(CardEmojis.getEmoji(cardKey) + " ");
    										}
    									}

    								}
    							}
    						}

    					}
    					for (String memberCardToRemove : memberCardsToRemove) {
    						WisherManager.deleteWish(memberTag, memberCardToRemove);
    						System.out.println(memberCardToRemove + " REMOVED in fmt");
    					}
    				}
    				if (!byFreq && cardList.length() > 0) {
    					displayInfo.append("`")
    					.append(mcti.getMemberName())
    					.append("` (")
    					.append(mcti.getCardQuantity() == MAXED_CARD_QUANTITY ? "MAXED" : mcti.getCardQuantity())
    					.append(") ")
    					.append(cardList)
    					.append("\n");
    				}
    				if (displayInfo.length() > 900) {
    					forceBreak = true;
    				}
    			}
    			if (!byFreq && !cardName.isEmpty() && displayInfo.length() > 0) {
    				cardsPrinted++;
    				printedSomething = true;
    				builder.appendField(cardTitles.get(cardName), displayInfo.toString(), false);
    			}
    			if (byFreq && !cardName.isEmpty()) {
    				cardsPrinted++;
					printedSomething = true;
    				StringBuilder freqView = new StringBuilder();
					Map<String, Integer> sortedFreq = cardsByFreq.entrySet()
					.stream()
					.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
					.collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
									LinkedHashMap::new));
					int optionsPrinted = 0;
					for (String cardToPrint : sortedFreq.keySet()) {
						freqView.append(cardToPrint);
						optionsPrinted++;
	                    if (optionsPrinted % 8 == 0) {
	                        freqView.append("\n");
	                    }
					}
					builder.appendField(cardTitles.get(cardName), freqView.toString(), false);
					cardsByFreq.clear();
    			}
    			if (cardsPrinted > 0) {
    				BotUtils.sendMessage(event.getChannel(), builder.build());
    			}
    			if (!printedSomething) {
    				builder.appendField("No trades found for you.", "Sadly there isn't any trade for you.\nThat could be because in your clan, few members are using the wish list function or most of you are maxed.", false);
    				BotUtils.sendMessage(event.getChannel(), builder.build());
    			}
    			builder.clearFields();
    		}

    		long timeToWait = 1000 - Duration.between(start, Instant.now()).toMillis();
    		if (timeToWait > 0) {
    			Thread.sleep(timeToWait);
    		}
    		logger.trace("I am here, about to write the final message.");
    		event.getChannel().getMessageByID(messageId).delete();

    	} catch (CrthException e) {
    		logger.error(e.getMessage(), e);
    		BotUtils.sendErrorMessage(event, e.getMessage());
    	} catch (IOException | InterruptedException e) {
    		logger.error(e.getMessage(), e);
    	} finally {
    		BotUtils.setTypingStatus(event.getChannel(), false);
    	}

    	Instant finish = Instant.now();
    	long timeElapsed = Duration.between(start, finish).toMillis();
    	logger.info("Time elapsed: " + timeElapsed + " ms (" + timeElapsed / 1000 + " s)");

    }

    /**
     * Gets the maxed cards of all the members and list them.
     *
     * @param event
     * @param args
     */
    public static void getMaxedCards(MessageReceivedEvent event, List<String> args, String playerTag) {
    	Instant start = Instant.now();

    	//        boolean onlyMyClan = BotUtils.onlyMyClan(args);

    	String rarity = getRarity(args);
//    	String playerTag = BotUtils.validateTag(event);
//    	if (playerTag == null) return;

    	try {

    		String playerInfo = ClashRoyaleAPIHelper.getPlayerInfo(playerTag);
    		JSONObject playerInfoJson = new JSONObject(playerInfo);
    		if (BotUtils.isClanless(event, playerInfoJson)) return;
    		JSONObject clanJson = playerInfoJson.getJSONObject("clan");
    		String authorClanTag = clanJson.getString("tag");

    		//            List<String> clansFromFamily = BotUtils.getValidClans(event, authorClanTag, onlyMyClan);
    		// TODO: Make it multithreading.
    		// FIX: Optimization. I am going by the wishes for each clan on the list.

    		String clanInfo = ClashRoyaleAPIHelper.getClanInfo(authorClanTag);
    		JSONObject clanInfoJson = new JSONObject(clanInfo);
    		String clanTag = clanInfoJson.getString("tag");
    		String clanName = clanInfoJson.getString("name");
    		String clanIcon = clanInfoJson.getJSONObject("badge").getString("image");

    		// Waiting message, bot processing.
    		EmbedBuilder builder = new EmbedBuilder();
    		builder.withAuthorName(clanName + " [#" + clanTag + "]");
    		builder.withAuthorIcon(clanIcon);
    		builder.withAuthorUrl("https://royaleapi.com/clan/" + clanTag);
    		builder.withTitle("Maxed clan list");
    		builder.withDesc("Looking for members with maxed cards...");
    		builder.appendField("Searching for members...", "Please wait.", false);
    		builder.withFooterText("Be patient. This can take a bit.");

    		BotUtils.setRarityColor(builder, rarity);
    		// TODO: Change the image.
    		//                builder.withThumbnail("https://raw.githubusercontent.com/RoyaleAPI/cr-api-assets/master/trade-tokens/tt-" + rarity.toLowerCase() + ".png");

    		long messageId = BotUtils.sendMessage(event.getChannel(), builder.build());
    		BotUtils.setTypingStatus(event.getChannel(), true);

    		String clanMembers = ClashRoyaleAPIHelper.getClanMembers(clanTag);
    		JSONArray clanMembersJson = new JSONArray(clanMembers);

    		Map<String, List<MemberCardInfo>> membersCardsByRarity = new HashMap<>();
    		membersCardsByRarity.put(COMMON.toString(), new ArrayList<>());
    		membersCardsByRarity.put(RARE.toString(), new ArrayList<>());
    		membersCardsByRarity.put(EPIC.toString(), new ArrayList<>());
    		membersCardsByRarity.put(LEGENDARY.toString(), new ArrayList<>());

    		final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_THREADS);
    		final List<Future<?>> futures = new ArrayList<>();
    		for (Object clanMemberJson : clanMembersJson) {
    			JSONObject memberJson = (JSONObject) clanMemberJson;
    			String memberTag = memberJson.getString("tag");
    			Future<?> future = executor.submit(() -> {
    				try {
    					String memberInfo = ClashRoyaleAPIHelper.getPlayerInfo(memberTag);
    					JSONObject memberInfoJson = new JSONObject(memberInfo);
    					String memberName = memberInfoJson.getString("name");

    					JSONArray memberCardsJson = memberInfoJson.getJSONArray("cards");
    					Iterator<Object> memberCardIterator = memberCardsJson.iterator();
    					while (memberCardIterator.hasNext()) {
    						JSONObject memberCardJson = (JSONObject) memberCardIterator.next();
    						String cardRarity = memberCardJson.getString("rarity");
    						if ((ALL_RARITIES.equals(rarity) || rarity.equals(cardRarity)) && BotUtils.isCardMaxed(memberCardJson)) {
    							String cardNameDisplay = memberCardJson.getString("name");
    							List<MemberCardInfo> memberCardTradeInfos = membersCardsByRarity.get(cardRarity);
    							memberCardTradeInfos.add(new MemberCardInfoBuilder()
    									.setMemberName(memberName)
    									.setMemberTag(memberTag)
    									.setCardName(cardNameDisplay)
    									.build()
    									);
    						}

    					}

    				} catch (IOException e) {
    					logger.error(e.getMessage(), e);
    				}
    				return memberTag;
    			});
    			futures.add(future);

    		}
    		try {
    			for (Future<?> future : futures) {
    				String memberTagString = (String) future.get();
    				logger.info(memberTagString + " done.");
    			}
    		} catch (InterruptedException | ExecutionException e) {
    			logger.error(e.getMessage(), e);
    		} finally {
    			shutdown(executor);
    		}

    		Comparator<MemberCardInfo> comparator = Comparator.comparing(MemberCardInfo::getCardName)
    				.thenComparing(MemberCardInfo::getMemberName, String.CASE_INSENSITIVE_ORDER);

    		membersCardsByRarity.get(COMMON.toString()).sort(comparator);
    		membersCardsByRarity.get(RARE.toString()).sort(comparator);
    		membersCardsByRarity.get(EPIC.toString()).sort(comparator);
    		membersCardsByRarity.get(LEGENDARY.toString()).sort(comparator);

    		builder.clearFields();
    		builder.withFooterText("The cards that nobody has maxed are not listed.");
    		String[] rarities = getRaritiesAsString();
    		if (!ALL_RARITIES.equals(rarity)) {
    			rarities = new String[]{rarity};
    		}
    		for (String rarityStr : rarities) {

    			BotUtils.setRarityColor(builder, rarityStr);
    			//TODO: Find a picture for each rarity. I was thinking in the question mark card of each rarity, but I can't find the asset.
    			//                    builder.withThumbnail("https://raw.githubusercontent.com/RoyaleAPI/cr-api-assets/master/trade-tokens/tt-" + rarityStr.toLowerCase() + ".png");

    			switch (rarityStr) {
    			case "Common":
    				builder.withDesc("Members with maxed commons.");
    				break;
    			case "Rare":
    				builder.withDesc("Members with maxed rares.");
    				break;
    			case "Epic":
    				builder.withDesc("Members with maxed epics.");
    				break;
    			case "Legendary":
    				builder.withDesc("Members with maxed legendaries.");
    				break;
    			}

    			StringBuilder displayInfo = new StringBuilder();
    			String cardName = "";
    			boolean forceBreak = false;
    			int cardsPrinted = 0;
    			boolean printedSomething = false;
    			for (MemberCardInfo mcti : membersCardsByRarity.get(rarityStr)) {
    				if (!cardName.equals(mcti.getCardName()) || forceBreak) {
    					if (!cardName.isEmpty() && displayInfo.length() > 0) {
    						cardsPrinted++;
    						printedSomething = true;
    						String cardKey = CardUtils.getCardKey(cardName);
    						String titleRow = String.format("%s **%s**", CardEmojis.getEmoji(cardKey), cardName);
    						builder.appendField(titleRow, displayInfo.toString(), false);
    					}
    					cardName = mcti.getCardName();
    					displayInfo.setLength(0);
    					forceBreak = false;
    				}
    				if (cardsPrinted > 3) {
    					cardsPrinted = 0;
    					BotUtils.sendMessage(event.getChannel(), builder.build());
    					builder.clearFields();
    				}

    				displayInfo.append("`")
    				.append(mcti.getMemberName())
    				.append("`")
    				.append("\n");

    				if (displayInfo.length() > 900) {
    					forceBreak = true;
    				}
    			}
    			if (!cardName.isEmpty() && displayInfo.length() > 0) {
    				cardsPrinted++;
    				printedSomething = true;
    				String cardKey = CardUtils.getCardKey(cardName);
    				String titleRow = String.format("%s **%s**", CardEmojis.getEmoji(cardKey), cardName);
    				builder.appendField(titleRow, displayInfo.toString(), false);
    			}
    			if (cardsPrinted > 0) {
    				BotUtils.sendMessage(event.getChannel(), builder.build());
    			}
    			if (!printedSomething) {
    				String titleNoMaxedCards = "There are no members with maxed cards...";
    				String descNoMaxedCards = String.format("...of %s rariry.", rarityStr);
    				builder.appendField(titleNoMaxedCards, descNoMaxedCards, false);
    				BotUtils.sendMessage(event.getChannel(), builder.build());
    			}
    			builder.clearFields();
    		}

    		long timeToWait = 1000 - Duration.between(start, Instant.now()).toMillis();
    		if (timeToWait > 0) {
    			Thread.sleep(timeToWait);
    		}
    		logger.trace("I am here, about to write the final message.");
    		event.getChannel().getMessageByID(messageId).delete();

    	} catch (CrthException e) {
    		logger.error(e.getMessage(), e);
    		BotUtils.sendErrorMessage(event, e.getMessage());
    	} catch (IOException | InterruptedException e) {
    		logger.error(e.getMessage(), e);
    	} finally {
    		BotUtils.setTypingStatus(event.getChannel(), false);
    	}
    }

    /**
     * Shows the player cards that can trade.
     *
     * @param event
     * @param args
     */
    public static void showMyTradableCards(MessageReceivedEvent event, List<String> args, String playerTag) {
//        String playerTag = BotUtils.validateTag(event);
//        if (playerTag == null) return;

        String rarity = getRarity(args);
        if (!ALL_RARITIES.equals(rarity)){
            args.remove(0);
        }

        IUser author = event.getAuthor();

        IUser listOwner = author;
//        if (args.size() > 0) {
//            String playerToCheck = args.get(0);
//
//            if (playerToCheck.startsWith("<@")) {
//                listOwner = event.getMessage().getMentions().get(0);
//                playerTag = BotUtils.getPlayerTag(listOwner);
//
//                if (playerTag.isEmpty()) {
//                    BotUtils.sendMessage(event.getChannel(), author.mention() + ", you can't see that player tradable cards because doesn't have a tag associated to the user.");
//                    return;
//                }
//
//            }
//        }

        try {

            String playerInfo = ClashRoyaleAPIHelper.getPlayerInfo(playerTag);
            JSONObject playerInfoJson = new JSONObject(playerInfo);

            IGuild guild = event.getGuild();
            String authorName = guild == null ? null : listOwner.getNicknameForGuild(guild);
            if (authorName == null || authorName.isEmpty()) {
                authorName = listOwner.getName();
            }

            // Waiting message, bot processing.
            EmbedBuilder builder = new EmbedBuilder();
            builder.withAuthorName(authorName + " [#" + playerTag + "]");
            builder.withAuthorIcon(listOwner.getAvatarURL());
//            builder.withAuthorUrl("https://royaleapi.com/player/" + playerTag);
            builder.withTitle("Tradable cards");
            builder.withDesc("Displaying only the cards that this player can trade.");
            builder.appendField("Searching for cards available to trade...", "Please wait.", false);
            builder.withFooterText("Be patient. This can take a bit.");

            BotUtils.setRarityColor(builder, rarity);
            // TODO: Change the image.
            if (!ALL_RARITIES.equals(rarity)) {
                builder.withThumbnail("https://raw.githubusercontent.com/RoyaleAPI/cr-api-assets/master/trade-tokens/tt-" + rarity.toLowerCase() + ".png");
            }

            Map<String, List<MemberCardInfo>> playerCardsByRarity = new HashMap<>();
            playerCardsByRarity.put(COMMON.toString(), new ArrayList<>());
            playerCardsByRarity.put(RARE.toString(), new ArrayList<>());
            playerCardsByRarity.put(EPIC.toString(), new ArrayList<>());
            playerCardsByRarity.put(LEGENDARY.toString(), new ArrayList<>());

            String playerName = playerInfoJson.getString("name");

            JSONArray playerCardsJson = playerInfoJson.getJSONArray("cards");
            Iterator<Object> playerCardIterator = playerCardsJson.iterator();
            while (playerCardIterator.hasNext()) {
                JSONObject playerCardJson = (JSONObject) playerCardIterator.next();
                int cardQuantity = playerCardJson.getInt("count");
                String cardRarity = playerCardJson.getString("rarity");
                if (cardRarity.equals(LEGENDARY.toString()) && playerCardJson.getInt("level") == 1) {
                    cardQuantity--;
                }
                boolean cardIsMaxed = false;
                if (BotUtils.isCardMaxed(playerCardJson)) {
                	cardIsMaxed = true;
                    cardQuantity = MAXED_CARD_QUANTITY;
                }
                String cardNameDisplay = playerCardJson.getString("name");
                if (cardIsMaxed && WisherManager.existsWishForAuthorByCardName(playerTag, cardNameDisplay)) {
                	WisherManager.deleteWish(playerTag, cardNameDisplay);
                	System.out.println(cardNameDisplay + " REMOVED in mtc");
                }
                if ((ALL_RARITIES.equals(rarity) || rarity.equals(cardRarity)) && !WisherManager.existsWishForAuthorByCardName(playerTag, cardNameDisplay) && cardQuantity >= getTradeQuantity(cardRarity)) {
                    List<MemberCardInfo> playerCardTradeInfos = playerCardsByRarity.get(cardRarity);
                    playerCardTradeInfos.add(new MemberCardInfoBuilder()
                            .setMemberName(playerName)
                            .setMemberTag(playerTag)
                            .setCardName(cardNameDisplay)
                            .setCardQuantity(cardQuantity)
                            .build()
                    );
                }

            }

            Comparator<MemberCardInfo> comparator = Comparator.comparing(MemberCardInfo::getCardName);

            playerCardsByRarity.get(COMMON.toString()).sort(comparator);
            playerCardsByRarity.get(RARE.toString()).sort(comparator);
            playerCardsByRarity.get(EPIC.toString()).sort(comparator);
            playerCardsByRarity.get(LEGENDARY.toString()).sort(comparator);

            builder.clearFields();
            builder.withFooterText("Only displaying the cards that the player can trade and are not in the wish list.");
            String[] rarities = getRaritiesAsString();
            if (!ALL_RARITIES.equals(rarity)) {
                rarities = new String[]{rarity};
            }
            StringBuilder sb = new StringBuilder();
            for (String rarityStr : rarities) {

                //TODO: Check how it could be a good visualization
                // Try with fixed indentation for numbers. Or for all the rarities on 5.

                int cardsPrinted = 0;
                Iterator<MemberCardInfo> iterator = playerCardsByRarity.get(rarityStr).iterator();
                while (iterator.hasNext()) {
                    MemberCardInfo memberCardInfo = iterator.next();
                    sb.append(CardEmojis.getEmoji(CardUtils.getCardKey(memberCardInfo.getCardName())) + " ");
                    cardsPrinted++;
                    if (cardsPrinted % 8 == 0) {
                        sb.append("\n");
                    }
                }

                builder.appendField(rarityStr, sb.toString(), false);
                sb.setLength(0);
            }

            BotUtils.sendMessage(event.getChannel(), builder.build());

        } catch (CrthException e) {
            logger.error(e.getMessage(), e);
            BotUtils.sendErrorMessage(event, e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            BotUtils.setTypingStatus(event.getChannel(), false);
        }
    }
    
    public static void getPlayersWishByRarity(MessageReceivedEvent event, List<String> args, String playerTag, String rarity) {
    	Instant start = Instant.now();
    	
    	try {
    		String playerInfo = ClashRoyaleAPIHelper.getPlayerInfo(playerTag);
    		JSONObject playerInfoJson = new JSONObject(playerInfo);
    		if (BotUtils.isClanless(event, playerInfoJson)) return;
    		JSONObject clanJson = playerInfoJson.getJSONObject("clan");
    		String authorClanTag = clanJson.getString("tag");

    		String clanInfo = ClashRoyaleAPIHelper.getClanInfo(authorClanTag);
    		JSONObject clanInfoJson = new JSONObject(clanInfo);
    		String clanTag = clanInfoJson.getString("tag");
    		String clanName = clanInfoJson.getString("name");
    		String clanIcon = clanInfoJson.getJSONObject("badge").getString("image");

    		// Waiting message, bot processing.
    		EmbedBuilder builder = new EmbedBuilder();
    		builder.withAuthorName(clanName + " [#" + clanTag + "]");
    		builder.withAuthorIcon(clanIcon);
    		builder.withAuthorUrl("https://royaleapi.com/clan/" + clanTag);
    		builder.withTitle("Who wants " + rarity + " cards?");
    		builder.withDesc("Displaying clan members of " + clanName);
    		builder.appendField("Searching for members...", "Please wait.", false);
    		builder.withFooterText("Be patient. This can take a bit.");

    		BotUtils.setRarityColor(builder, rarity);

    		long messageId = BotUtils.sendMessage(event.getChannel(), builder.build());
    		BotUtils.setTypingStatus(event.getChannel(), true);

    		String clanMembers = ClashRoyaleAPIHelper.getClanMembers(clanTag);
    		JSONArray clanMembersJson = new JSONArray(clanMembers);
    		Map<String, List<MemberCardInfo>> membersCardsByRarity = new HashMap<>();
    		membersCardsByRarity.put(COMMON.toString(), new ArrayList<>());
    		membersCardsByRarity.put(RARE.toString(), new ArrayList<>());
    		membersCardsByRarity.put(EPIC.toString(), new ArrayList<>());
    		membersCardsByRarity.put(LEGENDARY.toString(), new ArrayList<>());

    		final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_THREADS);
    		final List<Future<?>> futures = new ArrayList<>();
    		for (Object clanMemberJson : clanMembersJson) {
    			JSONObject memberJson = (JSONObject) clanMemberJson;
    			String memberTag = memberJson.getString("tag");
    			Future<?> future = executor.submit(() -> {
    				try {
    					String memberInfo = ClashRoyaleAPIHelper.getPlayerInfo(memberTag);
    					JSONObject memberInfoJson = new JSONObject(memberInfo);
    					String memberName = memberInfoJson.getString("name");

    					JSONArray memberCardsJson = memberInfoJson.getJSONArray("cards");
    					Iterator<Object> memberCardIterator = memberCardsJson.iterator();
    					while (memberCardIterator.hasNext()) {
    						JSONObject memberCardJson = (JSONObject) memberCardIterator.next();
    						String cardRarity = memberCardJson.getString("rarity");
    						String cardNameDisplay = memberCardJson.getString("name");
    						
    						int cardQuantity = memberCardJson.getInt("count");
    	                    int cardLevel = memberCardJson.getInt("displayLevel");
    	                    
    	                    if (cardLevel == 13 && WisherManager.existsWishForAuthorByCardName(memberTag, cardNameDisplay)) {
    	                    	WisherManager.deleteWish(memberTag, cardNameDisplay);
    	                    } else if (rarity.equals(cardRarity) && WisherManager.existsWishForAuthorByCardName(memberTag, cardNameDisplay)) {
    							if (checkPriorities(memberTag, cardNameDisplay)) {
    								List<MemberCardInfo> memberCardTradeInfos = membersCardsByRarity.get(cardRarity);
        							memberCardTradeInfos.add(new MemberCardInfoBuilder()
        									.setMemberName(memberName)
        									.setMemberTag(memberTag)
        									.setCardName(cardNameDisplay)
        									.setCardQuantity(cardQuantity)
                            				.setCardLevel(cardLevel)
        									.build()
        							);
    							}
    							
    						}

    					}

    				} catch (IOException e) {
    					logger.error(e.getMessage(), e);
    				}
    				return memberTag;
    			});
    			futures.add(future);

    		}
    		try {
    			for (Future<?> future : futures) {
    				String memberTagString = (String) future.get();
    				logger.info(memberTagString + " done.");
    			}
    		} catch (InterruptedException | ExecutionException e) {
    			logger.error(e.getMessage(), e);
    		} finally {
    			shutdown(executor);
    		}

    		Comparator<MemberCardInfo> comparator = Comparator.comparing(MemberCardInfo::getCardName)
    				.thenComparingInt(MemberCardInfo::getCardLevel).thenComparingInt(MemberCardInfo::getCardQuantity)
    				.thenComparing(MemberCardInfo::getMemberName, String.CASE_INSENSITIVE_ORDER);

    		membersCardsByRarity.get(COMMON.toString()).sort(comparator);
    		membersCardsByRarity.get(RARE.toString()).sort(comparator);
    		membersCardsByRarity.get(EPIC.toString()).sort(comparator);
    		membersCardsByRarity.get(LEGENDARY.toString()).sort(comparator);

    		builder.clearFields();
    		builder.withFooterText("The cards that nobody wants are not listed.");
    		
    		BotUtils.setRarityColor(builder, rarity);
			switch (rarity) {
			case "Common":
				builder.withDesc("Member wishes by commons.");
				break;
			case "Rare":
				builder.withDesc("Member wishes by rares.");
				break;
			case "Epic":
				builder.withDesc("Member wishes by epics.");
				break;
			case "Legendary":
				builder.withDesc("Member wishes by legendaries.");
				break;
			}

			StringBuilder displayInfo = new StringBuilder();
			String cardName = "";
			boolean forceBreak = false;
			int cardsPrinted = 0;
			boolean printedSomething = false;
			for (MemberCardInfo mcti : membersCardsByRarity.get(rarity)) {
				if (!cardName.equals(mcti.getCardName()) || forceBreak) {
					if (!cardName.isEmpty() && displayInfo.length() > 0) {
						cardsPrinted++;
						printedSomething = true;
						String cardKey = CardUtils.getCardKey(cardName);
						String titleRow = String.format("%s **%s**", CardEmojis.getEmoji(cardKey), cardName);
						builder.appendField(titleRow, displayInfo.toString(), false);
					}
					cardName = mcti.getCardName();
					displayInfo.setLength(0);
					forceBreak = false;
				}
				if (cardsPrinted > 4) {
					cardsPrinted = 0;
					BotUtils.sendMessage(event.getChannel(), builder.build());
					builder.clearFields();
				}

				displayInfo.append("**" + mcti.getMemberName() + "** ")
				.append(String.format("`Level %d (%d)`", mcti.getCardLevel(), mcti.getCardQuantity()))
				.append("\n");

				if (displayInfo.length() > 900) {
					forceBreak = true;
				}
			}
			if (!cardName.isEmpty() && displayInfo.length() > 0) {
				cardsPrinted++;
				printedSomething = true;
				String cardKey = CardUtils.getCardKey(cardName);
				String titleRow = String.format("%s **%s**", CardEmojis.getEmoji(cardKey), cardName);
				builder.appendField(titleRow, displayInfo.toString(), false);
			}
			if (cardsPrinted > 0) {
				BotUtils.sendMessage(event.getChannel(), builder.build());
			}
			if (!printedSomething) {
				String titleNoMaxedCards = "There are no members who want cards...";
				String descNoMaxedCards = String.format("...of %s rarity.", rarity);
				builder.appendField(titleNoMaxedCards, descNoMaxedCards, false);
				BotUtils.sendMessage(event.getChannel(), builder.build());
			}
			builder.clearFields();




    		long timeToWait = 1000 - Duration.between(start, Instant.now()).toMillis();
    		if (timeToWait > 0) {
    			Thread.sleep(timeToWait);
    		}
    		logger.trace("I am here, about to write the final message.");
    		event.getChannel().getMessageByID(messageId).delete();
    	} catch (CrthException e) {
    		logger.error(e.getMessage(), e);
    		BotUtils.sendErrorMessage(event, e.getMessage());
    	} catch (IOException | InterruptedException e) {
    		logger.error(e.getMessage(), e);
    	} finally {
    		BotUtils.setTypingStatus(event.getChannel(), false);
    	}
    	
    	
    }


    /**
     * Gets the card rarity that has to be process for the command called.
     *
     * @param args
     * @return the card rarity specified. `ALL`, otherwise.
     */
    private static String getRarity(List<String> args) {
        String rarity = ALL_RARITIES;
        if (args.isEmpty()) return rarity;
        switch (args.get(0).toLowerCase()) {
            case "common":
            case "commons":
            case "c":
                rarity = COMMON.toString();
                break;
            case "rare":
            case "rares":
            case "r":
                rarity = RARE.toString();
                break;
            case "epic":
            case "epics":
            case "e":
                rarity = EPIC.toString();
                break;
            case "legendary":
            case "legendaries":
            case "l":
                rarity = LEGENDARY.toString();
                break;
        }

        return rarity;
    }

    /**
     * Gets the card ammount to trade based on the card rarity.
     *
     * @param cardRarity
     * @return Ammount of cards that can be trade.
     */
    private static Integer getTradeQuantity(String cardRarity) {
        return CardUtils.Rarity.valueOf(cardRarity.toUpperCase()).getTradeQuantity();
    }

    private static List<MemberCardInfo> getClanMembersWhoWants(String cardName, String clanTag, AtomicInteger clanMembersQuantity, String cardKey) throws IOException, CrthException {
        String clanMembers = ClashRoyaleAPIHelper.getClanMembers(clanTag);
        JSONArray clanMembersJson = new JSONArray(clanMembers);
        List<MemberCardInfo> membersWhoWantCardList = new ArrayList<>();

        final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_THREADS);
        final List<Future<?>> futures = new ArrayList<>();
        for (Object clanMemberJson : clanMembersJson) {
            JSONObject memberJson = (JSONObject) clanMemberJson;
            String memberTag = memberJson.getString("tag");
            Future<?> future = executor.submit(() -> {
                try {
                    String memberInfo = ClashRoyaleAPIHelper.getPlayerInfo(memberTag);
                    JSONObject memberInfoJson = new JSONObject(memberInfo);
                    String memberName = memberInfoJson.getString("name");
                    String playerCardInfo = ClashRoyaleAPIHelper.getPlayerCardInfo(cardKey, memberInfoJson);
                    int cardQuantity = 0;
                    int cardLevel = 13;
                    if (!playerCardInfo.isEmpty()) {
                        JSONObject memberCardInfoJson = new JSONObject(playerCardInfo);
                        cardQuantity = memberCardInfoJson.getInt("count");
                        cardLevel = memberCardInfoJson.getInt("displayLevel");
                    }
                    if (cardLevel == 13 && WisherManager.existsWishForAuthorByCardName(memberTag, cardName)) {
                    	WisherManager.deleteWish(memberTag, cardName);
                    } else if (cardLevel < 13 && WisherManager.existsWishForAuthorByCardName(memberTag, cardName)) {
                    	if (checkPriorities(memberTag, cardName)) {
                    		membersWhoWantCardList.add(new MemberCardInfoBuilder()
                    				.setMemberName(memberName)
                    				.setMemberTag(memberTag)
                    				.setCardName(cardName)
                    				.setCardQuantity(cardQuantity)
                    				.setCardLevel(cardLevel)
                    				.build());
                    	}
                    }
                    clanMembersQuantity.getAndIncrement();

                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
                return memberTag;
            });
            futures.add(future);

        }
        try {
            for (Future<?> future : futures) {
                String memberTagString = (String) future.get();
                logger.info(memberTagString + " done.");
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
        } finally {
            shutdown(executor);
        }

        return membersWhoWantCardList;
    }

    /**
     * Shutdowns an executor.
     *
     * @param executor
     */
    private static void shutdown(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    /**
     * Appends the title line for the trade commands.
     *
     * @param stringBuilder
     */
    private static void appendTitleLine(StringBuilder stringBuilder) {
        stringBuilder.append("```diff\n")
                .append("    ")
                .append(StringUtils.leftPad("#", 5))
                .append(" Name")
                .append("\n");
    }
    
    
    private static Wish getPrioritizedWishOfRarity(String playerTag, String rarity) {
    	List<Wish> priorities = WisherManager.getPlayerPriorities(playerTag);
    	for (Wish priority : priorities) {
    		try {
    			String cardResponse = ClashRoyaleAPIHelper.getCardByName(priority.getCardName());
    			JSONObject cardInfo = new JSONObject(cardResponse);
    			String otherRarity = cardInfo.getString("rarity");
    			if (rarity.equals(otherRarity)) {
    				return priority;
    			}
    		} catch (IOException e) {
    			logger.error(e.getMessage(), e);
    		}
    	}
    	return null; // this is the case where a player does NOT have a prioritized card of this rarity
    }
    
    private static boolean checkPriorities(String playerTag, String cardName) {
    	String rarity = "";
    	try {
    		String cardResponse = ClashRoyaleAPIHelper.getCardByName(cardName);
    		JSONObject cardInfo = new JSONObject(cardResponse);
    		rarity = cardInfo.getString("rarity");
    	} catch (IOException e) {
    		logger.error(e.getMessage(), e);
    	}

    	List<Wish> priorities = WisherManager.getPlayerPriorities(playerTag);
    	boolean checkPriorities = priorities.isEmpty() || WisherManager.cardIsPriority(playerTag, cardName);
    	boolean otherSameRarityPriority = false;
    	if (!checkPriorities) {
    		Wish possiblePriority = getPrioritizedWishOfRarity(playerTag, rarity);
    		otherSameRarityPriority = possiblePriority != null && !possiblePriority.getCardName().equals(cardName);
    	}
    	return checkPriorities || !otherSameRarityPriority;
    }

}
