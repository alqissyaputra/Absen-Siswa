package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.AttendanceSessionDao
import com.example.data.dao.SchoolClassDao
import com.example.data.dao.StudentDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceSession
import com.example.data.model.SchoolClass
import com.example.data.model.Student

@Database(
    entities = [
        SchoolClass::class,
        Student::class,
        AttendanceRecord::class,
        AttendanceSession::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun schoolClassDao(): SchoolClassDao
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun attendanceSessionDao(): AttendanceSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "absen_siswa_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
