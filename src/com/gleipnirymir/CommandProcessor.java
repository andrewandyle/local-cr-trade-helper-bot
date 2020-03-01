package com.gleipnirymir;

import com.gleipnirymir.commands.WishListManagement;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;

public class CommandProcessor {

//    private static final Logger logger = LogManager.getLogger(CommandProcessor.class);
//
//    public static void setTagToPlayer(MessageReceivedEvent event, List<String> args) {
//        if (args.isEmpty()) {
//            BotUtils.sendMessage(event.getChannel(), event.getAuthor().mention() + ", you need to add your clash royale player tag.");
//            return;
//        }
//        String playerTag = args.get(0);
//        if (!playerTag.startsWith("#")) {
//            BotUtils.sendMessage(event.getChannel(), event.getAuthor().mention() + ", the tag must start with #.");
//            return;
//        }
//
//        playerTag = playerTag.substring(1);
//        IUser author = event.getAuthor();
//
//        try {
//            String playerInfo = ClashRoyaleAPIHelper.getPlayerInfo(playerTag);
//            JSONObject playerInfoJson = new JSONObject(playerInfo);
//
//            User user = new User(author.getLongID(), playerTag.toUpperCase());
//
//            Session session = HibernateUtils.getSessionFactory().openSession();
//            User userFetched = session.get(User.class, user.getDiscordAccountId());
//            if (userFetched != null) {
//                if (!user.getCrAccountTag().equals(userFetched.getCrAccountTag())) {
//                    Transaction transaction = session.beginTransaction();
//                    userFetched.setCrAccountTag(user.getCrAccountTag());
//                    session.update(userFetched);
//                    transaction.commit();
//                }
//            } else {
//                Transaction transaction = session.beginTransaction();
//                session.save(user);
//                transaction.commit();
//            }
//            if (session.isOpen()) {
//                session.close();
//            }
//
//            printPlayerInfo(event, playerTag, author, playerInfoJson);
//
//        } catch (CrthException e) {
//            logger.error(e.getMessage(), e);
//            BotUtils.sendErrorMessage(event, e.getMessage());
//            BotUtils.sendMessage(event.getChannel(), author.mention() + ", there was an error saving your tag account. Please, verify that the tag you introduce is correct.");
//        } catch (IOException e) {
//            logger.error(e.getMessage(), e);
//        } finally {
//            BotUtils.setTypingStatus(event.getChannel(), false);
//        }
//
//    }
//
//    private static void printPlayerInfo(MessageReceivedEvent event, String playerTag, IUser author, JSONObject playerInfoJson) {
//        IGuild guild = event.getGuild();
//        String authorName = guild == null ? null : author.getNicknameForGuild(guild);
//        if (authorName == null || authorName.isEmpty()) {
//            authorName = author.getName();
//        }
//
//        JSONObject clanJson = playerInfoJson.getJSONObject("clan");
//        String clanDescription = "Clanless";
//        if (clanJson != JSONObject.NULL) {
//            String authorClanTag = clanJson.getString("tag");
//            String authorClanName = clanJson.getString("name");
//            clanDescription = String.format("[#%s] %s", authorClanTag, authorClanName);
//        }
//
//        int trophies = playerInfoJson.getInt("trophies");
//        String playerName = playerInfoJson.getString("name");
//
//        EmbedBuilder builder = new EmbedBuilder();
//        builder.withAuthorName(authorName + " [#" + playerTag.toUpperCase() + "]");
//        builder.withAuthorIcon(author.getAvatarURL());
//        builder.withTitle("Player information");
//        builder.withDesc("Displaying the player information.");
//        builder.appendField("Name", playerName, true);
//        builder.appendField("Trophies", String.valueOf(trophies), true);
//        builder.appendField("Clan", clanDescription, true);
//        builder.withFooterText("Check this information. If it is not you, save your tag again with your correct tag.");
//
//        BotUtils.setRarityColor(builder, "defaultColor");
//
//        BotUtils.sendMessage(event.getChannel(), author.mention() + ", your tag account was successfully saved with the following player:", builder.build());
//    }


    // =================================================================
    // == WishList Management
    // =================================================================

    public static void deleteFromWishList(MessageReceivedEvent event, List<String> args, String playerTag) {
        WishListManagement.getInstance().delete(event, args, playerTag);
    }

    public static void printWishList(MessageReceivedEvent event, List<String> args, String playerTag) {
        WishListManagement.getInstance().print(event, args, playerTag);
    }

    public static void addToWishList(MessageReceivedEvent event, List<String> args, String playerTag) {
        WishListManagement.getInstance().add(event, args, playerTag);
    }
    
    public static void addPriority(MessageReceivedEvent event, List<String> args, String playerTag) {
    	WishListManagement.getInstance().prioritize(event, args, playerTag);
    }
    
    public static void deletePriority(MessageReceivedEvent event, List<String> args, String playerTag) {
    	WishListManagement.getInstance().deletePriority(event, args, playerTag);
    }


