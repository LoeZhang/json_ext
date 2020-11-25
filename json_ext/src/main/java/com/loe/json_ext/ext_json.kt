package com.loe.json_ext

import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * 安全实例化Json对象
 */
fun Json(jsonString: String? = null): JSONObject
{
    try
    {
        return JSONObject(jsonString)
    } catch (e: Exception)
    {
    }
    return JSONObject()
}

fun Json(key: String, value: Any): JSONObject
{
    return JSONObject().put(key, value)
}

/**
 * 安全实例化JsonArray对象
 */
fun JsonArray(jsString: String? = null): JSONArray
{
    try
    {
        return JSONArray(jsString)
    } catch (e: Exception)
    {
    }
    return JSONArray()
}

fun <T> jsOf(vararg elements: T): JSONArray
{
    val js = JsonArray()
    elements.forEach()
    {
        try
        {
            js.put(it)
        } catch (e: Exception)
        {
        }
    }
    return js
}

/**  Json安全取值，无null */

fun JSONObject?.gotString(key: String, default: String = ""): String
{
    val s = this?.optString(key, default)
    if (s == "null") return default
    return s ?: default
}

fun JSONObject?.gotInt(key: String, default: Int = 0): Int
{
    return this?.optInt(key, default) ?: default
}

fun JSONObject?.gotLong(key: String, default: Long = 0): Long
{
    return this?.optLong(key, default) ?: default
}

fun JSONObject?.gotDouble(key: String, default: Double = 0.0): Double
{
    return this?.optDouble(key, default) ?: default
}

fun JSONObject?.gotBoolean(key: String, default: Boolean = false): Boolean
{
    return this?.optBoolean(key, default) ?: default
}

fun JSONObject?.gotDoubleString(key: String, default: String? = null): String
{
    val d = gotDouble(key)
    if (default != null && d == 0.0) return default
    val l = d.toLong()
    return if (d - l == 0.0) l.toString() else d.toString()
}

fun JSONObject?.gotJson(key: String): JSONObject
{
    var o = this?.optJSONObject(key)
    try
    {
        if (this != null && o == null) o = JSONObject(optString(key))
    } catch (e: Exception)
    {
    }
    return o ?: JSONObject()
}

fun JSONObject?.gotArray(key: String): JSONArray
{
    var js = this?.optJSONArray(key)
    try
    {
        if (this != null && js == null) js = JSONArray(optString(key))
    } catch (e: Exception)
    {
    }
    return js ?: JSONArray()
}

/** JsonArray安全取值，无null */

fun JSONArray?.gotString(i: Int, default: String = ""): String
{
    val s = this?.optString(i, default)
    if (s == "null") return default
    return s ?: default
}

fun JSONArray?.gotInt(i: Int, default: Int = 0): Int
{
    return this?.optInt(i, default) ?: default
}

fun JSONArray?.gotLong(i: Int, default: Long = 0): Long
{
    return this?.optLong(i, default) ?: default
}

fun JSONArray?.gotDouble(i: Int, default: Double = 0.0): Double
{
    return this?.optDouble(i, default) ?: default
}

fun JSONArray?.gotBoolean(i: Int, default: Boolean = false): Boolean
{
    return this?.optBoolean(i, default) ?: default
}

fun JSONArray?.gotDoubleString(i: Int, default: String? = null): String
{
    val d = gotDouble(i)
    if (default != null && d == 0.0) return default
    val l = d.toLong()
    return if (d - l == 0.0) l.toString() else d.toString()
}

fun JSONArray?.gotJson(i: Int): JSONObject
{
    var o = this?.optJSONObject(i)
    try
    {
        if (this != null && o == null) o = JSONObject(optString(i))
    } catch (e: Exception)
    {
    }
    return o ?: JSONObject()
}

fun JSONArray?.gotArray(i: Int): JSONArray
{
    var js = this?.optJSONArray(i)
    try
    {
        if (this != null && js == null) js = JSONArray(optString(i))
    } catch (e: Exception)
    {
    }
    return js ?: JSONArray()
}

/** JsonArray遍历 */

inline fun <T> JSONArray?.forEach(action: (T) -> Unit)
{
    if (this != null) for (i in 0 until length()) action(get(i) as T)
}

inline fun <T> JSONArray?.forEachIndex(action: (i: Int, T) -> Unit)
{
    if (this != null) for (i in 0 until length()) action(i, get(i) as T)
}

