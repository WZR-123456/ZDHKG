package com.example.generator

import com.example.data.RecruitmentRecord

data class JobPreset(
    val name: String,
    val defaultMinSalary: Int,
    val defaultMaxSalary: Int,
    val defaultHours: String,
    val defaultNature: String = "全职"
)

object LayoutGenerator {
    
    val JOB_PRESETS = listOf(
        JobPreset("工厂普工", 3500, 5500, "08:00 - 20:00 (两班倒)"),
        JobPreset("电子厂操作工", 4000, 6000, "08:00 - 18:00 (可坐班)"),
        JobPreset("服装厂缝纫工", 3500, 7000, "08:00 - 19:30 (计件工资)"),
        JobPreset("食品厂工人", 3000, 4800, "07:30 - 17:30 (长白班)"),
        JobPreset("物流仓储分拣员", 4200, 5800, "20:00 - 06:00 (固定夜班)"),
        JobPreset("快递员/快递分拣", 5000, 8500, "07:00 - 19:00 (户外工作)"),
        JobPreset("外卖骑手", 6000, 10000, "弹性排班 / 自由接单"),
        JobPreset("餐饮服务员", 2800, 3800, "09:30 - 21:30 (轮班调休)"),
        JobPreset("后厨帮厨/厨师", 3500, 6500, "09:00 - 21:00 (包吃包住)"),
        JobPreset("超市收银员/理货员", 2500, 3500, "08:00 - 16:00 / 16:00 - 24:00"),
        JobPreset("门店导购/销售员", 3000, 7500, "09:00 - 18:00 (底薪加提成)"),
        JobPreset("保安门卫", 2200, 3200, "08:00 - 20:00 / 20:00 - 08:00"),
        JobPreset("保洁阿姨", 2000, 3000, "08:00 - 17:00 (长白班)"),
        JobPreset("家政保姆", 3800, 6000, "08:00 - 18:00 (单休)"),
        JobPreset("建筑工人/小工", 4500, 8000, "07:00 - 18:00 (计日发薪)"),
        JobPreset("装修学徒/装修工", 3000, 6500, "08:30 - 17:30 (有师傅带)"),
        JobPreset("汽修学徒/汽修工", 2500, 6000, "08:30 - 18:00 (带薪学徒)"),
        JobPreset("美容美发学徒", 2000, 4500, "09:30 - 19:30 (包教包会)"),
        JobPreset("美甲师/化妆师", 3500, 8000, "10:00 - 20:00 (提成极高)"),
        JobPreset("文员/前台/行政", 2600, 3600, "08:30 - 17:30 (空调坐班)"),
        JobPreset("客服专员", 3000, 5000, "09:00 - 18:00 (不打电话)"),
        JobPreset("会计/出纳", 3200, 5200, "08:30 - 17:30 (周末双休)"),
        JobPreset("司机/货车司机", 5500, 9500, "不定时排班 (多劳多得)"),
        JobPreset("叉车司机", 4200, 6500, "08:00 - 18:00 (需特种设备证)"),
        JobPreset("电工/焊工", 4800, 8500, "08:00 - 17:30 (需持证上岗)")
    )
    
    val STYLE_NAMES = listOf(
        "标准正式招工简章",
        "河北本地接地气大白话版",
        "超精简海报短文案",
        "抖音快手短视频口播版",
        "安稳留人走心版",
        "急招专属版",
        "日结临时工专属版",
        "学徒工培训专属版",
        "夫妻工专属版",
        "宝妈专属版",
        "夜班专属版",
        "坐班内勤专属版"
    )

