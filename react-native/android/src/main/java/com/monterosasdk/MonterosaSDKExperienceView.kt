package com.monterosasdk

import android.content.Context
import android.util.Log
import co.monterosa.identifykit.IdentifyKit
import co.monterosa.sdk.common.interfaces.IdentifyKitListener
import co.monterosa.sdk.common.models.Credentials
import co.monterosa.sdk.common.models.Signature
import co.monterosa.sdk.common.models.UserData
import co.monterosa.sdk.core.Core
import co.monterosa.sdk.core.models.MonterosaSdkError
import co.monterosa.sdk.launcherkit.ExperienceConfiguration
import co.monterosa.sdk.launcherkit.ExperienceView
import co.monterosa.sdk.launcherkit.ExperienceViewListener
import co.monterosa.sdk.launcherkit.Launcher
import co.monterosa.sdk.launcherkit.model.Message
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.RCTEventEmitter
import java.net.URL

class MonterosaSDKExperienceView(context: Context) : WrappedViewGroup<ExperienceView>(context), ExperienceViewListener, IdentifyKitListener {

  var configuration: Map<String, Any> = emptyMap()
    set(value) {
      val previousConfiguration = configuration.toConfiguration()
      val config = value.toConfiguration()
      field = value

      debug("Setting config: $config")

      if (config == null) {
        debug("The configuration passed was invalid. Received: $value")
        return
      }

      try {
        val core = config.core(context)
        if (config.isDifferentExperienceThan(previousConfiguration)) {
          debug("Recreating the Experience: $config")
          recreateExperience(config, core)
        }

        // Always update the token
        updateToken(config.token, core)
      } catch (e: Throwable) {
        debug("Exception during setting config: $e")
      }
    }

  private fun updateToken(token: String?, core: Core) {
    val identify = IdentifyKit.from(core)
    if (token != null) {
      identify.credentials = Credentials(token)
    } else {
      identify.logout()
    }
    debug("Updated token to " + identify.credentials)
  }

  private fun recreateExperience(config: Configuration, core: Core) {
    post {
      val overrideURL = config.experienceUrl?.let { URL(it) }
      val experience = Launcher.from(core).getExperience(
        context,
        ExperienceConfiguration(
          eventId = config.eventId,
          hidesHeadersAndFooters = config.hidesHeadersAndFooters,
          autoresizesHeight = config.autoresizesHeight,
          supportsLoadingState = false,
          loadingViewProvider = null,
          errorViewProvider = null,
          parameters = config.parameters,
          launchesURLsWithBlankTargetToChrome = config.launchesURLsWithBlankTargetToChrome
        ),
        customUrl = overrideURL
      )
      experience.identify = IdentifyKit.from(core)

      replaceWrappedView(experience)
    }
  }

  override fun didAddView(experience: ExperienceView) {
    experience.listener = this
    experience.identify?.listener?.add(this)
  }

  override fun didRemoveView(experience: ExperienceView) {
    experience.listener = null
    experience.identify?.listener?.remove(this)
  }

  private fun debug(message: String) {
    sendReactNativeMessage(EventType.DEBUG, Arguments.createMap().apply {
      putString("message", message)
    })
    Log.e("MonterosaSDK", message)
  }

  private fun sendReactNativeMessage(type: EventType, payload: ReadableMap) {
    val event = Arguments.createMap().apply {
      putString("type", type.value)
      putMap("payload", payload)
    }

    val reactContext = context as ReactContext
    reactContext
      .getJSModule(RCTEventEmitter::class.java)
      .receiveEvent(id, "onMessageReceived", event)
  }

  // Experience View Listener

  enum class ExperienceEventType(val value: String) {
    DID_START_LOADING("didStartLoading"),
    DID_END_LOADING("didEndLoading"),
    DID_FAIL_LOADING("didFailLoading"),
    DID_CHANGE_INTRINSIC_SIZE("didChangeIntrinsicSize"),
    DID_BECOME_READY("didBecomeReady"),
    ON_DISPLAY_FULL_SCREEN_CHANGED("onDisplayedFullScreenChanged")
  }

