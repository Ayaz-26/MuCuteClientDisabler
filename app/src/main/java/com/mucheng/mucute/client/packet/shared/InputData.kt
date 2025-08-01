package com.mucheng.mucute.client.game.packet.bedrock.shared

enum class InputData(val flag: Int) {
    START_JUMPING(0x1),
    JUMPING(0x2),
    SNEAKING(0x4),
    SPRINTING(0x8);

    companion object {
        fun fromFlag(flag: Int): Set<InputData> {
            return enumValues<InputData>().filter { flag and it.flag != 0 }.toSet()
        }
    }
}