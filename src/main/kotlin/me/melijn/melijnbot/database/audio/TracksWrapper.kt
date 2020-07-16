package me.melijn.melijnbot.database.audio

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.melijn.llklient.utils.LavalinkUtil
import me.melijn.melijnbot.internals.music.TrackUserData
import me.melijn.melijnbot.internals.music.toMessage
import java.util.*

class TracksWrapper(private val tracksDao: TracksDao, private val lastVoiceChannelDao: LastVoiceChannelDao) {

    suspend fun getMap(): Map<Long, List<AudioTrack>> {
        val map = tracksDao.getMap()
        val newMap = mutableMapOf<Long, List<AudioTrack>>()

        for ((guildId, trackMap) in map) {
            val newList = mutableListOf<AudioTrack>()
            val sortedTrackMap = trackMap.toSortedMap(kotlin.Comparator { o1, o2 ->
                o1.compareTo(o2)
            })
            for ((_, pair) in sortedTrackMap) {
                val track = LavalinkUtil.toAudioTrack(pair.first)
                if (pair.second.isNotEmpty()) {
                    track.userData = TrackUserData.fromMessage(pair.second)
                }
                newList.add(track)
            }
            newMap[guildId] = newList
        }

        return newMap
    }

    suspend fun put(guildId: Long, botId: Long, playingTrack: AudioTrack, queue: Queue<AudioTrack>) {
        //Concurrent modification don't ask me why
        val newQueue: Queue<AudioTrack> = LinkedList(queue)
        val playing = LavalinkUtil.toMessage(playingTrack)
        var ud: TrackUserData? = playingTrack.userData as TrackUserData?
            ?: TrackUserData(botId, "", "")

        var udMessage = ud?.toMessage() ?: ""

        tracksDao.set(guildId, 0, playing, udMessage)

        var goodIndex = 1
        for (track in newQueue) {
            if (track == null) continue
            val json = LavalinkUtil.toMessage(track)
            ud = playingTrack.userData as TrackUserData?
            udMessage = ud?.toMessage() ?: ""
            tracksDao.set(guildId, goodIndex++, json, udMessage)
        }
    }

    fun clear() {
        tracksDao.clear()
    }

    suspend fun addChannel(guildId: Long, channelId: Long) {
        lastVoiceChannelDao.add(guildId, channelId)
    }

    suspend fun getChannels(): Map<Long, Long> {
        return lastVoiceChannelDao.getMap()
    }

    fun clearChannels() {
        lastVoiceChannelDao.clear()
    }
}