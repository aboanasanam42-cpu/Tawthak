# توثيق (Tawthak) — Dental Case Documentation & Odontogram App

<div align="center">

![Android 15](https://img.shields.io/badge/Target_OS-Android_15_(API_35)-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin_2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Offline First](https://img.shields.io/badge/Storage-Room_DB_(Offline--First)-47A248?style=for-the-badge&logo=sqlite&logoColor=white)
![Gemini AI](https://img.shields.io/badge/AI_Engine-Google_Gemini_Flash-8E75FF?style=for-the-badge&logo=google&logoColor=white)

**تطبيق احترافي متكامل لتوثيق حالات طب وجراحة الأسنان، إدارة المرضى، مخطط الأسنان التفاعلي (FDI Odontogram)، التحليل الإكلينيكي الذكي بواسطة Gemini AI، وتتبع الصور الإشعاعية ومقارنات قبل/بعد.**

</div>

---

## 📑 جدول المحتويات (Table of Contents)
- [نظرة عامة على المشروع (Overview)](#-نظرة-عامة-على-المشروع-overview)
- [المعمارية البرمجية (Architecture)](#-المعمارية-البرمجية-architecture)
- [الميزات الأساسية (Core Features)](#-الميزات-الأساسية-core-features)
- [الذكاء الاصطناعي والتدقيق الطبي (AI Clinical Validation)](#-الذكاء-الاصطناعي-والتدقيق-الطبي-ai-clinical-validation)
- [هيكل المشروع والملفات (Project Structure)](#-هيكل-المشروع-والملفات-project-structure)
- [إعدادات البيئة والمفاتيح السرية (Environment & Secrets)](#-إعدادات-البيئة-والمفاتيح-السرية-environment--secrets)
- [أتمتة البناء والتوقيع (CI/CD & GitHub Actions)](#-أتمتة-البناء-والتوقيع-cicd--github-actions)
- [أوامر البناء والاختبار (Build & Run Commands)](#-أوامر-البناء-والاختبار-build--run-commands)

---

## 🦷 نظرة عامة على المشروع (Overview)
تطبيق **توثيق (Tawthak)** صُمم خصيصاً لأطباء وأخصائيي طب وجراحة الأسنان لإدارة وتوثيق العيادة بكفاءة عالية على نظام **Android 15**، مع دعم العمل الكامل دون اتصال بالإنترنت (**Offline-First**) عبر قاعدة بيانات **Room** المحلية المدمجة، والربط الذكي مع نموذج **Google Gemini** لتوفير تدقيق أمني واقتراح خطط علاجية متوافقة مع المعايير الدولية.

---

## 🏛 المعمارية البرمجية (Architecture)
يتبع المشروع مبادئ **Clean Architecture** مع نمط **MVVM (Model-View-ViewModel)** لضمان فصل الاهتمامات وقابلية الاختبار والصيانة:

```
┌────────────────────────────────────────────────────────┐
│                   Presentation Layer                   │
│        Jetpack Compose (Material 3) + ViewModels       │
│  (DashboardScreen, OdontogramCanvas, AiConsultDialog)  │
└───────────────────────────┬────────────────────────────┘
                            │ StateFlow / UI Events
┌───────────────────────────▼────────────────────────────┐
│                      Domain Layer                      │
│             Models, UseCases & FDI Helpers             │
│   (Patient, Visit, OdontogramRecord, ToothStatus)      │
└───────────────────────────┬────────────────────────────┘
                            │ Repositories / Interfaces
┌───────────────────────────▼────────────────────────────┐
│                       Data Layer                       │
│    Local Room Database (SQLite) + Gemini AI Service    │
│ (PatientDao, OdontogramDao, GeminiClinicalService)     │
└────────────────────────────────────────────────────────┘
```

---

## ✨ الميزات الأساسية (Core Features)

### 1. مخطط الأسنان التفاعلي (Interactive FDI Odontogram)
- دعم ترميز الاتحاد الدولي لطب الأسنان **FDI 2-Digit World Dental Notation**:
  - **الأسنان الدائمة (Permanent)**: 11-18, 21-28, 31-38, 41-48.
  - **الأسنان اللبنية (Primary / Pediatric)**: 51-55, 61-65, 71-75, 81-85.
- تتبع حالة السن التشخيصية:
  - `SOUND`: سليم
  - `CARIES`: تسوس نشط
  - `RESTORATION`: حشوة ترميمية
  - `ENDO`: علاج عصب / حشوة جذور
  - `CROWN`: تاج أو جسر
  - `MISSING`: سن مفقود / مخلوع
  - `IMPLANT`: زراعة أسنان ودعامة
- تتبع دقيق لأسطح الأسنان الخمسة:
  - **M** (Mesial)، **D** (Distal)، **O** (Occlusal/Incisal)، **B** (Buccal)، **L** (Lingual/Palatal).

### 2. إدارة المرضى والتنبيهات الطبية (Patient Management)
- حفظ بيانات المريض: الاسم الكامل، العمر، الجنس، رقم الهاتف، والتاريخ الطبي.
- تنبيهات طبية فورية بارزة في بطاقة الحالة (مثل أمراض القلب، الضغط، الحساسية).

### 3. سجل الزيارات والمعاملات المالية (Visits & Ledger)
- توثيق الشكوى الرئيسية، التشخيص، وخطة العلاج لكل زيارة.
- حساب تلقائي للتكلفة الإجمالية، المبالغ المدفوعة، والمتبقي (Cost, Paid, Balance).

### 4. التوثيق الإعلامي ومقارنة قبل/بعد (Clinical Media & Compare)
- تسجيل وحفظ الصور السريرية مع تصنيف الأنواع:
  - Intraoral Before & After (صور فموية قبل وبعد العلاج).
  - OPG (بانوراما)، Periapical (أشعة طرفية)، Cephalometric (سيفالومترية).
- عارض مقارنة ديناميكي مع شريط سحب تفاعلي (Interactive Before/After Slider).

### 5. تصدير التقارير الطبية (PDF Clinical Report)
- توليد تقرير فوري لحالة المريض يشمل ملخص الأسنان المشخصة وسجل الزيارات السريرية.

---

## 🤖 الذكاء الاصطناعي والتدقيق الطبي (AI Clinical Validation)
يعتمد التطبيق على محرك `GeminiClinicalService` المبني على موديل **Gemini 2.5 Flash**:

1. **التحليل الإكلينيكي الشامل (Case Analysis)**:
   - تحليل مصفوفة الأسنان المسجلة في الـ Odontogram مقارنة بشكاوى المريض.
   - اقتراح خطة علاجية مرحلية (المرحلة الإسعافية، المرحلة الترميمية، المرحلة التعويضية، مرحلة المتابعة والوقاية).
   - توفير ملخص توعوي لشرح الحالة للمريض باللغة العربية والإنجليزية.
2. **التدقيق الأمني لموانع الاستخدام (Contraindications & Safety Audit)**:
   - فحص آلي للتاريخ الطبي عند التخطيط لأي إجراء:
     - **حساسية البنسلين**: التوصية ببدائل مثل Clindamycin أو Azithromycin وتجنب مشتقات الأموكسيسيلين.
     - **الحساسية للأسبرين ومضادات الالتهاب**: التوصية بالباراسيتامول وتجنب الإيبوبروفين والكيتورولاك.
     - **ارتفاع ضغط الدم / أمراض القلب**: تحديد الجرعة القصوى للمخدر الموضعي مع الإبينفرين (<= 0.04mg) واقتراح Mepivacaine 3% بدون مقبض أوعية.
     - **مرضى السكري والسيولة**: التنبيه بفحوصات INR و HbA1c والاحتياطات الجراحية ومواعيد الجلسات.
3. **حفظ فوري في السجل**:
   - إمكانية حفظ توصيات الذكاء الاصطناعي بنقرة واحدة كزيارة جديدة موثقة في ملف المريض.

---

## 📂 هيكل المشروع والملفات (Project Structure)

```text
app/src/main/java/com/example/
├── data/
│   ├── ai/
│   │   ├── GeminiClinicalService.kt     # محرك الذكاء الاصطناعي والتحليل والتدقيق الطبي
│   │   └── GeminiModels.kt              # فئات الطلب والاستجابة لـ Gemini REST API
│   └── local/
│       ├── DentalDao.kt                 # واجهات استعلامات Room Database (DAOs)
│       ├── DentalDatabase.kt            # تكوين قاعدة البيانات المحلية ومحولات الأنواع
│       ├── DentalEntities.kt            # جداول قاعدة البيانات (Patients, Visits, Odontogram, Media)
│       └── Converters.kt                # TypeConverters للقوائم والتواريخ
├── domain/
│   ├── model/
│   │   └── DentalModels.kt              # كائنات النطاق (Patient, Visit, ToothStatus, FDI Quadrants)
│   └── repository/
│       └── DentalRepository.kt          # واجهة مستودع البيانات
├── ui/
│   ├── DentalViewModel.kt               # إدارة الحالة المركزية وتدفق البيانات (StateFlows)
│   ├── dashboard/
│   │   ├── DashboardScreen.kt           # الشاشة الرئيسية، بطاقة المريض النشط، الإحصائيات
│   │   ├── OdontogramCanvas.kt          # رسم ومخطط الأسنان التفاعلي (FDI Permanent & Primary)
│   │   ├── ToothActionSheet.kt          # نافذة تعديل تشخيص السن والأسطح (M, D, O, B, L)
│   │   └── AiClinicalConsultDialog.kt   # نافذة الاستشارة الطبية الذكية والتدقيق الأمني
│   ├── media/
│   │   └── MediaCompareScreen.kt        # شاشة الوسائط السريرية وشريط مقارنة قبل وبعد
│   ├── patients/
│   │   └── AddPatientDialog.kt          # نافذة إضافة مريض وتوثيق التاريخ الطبي
│   ├── visits/
│   │   └── AddVisitDialog.kt            # نافذة توثيق الإجراءات والبيانات المالية
│   ├── report/
│   │   └── ClinicalReportScreen.kt      # تقرير الحالة والطباعة
│   └── theme/
│       ├── Color.kt                     # ألوان الهوية الطبية وألوان حالات الأسنان الدولية
│       ├── Theme.kt                     # سمة Material 3 المتكيفة مع الوضع الداكن/الفاتح
│       └── Type.kt                      # نمط الخطوط والطباعة
└── MainActivity.kt                      # نقطة الانطلاق الرئيسية ودعم Edge-to-Edge
```

---

## 🔐 إعدادات البيئة والمفاتيح السرية (Environment & Secrets)
يستخدم المشروع **Secrets Gradle Plugin** لضمان عدم تسريب أي مفاتيح في الكود المصدري.

### ملف `.env` المحلي
أنشئ ملف `.env` في المجلد الرئيسي للمشروع وأضف المفاتيح التالية:

```properties
GEMINI_API_KEY=your_gemini_api_key_here
CM_KEYSTORE_PASSWORD=your_keystore_password
CM_KEY_ALIAS=your_key_alias
CM_KEY_PASSWORD=your_key_password
```

يتم قراءة مفتاح Gemini داخل الكود تلقائياً عبر:
```kotlin
val apiKey = BuildConfig.GEMINI_API_KEY
```

---

## 🚀 أتمتة البناء والتوقيع (CI/CD & GitHub Actions)
يحتوي المشروع على خط سير عمل مؤتمت في `.github/workflows/android_ci.yml` يدعم أسرار مستودع GitHub:

### المتغيرات السرية المطلوبة في GitHub Secrets:
1. `GEMINI_API_KEY`: مفتاح واجهة Gemini API.
2. `KEYSTORE_BASE64`: ملف `release.keystore` مشفر بنظام Base64.
3. `CM_KEYSTORE_PASSWORD`: كلمة مرور ملف الـ Keystore.
4. `CM_KEY_ALIAS`: اسم المفتاح (Alias) داخل ملف الـ Keystore.
5. `CM_KEY_PASSWORD`: كلمة مرور المفتاح.

تقوم أتمتة GitHub Actions بالخطوات التالية تلقائياً عند الدفع (`git push`):
- إعداد بيئة Java 17 و Android SDK.
- فك تشفير وتثبيت مفتاح التوقيع.
- حقن المتغيرات في ملف `.env`.
- تشغيل اختبارات الجودة `:app:testDebugUnitTest`.
- بناء حزم **Release APK** الموقعة ورفعها كـ Artifact جاهز للتحميل والتثبيت.

---

## 🛠 أوامر البناء والاختبار (Build & Run Commands)

### التحقق من بناء المشروع (Compile Applet)
```bash
gradle :app:assembleDebug
```

### تشغيل اختبارات الوحدة (Run Unit Tests)
```bash
gradle :app:testDebugUnitTest
```

### بناء حزمة الإصدار الموقعة (Build Signed Release APK)
```bash
gradle :app:assembleRelease
```

---

<div align="center">
<b>تطبيق توثيق (Tawthak) — دقة في التوثيق الإكلينيكي وسرعة في إدارة عيادة الأسنان.</b>
</div>
