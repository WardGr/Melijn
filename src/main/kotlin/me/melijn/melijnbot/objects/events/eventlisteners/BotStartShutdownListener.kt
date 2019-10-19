package me.melijn.melijnbot.objects.events.eventlisteners

import me.melijn.melijnbot.Container
import me.melijn.melijnbot.objects.events.AbstractListener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.StatusChangeEvent

class BotStartShutdownListener(container: Container) : AbstractListener(container) {


    override fun onEvent(event: GenericEvent) {
        if (event is StatusChangeEvent) {
            onStatusChange(event)
        }
    }

    private fun onStatusChange(event: StatusChangeEvent) {
        val shardManager = event.jda.shardManager ?: return
        if (event.newStatus == JDA.Status.CONNECTED) {
            val loadedAllShards = shardManager.shardCache.count { jda -> jda.status == JDA.Status.CONNECTED } == container.settings.shardCount
            if (!loadedAllShards) return
            logger.info("All shards ready")
            logger.info("Starting services..")
            container.serviceManager.init(shardManager)
//            container.serviceManager.startServices()

            logger.info("Services ready")
        }
    }
}