    // =================================================================
    // == Other
    // =================================================================

//    public static void checkMembers(MessageReceivedEvent event, List<String> args) {
//        String playerTag = BotUtils.validateTag(event);
//        if (playerTag == null) return;
//
//        boolean onlyMyClan = BotUtils.onlyMyClan(args);
//
//        try {
//            String playerInfo = ClashRoyaleAPIHelper.getPlayerInfo(playerTag);
//            JSONObject playerInfoJson = new JSONObject(playerInfo);
//            if (BotUtils.isClanless(event, playerInfoJson)) return;
//
//            JSONObject clanJson = playerInfoJson.getJSONObject("clan");
//            String authorClanTag = clanJson.getString("tag");
//
//            List<String> clansFromFamily = BotUtils.getValidClans(event, authorClanTag, onlyMyClan);
//            // TODO: Make it multithreading.
//            for (String clanFamilyTag : clansFromFamily) {
//
//                String clanInfo = ClashRoyaleAPIHelper.getClanInfo(clanFamilyTag);
//                JSONObject clanInfoJson = new JSONObject(clanInfo);
//                String clanTag = clanInfoJson.getString("tag");
//                String clanName = clanInfoJson.getString("name");
//                String clanIcon = clanInfoJson.getJSONObject("badge").getString("image");
//
//
//                // Waiting message, bot processing.
//                EmbedBuilder builder = new EmbedBuilder();
//                builder.withAuthorName(clanName + " [#" + clanTag + "]");
//                builder.withAuthorIcon(clanIcon);
//                builder.withAuthorUrl("https://royaleapi.com/clan/" + clanTag);
//                builder.withTitle("Checking memebers...");
//                builder.withDesc("that are registered.");
//                builder.appendField("Searching for members...", "Please wait.", false);
//                builder.withFooterText("Be patient. This can take a bit.");
//
//                BotUtils.setRarityColor(builder, "defaultColor");
//
//                long messageId = BotUtils.sendMessage(event.getChannel(), builder.build());
//                BotUtils.setTypingStatus(event.getChannel(), true);
//
//                String clanMembers = ClashRoyaleAPIHelper.getClanMembers(clanTag);
//                JSONArray clanMembersJson = new JSONArray(clanMembers);
//
//                String okChar = "+\u2714 ";   // ✔
//                String noChar = "-\u2716 ";   // ✖
//
//                int clanMembersQuantity = 0;
//                int registeredMemebers = 0;
//                StringBuilder sb = new StringBuilder();
//                List<String> membersList = new ArrayList<>();
//
//                for (Object clanMemberJson : clanMembersJson) {
//                    clanMembersQuantity++;
//                    JSONObject memberJson = (JSONObject) clanMemberJson;
//                    String memberTag = memberJson.getString("tag");
//                    String memberName = memberJson.getString("name");
//                    if (BotUtils.getUserByTag(memberTag) == null) {
//                        sb.append(noChar);
//                    } else {
//                        registeredMemebers++;
//                        sb.append(okChar);
//                    }
//                    sb.append(" ")
//                            .append(memberName);
//                    membersList.add(sb.toString());
//                    sb.setLength(0);
//                }
//
//                membersList.sort(String.CASE_INSENSITIVE_ORDER);
//
//                StringBuilder membersDisplayStringPart1 = new StringBuilder();
//                membersDisplayStringPart1.append("```diff\n");
//
//                StringBuilder membersDisplayStringPart2 = new StringBuilder();
//                membersDisplayStringPart2.append("```diff\n");
//
//                int listSize = membersList.size();
//                boolean hasPageBreak = listSize > 20;
//                int pageBreak = hasPageBreak ? listSize / 2 : listSize;
//
//                for (String memberItem : membersList) {
//                    if (pageBreak > 0) {
//                        membersDisplayStringPart1.append(memberItem).append("\n");
//                        pageBreak--;
//                    } else {
//                        membersDisplayStringPart2.append(memberItem).append("\n");
//                    }
//                }
//
//                membersDisplayStringPart1.append("```");
//                membersDisplayStringPart2.append("```");
//
//                builder.clearFields();
//                builder.appendField(registeredMemebers + " of " + clanMembersQuantity + " members registered found", membersDisplayStringPart1.toString(), false);
//                if (hasPageBreak) {
//                    builder.appendField("Continue...", membersDisplayStringPart2.toString(), false);
//                }
//                builder.withFooterText("Green (" + okChar.trim() + ") member is registered. Red (" + noChar.trim() + ") member is not registered.");
//
//                event.getChannel().getMessageByID(messageId).edit(builder.build());
//            }
//        } catch (CrthException e) {
//            logger.error(e.getMessage(), e);
//            BotUtils.sendErrorMessage(event, e.getMessage());
//        } catch (IOException e) {
//            logger.error(e.getMessage(), e);
//        } finally {
//            BotUtils.setTypingStatus(event.getChannel(), false);
//        }
//    }
//
//    public static void printUnknownCommand(MessageReceivedEvent event) {
//        BotUtils.sendMessage(event.getChannel(), "Unknown command. Please use " + BotUtils.BOT_PREFIX + "`help` for more information.");
//    }
//

}

