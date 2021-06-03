package ru.bengus.kotlinlib.session

class TooLateSessionSetException :
    IllegalStateException("It's too late to set session: response most likely already has been sent")