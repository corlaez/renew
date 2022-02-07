package com.corlaez.renew

/**
 * These are all the states that a Renew object can be in.
 *
 * Idle: The object can start sending/emitting values concurrently but has not done it yet
 * Running: A concurrent loop is sending/emitting values. Only one of this loops is allowed per object
 * Cancelled: Terminal state. The method cancel has been executed. All concurrent loops are stopped. It is not possible
 * to start a concurrent loop once this state is reached.
 *
 * @see Renew
 * */
public enum class RenewState {
    IDLE,
    RUNNING,
    CANCELLED
}
