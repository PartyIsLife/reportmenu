package me.party

import clepto.bukkit.B
import dev.implario.bukkit.platform.Platforms
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import dev.xdark.feder.NetUtil
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import me.func.mod.conversation.ModLoader
import me.func.mod.conversation.ModTransfer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.BukkitPlatform
import ru.cristalix.core.CoreApi
import ru.cristalix.core.inventory.IInventoryService
import ru.cristalix.core.inventory.InventoryService
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.party.IPartyService
import ru.cristalix.core.party.PartyService
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.transfer.ITransferService
import ru.cristalix.core.transfer.TransferService


class Main : JavaPlugin() {
    lateinit var app: Main
    private val coreApi: CoreApi = CoreApi.get()

    override fun onEnable() {
        app = this
        coreApi.init(BukkitPlatform(Bukkit.getServer(), Bukkit.getLogger(), this))
        coreApi.apply {
            registerService(ITransferService::class.java, TransferService(this.socketClient))
            registerService(IPartyService::class.java, PartyService(ISocketClient.get()))
            registerService(IInventoryService::class.java, InventoryService())
        }

        Platforms.set(PlatformDarkPaper())
//        Anime.include(Kit.STANDARD, Kit.EXPERIMENTAL, Kit.DIALOG, Kit.NPC)
        ModLoader.loadAll("mods")

        // Конфигурация реалма
        IRealmService.get().currentRealmInfo.apply {
            status = RealmStatus.WAITING_FOR_PLAYERS
            isLobbyServer = true
            readableName = "не важно"
            groupName = "Репортменю"
            servicedServers = arrayOf("PRM")
        }

        Bukkit.getMessenger().registerIncomingPluginChannel(this, "rmenu:send") { _, player: Player, bytes: ByteArray? ->
                val buf: ByteBuf = Unpooled.wrappedBuffer(bytes)
                val uid: String = NetUtil.readUtf8(buf)
                val nick: String = NetUtil.readUtf8(buf)
                val text: String = NetUtil.readUtf8(buf)
                player.sendMessage("$nick [$uid ] : $text")
        }

        B.regCommand({ player, _ ->
            ModTransfer().string(player.uniqueId.toString()).string(player.name).send("rmenu:call", player)
            null
        }, "bug")
    }
}
