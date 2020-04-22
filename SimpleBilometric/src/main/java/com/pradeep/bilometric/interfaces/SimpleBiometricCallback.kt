package com.pradeep.bilometric.interfaces

interface SimpleBiometricCallback {
    fun onAuthenticationSuccess()
    fun onAuthenticationError()
    fun onAuthenticationFailed()
    fun onNegativeButtonClick()
}