package com.gleipnirymir.commands;

import com.gleipnirymir.model.User;
import com.gleipnirymir.utils.BotUtils;
import com.gleipnirymir.utils.HibernateUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.StatusType;

import java.util.List;

/**
 * This class has commands that only can be used by me.
 */
public class MyExclusiveCommands {

    private static long MY_AUTHOR_ID = 119901900647825408L;

    public static void saveTagsFromNickname(MessageReceivedEvent event) {
        if (!isAuthorizedUser(event)) {
            return;
        }

        Session session = HibernateUtils.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        IGuild guild = event.getGuild();
        List<IUser> users = guild.getUsers();
        for (IUser user : users) {
            String nickname = user.getNicknameForGuild(guild);
            if (nickname != null) {
                int tagStartIndex = nickname.indexOf("[#");
                if (tagStartIndex > -1) {
                    String playerTag = nickname.substring(tagStartIndex + 2, nickname.length() - 1);
                    User userDB = new User(user.getLongID(), playerTag.toUpperCase());
                    User userFetched = session.get(User.class, userDB.getDiscordAccountId());

                    if (userFetched != null) {
                        if (!userDB.getCrAccountTag().equals(userFetched.getCrAccountTag())) {
                            userFetched.setCrAccountTag(userDB.getCrAccountTag());
                            session.update(userFetched);
                        }
                    } else {
                        session.save(userDB);
                    }

                }
            }
        }

        transaction.commit();
        if (session.isOpen()) {
            session.close();
        }

        BotUtils.sendMessage(event.getAuthor().getOrCreatePMChannel(), "Done.");

    }

    public static void changeBotDescriptionMessage(MessageReceivedEvent event, List<String> args) {
        if (!isAuthorizedUser(event)) {
            return;
        }

        String message = "$help";

        if (!args.isEmpty()){
            message = String.join(" ", args);
        }

        event.getClient().changePresence(StatusType.ONLINE, ActivityType.PLAYING, message);
    }

    private static boolean isAuthorizedUser(MessageReceivedEvent event) {
        IUser author = event.getAuthor();
        return MY_AUTHOR_ID == author.getLongID();
    }

}
