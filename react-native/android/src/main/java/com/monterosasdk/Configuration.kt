package com.monterosasdk

import android.content.Context
import co.monterosa.sdk.core.Core
import kotlin.collections.HashMap

data class Configuration(
  val host: String, 
  val projectId: String, 
  val eventId: String?, 
  val experienceUrl: String?, 
  val token: String?,
  val parameters: HashMap<String, String>,
  val autoresizesHeight: Boolean,
  val hidesHeadersAndFooters: Boolean,
  val launchesURLsWithBlankTargetToChrome: Boolean
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
    } catch (_: Throwable) { }

    Core.configure(context, host = host, projectId = projectId, name = coreId())

    return Core.core(coreId())
  }

  private fun coreId(): String {
    return ("$host-----$projectId")
  }
}

fun Map<String, Any>.toConfiguration(): Configuration? {
  val host = this["host"] as? String
  val projectId = this["projectId"] as? String

  if (host == null || projectId == null) {
    return null
  }

  return Configuration(
    host = host,
    projectId = projectId,
    eventId = this["eventId"] as? String,
    experienceUrl = this["experienceUrl"] as? String,
    token = this["token"] as? String,
    parameters = this["parameters"] as? HashMap<String, String> ?: HashMap<String, String>(),
    autoresizesHeight = this["autoresizesHeight"] as? Boolean ?: false,
    hidesHeadersAndFooters = this["hidesHeadersAndFooters"] as? Boolean ?: true,
    launchesURLsWithBlankTargetToChrome = this["launchesURLsWithBlankTargetToBrowser"] as? Boolean ?: true
  )
}
