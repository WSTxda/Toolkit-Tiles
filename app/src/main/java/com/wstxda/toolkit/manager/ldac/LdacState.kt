package com.wstxda.toolkit.manager.ldac

enum class LdacState(val value: Int) {
    ADAPTIVE(1003),
    CONNECTION(1002), // 330kbps
    BALANCED(1001),   // 660kbps
    QUALITY(1000)     // 990kbps
}