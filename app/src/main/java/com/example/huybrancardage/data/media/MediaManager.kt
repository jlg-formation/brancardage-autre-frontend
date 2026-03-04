package com.example.huybrancardage.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manager pour la gestion des médias (photos et documents)
 * Gère la compression, le stockage temporaire et la manipulation des images
 */
class MediaManager(private val context: Context) {

    companion object {
        private const val AUTHORITY = "com.example.huybrancardage.fileprovider"
        private const val IMAGES_DIR = "images"
        private const val MAX_IMAGE_SIZE = 1024 // Max dimension (largeur ou hauteur)
        private const val COMPRESSION_QUALITY = 85 // Qualité JPEG (0-100)
    }

    /**
     * Crée un fichier temporaire pour la prise de photo
     * @return Uri du fichier créé via FileProvider
     */
    fun createTempImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"

        val storageDir = File(context.cacheDir, IMAGES_DIR).apply {
            if (!exists()) mkdirs()
        }

        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

        return FileProvider.getUriForFile(context, AUTHORITY, imageFile)
    }

    /**
     * Compresse une image depuis son URI et retourne l'URI de l'image compressée
     * Corrige également l'orientation basée sur les données EXIF
     * @param sourceUri URI de l'image source
     * @return URI de l'image compressée ou null en cas d'erreur
     */
    suspend fun compressImage(sourceUri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            // Lire l'orientation EXIF avant de charger le bitmap
            val rotation = getExifRotation(sourceUri)

            // Charger le bitmap depuis l'URI
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return@withContext null

            // Appliquer la rotation EXIF si nécessaire
            val rotatedBitmap = rotateBitmap(originalBitmap, rotation)

            // Calculer les dimensions redimensionnées
            val scaledBitmap = scaleBitmap(rotatedBitmap, MAX_IMAGE_SIZE)

            // Créer le fichier de destination
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val compressedFileName = "COMPRESSED_${timeStamp}.jpg"

            val storageDir = File(context.cacheDir, IMAGES_DIR).apply {
                if (!exists()) mkdirs()
            }

            val compressedFile = File(storageDir, compressedFileName)

            // Compresser et sauvegarder
            FileOutputStream(compressedFile).use { outputStream ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream)
            }

            // Libérer la mémoire
            if (scaledBitmap != rotatedBitmap) {
                scaledBitmap.recycle()
            }
            if (rotatedBitmap != originalBitmap) {
                rotatedBitmap.recycle()
            }
            originalBitmap.recycle()

            FileProvider.getUriForFile(context, AUTHORITY, compressedFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Lit l'orientation EXIF de l'image et retourne l'angle de rotation nécessaire
     */
    private fun getExifRotation(uri: Uri): Int {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Applique une rotation à un bitmap si nécessaire
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap

        val matrix = Matrix().apply {
            postRotate(degrees.toFloat())
        }

        return Bitmap.createBitmap(
            bitmap,
            0, 0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    /**
     * Redimensionne un bitmap en conservant le ratio d'aspect
     */
    private fun scaleBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (ratio > 1) {
            // Image plus large que haute
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            // Image plus haute que large
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Récupère la taille d'un fichier en bytes depuis son URI
     */
    fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openInputStream(uri)?.use {
                it.available().toLong()
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Récupère le nom d'affichage d'un fichier depuis son URI
     */
    fun getFileName(uri: Uri): String? {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        return it.getString(nameIndex)
                    }
                }
            }
            // Fallback: extraire le nom du path
            uri.lastPathSegment
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Formate la taille en format lisible (Ko, Mo)
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_000_000 -> String.format(Locale.getDefault(), "%.1f Mo", bytes / 1_000_000.0)
            bytes >= 1_000 -> String.format(Locale.getDefault(), "%.1f Ko", bytes / 1_000.0)
            else -> "$bytes octets"
        }
    }

    /**
     * Supprime tous les fichiers temporaires du cache
     */
    fun clearCache() {
        val storageDir = File(context.cacheDir, IMAGES_DIR)
        if (storageDir.exists()) {
            storageDir.listFiles()?.forEach { it.delete() }
        }
    }
}

