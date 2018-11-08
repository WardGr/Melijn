package me.melijn.jda.commands.developer;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import gnu.trove.map.TLongObjectMap;
import me.melijn.jda.Helpers;
import me.melijn.jda.Melijn;
import me.melijn.jda.blub.Category;
import me.melijn.jda.blub.Command;
import me.melijn.jda.blub.CommandEvent;
import me.melijn.jda.music.MusicManager;
import me.melijn.jda.music.MusicPlayer;
import me.melijn.jda.utils.TaskScheduler;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static me.melijn.jda.Melijn.PREFIX;

public class ShutdownCommand extends Command {

    public ShutdownCommand() {
        this.commandName = "shutdown";
        this.description = "shut's the bot nicely down";
        this.usage = PREFIX + commandName;
        this.category = Category.DEVELOPER;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            File tempFile = new File("melijn.mp3");
            //save players before shutdown
            TLongObjectMap<MusicPlayer> players = MusicManager.getManagerInstance().getPlayers();
            if (players != null)
            players.forEachValue((player) -> {
                TaskScheduler.async(() -> Helpers.scheduleClose(player.getGuild().getAudioManager()), 9000);
                boolean paused = player.getPaused();
                BlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<>();
                if (player.getAudioPlayer().getPlayingTrack() != null)
                    queue.offer(player.getAudioPlayer().getPlayingTrack());
                player.getListener().getTracks().forEach(queue::offer);
                Melijn.mySQL.addQueue(player.getGuild().getIdLong(), player.getGuild().getSelfMember().getVoiceState().getChannel().getIdLong(), paused, queue);
                player.getAudioPlayer().stopTrack();
                player.getListener().getTracks().clear();
                MusicManager.getManagerInstance().loadSimpleTrack(player.getGuild(), tempFile.getPath());
                return true;
            });
            event.reply("Shutting down in 9 seconds");
            TaskScheduler.async(() -> {
                tempFile.delete();
                event.getJDA().shutdown();
            }, 9000);
        } catch (Exception e) {
            e.printStackTrace();
            event.reply("something went wrong :/");
        }
    }
}
