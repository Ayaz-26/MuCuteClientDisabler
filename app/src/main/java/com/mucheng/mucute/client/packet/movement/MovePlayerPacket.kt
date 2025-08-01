package com.mucheng.mucute.client.game.packet.bedrock.movement

import com.mucheng.mucute.client.game.packet.bedrock.shared.Vec3

class MovePlayerPacket {
    var pos: Vec3 = Vec3()
    var tick: Int = 0
    var onGround: Boolean = false
}