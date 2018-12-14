package org.revo.Util;

import com.comcast.viper.hlsparserj.PlaylistVersion;
import com.comcast.viper.hlsparserj.tags.UnparsedTag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.comcast.viper.hlsparserj.PlaylistFactory.parsePlaylist;

public class Utils {
    public static String read(Path path) {
        try {
            return Files.lines(path).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return "";
        }
    }

    public static Optional<UnparsedTag> getMasterTag(Path path) {
        return parsePlaylist(PlaylistVersion.TWELVE, read(path)).getTags().stream()
                .filter(it -> it.getTagName().equalsIgnoreCase("EXT-X-STREAM-INF"))
                .findAny();
    }

    static String format(long millis) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

}
