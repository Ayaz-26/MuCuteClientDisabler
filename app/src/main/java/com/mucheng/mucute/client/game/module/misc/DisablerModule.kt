package com.mucheng.mucute.client.game.module.misc

import com.mucheng.mucute.client.game.module.Module
import com.mucheng.mucute.client.packet.bedrock.auth.ClientPlayMode
import com.mucheng.mucute.client.packet.bedrock.auth.InputModeAuth
import com.mucheng.mucute.client.packet.bedrock.auth.PlayerAuthInputPacket
import com.mucheng.mucute.client.packet.bedrock.entity.HitResultType
import com.mucheng.mucute.client.packet.bedrock.movement.MovePlayerPacket
import com.mucheng.mucute.client.packet.bedrock.shared.InputData

class DisablerModule : Module() {
    // ===== Settings System =====
    private sealed class Setting<T>(
        val name: String,
        val description: String = "",
        var value: T
    ) {
        class EnumSetting<T : Enum<T>>(
            name: String,
            description: String = "",
            val values: Array<T>,
            defaultValue: T
        ) : Setting<T>(name, description, defaultValue)
    }

    private val settings = mutableListOf<Setting<*>>()
    private var currentMode = Mode.LIFEBEAT_OLD

    enum class Mode {
        LIFEBEAT_OLD,
        LIFEBEAT_NEW
    }

    // ===== Module Implementation =====
    init {
        name = "Disabler"
        description = "Bypass anti-cheat checks"
        
        // Register settings
        registerSetting(
            Setting.EnumSetting(
                "Server Mode",
                "Select anti-cheat version",
                Mode.values(),
                Mode.LIFEBEAT_OLD
            )
        )
    }

    private fun <T> registerSetting(setting: Setting<T>) {
        settings.add(setting)
        when (setting) {
            is Setting.EnumSetting<*> -> {
                @Suppress("UNCHECKED_CAST")
                currentMode = (setting as Setting.EnumSetting<Mode>).value
            }
        }
    }

    override fun getModeText(): String {
        return when (currentMode) {
            Mode.LIFEBEAT_OLD -> "Lifeboat (Old)"
            Mode.LIFEBEAT_NEW -> "Lifeboat (New)"
        }
    }

    // ===== Packet Handling =====
    override fun onSendPacket(packet: Any): Boolean {
        return when (currentMode) {
            Mode.LIFEBEAT_OLD -> handleOldMode(packet)
            Mode.LIFEBEAT_NEW -> handleNewMode(packet)
        }
    }

    private fun handleOldMode(packet: Any): Boolean {
        when (packet) {
            is PlayerAuthInputPacket -> {
                val perc = (packet.ticksAlive % 3) / 3f
                packet.position.y = lerp(
                    packet.position.y,
                    packet.position.y + if (perc < 0.5f) 0.02f else -0.02f,
                    perc
                )
                packet.move.y = -(1f / 3f)
                if (packet.ticksAlive % 3 == 0) {
                    packet.inputData = packet.inputData or InputData.START_JUMPING.flag
                }
                packet.inputData = packet.inputData or InputData.JUMPING.flag
            }
            is MovePlayerPacket -> {
                val perc = (packet.tick % 3) / 3f
                packet.pos.y = lerp(
                    packet.pos.y,
                    packet.pos.y + if (perc < 0.5f) 0.02f else -0.02f,
                    perc
                )
                packet.onGround = true
            }
        }
        return false
    }

    private fun handleNewMode(packet: Any): Boolean {
        if (packet is PlayerAuthInputPacket) {
            packet.playMode = ClientPlayMode.SCREEN
            packet.inputMode = InputModeAuth.TOUCH
            packet.ticksAlive = 0
            // Simulate hit result modification
            Game.localPlayer?.level?.hitResult?.type = HitResultType.AIR
        }
        return false
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

    // ===== UI Integration Helpers =====
    fun getSettings(): List<Setting<*>> {
        return settings.toList()
    }

    fun <T> setSettingValue(name: String, value: T) {
        settings.find { it.name == name }?.value = value
    }
}