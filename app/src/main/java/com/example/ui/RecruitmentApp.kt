package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RecruitmentRecord
import com.example.generator.LayoutGenerator
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecruitmentApp(
    viewModel: RecruitmentViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    
    // Bottom tab index state
    var selectedTab by remember { mutableStateOf(0) }
    
    // Showing confirmation dialogs
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // RJ logo box from the Sleek Design HTML
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "RJ",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                        Column {
                            Text(
                                "真才实招 AI",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.testTag("app_title")
                            )
                            Text(
                                "100% 真实合规招工 · 智能改写",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                            )
                        }
                    }
                },
                actions = {
                    // Quick toggles in the top bar
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("top_dark_mode_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "切换夜间模式"
                        )
                    }
                    IconButton(
                        onClick = { showResetSettingsDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "重置设置"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    label = { Text("制作广告") },
                    modifier = Modifier.testTag("tab_edit_document")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("文案预览") },
                    modifier = Modifier.testTag("tab_preview")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Menu, contentDescription = null) },
                    label = { Text("历史纪录") },
                    modifier = Modifier.testTag("tab_history")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("关于说明") },
                    modifier = Modifier.testTag("tab_about")
                )
            }
        }
    ) { innerPadding ->
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> FormBuilderTab(viewModel, onGenerate = {
                    selectedTab = 1 // Go to output screen on click
                })
                1 -> PreviewAndOptimizersTab(viewModel, clipboardManager, context)
                2 -> HistoryArchiveTab(
                    viewModel = viewModel,
                    records = allRecords,
                    onEditAndReload = { record ->
                        viewModel.loadFromRecord(record)
                        selectedTab = 0 // Go to Form builder screen
                    },
                    onClearHistoryRequest = { showClearHistoryDialog = true }
                )
                3 -> AboutGuideTab(context)
            }
        }
    }

    // Reset settings validation dialog
    if (showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSettingsDialog = false },
            title = { Text("是否重置应用偏好设置？") },
            text = { Text("系统将恢复默认的字体大小（中等）、行间距（标准）和亮色主题。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAllSettings()
                        showResetSettingsDialog = false
                        Toast.makeText(context, "设置已恢复默认", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("确定重置", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSettingsDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Clear history warning dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("清空历史记录？") },
            text = { Text("您确定要彻底清空所有的招工文案备份吗？该操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                        Toast.makeText(context, "历史数据已清空", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("确定清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

// ---------------------- TAB 1: FORM BUILDER ----------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormBuilderTab(
    viewModel: RecruitmentViewModel,
    onGenerate: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Preset Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "常用行业岗位快速一键预设:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LayoutGenerator.JOB_PRESETS.forEach { preset ->
                        AssistChip(
                            onClick = { viewModel.applyJobPreset(preset.name) },
                            label = { Text(preset.name, fontSize = 12.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (viewModel.jobTitle == preset.name) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        )
                    }
                }
            }
        }

        // Section 1: Required Basic Info
        Text(
            "一、核心必填项目",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        OutlinedTextField(
            value = viewModel.companyName,
            onValueChange = { viewModel.companyName = it; viewModel.runComplianceChecks() },
            label = { Text("招聘公司 / 门店 / 厂区名称") },
            placeholder = { Text("例如：磁县精工机械厂") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("company_name_input"),
            singleLine = true
        )

        // Address Section with Presets
        Column {
            OutlinedTextField(
                value = viewModel.address,
                onValueChange = { viewModel.address = it; viewModel.runComplianceChecks() },
                label = { Text("工作详细地址 (精确到街道/村口)") },
                placeholder = { Text("例如：河北省邯郸市磁县迎宾大道15号") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("address_input"),
                singleLine = true
            )
            
            // Quick common places select
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                viewModel.commonPlaces.forEach { place ->
                    SuggestionChip(
                        onClick = { viewModel.address = place; viewModel.runComplianceChecks() },
                        label = { Text(place.substringAfter("市").take(12) + "...", fontSize = 11.sp) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = viewModel.jobTitle,
            onValueChange = { viewModel.jobTitle = it; viewModel.runComplianceChecks() },
            label = { Text("招聘岗位名称") },
            placeholder = { Text("网格化岗位，如：缝纫工、保安") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("job_title_input"),
            singleLine = true
        )

        // Gender requirements
        Column {
            Text("招聘性别要求:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("不限", "仅招男工", "仅招女工", "男女均可、比例均衡").forEach { opt ->
                    FilterChip(
                        selected = viewModel.gender == opt,
                        onClick = { viewModel.gender = opt },
                        label = { Text(opt, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = viewModel.workerCount,
                onValueChange = { viewModel.workerCount = it },
                label = { Text("招聘人数 (人)") },
                placeholder = { Text("不填默认若干") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("worker_count_input"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            OutlinedTextField(
                value = viewModel.monthlyRestDays,
                onValueChange = { viewModel.monthlyRestDays = it; viewModel.runComplianceChecks() },
                label = { Text("月休天数 (天)") },
                placeholder = { Text("例如：4") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("rest_days_input"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // Salary Settings
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                "薪酬福利方案设计",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("月薪", "日结", "周结", "纯计件", "底薪+计件/提成").forEach { m ->
                    FilterChip(
                        selected = viewModel.salaryType == m,
                        onClick = { viewModel.salaryType = m },
                        label = { Text(m, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = viewModel.salaryMin.toString(),
                    onValueChange = { viewModel.salaryMin = it.toIntOrNull() ?: 0; viewModel.runComplianceChecks() },
                    label = { Text("最低薪资 (元)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text("至", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = viewModel.salaryMax.toString(),
                    onValueChange = { viewModel.salaryMax = it.toIntOrNull() ?: 0; viewModel.runComplianceChecks() },
                    label = { Text("最高薪资 (元)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            
            // Common Salary Preset Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                viewModel.commonSalaryRanges.forEach { range ->
                    SuggestionChip(
                        onClick = {
                            viewModel.salaryMin = range.first
                            viewModel.salaryMax = range.second
                            viewModel.runComplianceChecks()
                        },
                        label = { Text("${range.first} - ${range.second}", fontSize = 11.sp) }
                    )
                }
            }
        }

        // Shifts & Times
        Column {
            OutlinedTextField(
                value = viewModel.workingHours,
                onValueChange = { viewModel.workingHours = it; viewModel.runComplianceChecks() },
                label = { Text("上下班具体时间") },
                placeholder = { Text("例如：早班 08:00 - 18:00") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("working_hours_input"),
                singleLine = true
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                viewModel.commonHoursList.forEach { timeStr ->
                    SuggestionChip(
                        onClick = { viewModel.workingHours = timeStr; viewModel.runComplianceChecks() },
                        label = { Text(timeStr, fontSize = 11.sp) }
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Section 2: Detailed filtering conditions
        Text(
            "二、详细入职门槛条件",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        // Education
        Column {
            Text("学历背景要求:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("不限", "小学", "初中", "中专/高中", "大专及以上").forEach { edu ->
                    FilterChip(
                        selected = viewModel.education == edu,
                        onClick = { viewModel.education = edu },
                        label = { Text(edu, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Experience
        Column {
            Text("经验条件要求:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("不限", "1年以内", "1-3年", "3年以上").forEach { exp ->
                    FilterChip(
                        selected = viewModel.experience == exp,
                        onClick = { viewModel.experience = exp },
                        label = { Text(exp, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Work Nature
        Column {
            Text("工作性质与夜班排班:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("全职", "兼职", "临时工", "长期工", "学徒工", "实习").forEach { nature ->
                    FilterChip(
                        selected = viewModel.jobNature == nature,
                        onClick = { viewModel.jobNature = nature },
                        label = { Text(nature, fontSize = 12.sp) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("有无夜班安排:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                listOf("无夜班", "偶尔有", "固定夜班", "两班倒夜班").forEach { night ->
                    FilterChip(
                        selected = viewModel.hasNightShift == night,
                        onClick = { viewModel.hasNightShift = night },
                        label = { Text(night, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Overtime logic
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
                .padding(10.dp)
        ) {
            Text("加班与计薪机制:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("不加班", "偶尔加班", "固定加班", "自愿自定加班").forEach { design ->
                    FilterChip(
                        selected = viewModel.overtimeSituation == design,
                        onClick = { viewModel.overtimeSituation = design },
                        label = { Text(design, fontSize = 11.sp) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("加班费标准:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                listOf("无加班费", "按小时计", "按天结", "1.5倍计", "国定双倍/三倍").forEach { wage ->
                    FilterChip(
                        selected = viewModel.overtimeWage == wage,
                        onClick = { viewModel.overtimeWage = wage },
                        label = { Text(wage, fontSize = 10.sp) }
                    )
                }
            }
        }

        // Age limitations
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text("年龄范围选择: (${viewModel.ageMin} - ${viewModel.ageMax} 岁)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            RangeSlider(
                value = viewModel.ageMin.toFloat()..viewModel.ageMax.toFloat(),
                onValueChange = { range ->
                    viewModel.ageMin = range.start.toInt()
                    viewModel.ageMax = range.endInclusive.toInt()
                },
                valueRange = 16f..70f,
                steps = 54
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Section 3: Welfares (Genuine list only)
        Text(
            "三、真实的员工福利选择 (勾选即显示)",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = viewModel.eatWelfare,
                onValueChange = { viewModel.eatWelfare = it },
                label = { Text("餐饮福利 (如:免费工作餐)") },
                placeholder = { Text("不填不显示") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = viewModel.stayWelfare,
                onValueChange = { viewModel.stayWelfare = it },
                label = { Text("住宿待遇 (如:空调间热水器)") },
                placeholder = { Text("不填不显示") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = viewModel.socialSecurity,
            onValueChange = { viewModel.socialSecurity = it },
            label = { Text("社会保险/社保状况") },
            placeholder = { Text("例如：入职即交五险、转正申报或无社保") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Multiple optional check-chips
        Column {
            Text("其他非货币隐形福利福利勾选:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            val otherWelfareOptions = listOf(
                "全勤奖", "工龄奖 / 班龄补贴", "年终奖 / 十三薪", "高温补贴 / 季度防暑福利",
                "带薪年假", "节日福利礼盒", "定期团建大餐", "生日精选礼物", "带薪全面培训",
                "免费年度检查", "透明公正晋升空间", "内部员工特别折扣"
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                otherWelfareOptions.forEach { opt ->
                    val isChecked = viewModel.otherWelfare.value.contains(opt)
                    FilterChip(
                        selected = isChecked,
                        onClick = {
                            val cur = viewModel.otherWelfare.value.toMutableSet()
                            if (isChecked) cur.remove(opt) else cur.add(opt)
                            viewModel.otherWelfare.value = cur
                        },
                        label = { Text(opt, fontSize = 11.sp) }
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Section 4: Special populations
        Text(
            "四、特殊人群友好条件及工作偏好",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        val specRequirementsList = listOf(
            "接受应届生", "接受暑假工/寒假工团队", "接受1-3个月短期过渡工",
            "接受轻度/听障等残疾人就业", "接受宝妈（弹性接送段免夜班）", "全班坐着上班（坐班）",
            "不用出差跑外", "不用熬夜、绝对健康"
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            specRequirementsList.forEach { opt ->
                val isChecked = viewModel.specialRequirements.value.contains(opt)
                FilterChip(
                    selected = isChecked,
                    onClick = {
                        val current = viewModel.specialRequirements.value.toMutableSet()
                        if (isChecked) current.remove(opt) else current.add(opt)
                        viewModel.specialRequirements.value = current
                    },
                    label = { Text(opt, fontSize = 11.sp) }
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Generation Controls Mode
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "五、选择文案排版风格与生产引擎",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                // Style picker dropdown list layout
                Text("文案宣传风格 (共12大经典风格):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                
                var showDropdown by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    OutlinedButton(
                        onClick = { showDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(LayoutGenerator.STYLE_NAMES[viewModel.styleIndex], fontSize = 13.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        LayoutGenerator.STYLE_NAMES.forEachIndexed { idx, name ->
                            DropdownMenuItem(
                                text = { Text("${idx + 1}. $name", fontSize = 13.sp) },
                                onClick = {
                                    viewModel.styleIndex = idx
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("选择生成算法引擎:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedButton(
                        onClick = { viewModel.generationMode = "offline" },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = if (viewModel.generationMode == "offline") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = if (viewModel.generationMode == "offline") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "纯本地离线引擎",
                            fontSize = 12.sp,
                            color = if (viewModel.generationMode == "offline") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    ElevatedButton(
                        onClick = { viewModel.generationMode = "ai" },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = if (viewModel.generationMode == "ai") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (viewModel.generationMode == "ai") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "智能 AI 引擎",
                            fontSize = 12.sp,
                            color = if (viewModel.generationMode == "ai") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (viewModel.generationMode == "ai") {
                    Text(
                        "💡 智能 AI 系统会自动依照您的条件去除非法宣传词，润色生成最高端自然的语境句子！需联网并预填 Gemini API Key。",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                } else {
                    Text(
                        "🔒 离线排版模型 100% 运行在手机内部本地，速度极快，防隐秘追踪泄漏，高保真还原招工大白话！",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Large Generation Button
        Button(
            onClick = {
                viewModel.generateFlyer(onSuccess = onGenerate)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("generate_poster_button"),
            shape = RoundedCornerShape(50)
        ) {
            Icon(Icons.Default.Star, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("立即生成安全合规招工文案", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ---------------------- TAB 2: PREVIEW & AI OPTIMIZERS ----------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreviewAndOptimizersTab(
    viewModel: RecruitmentViewModel,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    context: Context
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Output title / Loader
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "文案制作画布与改写工作台",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("preview_header")
            )
            Spacer(modifier = Modifier.weight(1f))
            if (viewModel.isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                Text(" AI正在深度雕琢中...", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        }

        // Compliance Checklist Box (Instant scanning)
        ComplianceReport(viewModel)

        // The Generated Flyer Text Board (Interactive Preview Cards)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .testTag("generated_text_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header of the flyer board
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val formattedTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                    Text("【实效时间: $formattedTime】", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.weight(1f))
                    // Mode tag
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (viewModel.generationMode == "ai") Color(0xFF00C853) else Color(0xFF0288D1),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (viewModel.generationMode == "ai") "AI 深度改写" else "本地全离线",
                            fontSize = 9.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))

                // Render with selected styles (fonts and spacing)
                val fontSize = when (viewModel.fontSizeMode) {
                    "小" -> 13.sp
                    "大" -> 19.sp
                    "特大" -> 22.sp
                    else -> 16.sp
                }
                val lineSpacing = when (viewModel.lineSpacingMode) {
                    "紧凑" -> 1.2f
                    "宽松" -> 1.7f
                    else -> 1.4f
                }

                SelectionContainer {
                    if (viewModel.generatedText.isEmpty()) {
                        Text(
                            "暂无生成的招工文案。\n请在“制作广告”填写好后点击“智能排版”一键创建！",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp)
                        )
                    } else {
                        Text(
                            text = viewModel.generatedText,
                            fontSize = fontSize,
                            lineHeight = fontSize * lineSpacing,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generated_text_output")
                        )
                    }
                }
            }
        }

        // Cosmetic Controls Box (Pure interactive styling tools)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("文案可视化预览工具面板:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("字号大小:", fontSize = 12.sp, modifier = Modifier.width(60.dp))
                    listOf("小", "标准", "大", "特大").forEach { mode ->
                        FilterChip(
                            selected = viewModel.fontSizeMode == mode,
                            onClick = { viewModel.updateFontSize(mode) },
                            label = { Text(mode, fontSize = 11.sp) },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("行间距离:", fontSize = 12.sp, modifier = Modifier.width(60.dp))
                    listOf("紧凑", "标准", "宽松").forEach { m ->
                        FilterChip(
                            selected = viewModel.lineSpacingMode == m,
                            onClick = { viewModel.updateLineSpacing(m) },
                            label = { Text(m, fontSize = 11.sp) },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        }

        // Action Toolbar (Copy & TXT Document Export)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    if (viewModel.generatedText.isNotEmpty()) {
                        clipboardManager.setText(AnnotatedString(viewModel.generatedText))
                        Toast.makeText(context, "👉 招工文案已成功复制到剪贴板！", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "文案为空，请先制作广告生成", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("copy_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Share, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("一键复制文案")
            }

            Button(
                onClick = {
                    if (viewModel.generatedText.isNotEmpty()) {
                        exportToTextFile(context, viewModel.jobTitle, viewModel.generatedText)
                    } else {
                        Toast.makeText(context, "文案为空，无法导出文件", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1.5f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
            ) {
                Icon(Icons.Default.Share, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("一键群发/保存为TXT文件")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()

        // Section 3: The 15 AI micro-tune functions
        Text(
            "15大 AI 独立智慧微调与重写操控区",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Text(
            "说明：如果觉得初始输出文案需要深度重设侧重点，可直接点击下方智能按钮一键微调，本地和AI模式均可完美无瑕承接：",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Pasted rewrite box (For tool 1)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    RoundedCornerShape(8.dp)
                )
                .padding(10.dp)
        ) {
            Text("🔧 贴旧文案优化 (对应改写功能1):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = viewModel.pastedOldTextForRewrite,
                onValueChange = { viewModel.pastedOldTextForRewrite = it },
                label = { Text("粘贴您在别处看到的杂乱老旧招工文案") },
                placeholder = { Text("例如：招保安4000/月，随便来，不体检，老板好...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .testTag("pasted_text_input"),
                textStyle = TextStyle(fontSize = 12.sp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (viewModel.pastedOldTextForRewrite.trim().isEmpty()) {
                        Toast.makeText(context, "请先在上方贴入老旧的废乱招工信息！", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.executeRewriteOption(1, "旧文案优化润色")
                    }
                },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("一键全网优化洗稿", fontSize = 11.sp)
            }
        }

        // Remaining 14 dynamic micro-fine-tuning buttons
        val rewriteSpecs = listOf(
            Pair(2, "文案极致压缩 ➡️ 纯干货"),
            Pair(3, "文案详细扩写 ➡️ 增补丰富细节"),
            Pair(4, "去营销化严肃版 ➡️ 客观平铺直叙"),
            Pair(5, "同城引流优化版 ➡️ 磁县同城亲民引流"),
            Pair(6, "突出“长白班不加班”"),
            Pair(7, "突出“高配包吃住”福利"),
            Pair(8, "突出“工资足额准时发放”"),
            Pair(9, "突出“工作轻松好上手”"),
            Pair(10, "突出“晋升组长空间大”"),
            Pair(11, "改成暑假工极速过渡版"),
            Pair(12, "改成寒假工过年津贴版"),
            Pair(13, "改成短期工干完即走版"),
            Pair(14, "改成高薪短班兼职版"),
            Pair(15, "改成优秀残疾人群友好版")
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rewriteSpecs.forEach { (optionId, name) ->
                FilledTonalButton(
                    onClick = {
                        viewModel.executeRewriteOption(optionId, name) {
                            Toast.makeText(context, "已微调：$name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("rewrite_btn_$optionId")
                ) {
                    Icon(Icons.Default.Build, null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(name, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ---------------------- COMPLIANCE CHECK CARD ----------------------
@Composable
fun ComplianceReport(viewModel: RecruitmentViewModel) {
    val showWarning = viewModel.extremeWordsWarn.isNotEmpty() || viewModel.salaryIssueWarn || viewModel.completenessWarn

    if (showWarning) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, "警告", tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "招工安全合规性雷达扫描报告",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Extreme words warning
                if (viewModel.extremeWordsWarn.isNotEmpty()) {
                    Text(
                        "⚠️ 法律红线提示：检测到您使用了违背广告法的极限欺诈敏感虚幻词语: ${viewModel.extremeWordsWarn.joinToString("、")}",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "（红线禁令：保底、最赚钱、稳赚、暴富、躺赚、包分配等虚假承诺会引发求职纠纷。AI 引擎生成时将为您强力剔除、转为合法合规词汇。）",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Salary excess check
                if (viewModel.salaryIssueWarn) {
                    Text(
                        "📈 风险度提示：该招聘薪资区间(${viewModel.salaryMin}-${viewModel.salaryMax})可能偏高偏离邯郸磁县本地实际水平，甚至存在起止限倒挂情况。",
                        color = Color(0xFFD84315),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "（温馨警示：为杜绝虚假招牌引祸、避免求职者群访、请如实客观校对工资真实性。）",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Integrity checks
                if (viewModel.completenessWarn) {
                    Text(
                        "📍 完整度警示：招聘的核心数据尚未写全（厂区名称、上下班小时、月休等）。信息不全招聘反馈效果将暴跌60%！推荐回去补齐数据再行一键极佳排版。",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 11.sp
                    )
                }
            }
        }
    } else {
        // Zero issue success notification
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(1.dp, Color(0xFFF2B8B5)),
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "🛡",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "合规性检查通过",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "未发现\"暴富、高薪\"等敏感词汇",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                Text(
                    text = "SAFE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ---------------------- TAB 3: LOCAL HISTORY RECORDS ----------------------
@Composable
fun HistoryArchiveTab(
    viewModel: RecruitmentViewModel,
    records: List<RecruitmentRecord>,
    onEditAndReload: (RecruitmentRecord) -> Unit,
    onClearHistoryRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "全离文本地历史广告记录 (最多保存 50 条)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            
            if (records.isNotEmpty()) {
                TextButton(
                    onClick = onClearHistoryRequest,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("整盘清空", fontSize = 12.sp)
                }
            }
        }

        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "暂无数据",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "暂无任何存储的文案名录",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(records) { record ->
                    HistoryCard(
                        record = record,
                        onEdit = { onEditAndReload(record) },
                        onDelete = { viewModel.deleteHistoryItem(record) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryCard(
    record: RecruitmentRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = record.jobTitle,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "${record.salaryMin}-${record.salaryMax} 元",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(record.timestamp)),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "工作地: ${record.companyName} (${record.address})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = record.generatedText.take(130) + if (record.generatedText.length > 130) "..." else "",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))
                
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Edit, "重新编辑", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("重新加载调配", fontSize = 11.sp)
                }
            }
        }
    }
}

// ---------------------- TAB 4: ABOUT说明 ----------------------
@Composable
fun AboutGuideTab(context: Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "软件说明与招聘安全宝典",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "真实招工文案生成器是秉承「绝对真实、绿色招聘、全离线本地处理」信念研发的基层蓝领岗招募文案协助工具。彻底杜绝任何形式的编造薪资、夸大噱头或拖压欺诈词语，帮助广大门店商户、厂家用最诚实温润的话招到合适人才。",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Text("⚖️ 招聘安全铁律提醒", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)

        val policyPoints = listOf(
            "1. 【严格禁止收费行为】: 根据《劳动法》条款，用人单位招聘劳动者，不得以任何名义挪收、索要或者变相勒索求职者的体检、工服、入职、信息咨询中介、档案保管等任何手续费用。也不得扣押身份证、毕业证件等。",
            "2. 【不扯大网、不做虚言】: 严禁发布不切实际的高薪噱头，例如将4000月薪注水标注至10000+。这不仅会严重违反《广告法》，也会破坏本地诚信名望，导致新入职员工不满而引发突发辞职乃至集体投诉。",
            "3. 【完整透明交代工时】: 上下班时间和月休不能躲闪隐瞒，在宣传时明确标注倒班机制对吸引高质量求职者有关键正向效用，能够让真正能够干、靠谱安定的优秀蓝领人才主动留下来。",
            "4. 【夫妻与宝妈温情福利】: 基层招工的核心留人关键，往往不仅在于基本底薪的高低，还在于是否贴心：例如给年轻夫妇配对包干独立的夫妻间，或者放宽宝妈等弹性妇女工作接送时间。人性化关照不仅招工易，且极为长远安稳稳定。"
        )

        policyPoints.forEach { pt ->
            Text(pt, fontSize = 12.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider()

        Text("📢 纯离线一键导出并群发技术说明", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(
            "当您配置好条件产生招工大作后，建议直接选择「一键导出/保存TXT并群发」功能。系统会自动帮您转为标准的文本文档，并呼叫Android系统级文件共享分享引擎，让您能轻熟地直发至微信老乡群、QQ招募频道、磁县本地公众号论坛，或者将其转给海报设计室一键大字打印，效率暴翻10倍！",
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}

// Helper to write to local directory and share
fun exportToTextFile(context: Context, jobTitle: String, content: String) {
    try {
        val fileName = "招工简章_${jobTitle.ifEmpty { "通用" }}_${System.currentTimeMillis()}.txt"
        val cacheDir = context.cacheDir
        val file = File(cacheDir, fileName)
        file.writeText(content)
        
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, content)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "一键分享并另存为TXT招工文案文件"))
    } catch (e: Exception) {
        Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

// Helper color extension for surfaces
@Composable
fun ColorScheme.surfaceColorAtElevation(elevation: androidx.compose.ui.unit.Dp): Color {
    val alpha = when {
        elevation <= 0.dp -> 0f
        elevation <= 1.dp -> 0.05f
        elevation <= 2.dp -> 0.08f
        elevation <= 3.dp -> 0.11f
        elevation <= 6.dp -> 0.12f
        else -> 0.14f
    }
    return this.primary.copy(alpha = alpha).compositeOver(this.surface)
}

fun Color.compositeOver(background: Color): Color {
    val src = this
    val r = src.red * src.alpha + background.red * (1.1f - src.alpha)
    val g = src.green * src.alpha + background.green * (1.1f - src.alpha)
    val b = src.blue * src.alpha + background.blue * (1.1f - src.alpha)
    return Color(r.coerceIn(0f, 1f), g.coerceIn(0f, 1f), b.coerceIn(0f, 1f), 1f)
}

// Selection container helper if not standard
@Composable
fun SelectionContainer(content: @Composable () -> Unit) {
    androidx.compose.foundation.text.selection.SelectionContainer {
        content()
    }
}

// Simple textstyle class import
typealias TextStyle = androidx.compose.ui.text.TextStyle
