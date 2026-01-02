package com.watermelon.player.database

import androidx.room.TypeConverter

/**
 * Converters.kt
 * Room TypeConverters for non-primitive fields.
 * Currently:
 *   - String ↔ List<String> for future tag lists (currently tags are comma-separated String)
 *   - Can expand for Uri, Date, etc.
 */

class Converters {

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(data: String?): List<String>? {
        return data?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
    }

    // Future: add for tags Set, Uri, etc.
}
