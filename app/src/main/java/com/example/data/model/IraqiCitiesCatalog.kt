package com.example.data.model

object IraqiCitiesCatalog {

    val iraqiCities: List<CityLocation> = listOf(
        CityLocation(
            id = "iq_baghdad",
            nameEn = "Baghdad",
            nameAr = "بغداد",
            governorateEn = "Baghdad Governorate",
            governorateAr = "محافظة بغداد",
            latitude = 33.3152,
            longitude = 44.3661,
            elevationMeters = 34,
            isFavorite = true,
            climateNotesEn = "Central Mesopotamian alluvial plain; prone to dry Shamal winds and summer extreme heat.",
            climateNotesAr = "السهل الرسوبي الأوسط؛ يتأثر برياح الشمال الجافة وموجات الحر الشديدة صيفاً."
        ),
        CityLocation(
            id = "iq_basra",
            nameEn = "Basra",
            nameAr = "البصرة",
            governorateEn = "Basra Governorate",
            governorateAr = "محافظة البصرة",
            latitude = 30.5085,
            longitude = 47.7804,
            elevationMeters = 5,
            isFavorite = true,
            climateNotesEn = "Shatt al-Arab & Arabian Gulf influence; experiences extreme summer heat and humid Sharqi winds.",
            climateNotesAr = "تأثير شط العرب والخليج العربي؛ درجات حرارة قياسية صيفاً ورياح شرقية رطبة (الشرجي)."
        ),
        CityLocation(
            id = "iq_nasiriyah",
            nameEn = "Nasiriyah",
            nameAr = "الناصرية",
            governorateEn = "Dhi Qar Governorate",
            governorateAr = "محافظة ذي قار",
            latitude = 31.0439,
            longitude = 46.2573,
            elevationMeters = 7,
            isFavorite = true,
            climateNotesEn = "Euphrates basin & Mesopotamian Marshes corridor; high solar radiation and frequent dust transport.",
            climateNotesAr = "حوض الفرات وممر الأهوار؛ إشعاع شمسي عالٍ ونشاط متكرر لموجات الغبار."
        ),
        CityLocation(
            id = "iq_najaf",
            nameEn = "Najaf",
            nameAr = "النجف الأشرف",
            governorateEn = "Najaf Governorate",
            governorateAr = "محافظة النجف",
            latitude = 31.9959,
            longitude = 44.3146,
            elevationMeters = 60,
            isFavorite = true,
            climateNotesEn = "Western desert transition zone; susceptible to Western plateau dust plumes (Tooz).",
            climateNotesAr = "منطقة انتقالية نحو البادية الغربية؛ تتأثر سريعاً بموجات الغبار القادمة من الهضبة الغربية."
        ),
        CityLocation(
            id = "iq_karbala",
            nameEn = "Karbala",
            nameAr = "كربلاء المقدسة",
            governorateEn = "Karbala Governorate",
            governorateAr = "محافظة كربلاء",
            latitude = 32.6160,
            longitude = 44.0249,
            elevationMeters = 30,
            isFavorite = true,
            climateNotesEn = "Bounded by Razaza Lake and date palm oases; warm arid climate with Shamal breeze.",
            climateNotesAr = "تحيط بها بحيرة الرزازة وبساتين النخيل؛ مناخ جاف حار صيفاً ومعتدل شتاءً."
        ),
        CityLocation(
            id = "iq_mosul",
            nameEn = "Mosul",
            nameAr = "الموصل",
            governorateEn = "Nineveh Governorate",
            governorateAr = "محافظة نينوى",
            latitude = 36.3450,
            longitude = 43.1575,
            elevationMeters = 223,
            isFavorite = true,
            climateNotesEn = "Upper Tigris basin; semi-arid Mediterranean influence with cooler, wetter winters.",
            climateNotesAr = "حوض دجلة الأعلى؛ مناخ شبه جاف يتأثر بمنخفضات البحر المتوسط وأمطار شتوية أوفر."
        ),
        CityLocation(
            id = "iq_erbil",
            nameEn = "Erbil",
            nameAr = "أربيل",
            governorateEn = "Erbil Governorate",
            governorateAr = "محافظة أربيل",
            latitude = 36.1901,
            longitude = 44.0091,
            elevationMeters = 390,
            isFavorite = true,
            climateNotesEn = "Northern foothills; distinct four seasons with winter rainfall and mountain breezes.",
            climateNotesAr = "التلال الشمالية؛ أربعة فصول واضحة مع أمطار شتوية ونسائم جبلية."
        ),
        CityLocation(
            id = "iq_sulaymaniyah",
            nameEn = "Sulaymaniyah",
            nameAr = "السليمانية",
            governorateEn = "Sulaymaniyah Governorate",
            governorateAr = "محافظة السليمانية",
            latitude = 35.5613,
            longitude = 45.4304,
            elevationMeters = 882,
            isFavorite = true,
            climateNotesEn = "Zagros mountain valley; cooler summers, heavy winter precipitation, and downslope Azmar winds.",
            climateNotesAr = "وادي جبال زاغروس؛ صيف أكثر اعتدالاً وأمطار غزيرة شتاءً مع رياح جبلية نشطة."
        ),
        CityLocation(
            id = "iq_kirkuk",
            nameEn = "Kirkuk",
            nameAr = "كركوك",
            governorateEn = "Kirkuk Governorate",
            governorateAr = "محافظة كركوك",
            latitude = 35.4681,
            longitude = 44.3922,
            elevationMeters = 350,
            climateNotesEn = "Transition zone between Mesopotamian plain and northern highlands.",
            climateNotesAr = "منطقة انتقالية بين السهل الرسوبي والمرتفعات الشمالية."
        ),
        CityLocation(
            id = "iq_hillah",
            nameEn = "Hillah (Babylon)",
            nameAr = "الحلة (بابل)",
            governorateEn = "Babil Governorate",
            governorateAr = "محافظة بابل",
            latitude = 32.4637,
            longitude = 44.4196,
            elevationMeters = 34,
            climateNotesEn = "Hilla branch of the Euphrates; agricultural microclimate with higher river humidity.",
            climateNotesAr = "شط الحلة المتفرع من الفرات؛ مناخ زراعي محلي مع رطوبة نهرية نسبية."
        ),
        CityLocation(
            id = "iq_ramadi",
            nameEn = "Ramadi",
            nameAr = "الرمادي",
            governorateEn = "Al Anbar Governorate",
            governorateAr = "محافظة الأنبار",
            latitude = 33.4258,
            longitude = 43.2992,
            elevationMeters = 50,
            climateNotesEn = "Western Euphrates corridor & Habbaniyah Lake; primary gateway for western synoptic systems.",
            climateNotesAr = "ممر الفرات الغربي وبحيرة الحبانية؛ البوابة الأولى للمنخفضات الجوية الغربية."
        ),
        CityLocation(
            id = "iq_amarah",
            nameEn = "Amarah",
            nameAr = "العمارة",
            governorateEn = "Maysan Governorate",
            governorateAr = "محافظة ميسان",
            latitude = 31.8356,
            longitude = 47.1448,
            elevationMeters = 12,
            climateNotesEn = "Lower Tigris & Hawizeh Marshes; intense summer heat indices.",
            climateNotesAr = "حوض دجلة الأسفل وهور الحويزة؛ مؤشرات حرارة مرتفعة جداً في الصيف."
        ),
        CityLocation(
            id = "iq_kut",
            nameEn = "Kut",
            nameAr = "الكوت",
            governorateEn = "Wasit Governorate",
            governorateAr = "محافظة واسط",
            latitude = 32.5128,
            longitude = 45.8182,
            elevationMeters = 19,
            climateNotesEn = "Tigris bend plain; affected by cross-border wind corridors and dry summers.",
            climateNotesAr = "سهل واسط على نهر دجلة؛ يتأثر بنشاط الرياح السطحية وصيف شديد الحرارة."
        ),
        CityLocation(
            id = "iq_diwaniyah",
            nameEn = "Diwaniyah",
            nameAr = "الديوانية",
            governorateEn = "Al-Qadisiyyah Governorate",
            governorateAr = "محافظة القادسية",
            latitude = 31.9929,
            longitude = 44.9255,
            elevationMeters = 20,
            climateNotesEn = "Mid-Euphrates plain; continental desert thermal swings.",
            climateNotesAr = "سهل الفرات الأوسط؛ تباين حراري واضح بين الليل والنهار."
        ),
        CityLocation(
            id = "iq_samawah",
            nameEn = "Samawah",
            nameAr = "السماوة",
            governorateEn = "Muthanna Governorate",
            governorateAr = "محافظة المثنى",
            latitude = 31.3320,
            longitude = 45.2944,
            elevationMeters = 11,
            climateNotesEn = "Southern desert & Sawa Lake basin; frequent dust lifting during Shamal surges.",
            climateNotesAr = "بادية السماوة وحوض بحيرة ساوة؛ تصاعد الغبار أثناء هبوب رياح الشمال."
        ),
        CityLocation(
            id = "iq_duhok",
            nameEn = "Duhok",
            nameAr = "دهوك",
            governorateEn = "Duhok Governorate",
            governorateAr = "محافظة دهوك",
            latitude = 36.8671,
            longitude = 42.9884,
            elevationMeters = 565,
            climateNotesEn = "Northern mountain valley; cold winters with occasional snow in surrounding peaks.",
            climateNotesAr = "وادي جبلي شمالي؛ شتاء بارد مع تساقط الثلوج على القمم المحيطة."
        ),
        CityLocation(
            id = "iq_tikrit",
            nameEn = "Tikrit",
            nameAr = "تكريت",
            governorateEn = "Saladin Governorate",
            governorateAr = "محافظة صلاح الدين",
            latitude = 34.6095,
            longitude = 43.6851,
            elevationMeters = 110,
            climateNotesEn = "Middle Tigris & Makhoul range corridor; prone to Shamal dust channels.",
            climateNotesAr = "حوض دجلة الأوسط وممر مكحول؛ يتأثر بقنوات الغبار الشمالية الغربية."
        ),
        CityLocation(
            id = "iq_baqubah",
            nameEn = "Baqubah",
            nameAr = "بعقوبة",
            governorateEn = "Diyala Governorate",
            governorateAr = "محافظة ديالى",
            latitude = 33.7485,
            longitude = 44.6433,
            elevationMeters = 45,
            climateNotesEn = "Diyala river citrus groves; continental interior temperatures.",
            climateNotesAr = "بساتين نهر ديالى؛ مناخ قاري داخلي."
        )
    )

