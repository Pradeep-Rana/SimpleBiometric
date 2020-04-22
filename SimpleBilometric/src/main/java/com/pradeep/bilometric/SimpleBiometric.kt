package com.pradeep.bilometric

import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.pradeep.bilometric.interfaces.SimpleBiometricCallback
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class SimpleBiometric(val mActivity: FragmentActivity) {
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var biometricPrompt: BiometricPrompt
    val TAG: String = "SimpleBiometric"
    val executor: Executor = Executors.newSingleThreadExecutor()

    fun openBiometricDialog(title: String, description: String, negativeButtonText: String, biometriCallBack: SimpleBiometricCallback) {
        promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setDescription(description)
                .setNegativeButtonText(negativeButtonText)
                .build()

        biometricPrompt = BiometricPrompt(mActivity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) { // user clicked negative button
                    Log.d(TAG, "Negative button is clicked")
                    biometriCallBack.onNegativeButtonClick()
                } else { //TODO: Called when an unrecoverable error has been encountered and the operation is complete.
                    Log.d(TAG, "An unrecoverable error has been encountered and the operation is complete.")
                    biometriCallBack.onAuthenticationError()
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                //TODO: Called when a biometric is recognized.
                Log.d(TAG, "Success")
                biometriCallBack.onAuthenticationSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                //TODO: Called when a biometric is valid but not recognized.
                Log.d(TAG, "Error, Try again")
                biometriCallBack.onAuthenticationFailed()
            }
        })

        if ((BiometricManager.BIOMETRIC_SUCCESS).equals(BiometricManager.from(mActivity).canAuthenticate())) {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    fun canAuthenticate(): Boolean {
        return if ((BiometricManager.BIOMETRIC_SUCCESS).equals(BiometricManager.from(mActivity).canAuthenticate())) true else false
    }
}