package co.monterosa.sdk

import android.content.Context
import android.util.Log
import co.monterosa.sdk.common.MonterosaSdkError
import co.monterosa.sdk.common.interfaces.IdentifyKitListener
import co.monterosa.sdk.common.models.Credentials
import co.monterosa.sdk.common.models.Signature
import co.monterosa.sdk.common.models.UserData
import co.monterosa.sdk.core.Core
import co.monterosa.sdk.identifykit.IdentifyKit
import co.monterosa.sdk.launcherkit.ExperienceConfiguration
import co.monterosa.sdk.launcherkit.ExperienceView
import co.monterosa.sdk.launcherkit.ExperienceViewListener
import co.monterosa.sdk.launcherkit.Launcher
import co.monterosa.sdk.launcherkit.model.Message
import co.monterosa.sdk.launcherkit.model.ShareContent
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.events.RCTEventEmitter
import java.net.URL

class MonterosaSdkExperienceView(
    context: Context
) : WrappedViewGroup<ExperienceView>(context), ExperienceViewListener, IdentifyKitListener {

    var configuration: Map<String, Any> = emptyMap()
        set(value) {
            val previousConfiguration = field.toConfiguration()
            val config = value.toConfiguration()

            if (config == null) {
                debug("The configuration passed was invalid. Received: $value")
                return
            }

            debug("Setting config: $config")
            field = value

            try {
                val core = config.core(context)
                if (config.isDifferentExperienceThan(previousConfiguration)) {
                    debug("Recreating the Experience: $config")
                    recreateExperience(config, core)
                }

                // Always update the token
                updateToken(config.token, core)
            } catch (e: Exception) {
                Log.e(TAG, "Exception during setting config", e)
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
            val overrideURL = config.experienceUrl
                ?.takeIf {
                    it.isNotEmpty()
                }?.let {
                    URL(it)
                }

            val experience = Launcher.from(core).getExperience(
                context,
                ExperienceConfiguration(
                    eventId = config.eventId,
                    hidesHeadersAndFooters = config.hidesHeadersAndFooters,
                    autoresizesHeight = config.autoresizesHeight,
                    supportsLoadingState = false,
                    loadingViewProvider = null,
                    errorViewProvider = null,
                    backgroundColor = config.backgroundColor,
                    parameters = config.parameters,
                    allowsPopupBehavior = config.allowsPopupBehavior,
                    showsDefaultShareSheet = config.showsDefaultShareSheet,
                    launchesURLsWithBlankTargetToChrome = config.launchesURLsWithBlankTargetToBrowser
                ),
                customUrl = overrideURL
            )
            experience.identify = IdentifyKit.from(core)

            replaceWrappedView(experience)
        }
    }

    override fun didAddView(experience: ExperienceView) {
        Log.i(TAG, "added view ${experience.id}")

        experience.listener = this
        experience.identify?.listener?.add(this)
    }

    override fun didRemoveView(experience: ExperienceView) {
        Log.i(TAG, "removed view ${experience.id}")
        experience.listener = null
        experience.identify?.listener?.remove(this)
        experience.destroy()
    }

    private fun debug(message: String) {
        sendReactNativeMessage(EventType.DEBUG, Arguments.createMap().apply {
            putString("message", message)
        })
        Log.d("MonterosaSDK", message)
    }

    private fun sendReactNativeMessage(type: EventType, payload: ReadableMap) {
        val event = Arguments.createMap().apply {
            putString("type", type.value)
            putMap("payload", payload)
        }

        val reactContext = context as? ReactContext ?: run {
            Log.e(TAG, "Cannot send event '$type': context is not a ReactContext")
            return
        }

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
        DID_REQUEST_SHARE("didRequestShare"),
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

    override fun onIntrinsicSizeChanged(
        experienceView: ExperienceView,
        width: Float,
        height: Float
    ) {
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

    override fun onShare(experienceView: ExperienceView, shareContent: ShareContent) {
        super.onShare(experienceView, shareContent)
        sendReactNativeMessage(EventType.EXPERIENCE_EVENT, Arguments.createMap().apply {
            putString("event", ExperienceEventType.DID_REQUEST_SHARE.value)
            putMap("share", Arguments.createMap().apply {
                putString("url", shareContent.url)
                putString("title", shareContent.title)
                putString("description", shareContent.description)
                putString("imageUrl", shareContent.imageUrl)
            })
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
                putLong("timestamp", it.timestamp)
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

    fun destroy() {
        getWrappedChildView()?.let { experience ->
            didRemoveView(experience)
        }
    }

    fun sendMessage(action: String, payload: Map<String, Any>) {
        val experience = getWrappedChildView()

        if (experience == null) {
            debug("Sent a message when the Experience is not available")
            return
        }

        experience.sendMessage(action, payload)
        debug("Sent message with action: $action.")
    }

    fun sendRequest(action: String, payload: Map<String, Any>, timeout: Long) {
        val experience = getWrappedChildView()

        if (experience == null) {
            debug("Sent a message when the Experience is not available")
            return
        }

        experience.sendRequest(action, payload, timeout) {
            it.onFailure { error ->
                debug("Didn't obtain a response to our request with action $action. Error - $error")
            }
            it.onSuccess { response ->
                debug("Obtained a response to our message with $action. Response - $response")
            }
        }
    }

    companion object {
        private val TAG = MonterosaSdkExperienceView::class.java.simpleName
    }
}

enum class EventType(val value: String) {
    DEBUG("debug"),
    EXPERIENCE_MESSAGE("experienceMessage"),
    IDENTIFY_EVENT("identifyEvent"),
    ERROR("error"),
    EXPERIENCE_EVENT("experienceEvent")
}