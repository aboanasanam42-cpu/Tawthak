package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ClinicalMediaDao
import com.example.data.local.dao.OdontogramDao
import com.example.data.local.dao.PatientDao
import com.example.data.local.dao.VisitDao
import com.example.data.local.entity.ClinicalMediaEntity
import com.example.data.local.entity.OdontogramEntity
import com.example.data.local.entity.PatientEntity
import com.example.data.local.entity.VisitEntity

@Database(
    entities = [
        PatientEntity::class,
        VisitEntity::class,
        OdontogramEntity::class,
        ClinicalMediaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DentalDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun visitDao(): VisitDao
    abstract fun odontogramDao(): OdontogramDao
    abstract fun clinicalMediaDao(): ClinicalMediaDao

    companion object {
        @Volatile
        private var INSTANCE: DentalDatabase? = null

        fun getInstance(context: Context): DentalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DentalDatabase::class.java,
                    "dental_vault.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
