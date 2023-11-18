package de.speedboat.plugins.coreview.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe

class AppSettingsSecrets {
    companion object {
        private const val SUBSYSTEM_IDENTIFIER = "de.speedboat.plugins.coreview"

        fun getSecret(secret: AppSecrets): String? {
            val serviceName = generateServiceName(SUBSYSTEM_IDENTIFIER, secret.key)
            val credentialAttributes = CredentialAttributes(serviceName)
            return PasswordSafe.instance.getPassword(credentialAttributes)
        }

        fun setSecret(secret: AppSecrets, value: String?) {
            val serviceName = generateServiceName(SUBSYSTEM_IDENTIFIER, secret.key)
            val credentialAttributes = CredentialAttributes(serviceName)
            PasswordSafe.instance.setPassword(credentialAttributes, value)
        }
    }
}

enum class AppSecrets(val key: String) {
    OPEN_AI_API_KEY("OPEN_AI_API_KEY")
}
