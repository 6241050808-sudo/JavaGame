package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.data.model.JarManifestInfo
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object JarParser {

    fun parseJarFile(context: Context, uri: Uri): JarManifestInfo {
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            return parseJarStream(inputStream)
        } finally {
            inputStream?.close()
        }
    }

    fun parseJarStream(stream: InputStream?): JarManifestInfo {
        if (stream == null) {
            return fallbackInfo("ไม่พบไฟล์ JAR")
        }

        var manifestText = ""
        var iconBytes: ByteArray? = null
        var iconCandidateName = ""
        var totalClasses = 0
        var totalFiles = 0
        var totalBytes = 0L

        // Buffer the stream if needed or iterate zip entries
        val zis = ZipInputStream(stream)
        var entry: ZipEntry? = zis.nextEntry

        val pendingImages = mutableMapOf<String, ByteArray>()

        while (entry != null) {
            totalFiles++
            val name = entry.name
            val size = if (entry.size > 0) entry.size else 0L
            totalBytes += size

            if (name.equals("META-INF/MANIFEST.MF", ignoreCase = true)) {
                val bytes = readEntryBytes(zis)
                manifestText = String(bytes, Charsets.UTF_8)
            } else if (name.endsWith(".class", ignoreCase = true)) {
                totalClasses++
            } else if (name.endsWith(".png", ignoreCase = true) || name.endsWith(".jpg", ignoreCase = true)) {
                // Read potential icons (store first few small images < 256KB)
                if (pendingImages.size < 8) {
                    val bytes = readEntryBytes(zis)
                    val cleanKey = name.trimStart('/').lowercase()
                    pendingImages[cleanKey] = bytes
                }
            }
            zis.closeEntry()
            entry = zis.nextEntry
        }

        val manifestProps = parseManifestAttributes(manifestText)
        val midletName = manifestProps["MIDlet-Name"] ?: "เกม J2ME ไม่ทราบชื่อ"
        val midletVendor = manifestProps["MIDlet-Vendor"] ?: "Unknown Developer"
        val midletVersion = manifestProps["MIDlet-Version"] ?: "1.0.0"
        val midlet1 = manifestProps["MIDlet-1"] ?: ""
        val midletIcon = manifestProps["MIDlet-Icon"] ?: ""
        val config = manifestProps["MicroEdition-Configuration"] ?: "CLDC-1.1"
        val profile = manifestProps["MicroEdition-Profile"] ?: "MIDP-2.0"

        // Extract icon filename from MIDlet-1 (Format: "Name, /icon.png, class.name")
        var iconPath = ""
        var mainClass = ""
        if (midlet1.isNotEmpty()) {
            val parts = midlet1.split(",")
            if (parts.size >= 2) {
                iconPath = parts[1].trim()
            }
            if (parts.size >= 3) {
                mainClass = parts[2].trim()
            }
        }
        if (iconPath.isEmpty() && midletIcon.isNotEmpty()) {
            iconPath = midletIcon.trim()
        }

        val cleanIconPath = iconPath.trimStart('/').lowercase()
        iconBytes = pendingImages[cleanIconPath] ?: pendingImages.values.firstOrNull()

        var iconBitmap: Bitmap? = null
        if (iconBytes != null && iconBytes.isNotEmpty()) {
            try {
                iconBitmap = BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.size)
            } catch (e: Exception) {
                // Fallback icon
            }
        }

        return JarManifestInfo(
            midletName = midletName,
            midletVendor = midletVendor,
            midletVersion = midletVersion,
            midletIconPath = iconPath.ifEmpty { null },
            mainClass = mainClass.ifEmpty { null },
            configuration = config,
            profile = profile,
            totalClasses = totalClasses,
            totalFiles = totalFiles,
            jarSizeBytes = totalBytes,
            iconBitmap = iconBitmap
        )
    }

    private fun readEntryBytes(zis: ZipInputStream): ByteArray {
        val buffer = ByteArray(4096)
        val baos = ByteArrayOutputStream()
        var read: Int
        while (zis.read(buffer).also { read = it } != -1) {
            baos.write(buffer, 0, read)
        }
        return baos.toByteArray()
    }

    private fun parseManifestAttributes(text: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val lines = text.split("\r\n", "\n", "\r")
        var currentKey: String? = null
        var currentValue = StringBuilder()

        for (line in lines) {
            if (line.startsWith(" ") && currentKey != null) {
                // Continuation line
                currentValue.append(line.substring(1))
            } else {
                if (currentKey != null) {
                    map[currentKey] = currentValue.toString().trim()
                }
                val colonIdx = line.indexOf(':')
                if (colonIdx > 0) {
                    currentKey = line.substring(0, colonIdx).trim()
                    currentValue = StringBuilder(line.substring(colonIdx + 1).trim())
                } else {
                    currentKey = null
                }
            }
        }
        if (currentKey != null) {
            map[currentKey] = currentValue.toString().trim()
        }
        return map
    }

    private fun fallbackInfo(msg: String): JarManifestInfo {
        return JarManifestInfo(
            midletName = msg,
            midletVendor = "RetroJar",
            midletVersion = "1.0",
            midletIconPath = null,
            mainClass = null
        )
    }
}
