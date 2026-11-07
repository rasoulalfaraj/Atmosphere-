package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cached_weather")
data class CachedWeatherEntity(
    @PrimaryKey val cityId: String,
    val forecastJson: String,
    val airQualityJson: String,
    val radarJson: String,
    val cachedAtEpochMillis: Long
)

@Entity(tableName = "saved_cities")
data class SavedCityEntity(
    @PrimaryKey val id: String,
    val nameEn: String,
    val nameAr: String,
    val governorateEn: String,
    val governorateAr: String,
    val countryEn: String,
    val countryAr: String,
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Int,
    val timezone: String,
    val isIraqiCity: Boolean,
    val isFavorite: Boolean,
    val lastViewedEpochMillis: Long
)

@Dao
interface WeatherDao {
    @Query("SELECT * FROM cached_weather WHERE cityId = :cityId LIMIT 1")
    fun observeCachedWeather(cityId: String): Flow<CachedWeatherEntity?>

    @Query("SELECT * FROM cached_weather WHERE cityId = :cityId LIMIT 1")
    suspend fun getCachedWeatherOnce(cityId: String): CachedWeatherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedWeather(entity: CachedWeatherEntity)

    @Query("SELECT * FROM saved_cities ORDER BY isFavorite DESC, lastViewedEpochMillis DESC")
    fun observeSavedCities(): Flow<List<SavedCityEntity>>

    @Query("SELECT * FROM saved_cities ORDER BY isFavorite DESC, lastViewedEpochMillis DESC")
    suspend fun getSavedCitiesOnce(): List<SavedCityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSavedCity(city: SavedCityEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultCitiesIfEmpty(cities: List<SavedCityEntity>)

    @Query("DELETE FROM saved_cities WHERE id = :cityId")
    suspend fun deleteSavedCity(cityId: String)

    @Query("DELETE FROM cached_weather")
    suspend fun clearAllCache()
}

@Database(
    entities = [CachedWeatherEntity::class, SavedCityEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AtmosphereDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao

    companion object {
        @Volatile
        private var INSTANCE: AtmosphereDatabase? = null

        fun getInstance(context: Context): AtmosphereDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AtmosphereDatabase::class.java,
                    "atmosphere_ai_weather.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
