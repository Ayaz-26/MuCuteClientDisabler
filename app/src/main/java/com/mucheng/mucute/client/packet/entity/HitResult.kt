package com.mucheng.mucute.client.game.packet.bedrock.entity

enum class HitResultType {
    AIR,
    BLOCK,
    ENTITY
}

class HitResult {
    var type: HitResultType = HitResultType.AIR
}