package com.sjn.stamp.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.jakewharton.disklrucache.DiskLruCache
import java.io.*
import java.util.*
import java.util.concurrent.Executors
import java.util.regex.Pattern

@Suppress("unused")
class ParcelDiskCache<T : Parcelable?> @Throws(IOException::class)
private constructor(context: Context, private val classLoader: ClassLoader, name: String, maxSize: Long) : DiskCache<Parcelable?> {
    private val cache = DiskLruCache.open(
            File(context.externalCacheDir ?: context.cacheDir, name),
            getVersionCode(context) + Build.VERSION.SDK_INT, 1, maxSize)
    private val storeExecutor = Executors.newSingleThreadExecutor()
    private var saveInUI = true

    override val all: List<T>
        get() = getAll(null)

    override fun set(key: String, value: Parcelable?) {
        validateKey(key).let {
            Parcel.obtain().apply {
                writeString(PARCELABLE)
                writeParcelable(value, 0)
            }.let { parcel ->
                if (saveInUI) {
                    saveValue(cache, parcel, it)
                } else {
                    storeExecutor.execute(StoreParcelableValueTask(cache, parcel, it))
                }
            }
        }
    }

    operator fun set(key: String, values: List<T>) {
        validateKey(key).let {
            Parcel.obtain().apply {
                writeString(LIST)
                writeList(values)
            }.let { parcel ->
                if (saveInUI) {
                    saveValue(cache, parcel, it)
                } else {
                    storeExecutor.execute(StoreParcelableValueTask(cache, parcel, it))
                }
            }
        }
    }

    override fun get(key: String): T? {
        validateKey(key).let {
            getParcel(it)?.let { parcel ->
                try {
                    val type = parcel.readString()
                    if (type == LIST) {
                        throw IllegalAccessError("get list data with getList method")
                    }
                    if (type != null && type != PARCELABLE) {
                        throw IllegalAccessError("Parcel doesn't contain parcelable data")
                    }
                    return parcel.readParcelable<T>(classLoader)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    parcel.recycle()
                }
            }
        }
        return null
    }

    private fun getParcel(key: String): Parcel? {
        validateKey(key).let {
            try {
                cache.get(it)?.use { snapshot ->
                    getBytesFromStream(snapshot.getInputStream(0)).let { value ->
                        return Parcel.obtain().apply {
                            unmarshall(value, 0, value.size)
                            setDataPosition(0)
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun validateKey(key: String): String {
        val keyMatcher = getPattern(VALIDATE_KEY_REGEX).matcher(key)
        return StringBuilder().apply {
            while (keyMatcher.find()) {
                val group = keyMatcher.group()
                if (this.length + group.length > MAX_KEY_SYMBOLS) {
                    break
                }
                append(group)
            }
        }.toString().toLowerCase()
    }

    private fun getPattern(bodyRegex: String): Pattern =
            Pattern.compile(bodyRegex, Pattern.MULTILINE or Pattern.DOTALL or Pattern.CASE_INSENSITIVE)


    @JvmOverloads
    fun getList(key: String, itemClass: Class<*>? = null): List<T> {
        val res = ArrayList<T>()
        validateKey(key).let {
            getParcel(it)?.let { parcel ->
                try {
                    val type = parcel.readString()
                    if (type == PARCELABLE) {
                        throw IllegalAccessError("Get not a list data with get method")
                    }
                    if (type != null && type != LIST) {
                        throw IllegalAccessError("Parcel doesn't contain list data")
                    }
                    parcel.readList(res, if (itemClass != null) itemClass.classLoader else ArrayList::class.java.classLoader)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    parcel.recycle()
                }

            }
        }
        return res
    }

    override fun remove(key: String): Boolean {
        validateKey(key).let {
            try {
                return cache.remove(it.toLowerCase())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return false
    }

    private fun getAll(preffix: String?): List<T> {
        var list: MutableList<T> = ArrayList(1)
        cache.directory.listFiles()?.let { files ->
            list = ArrayList(files.size)
            files.map { it.name }
                    .filter { !TextUtils.isEmpty(preffix) && it.startsWith(preffix!!) && it.indexOf(".") > 0 || TextUtils.isEmpty(preffix) && it.indexOf(".") > 0 }
                    .map { it.substring(0, it.indexOf(".")) }
                    .forEach { key ->
                        get(key)?.let {
                            list.add(it)
                        }
                    }
        }
        return list
    }

    override fun clear() {
        try {
            cache.delete()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun exists(key: String): Boolean {
        validateKey(key).let {
            try {
                cache.get(it.toLowerCase())?.use {
                    return it.getLength(0) > 0
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return false
    }

    override fun close() {
        try {
            cache.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun shouldSaveInUI() {
        this.saveInUI = true
    }

    private class StoreParcelableValueTask(private val cache: DiskLruCache, private val value: Parcel, private val key: String) : Runnable {

        override fun run() {
            saveValue(cache, value, key)
        }
    }

    companion object {

        private const val LIST = "list"
        private const val PARCELABLE = "parcelable"
        private const val VALIDATE_KEY_REGEX = "[a-z0-9_-]{1,5}"
        private const val MAX_KEY_SYMBOLS = 62

        @Throws(IOException::class)
        fun open(context: Context, classLoader: ClassLoader, name: String, maxSize: Long): ParcelDiskCache<Parcelable> {
            return ParcelDiskCache(context, classLoader, name, maxSize)
        }

        private fun saveValue(cache: DiskLruCache?, value: Parcel, key: String) {
            cache ?: return
            key.toLowerCase().let { lowerKey ->
                try {
                    val skey = lowerKey.intern()
                    synchronized(skey) {
                        cache.edit(lowerKey).run {
                            writeBytesToStream(newOutputStream(0), value.marshall())
                            commit()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: RuntimeException) {
                    e.printStackTrace()
                } finally {
                    value.recycle()
                }
            }
        }

        @Throws(IOException::class)
        fun getBytesFromStream(source: InputStream): ByteArray {
            ByteArrayOutputStream().use { buffer ->
                source.use { inputStream ->
                    val data = ByteArray(1024)
                    var count: Int
                    while (true) {
                        count = inputStream.read(data, 0, data.size)
                        if (count == -1) {
                            break
                        }
                        buffer.write(data, 0, count)
                    }
                    buffer.flush()
                    return buffer.toByteArray()
                }
            }
        }

        @Throws(IOException::class)
        private fun writeBytesToStream(outputStream: OutputStream, bytes: ByteArray) {
            outputStream.use {
                it.write(bytes)
                it.flush()
            }
        }

        fun getVersionCode(context: Context): Int {
            try {
                return context.packageManager.getPackageInfo(context.packageName, 0).versionCode
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return 0
        }
    }
}