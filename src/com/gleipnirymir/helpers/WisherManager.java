package com.gleipnirymir.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gleipnirymir.model.Wish;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class WisherManager {

    public static String WISHES_PATH = "C:/Users/andre/wishes/";

    private static Map<String, List<Wish>> wishes = new HashMap<>();

    public static List<Wish> getWishes(String playerTag) {
        if (wishes.get(playerTag) == null) {
            wishes.put(playerTag, getFromFile(playerTag));
        }

        return wishes.get(playerTag);
    }

    public static boolean existsWishForAuthorByCardName(String playerTag, String cardName) {
        List<Wish> wishes = getWishes(playerTag);
        return wishes.stream().map(Wish::getCardName).anyMatch(cardName::equals);
    }

    public static boolean deleteWish(String playerTag, String cardName) {
        List<Wish> wishes = getWishes(playerTag);
        Wish wish = wishes.stream().filter(w -> w.getCardName().equals(cardName)).findFirst().orElse(null);

        boolean wasWishRemoved = wishes.remove(wish);
        if (wasWishRemoved) {
            save(playerTag);
        }
        return wasWishRemoved;
    }

    public static void save(String playerTag) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Writing to a file
            mapper.writeValue(new File(getPathname(playerTag)), wishes.get(playerTag));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addWish(String playerTag, String cardName) {
        List<Wish> wishes = getWishes(playerTag);
        wishes.add(new Wish(cardName));
        save(playerTag);
    }
    
    // +* Andrew Le - this method iterates through an entire wishlist and returns a list of the prioritized cards.
    public static List<Wish> getPlayerPriorities(String playerTag) {
    	List<Wish> wishes = getWishes(playerTag);
    	List<Wish> priorities = new ArrayList<>();
    	for (Wish wish : wishes) {
    		if (wish.isPriority()) {
    			priorities.add(wish);
    		}
    	}
    	return priorities;		
    }
    
    // +* Andrew Le - this method sets the priority field of a wish to true when the addPriority command is called.
    public static void addPriority(String playerTag, String cardName) {
    	List<Wish> wishes = getWishes(playerTag);
    	Wish wish = wishes.stream().filter(w -> w.getCardName().equals(cardName)).findFirst().orElse(null);
    	if (wish != null) {
    		wish.setPriority(true);
    	}
    	save(playerTag);
    }
    
    // +* Andrew Le - this method sets the priority field of a prioritized wish to false
    // This is called when the deletePriority command is called, or when addPriority is called on a wish of a rarity
    // where there is already a priority. Only one priority is allowed per rarity.
    public static boolean deletePriority(String playerTag, String cardName) {
    	List<Wish> wishes = getWishes(playerTag);
    	Wish wish = wishes.stream().filter(w -> w.getCardName().equals(cardName)).findFirst().orElse(null);
    	boolean wasPriorityRemoved = false;
    	if (wish != null && wish.isPriority()) {
    		wish.setPriority(false);
    		wasPriorityRemoved = true;
    	}
    	if (wasPriorityRemoved) {
    		save(playerTag);
    	}
    	return wasPriorityRemoved;
    }
    
    // +* Andrew Le - this method checks if a wish is a priority (in the list of player's prioritized wishes).
    public static boolean cardIsPriority(String playerTag, String cardName) {
	    List<Wish> priorities = getPlayerPriorities(playerTag);
	    return priorities.stream().map(Wish::getCardName).anyMatch(cardName::equals);
	}

    private static List<Wish> getFromFile(String playerTag) {
        List<Wish> wishList = new ArrayList<>();

        String wishFile = getPathname(playerTag);
        if (Files.exists(Paths.get(wishFile))) {
            try {
                String wishListString = new String(Files.readAllBytes(Paths.get(wishFile)));
                JSONArray wishJsonArray = new JSONArray(wishListString);
                for (Object wishObj : wishJsonArray) {
                    JSONObject wishJson = (JSONObject) wishObj;
                    Wish wish = new Wish(wishJson.getString("cardName"));
                    // +* Andrew Le - check if priority property is null because old wishlist files didn't have this
                    // Even if this was null from the start, the property would be initialized when writing back to the file
                    if (!wishJson.isNull("priority")) {
                	    wish.setPriority(wishJson.getBoolean("priority"));
                	}
                    wishList.add(wish);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return wishList;

    }

    private static String getPathname(String playerTag) {
        return WISHES_PATH + playerTag + ".json";
    }

}
