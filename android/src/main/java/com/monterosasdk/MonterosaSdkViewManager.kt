package com.monterosasdk

import android.util.Log
import co.monterosa.sdk.common.enums.Logger
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.bridge.ReadableMap;

import com.facebook.react.bridge.ReadableArray
import com.facebook.react.uimanager.ViewGroupManager

class MonterosaSdkExperienceViewManager : ViewGroupManager<MonterosaSDKExperienceView>() {
  override fun getName() = "MonterosaSdkExperienceView"

  init {
    Logger.debugEnabled = true
  }

  override fun createViewInstance(reactContext: ThemedReactContext): MonterosaSDKExperienceView {
    Log.d("MonterosaSDK", "Creating a view instance.")

    return MonterosaSDKExperienceView(reactContext)
  }

  @ReactProp(name = "configuration")
  fun setConfiguration(view: MonterosaSDKExperienceView, configuration: ReadableMap) {
    Log.d("MonterosaSDK", "Updating configuration of a view")

    view.configuration = configuration.toHashMap() as Map<String, Any>
  }

  /**
   * Commands
   */
  override fun getCommandsMap() = mapOf(
    "sendMessageToNode" to COMMAND_SEND_MESSAGE,
    "sendRequestToNode" to COMMAND_SEND_REQUEST
  )

  @Deprecated("Deprecated in Java")
  override fun receiveCommand(
    root: MonterosaSDKExperienceView,
    commandId: Int,
    args: ReadableArray?
  ) {
    super.receiveCommand(root, commandId, args)
    Log.d("MonterosaSDK", "Args: $args")
    Log.d("MonterosaSDK", "commandId: $commandId")

    if (args == null) return

    when (commandId) {
      // action, payload
      COMMAND_SEND_MESSAGE -> root.sendMessage(
        args.getString(0) ?: "",
        args.getMap(1)?.toHashMap() as Map<String, Any>
      )
      // action, payload, timeoutSeconds
      COMMAND_SEND_REQUEST -> root.sendRequest(
        args.getString(0) ?: "",
        args.getMap(1)?.toHashMap() as Map<String, Any>,
        args.getInt(2).toLong()
      )
    }
  }

  override fun receiveCommand(
    root: MonterosaSDKExperienceView,
    commandId: String?,
    args: ReadableArray?
  ) {
    receiveCommand(root, commandId?.toInt() ?: -1, args)
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

  override fun needsCustomLayoutForChildren(): Boolean = true

  companion object {
    private const val COMMAND_SEND_MESSAGE = 1
    private const val COMMAND_SEND_REQUEST = 2
  }
}
