package co.monterosa.sdk

import android.util.Log
import co.monterosa.sdk.common.enums.Logger
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp

class MonterosaSdkViewManager : ViewGroupManager<MonterosaSdkExperienceView>() {
    override fun getName() = "MonterosaSdkExperienceView"

    init {
        Logger.debugEnabled = true
    }

    override fun createViewInstance(reactContext: ThemedReactContext): MonterosaSdkExperienceView {
        Log.d("MonterosaSDK", "Creating a view instance.")

        return MonterosaSdkExperienceView(reactContext)
    }

    @ReactProp(name = "configuration")
    fun setConfiguration(view: MonterosaSdkExperienceView, configuration: ReadableMap) {
        Log.d("MonterosaSDK", "Updating configuration of a view")
        Log.d("MonterosaSDK", "config=$configuration")

        view.configuration = configuration.toNonNullMap()
    }

    /**
     * Commands
     */
    override fun getCommandsMap() = mapOf(
        "sendMessageToNode" to COMMAND_SEND_MESSAGE,
        "sendRequestToNode" to COMMAND_SEND_REQUEST
    )

    override fun receiveCommand(
        root: MonterosaSdkExperienceView,
        commandId: String?,
        args: ReadableArray?
    ) {
        Log.d("MonterosaSDK", "Args: $args")
        Log.d("MonterosaSDK", "commandId: $commandId")

        if (args == null) return

        val action = args.getString(0) ?: return
        val payload = args.getMap(1)?.toNonNullMap() ?: emptyMap()

        when (commandId) {
            // action, payload
            "sendMessageToNode" -> {
                root.sendMessage(action, payload)
            }
            // action, payload, timeoutSeconds
            "sendRequestToNode" -> {
                if (args.size() < 3) {
                    Log.w("MonterosaSDK", "sendRequestToNode requires 3 arguments, got ${args.size()}")
                    return
                }
                val timeout = args.getInt(2).toLong()
                root.sendRequest(action, payload, timeout)
            }
        }
    }

    override fun getExportedCustomBubblingEventTypeConstants(): Map<String, Any> {
        return mapOf(
            "onMessageReceived" to mapOf(
                "phasedRegistrationNames" to mapOf(
                    "bubbled" to "onMessageReceived"
                )
            )
        )
    }

    override fun onDropViewInstance(view: MonterosaSdkExperienceView) {
        Log.d("MonterosaSDK", "Dropping view instance.")
        view.destroy()
        super.onDropViewInstance(view)
    }

    override fun needsCustomLayoutForChildren(): Boolean = true

    companion object {
        private const val COMMAND_SEND_MESSAGE = 1
        private const val COMMAND_SEND_REQUEST = 2
    }
}