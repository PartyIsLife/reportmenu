package me.party.mod

import dev.xdark.clientapi.event.input.KeyPress
import io.netty.buffer.Unpooled
import org.lwjgl.input.Keyboard
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.clientapi.readUtf8
import ru.cristalix.clientapi.writeUtf8
import ru.cristalix.clientapi.writeVarInt
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.ContextGui
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.utility.*
import java.util.*
import kotlin.collections.ArrayList

class Main : KotlinMod() {
    private lateinit var mod: Main
    private lateinit var menu: ContextGui
    private lateinit var textDiv: TextElement
    private lateinit var uid: String
    private lateinit var nick: String
    val typedText: ArrayList<String> = mutableListOf<String>() as ArrayList<String>
    override fun onEnable() {
        mod = this
        UIEngine.initialize(this)
        // кривой фронт
        val element = rectangle {
                size.apply {
                    x = 400.0
                    y = 300.0
                }
                color.apply {
                    red = 60
                    green = 104
                    blue = 18
                    alpha = 0.5
                }
                // Крестик справа сверху
                +rectangle {
                    origin = TOP_RIGHT
                    align = TOP_RIGHT
                    size.apply {
                        x = 20.0
                        y = 20.0
                    }
                    color.apply {
                        red = 60
                        green = 104
                        blue = 18
                        alpha = 0.5
                    }
                    +text {
                        content = "X"
                    }
                    onClick {
                        menu.close()
                    }
                }
                // Выбор режима

                // Ввод текста
                +rectangle {
                    origin = LEFT
                    offset.y = 50.0
                    size.apply {
                        x = 300.0
                        y = 150.0
                    }
                    color.apply {
                        red = 60
                        green = 104
                        blue = 18
                        alpha = 0.6
                    }
                    textDiv = +text {

                    }
                }

                // Отправить
                +rectangle {
                    origin = BOTTOM_RIGHT
                    size.apply {
                        x = 300.0
                        y = 150.0
                    }
                    color.apply {
                        red = 60
                        green = 104
                        blue = 18
                        alpha = 0.6
                    }
                    +text{
                        content = "Отправить"
                    }
                    onClick{
                        menu.close()
                        typedText.clear()
                        var buffer = Unpooled.buffer()
                        buffer.writeUtf8(uid)
                        buffer.writeUtf8(nick)
                        buffer.writeUtf8(textDiv.content)
                        //buffer.writeInt(text.content.toInt())
                        clientApi.clientConnection().sendPayload("rmenu:send", buffer)
                        textDiv.content = ""
                    }
                }
            }
        menu.addChild(element)
        UIEngine.overlayContext.addChild(menu)

        registerChannel("rmenu:call"){
            uid = readUtf8()
            nick = readUtf8()
            menu.open()
        }

        registerHandler<KeyPress> {
            //если бэкспейс
            if(key == 8) typedText.removeLast()
            //если не escape
            if(key != 27) {
                typedText.add(Keyboard.getKeyName(key))
                var string = ""
                typedText.forEach {
                    string = string.plus(it)
                }
                textDiv.content = string
            }
        }

    }
}