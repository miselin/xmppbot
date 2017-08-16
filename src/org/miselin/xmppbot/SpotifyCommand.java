package org.miselin.xmppbot;

import java.util.ArrayList;
import java.util.List;

/**
 * SpotifyCommand converts Spotify URIs into URLs.
 *
 * TODO: it would be great if it also showed info about the song/artist/album.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class SpotifyCommand implements BaseCommand {

    @Override
    public String usage() {
        return "[Spotify URI]+";
    }

    @Override
    public String description() {
        return "convert Spotify URIs into clickable URLs.";
    }

    @Override
    public String token() {
        return "spotify";
    }

    @Override
    public String[] handle(String message, String from) {
        String[] entries = message.split(" ");

        List<String> messages = new ArrayList<>();
        for (String entry : entries) {
            if (!entry.startsWith("spotify:")) {
                continue;
            }

            String[] components = entry.split(":");

            messages.add(String.format("http://open.spotify.com/%s/%s", components[1], components[2]));
        }

        return messages.toArray(new String[messages.size()]);
    }

}