    fun generateOfflineText(record: RecruitmentRecord): String {
        val company = record.companyName.ifEmpty { "【招聘单位】" }
        val job = record.jobTitle.ifEmpty { "【招聘岗位】" }
        val address = record.address.ifEmpty { "【工作详细地址】" }
        val salaryStr = "${record.salaryMin}-${record.salaryMax}元/月"
        val countStr = if (record.workerCount.isEmpty()) "若干" else "${record.workerCount}"
        val restStr = if (record.monthlyRestDays.isEmpty()) "商议" else "${record.monthlyRestDays}"
        
        // Welfare composition
        val welfareList = mutableListOf<String>()
        if (record.eatWelfare.isNotEmpty()) welfareList.add("包吃（${record.eatWelfare}）")
        if (record.stayWelfare.isNotEmpty()) welfareList.add("包住（${record.stayWelfare}）")
        if (record.socialSecurity.isNotEmpty()) welfareList.add("社保（${record.socialSecurity}）")
        if (record.otherWelfare.isNotEmpty()) {
            record.otherWelfare.split(",").filter { it.isNotEmpty() }.forEach {
                welfareList.add(it)
            }
        }
        val welfareFormatted = if (welfareList.isEmpty()) "面议" else welfareList.joinToString("、")

        // Conditions
        val ageStr = "${record.ageMin} - ${record.ageMax}岁"
        
        return when (record.styleIndex) {
            0 -> """
【$company 招聘简章】

一、招聘岗位：$job
二、招聘人数：$countStr
三、任职要求：
  - 年龄范围：$ageStr
  - 学历要求：${record.education}
  - 经验要求：${record.experience}
  - 性别要求：${record.gender}
  - 工作性质：${record.jobNature}
四、福利薪资：
  - 薪资类型：${record.salaryType}
  - 薪资范围：$salaryStr
  - 福利保障：$welfareFormatted
五、工作时间：
  - 工作班次：${record.workingHours}  (有无夜班：${record.hasNightShift})
  - 月休天数：$restStr 天/月
  - 加班情况：${record.overtimeSituation} (加班资费：${record.overtimeWage})
六、工作详址：
  - $address
七、法律声明：
  本招聘信息真实有效，不收取任何服务费用，求职者谨防诈骗！
  
有意者请联系：____________________
            """.trimIndent()
            
            1 -> """
本地招工，真实不用瞅！
咱【$company】招人啦，这回想要招 $countStr 个【$job】。

【干啥活、咋上班】：
平时就在 $address。工作性质是【${record.jobNature}】。
上下班时间：${record.workingHours}。月休能休 $restStr 天。
家里近、不用东奔西走，加班那块是【${record.overtimeSituation}】，有夜班吗：【${record.hasNightShift}】。

【啥人能来】：
年龄 $ageStr，学历【${record.education}】就行，经验【${record.experience}】，男女【${record.gender}】。

【吃住工资咋算】：
每个月实实在在能到手 $salaryStr，按【${record.salaryType}】发钱。
吃住这块安排得挺得当：$welfareFormatted。

老乡们！本单位绝对没套路。
有意者联系：____________________
【真实有效，绝对不收一分钱，防诈骗上心！】
            """.trimIndent()

            2 -> """
【$job 招聘】招 $countStr 人
※ 单位：$company
※ 待遇：$salaryStr（一月休 $restStr 天）
※ 地址：$address
※ 福利：${if (welfareList.isEmpty()) "免中介，福利好" else welfareList.take(3).joinToString(" ")}
有意思的请在微信或电话联系！
本条招工信息由本单位直签发布，绝非中介，不收取求职者一分钱。
            """.trimIndent()

            3 -> """
邯郸磁县本地招工，找工作的瞧过来！
由于业务发展，我们【$company】急招【$job】员工 $countStr 名。
年龄要求在 $ageStr 内，学历要求【${record.education}】，经验【${record.experience}】，男女【${record.gender}】！
上班具体时间：${record.workingHours}
工资标准：实发【$salaryStr】，属于【${record.salaryType}】。
吃住安排：$welfareFormatted
不收取任何服务费，想赚钱的不要错过。
工作地点就在：$address
有意向请电话联系我！
            """.trimIndent()

            4 -> """
踏踏实实安稳活，不折腾，长远有保障！
【$company】招聘【$job】$countStr 人。
我们承诺工资按时发放、管理超级人性化，专门留人。

【我们的优势】：
- 稳定有活：不用天天换厂，干活踏实。
- 薪资不拖欠：$salaryStr（${record.salaryType}），加班算【${record.overtimeWage}】
- 关爱家庭：月休 $restStr 天，作息很体恤，让您家庭工作全顾上。
- 吃住福利：$welfareFormatted
- 要求非常友好：只要人勤快踏实，年龄在 $ageStr 之间，学历【${record.education}】。

地址：$address
有意向求稳的人群可速与我们取得联系。
【法律声明：真实合法，不收任何押金、体检费。】
            """.trimIndent()

            5 -> """
!!! 急招！急招！火速到岗 !!!
【$company】因岗位告急，急聘【$job】。招聘空缺 $countStr 名，当天面试，通过者直接办理入职当天安排上岗！

【要点指南】：
- 薪资区间：$salaryStr (保证不拖欠，【${record.salaryType}】)
- 上班时间：${record.workingHours} (可立即适应)
- 福利保障：$welfareFormatted
- 要求简易：年龄 $ageStr，身体健康，能干的速来！
- 地点：$address

别犹豫了，直接进组。
有意者直接联系：____________________
【承诺本招聘直接面试，全程不收取一分钱！】
            """.trimIndent()

            6 -> """
【日结/临时工直招】
人走账清，坚守诺言，下班当场发钱！
【$company】招收临时【$job】$countStr 名。

- 薪资标准：现结【$salaryStr】，干完下班立刻转账。
- 工作时间：${record.workingHours}，工作是【${record.jobNature}】
- 活计说明：需要年龄在 $ageStr 之间，无需基础经验，工作环境干净
- 吃住待遇：$welfareFormatted
- 详细位置：$address

有意者欢迎立刻电联，名额一满就停。
【郑重承诺：不收取任何保证金及费用，求职者请放心。】
            """.trimIndent()

            7 -> """
学百门不如精一艺！带薪学徒，师傅带！
【$company】高薪招收【$job】学徒 $countStr 精英。

- 学徒底薪：学徒起步期间按【${record.salaryMin}】发，随着熟练度学成后提薪至【$salaryStr】
- 师傅带教：师傅一对一，从最简单基础活手把手教。
- 任职要求：年龄 $ageStr 内，学习欲望强烈，不限学历基础，踏实好学
- 吃住环境：$welfareFormatted
- 工作详址：$address

只要你肯学，后期发展空间巨大，一生常年有活干。
想拿一技傍身的速与大徒经理联络：
【招聘无任何收费，纯粹技术传承，求职者谨防套路！】
            """.trimIndent()

            8 -> """
夫妻同心，同吃同住，两口子挣双份薪水！
【$company】现对外招聘【$job】夫妻搭档，招 $countStr 人。

- 两口子总薪酬在 ${record.salaryMin * 2} - ${record.salaryMax * 2} 元/月！
- 食宿特惠：提供免费夫妻专属宿间（包含空调、热水器、洗衣机），不用分开住，家庭幸福。
- 福利构成：$welfareFormatted
- 条件简单：双人年龄均在 $ageStr 范围内，身体健康
- 上班地址：$address

一家人在一起过日子，挣钱安稳长远。
有意夫妻工可直接前来面试体验：____________________
【良心招聘免押金，谨防欺诈收费。】
            """.trimIndent()

            9 -> """
宝妈看过来！暖心照顾家庭，接送俩不误！
【$company】专门提供【$job】宝妈适岗。

- 心动亮点：【可接送孩子、工作弹性时间】、下班不熬夜
- 薪资报酬：$salaryStr，绝不亏欠。
- 作息安排：${record.workingHours}，月休 $restStr 天，随时顾家
- 优越环境：$welfareFormatted，无负重体力活，工作氛围和睦
- 地址：$address

欢迎想挣点零花钱又不耽误看孩子的本地宝妈加入！
联系：____________________
【真实岗位，不收取任何面试和材料费。】
            """.trimIndent()

            10 -> """
【夜班高薪岗位直聘】
不隐瞒夜班性质，但给足高工资！
【$company】招聘【$job】，特设高额夜班专项补贴。

- 薪资区间：$salaryStr（高薪不注水，特含夜餐宵夜补助）
- 上班时间：${record.workingHours}（有夜班：${record.hasNightShift}）
- 福利制度：包吃【${record.eatWelfare}】、包住【${record.stayWelfare}】
- 宿舍标准：冷暖空调、干衣机、消音宿舍，保证白天能踏实静音休息！
- 条件：年龄 $ageStr 内，能够倒班、精力充沛，男女【${record.gender}】
- 地址：$address

想多挣高提额的勇士欢迎加入。
有意联系：____________________
【法律防骗：招聘完全不收取劳务中介费。】
            """.trimIndent()

            11 -> """
【空调暖冬/清爽空调，干净坐班招聘】
【$company】诚意诚聘【$job】$countStr 名，不累、不用跑户外！

- 优势特色：【纯坐班、非体力活、不用晒太阳】，冬暖夏凉办公室
- 薪资标准：实打实的 $salaryStr
- 上班时间：${record.workingHours}，月休 $restStr 天，按时下班
- 额外福利：$welfareFormatted
- 任职条件：不需风吹日晒。只要做事耐心、年龄 $ageStr，学历【${record.education}】即可。
- 详址：$address

办公室宽大和善，坐席充裕！
有意应聘速联：____________________
【严肃声明：本合规职位绝不收取任何费用。】
            """.trimIndent()

            else -> ""
        }
    }

