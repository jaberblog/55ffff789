# ساخت APK با GitHub Actions

این پروژه Workflow آماده دارد و پس از Push شدن به GitHub، فایل APK را به‌صورت خودکار می‌سازد.

## یک‌بار راه‌اندازی

1. وارد GitHub شوید و یک مخزن جدید (Repository) بسازید؛ بهتر است نام آن `NovaPayAndroid` باشد.
2. فایل ZIP پروژه را استخراج کنید.
3. ترمینال را در پوشه `NovaPayAndroid` (همان پوشه‌ای که `settings.gradle.kts` در آن است) باز کنید.
4. دستورات زیر را با جایگزین کردن آدرس مخزن خود اجرا کنید:

```bash
git init
git add .
git commit -m "Add NovaPay Android project"
git branch -M main
git remote add origin https://github.com/USERNAME/NovaPayAndroid.git
git push -u origin main
```

## دریافت APK به‌صورت ZIP

1. در صفحه مخزن GitHub وارد زبانه **Actions** شوید.
2. اجرای Workflow با نام **Build NovaPay APK** را باز کنید.
3. منتظر بمانید تا وضعیت اجرا **Success** شود.
4. پایین صفحهٔ همان اجرا، در قسمت **Artifacts** روی `NovaPay-debug-apk` کلیک کنید.
5. GitHub یک فایل ZIP دانلود می‌کند. داخل آن `NovaPay.apk` و `SHA256SUMS.txt` قرار دارد.

هر بار که روی شاخهٔ `main` یا `master` Push انجام دهید، نسخهٔ جدید APK ساخته می‌شود. همچنین می‌توانید از Actions گزینهٔ **Run workflow** را برای اجرای دستی انتخاب کنید.

## مشخصات خروجی

- نوع خروجی: Debug APK (برای نصب و تست)
- نام فایل داخل ZIP: `NovaPay.apk`
- حداقل Android: 7.0 / API 24
- این نسخه با کلید Debug خودکار Android امضا می‌شود؛ برای انتشار در Google Play باید Release signing و کلید اختصاصی امن تنظیم شود.
- داده‌ها و عملیات مالی پروژه نمایشی هستند و به بانک یا Backend واقعی متصل نیستند.

## اگر Build شکست خورد

داخل اجرای Workflow، مرحله‌ای که قرمز شده را باز کنید و Log آن را بررسی کنید. فایل‌های اصلی پیکربندی عبارت‌اند از:

- `.github/workflows/build-apk.yml`
- `build.gradle.kts`
- `app/build.gradle.kts`

این Workflow از JDK 17، Gradle 8.10.2 و Android SDK 35 استفاده می‌کند.
