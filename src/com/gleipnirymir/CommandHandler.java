package com.gleipnirymir;

import com.gleipnirymir.commands.TradeCommands;
import com.gleipnirymir.utils.BotUtils;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class CommandHandler {

    private static final Logger logger = LogManager.getLogger(CommandHandler.class);

    // A static map of commands mapping from command string to the functional impl
    private static Map<String, Command> commandMap = new HashMap<>();

    // Might be better practice to do this from an instantiated objects constructor
    static {

        commandMap.put("test", (event, args) -> BotUtils.sendMessage(event.getChannel(), "Test is working. <:think_hog:622896363608211497>"));

        // =================================================================
        // == Configuration
        // +* Andrew Le - Commented out for local version - MySQL database not being used
        // The playerTag is now passed as an argument into these commands instead of using the database
        // =================================================================

//        commandMap.put("saveTag".toLowerCase(), (event, args) -> CommandProcessor.setTagToPlayer(event, args));
//        commandMap.put("save".toLowerCase(), (event, args) -> CommandProcessor.setTagToPlayer(event, args));

        // =================================================================
        // == WishList Management
        // =================================================================

        commandMap.put("awl", (event, args) -> { 
        	String playerTag = args.get(0);
        	args.remove(0);
        	CommandProcessor.addToWishList(event, args, playerTag);
        });
        commandMap.put("addToWishList".toLowerCase(), (event, args) -> { 
        	String playerTag = args.get(0);
        	args.remove(0);
        	CommandProcessor.addToWishList(event, args, playerTag);
        });

        commandMap.put("wl", (event, args) -> {
        	String playerTag = args.get(0);
        	args.remove(0);
        	CommandProcessor.printWishList(event, args, playerTag);
        });
        commandMap.put("wishList".toLowerCase(), (event, args) -> {
        	String playerTag = args.get(0);
        	args.remove(0);
        	CommandProcessor.printWishList(event, args, playerTag);
        });

        commandMap.put("dwl", (event, args) -> { 
        	String playerTag = args.get(0);
        	args.remove(0);
        	CommandProcessor.deleteFromWishList(event, args, playerTag);
        });
        commandMap.put("rwl", (event, args) -> {
        	String playerTag = args.get(0);
        	args.remove(0);
        	CommandProcessor.deleteFromWishList(event, args, playerTag);
        });
        commandMap.put("deleteFromWishList".toLowerCase(), (event, args) -> {
        	String playerTag = args.get(0);
        	args.remove(0);
        	CommandProcessor.deleteFromWishList(event, args, playerTag);
        });
        
        // =================================================================
        // +* Andrew Le - Priority commands
        // =================================================================
        
        commandMap.put("ap", (event, args) -> {
        	String playerTag = args.get(0);
        	args.remove(0);
        	CommandProcessor.addPriority(event, args, playerTag);
        });
        commandMap.put("prioritize".toLowerCase(), (event, args) -> {
        	String playerTag = args.get(0);
        	args.remove(0);
        	CommandProcessor.addPriority(event, args, playerTag);
        });
        commandMap.put("addPriority".toLowerCase(), (event, args) -> {
        	String playerTag = args.get(0);
        	args.remove(0);
        	CommandProcessor.addPriority(event, args, playerTag);
        });
        
        commandMap.put("dp", (event, args) -> {
        	String playerTag = args.get(0);
        	args.remove(0);
        	CommandProcessor.deletePriority(event, args, playerTag);
        });
        commandMap.put("deprioritize".toLowerCase(), (event, args) -> {
        	String playerTag = args.get(0);
        	args.remove(0);
        	CommandProcessor.deletePriority(event, args, playerTag);
        });
        commandMap.put("deletePriority".toLowerCase(), (event, args) -> {
        	String playerTag = args.get(0);
        	args.remove(0);
        	CommandProcessor.deletePriority(event, args, playerTag);
        });


        // =================================================================
        // == Trade commands
        // =================================================================

        commandMap.put("whoHas".toLowerCase(), (event, args) -> {

            List<String> argsWithoutCommand = args;
            boolean showAll = args.get(0).equals("-all");
            if (showAll) {
                argsWithoutCommand.remove(0);
            }
            String playerTag = argsWithoutCommand.get(0);
        	argsWithoutCommand.remove(0);
            TradeCommands.getPlayersByCard(event, argsWithoutCommand, showAll, playerTag);

        });

        commandMap.put("whoWants".toLowerCase(), (event, args) -> {
        	String playerTag = args.get(0);
        	args.remove(0);
        	TradeCommands.getPlayersWishByCard(event, args, playerTag);
        });

        commandMap.put("whoTakes".toLowerCase(), (event, args) -> {
        	String playerTag = args.get(0);
        	args.remove(0);
        	TradeCommands.getPlayersForTrade(event, args, playerTag);
        });

        commandMap.put("findMeTrades".toLowerCase(), (event, args) -> {
        	List<String> argsWithoutCommand = args;
        	// +* Andrew Le - Parameter to display fmt by frequency
        	boolean byFreq = args.get(0).equals("freq");
        	if (byFreq) {
        		argsWithoutCommand.remove(0);
        	}
        	// +* Andrew Le - Parameter to filter fmt to only give away your maxed cards
        	boolean maxOnly = args.contains("max");
        	if (maxOnly) {
        		argsWithoutCommand.remove("max");
        	}
        	String playerTag = argsWithoutCommand.get(0);
        	argsWithoutCommand.remove(0);
        	TradeCommands.getSmartTrades(event, argsWithoutCommand, byFreq, maxOnly, playerTag);
        });
        commandMap.put("fmt".toLowerCase(), (event, args) -> {
        	List<String> argsWithoutCommand = args;
        	boolean byFreq = args.get(0).equals("freq");
        	if (byFreq) {
        		argsWithoutCommand.remove(0);
        	}
        	boolean maxOnly = args.contains("max");
        	if (maxOnly) {
        		argsWithoutCommand.remove("max");
        	}
        	String playerTag = argsWithoutCommand.get(0);
        	argsWithoutCommand.remove(0);
        	TradeCommands.getSmartTrades(event, argsWithoutCommand, byFreq, maxOnly, playerTag);
        });

        commandMap.put("maxedCards".toLowerCase(), (event, args) -> {
        	String playerTag = args.get(0);
        	args.remove(0);
        	TradeCommands.getMaxedCards(event, args, playerTag);
        });
        commandMap.put("mc".toLowerCase(), (event, args) -> {
        	String playerTag = args.get(0);
        	args.remove(0);
        	TradeCommands.getMaxedCards(event, args, playerTag);
        });

        commandMap.put("myTradableCards".toLowerCase(), (event, args) -> {
        	String playerTag = args.get(0);
        	args.remove(0);
        	TradeCommands.showMyTradableCards(event, args, playerTag);
        });
        commandMap.put("mtc".toLowerCase(), (event, args) -> {
        	String playerTag = args.get(0);
        	args.remove(0);
        	TradeCommands.showMyTradableCards(event, args, playerTag);
        });


        // =================================================================
        // == Help commands
        // =================================================================

//        commandMap.put("card", (event, args) -> HelpCommands.getCardInfo(event, args));
//
//        commandMap.put("cards", (event, args) -> HelpCommands.printCardNames(event, args));
//
//        commandMap.put("cardAliases".toLowerCase(), (event, args) -> HelpCommands.printCardAliasList(event));
//
//        commandMap.put("help", (event, args) -> {
//
//            boolean onInvokedChannel = !args.isEmpty() && args.get(0).equals("-cc");
//            HelpCommands.printHelp(event, onInvokedChannel);
//
//        });


        // =================================================================
        // == My Exclusive Commands
        // =================================================================

//        commandMap.put("saveMembersTags".toLowerCase(), (event, args) -> MyExclusiveCommands.saveTagsFromNickname(event));
//
//        commandMap.put("command", (event, args) -> {
//
//        });
//
//        commandMap.put("changeBotDescriptionMessage".toLowerCase(), (event, args) -> MyExclusiveCommands.changeBotDescriptionMessage(event, args));
//        commandMap.put("CBDM".toLowerCase(), (event, args) -> MyExclusiveCommands.changeBotDescriptionMessage(event, args));
//        commandMap.put("CBMD".toLowerCase(), (event, args) -> MyExclusiveCommands.changeBotDescriptionMessage(event, args));
//
//
//        // =================================================================
//        // == Others
//        // =================================================================
//
//        commandMap.put("checkMembers".toLowerCase(), (event, args) -> CommandProcessor.checkMembers(event, args));
//        commandMap.put("cm", (event, args) -> CommandProcessor.checkMembers(event, args));


    }


    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {

        // Note for error handling, you'll probably want to log failed commands with a logger or sout
        // In most cases it's not advised to annoy the user with a reply incase they didn't intend to trigger a
        // command anyway, such as a user typing ?notacommand, the bot should not say "notacommand" doesn't exist in
        // most situations. It's partially good practise and partially developer preference

        // Given a message "/test arg1 arg2", argArray will contain ["/test", "arg1", "arg"]
        String[] argArray = event.getMessage().getContent().split(" ");

        // First ensure at least the command and prefix is present, the arg length can be handled by your command func
        if (argArray.length == 0)
            return;

        // Check if the first arg (the command) starts with the prefix defined in the utils class
        if (!argArray[0].startsWith(BotUtils.BOT_PREFIX))
            return;

        // Extract the "command" part of the first arg out by ditching the amount of characters present in the prefix
        String commandStr = argArray[0].substring(BotUtils.BOT_PREFIX.length()).toLowerCase();

        // Load the rest of the args in the array into a List for safer access
        List<String> argsList = new ArrayList<>(Arrays.asList(argArray));
        argsList.remove(0); // Remove the command

        // Lower case all the args.
        argsList.replaceAll(String::toLowerCase);

        // Instead of delegating the work to a switch, automatically do it via calling the mapping if it exists

        // FIXME: Only for testing. Comment on PROD.
//        if (119901900647825408L != event.getAuthor().getLongID())
//            return;

        if (commandMap.containsKey(commandStr)) {
            StringBuffer strBuffer = new StringBuffer();
            logger.info(strBuffer.append("Command ")
                    .append(commandStr)
                    .append(" started by ")
                    .append(event.getAuthor().getName())
                    .append(" (")
                    .append(event.getAuthor().getLongID())
                    .append(").")
                    .toString());
            BotUtils.setTypingStatus(event.getChannel(), true);
            commandMap.get(commandStr).runCommand(event, argsList);
            BotUtils.setTypingStatus(event.getChannel(), false);
            strBuffer = new StringBuffer();
            logger.info(strBuffer.append("Command ")
                    .append(commandStr)
                    .append(" finished. ")
                    .append(event.getAuthor().getName())
                    .append(" (")
                    .append(event.getAuthor().getLongID())
                    .append(").")
                    .toString());
        }
//        else
//            CommandProcessor.printUnknownCommand(event);

    }

}