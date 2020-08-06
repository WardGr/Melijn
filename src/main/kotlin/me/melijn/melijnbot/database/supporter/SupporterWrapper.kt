package me.melijn.melijnbot.database.supporter

import com.fasterxml.jackson.module.kotlin.readValue
import me.melijn.melijnbot.database.DaoManager
import me.melijn.melijnbot.objectMapper

class SupporterWrapper(private val supporterDao: SupporterDao) {

    init {
        DaoManager.afterTableFunctions.add {
            supporterDao.getSupporters { supporters ->
                supporterDao.setCacheEntry("users", objectMapper.writeValueAsString(supporters.map { it.userId }))
                supporterDao.setCacheEntry("guilds", objectMapper.writeValueAsString(supporters.map { it.guildId }))
                for (supporter in supporters) {
                    supporterDao.setCacheEntry("${supporter.userId}", objectMapper.writeValueAsString(supporter))
                }
            }
        }
    }

    suspend fun getUsers(): List<Long> {
        return supporterDao.getCacheEntry("users")?.let {
            objectMapper.readValue<List<Long>>(it)
        } ?: supporterDao.getSupporters().map { it.userId }
    }

    suspend fun add(userId: Long) {
        val currentUsers = getUsers()
        if (!currentUsers.contains(userId)) {
            val addTime = System.currentTimeMillis()
            val newUser = Supporter(userId, -1, addTime, 0)
            supporterDao.addUser(newUser)

            supporterDao.setCacheEntry("users", objectMapper.writeValueAsString(currentUsers + userId))
            supporterDao.setCacheEntry(userId, objectMapper.writeValueAsString(newUser))
        }
    }

    suspend fun remove(userId: Long) {
        val currentUsers = getUsers()
        if (currentUsers.contains(userId)) {
            supporterDao.removeUser(userId)
            supporterDao.removeCacheEntry(userId)
            supporterDao.setCacheEntry("users", objectMapper.writeValueAsString(currentUsers - userId))
        }
    }

    suspend fun setGuild(authorId: Long, guildId: Long) {
        val supporters = supporterDao.getSupporters().toMutableList()
        val lastGuildPickTime = System.currentTimeMillis()

        val supporter = supporterDao.getCacheEntry("$authorId")?.let {
            val supp = objectMapper.readValue<Supporter>(it)
            Supporter(authorId, guildId, supp.startMillis, lastGuildPickTime)
        } ?: Supporter(authorId, guildId, lastGuildPickTime, lastGuildPickTime)

        supporters.removeIf { it.userId == authorId }
        supporters.add(supporter)

        supporterDao.setGuild(authorId, guildId, lastGuildPickTime)

        supporterDao.setCacheEntry("users", objectMapper.writeValueAsString(supporters.map { it.userId }))
        supporterDao.setCacheEntry("guilds", objectMapper.writeValueAsString(supporters.map { it.guildId }))
    }

    suspend fun getSupporter(supporterId: Long): Supporter? {
        return supporterDao.getCacheEntry(supporterId)?.let { objectMapper.readValue<Supporter>(it) }
    }

    suspend fun getGuilds(): List<Long> {
        return supporterDao.getCacheEntry("guilds")?.let { objectMapper.readValue<List<Long>>(it) }
            ?: supporterDao.getSupporters().map { it.guildId }
    }
}
