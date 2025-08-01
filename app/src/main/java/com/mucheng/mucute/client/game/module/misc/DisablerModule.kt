package com.mucheng.mucute.client.game.module.misc

import com.mucheng.mucute.client.game.module.Module
import com.mucheng.mucute.client.game.settings.EnumSetting
import com.mucheng.mucute.client.game.settings.SettingCategory

class DisablerModule : Module() {
    private enum class ServerMode {
        LIFEBEAT_OLD, 
        LIFEBEAT_NEW
    }

    private var mode = ServerMode.LIFEBEAT_OLD
    private var lastMs = System.currentTimeMillis()
    private var timeMs = -1L

    init {
        name = "Disabler"
        description = "Disable the anticheat"
        category = SettingCategory.MISC
        
        registerSetting(EnumSetting(
            "Server", 
            "Change mode", 
            ServerMode.values().map { it.name.replace("_", "-") },
            ServerMode.LIFEBEAT_OLD.name.replace("_", "-")
        ) { newValue ->
            mode = ServerMode.valueOf(newValue.replace("-", "_"))
        })
    }

    override fun getModeText(): String {
        return "Lifeboat"
    }

    private fun getCurrentMs(): Long {
        return System.currentTimeMillis()
    }

    private fun getElapsedTime(): Long {
        return getCurrentMs() - timeMs
    }

    private fun resetTime() {
        lastMs = getCurrentMs()
        timeMs = getCurrentMs()
    }

    private fun hasTimedElapsed(time: Long, reset: Boolean): Boolean {
        if (getCurrentMs() - lastMs > time) {
            if (reset) resetTime()
            return true
        }
        return false
    }

    override fun onSendPacket(packet: Any): Boolean {
        val packetName = packet::class.simpleName ?: return false

        when (mode) {
            ServerMode.LIFEBEAT_OLD -> {
                if (packetName == "PlayerAuthInputPacket" || packetName == "MovePlayerPacket") {
                    // Type-safe casting with Kotlin's smart casts
                    if (packet is PlayerAuthInputPacket) {
                        val perc = (packet.ticksAlive % 3) / 3.0f
                        val targetY = if (perc < 0.5f) 0.02f else -0.02f
                        packet.position.y = lerp(packet.position.y, packet.position.y + targetY, perc)
                        packet.move.y = -(1.0f / 3.0f)
                        if (packet.ticksAlive % 3 == 0) {
                            packet.inputData = packet.inputData or InputData.START_JUMPING.flag
                        }
                        packet.inputData = packet.inputData or InputData.JUMPING.flag
                    }
                    if (packet is MovePlayerPacket) {
                        val perc = (packet.tick % 3) / 3.0f
                        val targetY = if (perc < 0.5f) 0.02f else -0.02f
                        packet.pos.y = lerp(packet.pos.y, packet.pos.y + targetY, perc)
                        packet.onGround = true
                    }
                }
            }
            ServerMode.LIFEBEAT_NEW -> {
                if (packetName == "PlayerAuthInputPacket") {
                    if (packet is PlayerAuthInputPacket) {
                        packet.playMode = ClientPlayMode.SCREEN
                        packet.inputMode = InputModeAuth.TOUCH
                        packet.ticksAlive = 0
                        // Simulate HitResult modification
                        Game.localPlayer?.level?.hitResult?.type = HitResultType.AIR
                    }
                }
            }
        }
        return false // Never block packets, only modify
    }

    private fun lerp(a: Float, b: Float, t: Float): Float {
        return a + (b - a) * t
    }
}