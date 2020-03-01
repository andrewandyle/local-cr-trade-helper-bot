package com.gleipnirymir;

import org.apache.log4j.BasicConfigurator;

import com.gleipnirymir.helpers.ClashRoyaleAPIHelper;
import com.gleipnirymir.utils.BotUtils;
import sx.blah.discord.api.IDiscordClient;

public class MainRunner {
	
	/* NOTE: This bot is no longer functional, as the API services have been discontinued.
	 * 
	 * I have stored away my keys and migrating to a different API is a work in progress.
	 * 
	 * RoyaleAPI Developer Note: Our API has been a staple in the Clash Royale
	 * community since its launch in Summer 2017. The API has served over 2,000 
	 * active developer tokens and handles 15 million requests daily. Unfortunately,
	 * the cost of maintaining the resources to support this has far exceeded our 
	 * ability to raise funds largely due to the limitations of the Supercell Fan 
	 * Content Policy. After long and careful consideration we are discontinuing the
	 * API service effective March 1, 2020. */
	
	private static final String DISCORD_TOKEN = "";
	private static final String API_TOKEN = "";

    public static void main(String[] args){

    	BasicConfigurator.configure();
        IDiscordClient cli = BotUtils.getBuiltDiscordClient(DISCORD_TOKEN);

        // Register a listener via the EventSubscriber annotation which allows for organization and delegation of events
        cli.getDispatcher().registerListener(new CommandHandler());

        // Only login after all events are registered otherwise some may be missed.
        cli.login();

        ClashRoyaleAPIHelper.setApiToken(API_TOKEN);
    }

}