  override fun onDisplayedFullScreenChanged(experienceView: ExperienceView, fullscreen: Boolean) {
    sendReactNativeMessage(EventType.EXPERIENCE_EVENT, Arguments.createMap().apply {
      putString("event", ExperienceEventType.ON_DISPLAY_FULL_SCREEN_CHANGED.value)
      putBoolean("fullscreen", fullscreen)
    })
  }

  override fun onEndLoading(experienceView: ExperienceView) {
    super.onEndLoading(experienceView)
    sendReactNativeMessage(EventType.EXPERIENCE_EVENT, Arguments.createMap().apply {
      putString("event", ExperienceEventType.DID_END_LOADING.value)
    })
  }

  override fun onFailLoading(experienceView: ExperienceView, error: MonterosaSdkError) {
    super.onFailLoading(experienceView, error)
    sendReactNativeMessage(EventType.EXPERIENCE_EVENT, Arguments.createMap().apply {
      putString("event", ExperienceEventType.DID_FAIL_LOADING.value)
      putMap("error", error.toJavascriptDictionary("Failed to load"))
    })
  }

  override fun onIntrinsicSizeChanged(experienceView: ExperienceView, width: Float, height: Float) {
    super.onIntrinsicSizeChanged(experienceView, width, height)
    sendReactNativeMessage(EventType.EXPERIENCE_EVENT, Arguments.createMap().apply {
      putString("event", ExperienceEventType.DID_CHANGE_INTRINSIC_SIZE.value)
      putMap("size", Arguments.createMap().apply {
        putDouble("width", width.toDouble())
        putDouble("height", height.toDouble())
      })
    })
  }

  override fun onMessage(experienceView: ExperienceView, message: Message) {
    super.onMessage(experienceView, message)
    sendReactNativeMessage(EventType.EXPERIENCE_MESSAGE, message.toJavascriptDictionary())
  }

  override fun onReady(experienceView: ExperienceView) {
    super.onReady(experienceView)
    sendReactNativeMessage(EventType.EXPERIENCE_EVENT, Arguments.createMap().apply {
      putString("event", ExperienceEventType.DID_BECOME_READY.value)
    })
  }

  override fun onStartLoading(experienceView: ExperienceView) {
    super.onStartLoading(experienceView)
    sendReactNativeMessage(EventType.EXPERIENCE_EVENT, Arguments.createMap().apply {
      putString("event", ExperienceEventType.DID_START_LOADING.value)
    })
  }

  // IdentifyKit Listener

  enum class IdentifyEventType(val value: String) {
    DID_UPDATE_CREDENTIALS("didUpdateCredentials"),
    DID_UPDATE_USER_DATA("didUpdateUserData"),
    DID_UPDATE_SESSION_SIGNATURE("didUpdateSessionSignature"),
    DID_REQUEST_LOGIN_BY_EXPERIENCE("didRequestLoginByExperience"),
    DID_FAIL_CREDENTIALS_VALIDATION("didFailCredentialsValidation")
  }

  override fun onCredentialsUpdated(credentials: Credentials?) {
    super.onCredentialsUpdated(credentials)
    sendReactNativeMessage(EventType.IDENTIFY_EVENT, Arguments.createMap().apply {
      putString("event", IdentifyEventType.DID_UPDATE_CREDENTIALS.value)
      putString("credentials", credentials?.token)
    })
  }

  override fun onCredentialsValidationFailed(exception: Exception) {
    super.onCredentialsValidationFailed(exception)
    sendReactNativeMessage(EventType.IDENTIFY_EVENT, Arguments.createMap().apply {
      putString("event", IdentifyEventType.DID_FAIL_CREDENTIALS_VALIDATION.value)
      putMap("error", exception.toJavascriptDictionary("Failed to validate credentials."))
    })
  }

  override fun onLoginRequestedByExperience() {
    super.onLoginRequestedByExperience()
    sendReactNativeMessage(EventType.IDENTIFY_EVENT, Arguments.createMap().apply {
      putString("event", IdentifyEventType.DID_REQUEST_LOGIN_BY_EXPERIENCE.value)
    })
  }