inline fun <T> JSONArray?.forEachReverse(action: (T) -> Unit)
{
    if (this != null) for (i in length() - 1 downTo 0) action(get(i) as T)
}

inline fun <T> JSONArray?.forEachIndexReverse(action: (i: Int, T) -> Unit)
{
    if (this != null) for (i in length() - 1 downTo 0) action(i, get(i) as T)
}

/** 对象转Json */

fun Any?.toJson(): JSONObject
{
    if(this is JSONObject)
    {
        return this
    }
    val json = JSONObject()
    if (this == null) return json
    this.javaClass.declaredFields.forEach()
    { field ->
        field.isAccessible = true
        try
        {
            val annotationName = field.getAnnotation(JsonName::class.java)?.value
            val name = annotationName ?: field.name
            val cls = field.type.kotlin
            if (isBean(cls))
            {
                if (field.type.simpleName.contains("List"))
                {
                    json.put(name, (field.get(this) as List<*>).toJsonArray())
                } else
                {
                    json.put(name, field.get(this).toJson())
                }
            } else
            {
                json.put(name, field.get(this))
            }
        } catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
    return json
}

fun List<*>?.toJsonArray(): JSONArray
{
    if(this is JSONArray)
    {
        return this
    }
    val js = JSONArray()
    if (this == null) return js
    forEach()
    {
        if (it != null)
        {
            val cls = it::class
            if (isBean(cls))
            {
                if (cls.java.simpleName.contains("List"))
                {
                    js.put((it as List<*>).toJsonArray())
                } else
                {
                    js.put(it.toJson())
                }
            } else
            {
                js.put(it)
            }
        } else js.put(null)
    }
    return js
}

/** Json转对象 */

inline fun <reified T : Any> JSONObject?.toBean(): T
{
    return toBean(T::class.java.newInstance())
}

fun <T : Any> JSONObject?.toBean(o: T): T
{
    if (this == null) return o
    o::class.java.declaredFields.forEach()
    { field ->
        field.isAccessible = true
        try
        {
            val annotationName = field.getAnnotation(JsonName::class.java)?.value
            val fieldName = annotationName ?: field.name
            val cls = field.type.kotlin
            if (has(fieldName)) when (cls)
            {
                String::class ->
                {
                    field.set(o, gotString(fieldName))
                }
                Int::class ->
                {
                    field.set(o, gotInt(fieldName))
                }
                Long::class ->
                {
                    field.set(o, gotLong(fieldName))
                }
                Boolean::class ->
                {
                    if (gotInt(fieldName) == 1)
                    {
                        field.set(o, true)
                    } else
                    {
                        field.set(o, gotBoolean(fieldName))
                    }
                }
                Double::class ->
                {
                    field.set(o, gotDouble(fieldName))
                }
                Float::class ->
                {
                    field.set(o, gotDouble(fieldName).toFloat())
                }
                else ->
                {
                    if (cls.java.simpleName.contains("List"))
                    {
                        field.set(
                            o,
                            gotArray(fieldName).toList((field.genericType as ParameterizedType).actualTypeArguments[0])
                        )
                    } else
                    {
                        field.set(o, gotJson(fieldName).toBean(field.type.newInstance()))
                    }
                }
            }
        } catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
    return o
}

inline fun <reified T : Any> JSONArray?.toList(): ArrayList<T>
{
    return toList(T::class.java) as ArrayList<T>
}

fun JSONArray?.toList(type: Type): ArrayList<*>
{
    val list = ArrayList<Any>()
    if (this == null) return list
    var isList: Boolean
    val isBean: Boolean
    if (type is ParameterizedType)
    {
        isBean = true
        isList = true
    } else
    {
        var cls = (type as Class<*>).kotlin
        isBean = isBean(cls)
        isList = type.simpleName.contains("List")
    }
    for (i in 0 until length())
    {
        if (isBean)
        {
            if (isList)
            {
                list.add(optJSONArray(i).toList((type as ParameterizedType).actualTypeArguments[0]))
            } else
            {
                val o = (type as Class<*>).newInstance()
                list.add(optJSONObject(i).toBean(o))
            }
        } else
        {
            list.add(opt(i))
        }
    }
    return list
}

private fun isBean(cls: KClass<*>): Boolean
{
    return when (cls)
    {
        String::class,
        Int::class,
        Long::class,
        Boolean::class,
        Double::class,
        Float::class -> false
        else -> true
    }
}

/**
 * Name注解
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonName(val value: String)