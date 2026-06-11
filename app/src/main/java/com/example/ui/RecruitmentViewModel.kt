package com.example.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.GenerateContentRequest
import com.example.api.GenerateContentResponse
import com.example.api.RetrofitClient
import com.example.api.Part
import com.example.api.Content
import com.example.data.AppDatabase
import com.example.data.RecruitmentRecord
import com.example.data.RecruitmentRepository
import com.example.generator.LayoutGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecruitmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecruitmentRepository
    private val prefs: SharedPreferences = application.getSharedPreferences("recruitment_settings", Context.MODE_PRIVATE)

    // DB list
    val allRecords: StateFlow<List<RecruitmentRecord>>

    init {
        val dao = AppDatabase.getDatabase(application).recruitmentDao()
        repository = RecruitmentRepository(dao)
        allRecords = repository.allRecords.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // --- Core Form States ---
    var companyName by mutableStateOf("")
    var address by mutableStateOf("")
    var jobTitle by mutableStateOf("")
    var gender by mutableStateOf("不限")
    var workerCount by mutableStateOf("")
    var salaryType by mutableStateOf("月薪")
    var salaryMin by mutableStateOf(3000)
    var salaryMax by mutableStateOf(5000)
    var monthlyRestDays by mutableStateOf("4")
    var workingHours by mutableStateOf("早班 08:00 - 18:00")
    
    var education by mutableStateOf("不限")
    var ageMin by mutableStateOf(18)
    var ageMax by mutableStateOf(50)
    var experience by mutableStateOf("不限")
    var jobNature by mutableStateOf("全职")
    var hasNightShift by mutableStateOf("无")
    var overtimeSituation by mutableStateOf("自愿加班")
    var overtimeWage by mutableStateOf("按小时")
    
    var eatWelfare by mutableStateOf("")
    var stayWelfare by mutableStateOf("")
    var socialSecurity by mutableStateOf("")
    
    // Other welfares mapped as a set
    var otherWelfare = mutableStateOf(setOf<String>())
    // Special conditions mapped as a set
    var specialRequirements = mutableStateOf(setOf<String>())

    // --- Generation & Settings Configurations ---
    var styleIndex by mutableStateOf(0)
    var isDarkMode by mutableStateOf(prefs.getBoolean("dark_mode", false))
    var fontSizeMode by mutableStateOf(prefs.getString("font_size", "标准") ?: "标准") // 小 / 标准 / 大 / 特大
    var lineSpacingMode by mutableStateOf(prefs.getString("line_spacing", "标准") ?: "标准") // 紧凑 / 标准 / 宽松
    
    // Core output result
    var generatedText by mutableStateOf("")
    var isGenerating by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var generationMode by mutableStateOf("offline") // "offline" or "ai"

    // Raw manual pasted text for rewriting
    var pastedOldTextForRewrite by mutableStateOf("")

    // Compliance warnings
    var extremeWordsWarn by mutableStateOf(emptyList<String>())
    var salaryIssueWarn by mutableStateOf(false)
    var completenessWarn by mutableStateOf(false)

    // Selected loading record ID (null if new)
    var editingRecordId by mutableStateOf<Int?>(null)

    // Pre-populate form with preset info
    fun applyJobPreset(jobName: String) {
        val preset = LayoutGenerator.JOB_PRESETS.find { it.name == jobName }
        if (preset != null) {
            jobTitle = preset.name
            salaryMin = preset.defaultMinSalary
            salaryMax = preset.defaultMaxSalary
            workingHours = preset.defaultHours
            jobNature = preset.defaultNature
            runComplianceChecks()
        }
    }

    // Toggle Dark Mode
    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
        prefs.edit().putBoolean("dark_mode", isDarkMode).apply()
    }

    // Set Font Size
    fun updateFontSize(size: String) {
        fontSizeMode = size
        prefs.edit().putString("font_size", size).apply()
    }

    // Set Line Spacing
    fun updateLineSpacing(spacing: String) {
        lineSpacingMode = spacing
        prefs.edit().putString("line_spacing", spacing).apply()
    }

    // Run form checking logic
    fun runComplianceChecks() {
        val fullFormText = "$companyName $address $jobTitle $workingHours"
        extremeWordsWarn = LayoutGenerator.getExtremeWordsUsed(fullFormText) + LayoutGenerator.getExtremeWordsUsed(generatedText)
        
        salaryIssueWarn = (salaryMin > 15000 || salaryMax > 20000 || (salaryMin != 0 && salaryMin >= salaryMax))
        
        completenessWarn = (companyName.isEmpty() || address.isEmpty() || jobTitle.isEmpty() || workingHours.isEmpty() || monthlyRestDays.isEmpty())
    }

    // Quick select presets values helper
    val commonHoursList = listOf(
        "早班 08:00 - 18:00",
        "早班 08:30 - 17:30",
        "两班倒 08:00-20:00/20:00-08:00",
        "三班倒 (八小时轮班制)",
        "弹性排班 / 自由接单",
        "夜班 20:00 - 早06:00"
    )

    val commonSalaryRanges = listOf(
        Pair(2000, 3000),
        Pair(3000, 5000),
        Pair(4000, 6000),
        Pair(5500, 8000),
        Pair(6000, 10000),
        Pair(8000, 15000)
    )

    val commonPlaces = listOf(
        "河北省邯郸市磁县迎宾大道路口",
        "河北省邯郸市复兴区建设大街",
        "河北省邯郸市丛台区人民路商圈",
        "河北省邯郸市永年区西大门工业区",
        "河北省邯郸市武安市工业大道街口"
    )

    // Build the record from current form state
    private fun buildRecord(): RecruitmentRecord {
        return RecruitmentRecord(
            id = editingRecordId ?: 0,
            companyName = companyName,
            address = address,
            jobTitle = jobTitle,
            gender = gender,
            workerCount = workerCount,
            salaryType = salaryType,
            salaryMin = salaryMin,
            salaryMax = salaryMax,
            monthlyRestDays = monthlyRestDays,
            workingHours = workingHours,
            education = education,
            ageMin = ageMin,
            ageMax = ageMax,
            experience = experience,
            jobNature = jobNature,
            hasNightShift = hasNightShift,
            overtimeSituation = overtimeSituation,
            overtimeWage = overtimeWage,
            eatWelfare = eatWelfare,
            stayWelfare = stayWelfare,
            socialSecurity = socialSecurity,
            otherWelfare = otherWelfare.value.joinToString(","),
            specialRequirements = specialRequirements.value.joinToString(","),
            generatedText = generatedText,
            styleIndex = styleIndex
        )
    }

    // Load data from historical record
    fun loadFromRecord(record: RecruitmentRecord) {
        editingRecordId = record.id
        companyName = record.companyName
        address = record.address
        jobTitle = record.jobTitle
        gender = record.gender
        workerCount = record.workerCount
        salaryType = record.salaryType
        salaryMin = record.salaryMin
        salaryMax = record.salaryMax
        monthlyRestDays = record.monthlyRestDays
        workingHours = record.workingHours
        education = record.education
        ageMin = record.ageMin
        ageMax = record.ageMax
        experience = record.experience
        jobNature = record.jobNature
        hasNightShift = record.hasNightShift
        overtimeSituation = record.overtimeSituation
        overtimeWage = record.overtimeWage
        eatWelfare = record.eatWelfare
        stayWelfare = record.stayWelfare
        socialSecurity = record.socialSecurity
        otherWelfare.value = record.otherWelfare.split(",").filter { it.isNotEmpty() }.toSet()
        specialRequirements.value = record.specialRequirements.split(",").filter { it.isNotEmpty() }.toSet()
        generatedText = record.generatedText
        styleIndex = record.styleIndex
        runComplianceChecks()
    }

    // Clear all inputs
    fun clearAllInputs() {
        editingRecordId = null
        companyName = ""
        address = ""
        jobTitle = ""
        gender = "不限"
        workerCount = ""
        salaryType = "月薪"
        salaryMin = 3000
        salaryMax = 5000
        monthlyRestDays = "4"
        workingHours = "早班 08:00 - 18:00"
        education = "不限"
        ageMin = 18
        ageMax = 50
        experience = "不限"
        jobNature = "全职"
        hasNightShift = "无"
        overtimeSituation = "自愿加班"
        overtimeWage = "按小时"
        eatWelfare = ""
        stayWelfare = ""
        socialSecurity = ""
        otherWelfare.value = emptySet()
        specialRequirements.value = emptySet()
        generatedText = ""
        pastedOldTextForRewrite = ""
        extremeWordsWarn = emptyList()
        salaryIssueWarn = false
        completenessWarn = false
        errorMessage = ""
    }

    // Reset settings to default
    fun resetAllSettings() {
        isDarkMode = false
        fontSizeMode = "标准"
        lineSpacingMode = "标准"
        prefs.edit().clear().apply()
    }

    // Main text generator trigger
    fun generateFlyer(onSuccess: () -> Unit = {}) {
        errorMessage = ""
        
        if (generationMode == "offline") {
            // Offline rules-based generator
            val record = buildRecord()
            generatedText = LayoutGenerator.generateOfflineText(record)
            runComplianceChecks()
            saveToHistory()
            onSuccess()
        } else {
            // Online Gemini Generator
            val record = buildRecord()
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                errorMessage = "API 密钥未配置，请先在 AI Studio 的 Secrets 面板设置 GEMINI_API_KEY。现已自动切换到本地离线生成！"
                generationMode = "offline"
                generateFlyer(onSuccess)
                return
            }

            isGenerating = true
            val promptText = LayoutGenerator.getPrompt(record)

            viewModelScope.launch {
                val responseText = callGeminiApi(apiKey, promptText)
                if (responseText.startsWith("Error:") || responseText.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        errorMessage = "AI 生成失败 ($responseText)。已为您返回本地离线版文案！"
                        generatedText = LayoutGenerator.generateOfflineText(record)
                        runComplianceChecks()
                        saveToHistory()
                        isGenerating = false
                        onSuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        generatedText = responseText
                        runComplianceChecks()
                        saveToHistory()
                        isGenerating = false
                        onSuccess()
                    }
                }
            }
        }
    }

    // Call Gemini API with direct REST
    private suspend fun callGeminiApi(apiKey: String, prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = com.example.api.GenerationConfig(temperature = 0.6f)
            )
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No text generated"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    // Invoke rewrite/micro-tune functionalities using Gemini or fallback
    fun executeRewriteOption(optionId: Int, optionName: String, onSuccess: () -> Unit = {}) {
        errorMessage = ""
        
        // Formulate target instructions based on the selected micro-tune function
        val rewriteGoal = when (optionId) {
            1 -> "旧文案优化润色：根据贴入的这篇老旧招工信息，重新梳理它的逻辑脉络，整理成分层排布，去粗取精，剔除营销废话大白话与欺诈敏感违规词，使之焕然一新。\n原有老旧文案内容为:【$pastedOldTextForRewrite】"
            2 -> "文案极致压缩：将招聘文案压缩到最短程度，字数严厉限至最少，仅仅保留：岗位、薪资、最吸引人的1个福利和地址，把所有多余废话全部砍掉！"
            3 -> "文案详细扩写：在保证数据真实的前提下，给该岗位加入温馨真实的工作流程、日常相处氛围、以及宽厚包容的管理人性化细节，让人感觉真实专业、好相处、值得来！"
            4 -> "去营销化严肃版：删去所有浮夸营销修饰，采用一针见血、平铺直叙、冷静克制的机关单位式叙事，直接呈现工作岗位、内容详情和真实福利待遇，安全守信。"
            5 -> "同城引流优化版：针对同城流量加持。在文案开头多处加入“邯郸本地开工”、“磁县附近做事”、“家门口干活、不用再去跑外省受气”等字眼，极大激发同城熟人社会引流。"
            6 -> "突出‘长白班不加班’：对白班和休息时间安排进行特写，通篇强调‘没有夜班、晚上安溪、下班时间绝对规整、坚决绝不强制加班加班极少’。"
            7 -> "突出‘包吃住 welfare’：对食宿进行立体渲染，把免费三餐的工作餐品质（菜色好、管饱常换）、舒适集体宿舍热水空调、夫妻单人宿舍、独立卫浴全套配置作为核心王牌介绍。"
            8 -> "突出‘工资按时发’：消除工人最大顾虑。在文案前、中、后面多次承诺‘绝不拖欠压工资’、‘每月准时直接打卡’、‘人走账清，随时结算’！"
            9 -> "突出‘工作轻松’：特别描摹工作动作‘可以坐班、没有重搬重物体力操作、车间冬暖夏凉空调满额运行、节奏温和不催产，好上手易学习’。"
            10 -> "突出‘公平公开晋升空间’：给有志青年写，描述哪怕没经验只要干得好也有提拔机制，有机会一年内晋升为组长、班长、车间助理，提高收入。"
            11 -> "改写为‘暑假工版本’：修改表达口吻，欢迎年轻在校生和高考后的暑期临时工。注明‘可以干到九月份开学’、‘无套路人走账清、支持开具实习实践公章说明’。"
            12 -> "改写为‘寒假工版本’：适合年底春节前后。专门描述假期安排‘过年可弹性安排放假或享受新年留守双倍留守礼包、车间暖气足，包吃住，方便回校’。"
            13 -> "改写为‘短期工版本’：去掉一切需要转正或长期绑定的辞令。明确打出‘可以干1个月、干完结账’、‘工期自由、适合快速搬家或需要临时过渡赚生活费的兄弟’。"
            14 -> "改写为‘兼职版本’：改成更慵懒弹性的排餐方式，突出‘一天只干4小时’或‘周末双休’、‘副业自选，闲置时间换高薪’。"
            15 -> "改写为‘残疾人友好岗位’：通篇饱含大爱人情味，强调可以招收具备一定动手能力的肢体/听力障碍等轻度残疾人。突出‘轻体力工作、大家温和包容、提供必要的合理关爱无差别对待’。"
            else -> ""
        }

        if (generationMode == "offline") {
            // Offline fallback rewrite
            // Apply lightweight procedural rules to simulate changes in local texts
            val textToRewrite = if (optionId == 1 && pastedOldTextForRewrite.isNotEmpty()) pastedOldTextForRewrite else generatedText
            
            var modified = textToRewrite
            when (optionId) {
                1 -> modified = "【润色优选版】\n" + LayoutGenerator.generateOfflineText(buildRecord())
                2 -> modified = modified.lines().filter { it.contains("岗位") || it.contains("地址") || it.contains("薪资") || it.contains("福利") }.joinToString("\n")
                3 -> modified = modified.replace("岗位：", "日常工作：在整洁明亮的环境中，负责简单的产品整理和品质维护。岗位：")
                4 -> modified = "【去营销化・合规严肃版】\n" + modified.replace("急招", "招聘").replace("火速", "办理").replace("老乡", "求职者")
                5 -> modified = "【磁县/邯郸老乡首选，就在家门口！】\n" + modified
                6 -> modified = "★ 核心特色：特聘长白班！拒绝强制加班！按时打卡下班！\n" + modified
                7 -> modified = "★ 吃住优越：包一日三餐热乎饭菜！宿舍精配格力冷暖空调 + 史密斯恒温热水器！\n" + modified
                8 -> modified = "★ 信誉保障：每月足额打卡发薪！绝不无故拖扣一分钱！走人时干完当场结清！\n" + modified
                9 -> modified = "★ 工作轻松：大部分岗位配备舒适坐椅，动作简单不用干重力物，上手只消十分钟！\n" + modified
                10 -> modified = "★ 广阔空间：技术过硬可提班组长！本单位透明公开多干多得晋升！\n" + modified
                11 -> modified = "【温馨欢迎优秀学生暑假工加盟・可写实践证明・可干至开学】\n" + modified
                12 -> modified = "【温馨欢迎学生寒假工加盟・包吃住提供暖气・过年不返乡享丰厚津贴】\n" + modified
                13 -> modified = "【短期干活・工期随选灵活・离职立清全部余额】\n" + modified
                14 -> modified = "【灵活副业・每天弹性干活排班・不影响主业生活】\n" + modified
                15 -> modified = "【暖心福利：对轻度肢残/听残老乡非常包容无差别待遇，特供舒心低压位】\n" + modified
            }
            generatedText = modified
            runComplianceChecks()
            saveToHistory()
            onSuccess()
        } else {
            // Online Gemini Rewrite / Fine-tuning
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                errorMessage = "API 密钥未配置，请先在 AI Studio 的 Secrets 面板设置 GEMINI_API_KEY。现已切换到本地离线优化！"
                generationMode = "offline"
                executeRewriteOption(optionId, optionName, onSuccess)
                return
            }

            isGenerating = true
            val baseRecord = buildRecord()
            
            // If option is 1, paste old text is the primary context
            val customPrompt = LayoutGenerator.getPrompt(baseRecord, rewriteGoal)

            viewModelScope.launch {
                val responseText = callGeminiApi(apiKey, customPrompt)
                if (responseText.startsWith("Error:") || responseText.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        errorMessage = "AI 改写调用失败 ($responseText)。系统利用本地规则为您完成改写！"
                        isGenerating = false
                        // Falls back
                        generationMode = "offline"
                        executeRewriteOption(optionId, optionName, onSuccess)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        generatedText = responseText
                        runComplianceChecks()
                        saveToHistory()
                        isGenerating = false
                        onSuccess()
                    }
                }
            }
        }
    }

    // Save current generated flyer to local history
    fun saveToHistory() {
        if (generatedText.trim().isEmpty()) return
        val record = buildRecord()
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(record)
        }
    }

    // Delete historical item
    fun deleteHistoryItem(record: RecruitmentRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteById(record.id)
            if (editingRecordId == record.id) {
                withContext(Dispatchers.Main) {
                    editingRecordId = null
                }
            }
        }
    }

    // Clear whole history
    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
            withContext(Dispatchers.Main) {
                editingRecordId = null
            }
        }
    }
}
