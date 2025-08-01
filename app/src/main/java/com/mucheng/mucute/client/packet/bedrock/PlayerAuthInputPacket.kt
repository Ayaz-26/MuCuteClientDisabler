package com.mucheng.mucute.client.game.packet.bedrock.auth

import com.mucheng.mucute.client.game.packet.bedrock.shared.Vec3
import com.mucheng.mucute.client.game.packet.bedrock.shared.InputData

class PlayerAuthInputPacket {
    var position: Vec3 = Vec3()
    var move: Vec3 = Vec3()
    var inputData: Int = 0
    var ticksAlive: Int = 0
    var playMode: ClientPlayMode = ClientPlayMode.NORMAL
    var inputMode: InputModeAuth = InputModeAuth.KEYBOARD_MOUSE
}