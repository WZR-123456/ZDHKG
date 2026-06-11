package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recruitment_records")
data class RecruitmentRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val companyName: String = "",
    val address: String = "",
    val jobTitle: String = "",
    val gender: String = "不限",
    val workerCount: String = "",
    val salaryType: String = "月薪",
    val salaryMin: Int = 3000,
    val salaryMax: Int = 5000,
    val monthlyRestDays: String = "4",
    val workingHours: String = "早班 08:00 - 18:00",
    
    val education: String = "不限",
    val ageMin: Int = 18,
    val ageMax: Int = 50,
    val experience: String = "不限",
    val jobNature: String = "全职",
    val hasNightShift: String = "无",
    val overtimeSituation: String = "自愿加班",
    val overtimeWage: String = "按小时",
    
    val eatWelfare: String = "",
    val stayWelfare: String = "",
    val socialSecurity: String = "",
    val otherWelfare: String = "", // Comma-separated list of checked benefits
    
    val specialRequirements: String = "", // Comma-separated list of special options
    
    val generatedText: String = "",
    val styleIndex: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
