package co.monterosa.sdk

import android.content.Context
import android.util.Log
import androidx.core.graphics.toColorInt
import co.monterosa.sdk.core.Core

data class Configuration(
    val host: String,
    val projectId: String,
    val eventId: String?,
    val experienceUrl: String?,
    val token: String?,
    val backgroundColor: Int?,
    val parameters: HashMap<String, String>,
    val autoresizesHeight: Boolean,
    val allowsPopupBehavior: Boolean,
    val showsDefaultShareSheet: Boolean,
    val hidesHeadersAndFooters: Boolean,
    val launchesURLsWithBlankTargetToBrowser: Boolean
) {
    fun isDifferentExperienceThan(previousConfiguration: Configuration?): Boolean {
        if (previousConfiguration == null) {
            return true
        }

        return previousConfiguration.host != host ||
                previousConfiguration.projectId != projectId ||
                previousConfiguration.eventId != eventId ||
                previousConfiguration.experienceUrl != experienceUrl ||
                previousConfiguration.parameters != parameters
    }

    fun core(context: Context): Core {
        try {
            return Core.core(coreId())
        } catch (_: Exception) {
            // Core not yet configured for this host/project — fall through to configure it
        }

        Core.configure(context, host = host, projectId = projectId, name = coreId())

        return Core.core(coreId())
    }

    private fun coreId(): String {
        return ("$host-----$projectId")
    }
}

fun Map<String, Any>.toConfiguration(): Configuration? {
    val host = this["host"]?.toString()
    val projectId = this["projectId"]?.toString()

    if (host == null || projectId == null) {
        return null
    }

    val color = (this["backgroundColor"]?.toString())?.let {
        try {
            it.toColorInt()
        } catch (_: Exception) {
            Log.w("MonterosaSDK", "Invalid backgroundColor value: $it")
            null
        }
    }

    val parameters = (this["parameters"] as? Map<*, *>)?.mapNotNull {
        val k = it.key as? String
        val v = it.value as? String
        if (k != null && v != null) k to v else null
    }?.toMap() as? HashMap ?: hashMapOf()

    val launchesURLsWithBlankTargetToBrowser =
        this["launchesURLsWithBlankTargetToBrowser"] as? Boolean ?: true

    return Configuration(
        host = host,
        projectId = projectId,
        backgroundColor = color,
        parameters = parameters,
        eventId = this["eventId"]?.toString(),
        experienceUrl = this["experienceUrl"]?.toString(),
        token = this["token"]?.toString(),
        autoresizesHeight = this["autoresizesHeight"] as? Boolean ?: false,
        hidesHeadersAndFooters = this["hidesHeadersAndFooters"] as? Boolean ?: true,
        allowsPopupBehavior = this["allowsPopupBehavior"] as? Boolean ?: false,
        showsDefaultShareSheet = this["showsDefaultShareSheet"] as? Boolean ?: true,
        launchesURLsWithBlankTargetToBrowser = launchesURLsWithBlankTargetToBrowser
    )
}