  override fun onSessionSignatureUpdated(signature: Signature?) {
    super.onSessionSignatureUpdated(signature)

    val sig = signature?.let {
      Arguments.createMap().apply {
        putString("sig", it.signature)
        putInt("timestamp", it.timestamp.toInt())
        putString("userId", it.userId)
      }
    }

    sendReactNativeMessage(EventType.IDENTIFY_EVENT, Arguments.createMap().apply {
      putString("event", IdentifyEventType.DID_UPDATE_SESSION_SIGNATURE.value)
      putMap("signature", sig)
    })
  }

  override fun onUserDataUpdated(userData: UserData?) {
    super.onUserDataUpdated(userData)
    sendReactNativeMessage(EventType.IDENTIFY_EVENT, Arguments.createMap().apply {
      putString("event", IdentifyEventType.DID_UPDATE_USER_DATA.value)
      putMap("userData", userData?.toJavascriptDictionary())
    })
  }

  fun sendMessage(action: String, payload: Map<String, Any>) {
    val experience = getChildAt(0) as? ExperienceView

    if (experience == null) {
      debug("Sent a message when the Experience is not available")
      return
    }

    experience.sendMessage(action, payload)
    debug("Sent message with action: $action.")
  }

  fun sendRequest(action: String, payload: Map<String, Any>, timeout: Long) {
    val experience = getChildAt(0) as? ExperienceView

    if (experience == null) {
      debug("Sent a message when the Experience is not available")
      return
    }

    experience.sendRequest(action, payload, timeout) {
      it.onFailure { error ->
        debug("Didn't obtain a responset to our request with action $action. Error - $error")
      }
      it.onSuccess { response ->
        debug("Obtained a response to our message with $action. Response - $response")
      }
    }
  }
}

enum class EventType(val value: String) {
  DEBUG("debug"),
  EXPERIENCE_MESSAGE("experienceMessage"),
  IDENTIFY_EVENT("identifyEvent"),
  ERROR("error"),
  EXPERIENCE_EVENT("experienceEvent")
}

fun Throwable.toJavascriptDictionary(message: String): ReadableMap {
  return Arguments.createMap().apply {
    putString("error", this@toJavascriptDictionary.toString())
    putString("message", message)
  }
}

fun UserData.toJavascriptDictionary(): ReadableMap {
  return this.data.data.toReadableMap()
}

fun Message.toJavascriptDictionary(): ReadableMap {
  return Arguments.createMap().apply {
    putString("action", action)
    putMap("payload", payload.toReadableMap())
    putString("respondingTo", respondingTo)
    putString("source", source?.name)
  }
}

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toReadableMap(): ReadableMap {
  val writableMap: WritableMap = Arguments.createMap()
  for ((key, value) in this) {
    when (value) {
      is Int -> writableMap.putInt(key, value)
      is Double -> writableMap.putDouble(key, value)
      is Float -> writableMap.putDouble(key, value.toDouble())
      is Long -> writableMap.putDouble(key, value.toDouble())
      is Boolean -> writableMap.putBoolean(key, value)
      is String -> writableMap.putString(key, value)
      is List<*> -> writableMap.putArray(key, (value as? List<Any?>)?.toReadableArray())
      is Map<*, *> -> writableMap.putMap(key, (value as? Map<String, Any?>)?.toReadableMap())
      else -> {
        Log.e("MonterosaSDK", "Type in map not supported: $value")
      }
    }
  }
  return writableMap
}

@Suppress("UNCHECKED_CAST")
fun List<Any?>.toReadableArray(): ReadableArray {
  val writableArray: WritableArray = Arguments.createArray()
  this.forEach {
    val value = it
    when (value) {
      is Int -> writableArray.pushInt(value)
      is Double -> writableArray.pushDouble(value)
      is Float -> writableArray.pushDouble(value.toDouble())
      is Long -> writableArray.pushDouble(value.toDouble())
      is Boolean -> writableArray.pushBoolean(value)
      is String -> writableArray.pushString(value)
      is List<*> -> writableArray.pushArray((value as? List<Any?>)?.toReadableArray())
      is Map<*, *> -> writableArray.pushMap((value as? Map<String, Any>)?.toReadableMap())
      else -> {
        Log.e("MonterosaSDK", "Type in map not supported: $value")
      }
    }

  }
  return writableArray
}
