package ru.bengus.kotlinlib.session

class TooEarlySessionGetException :
    IllegalStateException("It's too early to get session: session is not received yet from request")