    val regionalAndGlobalCities: List<CityLocation> = listOf(
        CityLocation(
            id = "ae_dubai",
            nameEn = "Dubai",
            nameAr = "دبي",
            governorateEn = "Dubai",
            governorateAr = "إمارة دبي",
            countryEn = "United Arab Emirates",
            countryAr = "الإمارات",
            latitude = 25.2048,
            longitude = 55.2708,
            elevationMeters = 16,
            timezone = "Asia/Dubai",
            isIraqiCity = false
        ),
        CityLocation(
            id = "kw_kuwait",
            nameEn = "Kuwait City",
            nameAr = "مدينة الكويت",
            governorateEn = "Al Asimah",
            governorateAr = "العاصمة",
            countryEn = "Kuwait",
            countryAr = "الكويت",
            latitude = 29.3759,
            longitude = 47.9774,
            elevationMeters = 15,
            timezone = "Asia/Kuwait",
            isIraqiCity = false
        ),
        CityLocation(
            id = "jo_amman",
            nameEn = "Amman",
            nameAr = "عمّان",
            governorateEn = "Amman",
            governorateAr = "محافظة العاصمة",
            countryEn = "Jordan",
            countryAr = "الأردن",
            latitude = 31.9454,
            longitude = 35.9284,
            elevationMeters = 780,
            timezone = "Asia/Amman",
            isIraqiCity = false
        ),
        CityLocation(
            id = "tr_istanbul",
            nameEn = "Istanbul",
            nameAr = "إسطنبول",
            governorateEn = "Marmara",
            governorateAr = "مرمرة",
            countryEn = "Türkiye",
            countryAr = "تركيا",
            latitude = 41.0082,
            longitude = 28.9784,
            elevationMeters = 40,
            timezone = "Europe/Istanbul",
            isIraqiCity = false
        ),
        CityLocation(
            id = "gb_london",
            nameEn = "London",
            nameAr = "لندن",
            governorateEn = "Greater London",
            governorateAr = "لندن الكبرى",
            countryEn = "United Kingdom",
            countryAr = "المملكة المتحدة",
            latitude = 51.5074,
            longitude = -0.1278,
            elevationMeters = 24,
            timezone = "Europe/London",
            isIraqiCity = false
        )
    )

    val allCatalogCities: List<CityLocation> = iraqiCities + regionalAndGlobalCities

    fun findById(id: String): CityLocation =
        allCatalogCities.firstOrNull { it.id == id } ?: iraqiCities.first()
}
