package co.monterosa.sdk

import android.util.Log
import co.monterosa.sdk.common.models.UserData
import co.monterosa.sdk.launcherkit.model.Message
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap


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

fun Map<String, Any?>.toReadableMap(): ReadableMap {
    val writableMap: WritableMap = Arguments.createMap()
    for ((key, value) in this) {
        when (value) {
            null -> writableMap.putNull(key)
            is Int -> writableMap.putInt(key, value)
            is Double -> writableMap.putDouble(key, value)
            is Float -> writableMap.putDouble(key, value.toDouble())
            is Long -> writableMap.putDouble(key, value.toDouble())
            is Boolean -> writableMap.putBoolean(key, value)
            is String -> writableMap.putString(key, value)
            is List<*> -> writableMap.putArray(key, value.toReadableArray())
            is Map<*, *> -> writableMap.putMap(key, (value.asMap())?.toReadableMap())
            else -> {
                Log.e("MonterosaSDK", "Type in map not supported: $value")
            }
        }
    }
    return writableMap
}

fun List<Any?>.toReadableArray(): ReadableArray {
    val writableArray: WritableArray = Arguments.createArray()
    this.forEach { value ->
        when (value) {
            null -> writableArray.pushNull()
            is Int -> writableArray.pushInt(value)
            is Double -> writableArray.pushDouble(value)
            is Float -> writableArray.pushDouble(value.toDouble())
            is Long -> writableArray.pushDouble(value.toDouble())
            is Boolean -> writableArray.pushBoolean(value)
            is String -> writableArray.pushString(value)
            is List<*> -> writableArray.pushArray(value.toReadableArray())
            is Map<*, *> -> writableArray.pushMap((value.asMap())?.toReadableMap())
            else -> {
                Log.e("MonterosaSDK", "Type in map not supported: $value")
            }
        }
    }
    return writableArray
}

fun Any?.asMap(): Map<String, Any?>? {
    return when (this) {
        is Map<*, *> -> this.toStringKeyMap()
        else -> null
    }
}

fun Map<*, *>.toStringKeyMap(): Map<String, Any?> {
    val result = mutableMapOf<String, Any?>()

    for ((key, value) in this) {
        val stringKey = key as? String ?: continue
        result[stringKey] = value.normalize()
    }

    return result
}

fun Any?.normalize(): Any? {
    return when (this) {
        null -> null

        // Primitives (safe)
        is String, is Boolean -> this

        // Numbers (preserve Int vs Double)
        is Int, is Long, is Float, is Double -> {
            when (this) {
                is Double -> this.toBestNumber()
                is Float -> this.toDouble().toBestNumber()
                is Long -> this.toDouble().toBestNumber()
                else -> this
            }
        }

        is Map<*, *> -> this.toStringKeyMap()

        is List<*> -> this.map { it.normalize() }

        is Array<*> -> this.map { it.normalize() }

        else -> this.toString()
    }
}

fun Double.toBestNumber(): Any {
    return if (this.isFinite() &&
        this >= Int.MIN_VALUE &&
        this <= Int.MAX_VALUE &&
        this == this.toInt().toDouble()
    ) {
        this.toInt()
    } else {
        this
    }
}

fun ReadableMap.toMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    val iterator = keySetIterator()

    while (iterator.hasNextKey()) {
        val key = iterator.nextKey()
        map[key] = when (getType(key)) {
            ReadableType.Null -> null
            ReadableType.Boolean -> getBoolean(key)
            ReadableType.Number -> getDouble(key).toBestNumber()
            ReadableType.String -> getString(key)
            ReadableType.Map -> getMap(key)?.toMap()
            ReadableType.Array -> getArray(key)?.toList()
        }
    }

    return map
}

fun ReadableArray.toList(): List<Any?> {
    val list = mutableListOf<Any?>()

    for (i in 0 until size()) {
        list.add(
            when (getType(i)) {
                ReadableType.Null -> null
                ReadableType.Boolean -> getBoolean(i)
                ReadableType.Number -> getDouble(i).toBestNumber()
                ReadableType.String -> getString(i)
                ReadableType.Map -> getMap(i)?.toMap()
                ReadableType.Array -> getArray(i)?.toList()
            }
        )
    }

    return list
}

@Suppress("Unchecked_cast")
fun ReadableMap.toNonNullMap() = toMap().filterValues { it != null } as Map<String, Any>