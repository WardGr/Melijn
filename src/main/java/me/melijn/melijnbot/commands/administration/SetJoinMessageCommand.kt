package me.melijn.melijnbot.commands.administration

import me.melijn.melijnbot.commandutil.administration.MessageCommandUtil
import me.melijn.melijnbot.enums.MessageType
import me.melijn.melijnbot.objects.command.AbstractCommand
import me.melijn.melijnbot.objects.command.CommandCategory
import me.melijn.melijnbot.objects.command.CommandContext

class SetJoinMessageCommand : AbstractCommand("command.join") {

    init {
        id = 34
        name = "setJoinMessage"
        aliases = arrayOf("sjm")
        commandCategory = CommandCategory.ADMINISTRATION
    }

    override suspend fun execute(context: CommandContext) {
        if (context.args.isEmpty()) {
            MessageCommandUtil.showMessage(this, context, MessageType.JOIN)
        } else {
            MessageCommandUtil.setMessage(this, context, MessageType.JOIN)
        }
    }


}