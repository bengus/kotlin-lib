package ru.bengus.kotlinlib.lock

import org.redisson.*

class RedissonLockManager : LockManagerInterface {
    val s = Redisson.create()
}