    // Checking extreme words
    fun getExtremeWordsUsed(text: String): List<String> {
        val badWords = listOf("保底", "最高", "稳赚", "暴富", "躺赚", "日入过万", "零压力", "百分百高薪", "包分配", "包就业", "轻松赚钱")
        return badWords.filter { text.contains(it) }
    }

    // Creating prompt for Gemini based on style & record
    fun getPrompt(record: RecruitmentRecord, rewriteInstruction: String = ""): String {
        val baseData = """
        【企业基本信息】:
        公司/门店/厂区名称: ${record.companyName}
        工作详细地址: ${record.address}
        招聘岗位名称: ${record.jobTitle}
        招聘性别要求: ${record.gender}
        招聘人数: ${record.workerCount}人
        薪资类型: ${record.salaryType}
        薪资区间: ${record.salaryMin} - ${record.salaryMax}元/月
        月休天数: ${record.monthlyRestDays}天
        上下班具体时间: ${record.workingHours}
        学历要求: ${record.education}
        年龄范围: ${record.ageMin} - ${record.ageMax}岁
        经验要求: ${record.experience}
        工作性质: ${record.jobNature}
        有无夜班: ${record.hasNightShift}
        加班情况: ${record.overtimeSituation}
        加班工资: ${record.overtimeWage}
        吃住福利: 包吃(${record.eatWelfare}), 包住(${record.stayWelfare})
        社保待遇: ${record.socialSecurity}
        其他福利: ${record.otherWelfare}
        特殊/暖心要求: ${record.specialRequirements}
        """.trimIndent()

        val stylePrompt = when (record.styleIndex) {
            0 -> "写作风格: 【标准正式招工简章】。按照国家规范格式撰写。分为八大部分：公司简介、招聘岗位、岗位职责、任职要求、薪资待遇、食宿福利、工作时间、报名须知。用词要严谨规范，没有口语内容，分行排版精美合理。"
            1 -> "写作风格: 【河北本地接地气大白话版】。用河北邯郸、磁县等当地普通老百姓听得懂的大白话口语写，无官方修饰。注重回答老乡最关心的“累不累”、“吃住条件”、“能不能存下钱”、“工资发得准不准”，语气温厚朴实真实。"
            2 -> "写作风格: 【超精简海报短文案】。严格限制在50字内。只保留四个核心：地点、岗位、薪资、最吸引人的1个福利。每一句单独成行，超级短促，极度提炼，字体抓人眼球。"
            3 -> "写作风格: 【短视频口播字幕版】。将文案切成极短的10-15个字的短句排版，方便朗读。开头必须是“邯郸找工作的注意了！”或“磁县本地招工！”，要富有口语活力，便于快速念完。"
            4 -> "写作风格: 【安稳留人走心版】。弱化任何拼身体、高强度的描述。主打“稳定常年有活干、不拖欠工资、管理人性化、生活作息有规律、踏实安心”，吸引想长期安稳干下去顾家的老实求职者。"
            5 -> "写作风格: 【急招专属版】。开头必须标注【急招！急招！急招！】，用高涨的心态呼吁：当天直接面试、当天通过当夜上班或立即上岗，突出差人手和高确定性，去掉一切慢节奏介绍。"
            6 -> "写作风格: 【日结临时工专属版】。突出：工期完全自由灵活、下班立结、人走账清。明白说明能当班干几天、一天拿多少、什么工作节奏。绝对不能保留任何转正社保之类的废话。"
            7 -> "写作风格: 【学徒工培训专属版】。突出：零经验白纸可进，老师傅全程手把手负责，学会必定重用涨薪。弱化体力活描述，渲染“学到了技术就是端起了铁饭碗”的成长感，吸引虚心向学的进取员工。"
            8 -> "写作风格: 【夫妻工专属版】。突出：包独立小家双人夫妻间，两口子住一起，空空调房电视齐备，提供两个人的总薪资待遇（约 ${record.salaryMin * 2} 到 ${record.salaryMax * 2}元），同吃同住赚双份！"
            9 -> "写作风格: 【宝妈专属版】。突出：特设弹性作息、包接送孩子有接送段、下午提前下班照顾家，环境干净，无搬重物体力活。充满关爱和体温，拉近同理心。"
            10 -> "写作风格: 【夜班专属版】。做人真诚不隐瞒辛苦，老老实实写明需要上夜班，但强调高额的夜班专门现金补贴、以及寂静消音休息仓等好保障，还有夜班事少、管理松快、白天高度自由的特权。"
            11 -> "写作风格: 【坐班内勤专属版】。突出：办公室工作、环境恒温干干净净、无一切风吹日晒和干体力活，高档干净办公条件、作息十分规范。"
            else -> "写作风格: 【标准招工格式】。"
        }

        val fineTuneText = if (rewriteInstruction.isNotEmpty()) {
            "【改写特定目标】：$rewriteInstruction\n请在符合全局规范下，按照该需求进行精确改写优化，大幅突出这个修改重点。"
        } else {
            ""
        }

        return """
        你是一个拥有丰富招工文案撰写经验的高级资深招聘总监。请为我量身撰写一篇完全真实合规合法、绝不夸张造假、具有人情味、排版整洁、方便复制发布、吸引基层人才的招工文案。

        【必须绝对服从的红线原则】：
        1. 必须完全严格依据下方提供的真实表单数据来写。绝对不能编造任何虚假的薪资、福利。
        2. 全程严厉禁止使用违规和虚假的敏感网络营销词：保底、最高、稳赚、暴富、躺赚、日入过万、零压力、百分百高薪、包分配、包就业、轻松赚钱。一经查出视为失败。
        3. 不能捏造真实的联系方式（手机、微信号）。必须统一在整篇文案的末尾另起一行，空出位置：【有意者请联系：____________________】。
        4. 在文案的整机最底部，必须加上合规预警防骗：【法律预警：本招聘保证真实有效，本单位绝不收取任何服务、体检、中介手续费用，求职者谨防诈骗！】
        
        【招聘表单真实数据】：
        $baseData
        
        $stylePrompt
        
        $fineTuneText
        
        请直接输出生成的中文招工文案（不包含任何解释说明等废话，以便一键复制）：
        """.trimIndent()
    }
}
