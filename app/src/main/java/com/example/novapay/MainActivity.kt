package com.example.novapay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection

private val Background = Color(0xFF171A22)
private val SurfaceDark = Color(0xFF20242E)
private val SurfaceRaised = Color(0xFF282D38)
private val Gold = Color(0xFFFFC21C)
private val GoldSoft = Color(0xFF4A3A12)
private val TextMain = Color(0xFFF4F5F7)
private val TextMuted = Color(0xFF9299A8)
private val Line = Color(0xFF353B48)
private val Green = Color(0xFF39C98A)
private val Red = Color(0xFFFF6574)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NovaPayTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    NovaPayApp()
                }
            }
        }
    }
}

@Composable
private fun NovaPayTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Gold,
            onPrimary = Color(0xFF1A1A1A),
            background = Background,
            surface = SurfaceDark,
            onSurface = TextMain,
            onBackground = TextMain,
            secondary = TextMuted
        ),
        content = content
    )
}

private enum class AppScreen(val title: String, val tab: Int = -1) {
    Welcome("نوواپی"), Login("ورود"), Register("ساخت حساب"), Forgot("بازیابی رمز"),
    Home("خانه", 0), Wallet("کیف پول", 1), Activity("تراکنش‌ها", 2), Profile("پروفایل", 3),
    Transfer("انتقال وجه"), TopUp("افزایش موجودی"), Withdraw("برداشت وجه"), Pay("پرداخت"),
    Scan("اسکن کد"), MyQr("کد پرداخت من"), Cards("کارت‌های من"), CardDetail("جزئیات کارت"),
    Bills("قبوض و خدمات"), BillDetail("پرداخت قبض"), Receipt("رسید تراکنش"),
    Notifications("اعلان‌ها"), Contacts("مخاطبین"), ContactDetail("اطلاعات مخاطب"),
    Settings("تنظیمات"), Security("امنیت حساب"), Support("پشتیبانی"), Chat("گفت‌وگو با پشتیبانی"),
    Statement("گزارش حساب"), TransactionDetail("جزئیات تراکنش"), Success("عملیات موفق")
}

private data class Tx(
    val title: String,
    val subtitle: String,
    val amount: String,
    val positive: Boolean = false,
    val icon: ImageVector = Icons.Filled.Payments
)

private val demoTransactions = listOf(
    Tx("خرید فروشگاهی", "امروز • ۱۴:۳۲", "− ۲۴۵٬۰۰۰", icon = Icons.Filled.ShoppingBag),
    Tx("واریز از سارا", "امروز • ۱۱:۰۸", "+ ۱٬۲۰۰٬۰۰۰", true, Icons.Filled.ArrowDownward),
    Tx("پرداخت رستوران", "دیروز • ۲۰:۱۵", "− ۳۸۰٬۰۰۰", icon = Icons.Filled.Restaurant),
    Tx("شارژ کیف پول", "۱۶ آبان • ۰۹:۴۰", "+ ۵۰۰٬۰۰۰", true, Icons.Filled.AddCard),
    Tx("سرویس حمل‌ونقل", "۱۵ آبان • ۱۸:۲۲", "− ۹۸٬۰۰۰", icon = Icons.Filled.DirectionsCar),
    Tx("قبض اینترنت", "۱۴ آبان • ۱۰:۰۵", "− ۲۱۰٬۰۰۰", icon = Icons.Filled.Wifi)
)

@Composable
private fun NovaPayApp() {
    val backStack: SnapshotStateList<AppScreen> = remember { mutableStateListOf(AppScreen.Welcome) }
    val screen = backStack.last()
    var transferAmount by remember { mutableStateOf("۱٬۲۰۰٬۰۰۰") }
    var lastAction by remember { mutableStateOf("انتقال وجه") }
    var lastAmount by remember { mutableStateOf("۱٬۲۰۰٬۰۰۰ تومان") }

    fun navigate(target: AppScreen) {
        if (backStack.lastOrNull() != target) backStack.add(target)
    }
    fun goBack() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }
    fun finishAction(title: String, amount: String) {
        lastAction = title
        lastAmount = amount
        navigate(AppScreen.Success)
    }

    BackHandler(enabled = backStack.size > 1) { goBack() }

    val showBottom = screen.tab >= 0
    Scaffold(
        containerColor = Background,
        topBar = {
            if (screen !in listOf(AppScreen.Welcome, AppScreen.Login, AppScreen.Register, AppScreen.Forgot, AppScreen.Home)) {
                AppTopBar(title = screen.title, onBack = ::goBack, onAction = {
                    if (screen == AppScreen.Activity) navigate(AppScreen.Statement)
                    else navigate(AppScreen.Notifications)
                })
            }
        },
        bottomBar = {
            if (showBottom) BottomNavigationBar(selected = screen.tab, onSelect = { index ->
                val destination = when (index) {
                    0 -> AppScreen.Home
                    1 -> AppScreen.Wallet
                    2 -> AppScreen.Activity
                    else -> AppScreen.Profile
                }
                backStack.clear()
                backStack.add(AppScreen.Home)
                if (destination != AppScreen.Home) backStack.add(destination)
            })
        }
    ) { padding ->
        Box(
            Modifier.fillMaxSize().background(Background).padding(padding)
        ) {
            when (screen) {
                AppScreen.Welcome -> WelcomeScreen(
                    onLogin = { navigate(AppScreen.Login) },
                    onRegister = { navigate(AppScreen.Register) }
                )
                AppScreen.Login -> LoginScreen(
                    onLogin = { backStack.clear(); backStack.add(AppScreen.Home) },
                    onRegister = { navigate(AppScreen.Register) },
                    onForgot = { navigate(AppScreen.Forgot) }
                )
                AppScreen.Register -> RegisterScreen(onDone = { backStack.clear(); backStack.add(AppScreen.Home) }, onLogin = ::goBack)
                AppScreen.Forgot -> ForgotScreen(onDone = ::goBack)
                AppScreen.Home -> HomeScreen(
                    onOpen = ::navigate,
                    onSeeAll = { navigate(AppScreen.Activity) }
                )
                AppScreen.Wallet -> WalletScreen(onOpen = ::navigate)
                AppScreen.Activity -> ActivityScreen(onOpen = ::navigate)
                AppScreen.Profile -> ProfileScreen(onOpen = ::navigate, onLogout = { backStack.clear(); backStack.add(AppScreen.Welcome) })
                AppScreen.Transfer -> TransferScreen(
                    amount = transferAmount,
                    onAmountChange = { transferAmount = it },
                    onContinue = { finishAction("انتقال وجه", "$transferAmount تومان") },
                    onContacts = { navigate(AppScreen.Contacts) }
                )
                AppScreen.TopUp -> MoneyFormScreen(
                    heading = "افزایش موجودی کیف پول",
                    hint = "مبلغ مورد نظر را وارد کنید",
                    button = "ادامه و پرداخت",
                    icon = Icons.Filled.AddCircle,
                    onDone = { finishAction("افزایش موجودی", "$it تومان") }
                )
                AppScreen.Withdraw -> MoneyFormScreen(
                    heading = "برداشت از کیف پول",
                    hint = "مبلغ برداشت را وارد کنید",
                    button = "ثبت درخواست برداشت",
                    icon = Icons.Filled.ArrowUpward,
                    onDone = { finishAction("برداشت وجه", "$it تومان") }
                )
                AppScreen.Pay -> PayScreen(onOpen = ::navigate, onDone = { finishAction("پرداخت", "$it تومان") })
                AppScreen.Scan -> ScanScreen(onDone = { finishAction("پرداخت با کد", "۲۵۰٬۰۰۰ تومان") })
                AppScreen.MyQr -> MyQrScreen()
                AppScreen.Cards -> CardsScreen(onOpen = ::navigate)
                AppScreen.CardDetail -> CardDetailScreen(onDone = { finishAction("درخواست صدور کارت", "بدون مبلغ") })
                AppScreen.Bills -> BillsScreen(onOpen = ::navigate)
                AppScreen.BillDetail -> BillDetailScreen(onDone = { finishAction("پرداخت قبض", "۳۲۰٬۰۰۰ تومان") })
                AppScreen.Receipt -> ReceiptScreen(action = lastAction, amount = lastAmount, onHome = { backStack.clear(); backStack.add(AppScreen.Home) })
                AppScreen.Notifications -> NotificationsScreen()
                AppScreen.Contacts -> ContactsScreen(onOpen = { navigate(AppScreen.ContactDetail) })
                AppScreen.ContactDetail -> ContactDetailScreen(onTransfer = { navigate(AppScreen.Transfer) })
                AppScreen.Settings -> SettingsScreen(onOpen = ::navigate)
                AppScreen.Security -> SecurityScreen()
                AppScreen.Support -> SupportScreen(onChat = { navigate(AppScreen.Chat) })
                AppScreen.Chat -> ChatScreen()
                AppScreen.Statement -> StatementScreen()
                AppScreen.TransactionDetail -> TransactionDetailScreen(onReceipt = { navigate(AppScreen.Receipt) })
                AppScreen.Success -> SuccessScreen(action = lastAction, amount = lastAmount, onReceipt = { navigate(AppScreen.Receipt) }, onHome = { backStack.clear(); backStack.add(AppScreen.Home) })
            }
        }
    }
}

@Composable
private fun AppTopBar(title: String, onBack: () -> Unit, onAction: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Background).statusBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(42.dp).clip(CircleShape).background(SurfaceDark)) {
            Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = TextMain)
        }
        Spacer(Modifier.width(12.dp))
        Text(title, color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        IconButton(onClick = onAction, modifier = Modifier.size(42.dp).clip(CircleShape).background(SurfaceDark)) {
            Icon(Icons.Filled.MoreVert, contentDescription = "گزینه‌ها", tint = TextMain)
        }
    }
}

@Composable
private fun BottomNavigationBar(selected: Int, onSelect: (Int) -> Unit) {
    val items = listOf(
        Triple("خانه", Icons.Filled.Home, 0),
        Triple("کیف پول", Icons.Filled.AccountBalanceWallet, 1),
        Triple("تراکنش", Icons.Filled.History, 2),
        Triple("پروفایل", Icons.Filled.Person, 3)
    )
    Row(
        Modifier.fillMaxWidth().navigationBarsPadding().background(SurfaceDark).padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (label, icon, index) ->
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).clickable { onSelect(index) }.padding(vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(icon, contentDescription = label, tint = if (selected == index) Gold else TextMuted, modifier = Modifier.size(21.dp))
                Spacer(Modifier.height(3.dp))
                Text(label, fontSize = 10.sp, color = if (selected == index) Gold else TextMuted)
            }
        }
    }
}

@Composable
private fun PageColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
        content = content
    )
}

@Composable
private fun SectionTitle(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
        if (action != null) Text(action, color = Gold, fontSize = 12.sp, modifier = Modifier.clickable { onAction?.invoke() }.padding(4.dp))
    }
}

@Composable
private fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(SurfaceDark).border(1.dp, Line.copy(alpha = .55f), RoundedCornerShape(18.dp)).padding(15.dp),
        content = content
    )
}

@Composable
private fun PrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(13.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color(0xFF171717), disabledContainerColor = Gold.copy(alpha = .35f))
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SecondaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(13.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Line),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMain)
    ) { Text(text, fontSize = 13.sp) }
}

@Composable
private fun NovaLogo(size: Int = 64) {
    Box(Modifier.size(size.dp).clip(RoundedCornerShape((size / 4).dp)).background(Gold), contentAlignment = Alignment.Center) {
        Icon(Icons.Filled.CurrencyExchange, contentDescription = "NovaPay", tint = Color(0xFF1B1B1B), modifier = Modifier.size((size * .55).dp))
    }
}

@Composable
private fun WelcomeScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.height(22.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            NovaLogo(82)
            Spacer(Modifier.height(18.dp))
            Text("NOVAPAY", color = TextMain, fontSize = 25.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Text("مدیریت ساده و امن پول", color = TextMuted, fontSize = 13.sp)
            Spacer(Modifier.height(44.dp))
            Panel(Modifier.fillMaxWidth()) {
                Text("همه چیز در یک کیف پول", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text("انتقال وجه، پرداخت قبض و مدیریت کارت‌ها؛ سریع، شفاف و همیشه در دسترس.", color = TextMuted, fontSize = 13.sp, lineHeight = 22.sp)
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeaturePill(Icons.Filled.Shield, "امن")
                    FeaturePill(Icons.Filled.FlashOn, "سریع")
                    FeaturePill(Icons.Filled.QueryStats, "شفاف")
                }
            }
        }
        Column {
            PrimaryButton("ورود به حساب", onClick = onLogin)
            Spacer(Modifier.height(10.dp))
            SecondaryButton("ساخت حساب جدید", onClick = onRegister)
            Spacer(Modifier.height(12.dp))
            Text("با ادامه، قوانین و حریم خصوصی نوواپی را می‌پذیرید.", color = TextMuted, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun FeaturePill(icon: ImageVector, label: String) {
    Row(Modifier.clip(RoundedCornerShape(30.dp)).background(SurfaceRaised).padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Gold, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(5.dp))
        Text(label, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun LoginScreen(onLogin: () -> Unit, onRegister: () -> Unit, onForgot: () -> Unit) {
    var phone by remember { mutableStateOf("09") }
    var password by remember { mutableStateOf("") }
    PageColumn {
        Spacer(Modifier.height(20.dp))
        NovaLogo(58)
        Spacer(Modifier.height(20.dp))
        Text("خوش برگشتی!", color = TextMain, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text("برای ورود، اطلاعات حساب خود را وارد کنید.", color = TextMuted, fontSize = 13.sp)
        Spacer(Modifier.height(25.dp))
        InputField("شماره موبایل", phone, { phone = it }, leading = Icons.Filled.Phone)
        Spacer(Modifier.height(14.dp))
        InputField("رمز عبور", password, { password = it }, leading = Icons.Filled.Lock, secret = true)
        Text("رمز عبور را فراموش کرده‌اید؟", color = Gold, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start).clickable(onClick = onForgot).padding(vertical = 12.dp))
        Spacer(Modifier.height(12.dp))
        PrimaryButton("ورود امن", enabled = phone.length >= 5 && password.isNotBlank(), onClick = onLogin)
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("حساب کاربری ندارید؟ ", color = TextMuted, fontSize = 12.sp)
            Text("ثبت‌نام", color = Gold, fontSize = 12.sp, modifier = Modifier.clickable(onClick = onRegister))
        }
        Spacer(Modifier.height(25.dp))
        Panel(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Fingerprint, null, tint = Gold, modifier = Modifier.size(25.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("ورود با اثر انگشت", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("پس از فعال‌سازی از ورود سریع استفاده کنید", color = TextMuted, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun RegisterScreen(onDone: () -> Unit, onLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var accepted by remember { mutableStateOf(true) }
    PageColumn {
        Text("حساب نوواپی بسازید", color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("چند قدم تا مدیریت هوشمند پول فاصله دارید.", color = TextMuted, fontSize = 12.sp)
        Spacer(Modifier.height(22.dp))
        InputField("نام و نام خانوادگی", name, { name = it }, leading = Icons.Filled.Person)
        Spacer(Modifier.height(13.dp))
        InputField("شماره موبایل", phone, { phone = it }, leading = Icons.Filled.Phone)
        Spacer(Modifier.height(13.dp))
        InputField("رمز عبور", password, { password = it }, leading = Icons.Filled.Lock, secret = true)
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(accepted, { accepted = it }, colors = CheckboxDefaults.colors(checkedColor = Gold, checkmarkColor = Color.Black))
            Text("قوانین استفاده و حریم خصوصی را می‌پذیرم.", color = TextMuted, fontSize = 11.sp)
        }
        Spacer(Modifier.height(12.dp))
        PrimaryButton("ایجاد حساب", enabled = name.isNotBlank() && phone.length >= 5 && password.length >= 4 && accepted, onClick = onDone)
        Spacer(Modifier.height(16.dp))
        SecondaryButton("قبلاً حساب ساخته‌ام؛ ورود", onClick = onLogin)
    }
}

@Composable
private fun ForgotScreen(onDone: () -> Unit) {
    var phone by remember { mutableStateOf("") }
    PageColumn {
        Icon(Icons.Filled.MarkEmailRead, null, tint = Gold, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text("بازیابی دسترسی", color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("شماره موبایل ثبت‌شده را وارد کنید تا کد تأیید برای شما ارسال شود.", color = TextMuted, fontSize = 13.sp, lineHeight = 22.sp)
        Spacer(Modifier.height(24.dp))
        InputField("شماره موبایل", phone, { phone = it }, leading = Icons.Filled.Phone)
        Spacer(Modifier.height(18.dp))
        PrimaryButton("ارسال کد تأیید", enabled = phone.length >= 5, onClick = onDone)
    }
}

@Composable
private fun HomeScreen(onOpen: (AppScreen) -> Unit, onSeeAll: () -> Unit) {
    PageColumn {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("سلام، خوش آمدید 👋", color = TextMuted, fontSize = 12.sp)
                Text("حساب نوواپی شما", color = TextMain, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { onOpen(AppScreen.Notifications) }, modifier = Modifier.clip(CircleShape).background(SurfaceDark)) {
                Icon(Icons.Filled.NotificationsNone, null, tint = TextMain)
            }
            Spacer(Modifier.width(7.dp))
            NovaLogo(38)
        }
        Spacer(Modifier.height(18.dp))
        BalanceCard(onClick = { onOpen(AppScreen.Wallet) })
        Spacer(Modifier.height(20.dp))
        SectionTitle("دسترسی سریع")
        Spacer(Modifier.height(12.dp))
        QuickActionGrid(onOpen)
        Spacer(Modifier.height(22.dp))
        SectionTitle("فعالیت‌های اخیر", "مشاهده همه", onSeeAll)
        Spacer(Modifier.height(10.dp))
        Panel(Modifier.fillMaxWidth()) {
            demoTransactions.take(4).forEachIndexed { index, tx ->
                TransactionRow(tx) { onOpen(AppScreen.TransactionDetail) }
                if (index != 3) HorizontalDivider(color = Line.copy(alpha = .55f), modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Spacer(Modifier.height(18.dp))
        SectionTitle("نمای کلی هزینه‌ها")
        Spacer(Modifier.height(10.dp))
        SpendingPanel()
        Spacer(Modifier.height(15.dp))
    }
}

@Composable
private fun BalanceCard(onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Gold).clickable(onClick = onClick).padding(20.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("موجودی کل", color = Color(0xFF4D3A08), fontSize = 12.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.Visibility, null, tint = Color(0xFF4D3A08), modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("۲۴٬۸۵۰٬۰۰۰", color = Color(0xFF171717), fontSize = 27.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(6.dp))
            Text("تومان", color = Color(0xFF4D3A08), fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        }
        Spacer(Modifier.height(17.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("شماره کیف پول  •••• ۴۸۲۱", color = Color(0xFF4D3A08), fontSize = 10.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ArrowBack, null, tint = Color(0xFF171717), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun QuickActionGrid(onOpen: (AppScreen) -> Unit) {
    val actions = listOf(
        Triple("انتقال وجه", Icons.Filled.SwapHoriz, AppScreen.Transfer),
        Triple("افزایش موجودی", Icons.Filled.Add, AppScreen.TopUp),
        Triple("پرداخت", Icons.Filled.QrCodeScanner, AppScreen.Pay),
        Triple("برداشت", Icons.Filled.ArrowUpward, AppScreen.Withdraw),
        Triple("کارت‌ها", Icons.Filled.CreditCard, AppScreen.Cards),
        Triple("قبوض", Icons.Filled.ReceiptLong, AppScreen.Bills),
        Triple("کد من", Icons.Filled.QrCodeScanner, AppScreen.MyQr),
        Triple("گزارش", Icons.Filled.BarChart, AppScreen.Statement)
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        actions.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                row.forEach { (label, icon, destination) ->
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(15.dp)).background(SurfaceDark).clickable { onOpen(destination) }.padding(vertical = 13.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(Modifier.size(39.dp).clip(RoundedCornerShape(13.dp)).background(GoldSoft), contentAlignment = Alignment.Center) {
                            Icon(icon, null, tint = Gold, modifier = Modifier.size(21.dp))
                        }
                        Spacer(Modifier.height(7.dp))
                        Text(label, color = TextMuted, fontSize = 10.sp, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun SpendingPanel() {
    Panel(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("هزینه این ماه", color = TextMuted, fontSize = 11.sp)
                Text("۸٬۴۲۰٬۰۰۰ تومان", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Text("۱۲٪ کمتر از ماه قبل", color = Green, fontSize = 10.sp)
        }
        Spacer(Modifier.height(15.dp))
        Canvas(Modifier.fillMaxWidth().height(72.dp)) {
            val bars = listOf(.42f, .62f, .36f, .82f, .55f, .72f, .47f, .9f, .61f, .74f, .5f, .66f)
            val gap = size.width / (bars.size * 2f)
            bars.forEachIndexed { i, h ->
                val x = gap * (i * 2 + 1)
                drawRoundRect(
                    color = if (i == 7) Gold else SurfaceRaised,
                    topLeft = Offset(x, size.height * (1f - h)),
                    size = androidx.compose.ui.geometry.Size(gap, size.height * h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("هفته ۱", "هفته ۲", "هفته ۳", "هفته ۴").forEach { Text(it, color = TextMuted, fontSize = 9.sp) }
        }
    }
}

@Composable
private fun WalletScreen(onOpen: (AppScreen) -> Unit) {
    PageColumn {
        Panel(Modifier.fillMaxWidth()) {
            Text("موجودی قابل استفاده", color = TextMuted, fontSize = 12.sp)
            Spacer(Modifier.height(7.dp))
            Text("۲۴٬۸۵۰٬۰۰۰ تومان", color = TextMain, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(13.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallAction("افزایش", Icons.Filled.Add, Modifier.weight(1f)) { onOpen(AppScreen.TopUp) }
                SmallAction("برداشت", Icons.Filled.ArrowUpward, Modifier.weight(1f)) { onOpen(AppScreen.Withdraw) }
                SmallAction("انتقال", Icons.Filled.SwapHoriz, Modifier.weight(1f)) { onOpen(AppScreen.Transfer) }
            }
        }
        Spacer(Modifier.height(20.dp))
        SectionTitle("حساب‌ها و دارایی‌ها", "مدیریت") { onOpen(AppScreen.Cards) }
        Spacer(Modifier.height(10.dp))
        listOf(
            Triple("کیف پول اصلی", "•••• ۴۸۲۱", "۲۴٬۸۵۰٬۰۰۰ تومان"),
            Triple("حساب پس‌انداز", "•••• ۱۹۰۲", "۸٬۲۰۰٬۰۰۰ تومان"),
            Triple("موجودی هدیه", "•••• ۷۶۰۱", "۴۵۰٬۰۰۰ تومان")
        ).forEachIndexed { index, item ->
            Panel(Modifier.fillMaxWidth().clickable { onOpen(AppScreen.CardDetail) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(if (index == 0) GoldSoft else SurfaceRaised), contentAlignment = Alignment.Center) {
                        Icon(if (index == 0) Icons.Filled.Wallet else Icons.Filled.Savings, null, tint = if (index == 0) Gold else TextMuted)
                    }
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.first, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(item.second, color = TextMuted, fontSize = 10.sp)
                    }
                    Text(item.third, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(9.dp))
        }
        Spacer(Modifier.height(8.dp))
        SectionTitle("کارت‌های بانکی", "مشاهده همه") { onOpen(AppScreen.Cards) }
        Spacer(Modifier.height(10.dp))
        BankCardVisual("NOVA BLACK", "••••  ••••  ••••  ۴۸۲۱", "۰۸/۲۸", Gold)
        Spacer(Modifier.height(12.dp))
        BankCardVisual("NOVA PLATINUM", "••••  ••••  ••••  ۱۹۰۲", "۰۴/۲۹", SurfaceRaised)
    }
}

@Composable
private fun BankCardVisual(name: String, number: String, expiry: String, color: Color) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(19.dp)).background(color).padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(name, color = if (color == Gold) Color.Black else TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.Wifi, null, tint = if (color == Gold) Color.Black else TextMuted)
        }
        Spacer(Modifier.height(24.dp))
        Text(number, color = if (color == Gold) Color.Black else TextMain, fontSize = 16.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(15.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("NOVA PAY", color = if (color == Gold) Color.Black else TextMain, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Text(expiry, color = if (color == Gold) Color.Black else TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SmallAction(text: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(SurfaceRaised).clickable(onClick = onClick).padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Gold, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(5.dp))
        Text(text, color = TextMuted, fontSize = 10.sp)
    }
}

@Composable
private fun ActivityScreen(onOpen: (AppScreen) -> Unit) {
    var selectedFilter by remember { mutableStateOf("همه") }
    var query by remember { mutableStateOf("") }
    PageColumn {
        InputField("جست‌وجوی تراکنش", query, { query = it }, leading = Icons.Filled.Search)
        Spacer(Modifier.height(13.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("همه", "واریز", "برداشت", "پرداخت").forEach { option ->
                Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (selectedFilter == option) Gold else SurfaceDark).clickable { selectedFilter = option }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                    Text(option, color = if (selectedFilter == option) Color.Black else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(17.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("آبان ۱۴۰۵")
            Icon(Icons.Filled.FilterList, null, tint = Gold, modifier = Modifier.clickable { selectedFilter = "همه" })
        }
        Spacer(Modifier.height(8.dp))
        Panel(Modifier.fillMaxWidth()) {
            demoTransactions.filter { tx ->
                val matchesQuery = query.isBlank() || tx.title.contains(query)
                val matchesFilter = when (selectedFilter) {
                    "واریز" -> tx.positive
                    "برداشت", "پرداخت" -> !tx.positive
                    else -> true
                }
                matchesQuery && matchesFilter
            }.forEachIndexed { index, tx ->
                TransactionRow(tx) { onOpen(AppScreen.TransactionDetail) }
                if (index < demoTransactions.lastIndex) HorizontalDivider(color = Line.copy(alpha = .5f), modifier = Modifier.padding(vertical = 9.dp))
            }
        }
        Spacer(Modifier.height(15.dp))
        SecondaryButton("دریافت گزارش کامل", onClick = { onOpen(AppScreen.Statement) })
    }
}

@Composable
private fun TransactionRow(tx: Tx, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(SurfaceRaised), contentAlignment = Alignment.Center) {
            Icon(tx.icon, null, tint = if (tx.positive) Green else Gold, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(tx.title, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(tx.subtitle, color = TextMuted, fontSize = 10.sp)
        }
        Text(tx.amount, color = if (tx.positive) Green else TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProfileScreen(onOpen: (AppScreen) -> Unit, onLogout: () -> Unit) {
    PageColumn {
        Panel(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(58.dp).clip(CircleShape).background(GoldSoft), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, null, tint = Gold, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("کاربر نوواپی", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("۰۹۱۲ ••• •۴۸۲۱", color = TextMuted, fontSize = 11.sp)
                }
                Icon(Icons.Filled.Edit, null, tint = Gold, modifier = Modifier.clickable { onOpen(AppScreen.Settings) })
            }
        }
        Spacer(Modifier.height(18.dp))
        SectionTitle("حساب کاربری")
        Spacer(Modifier.height(8.dp))
        MenuRow(Icons.Filled.Person, "اطلاعات شخصی", "مشاهده و ویرایش") { onOpen(AppScreen.Settings) }
        MenuRow(Icons.Filled.CreditCard, "کارت‌های من", "مدیریت کارت‌ها") { onOpen(AppScreen.Cards) }
        MenuRow(Icons.Filled.Security, "امنیت و حریم خصوصی", "رمز، اثر انگشت و نشست‌ها") { onOpen(AppScreen.Security) }
        MenuRow(Icons.Filled.Notifications, "اعلان‌ها", "پیام‌ها و اطلاع‌رسانی‌ها") { onOpen(AppScreen.Notifications) }
        Spacer(Modifier.height(13.dp))
        SectionTitle("راهنما")
        Spacer(Modifier.height(8.dp))
        MenuRow(Icons.Filled.SupportAgent, "پشتیبانی", "پاسخ به پرسش‌ها") { onOpen(AppScreen.Support) }
        MenuRow(Icons.Filled.Settings, "تنظیمات برنامه", "زبان و ظاهر") { onOpen(AppScreen.Settings) }
        Spacer(Modifier.height(14.dp))
        SecondaryButton("خروج از حساب", onClick = onLogout)
    }
}

@Composable
private fun MenuRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(SurfaceDark).clickable(onClick = onClick).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(SurfaceRaised), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Gold, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextMuted, fontSize = 10.sp)
        }
        Icon(Icons.Filled.ChevronLeft, null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
    Spacer(Modifier.height(7.dp))
}

@Composable
private fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leading: ImageVector? = null,
    secret: Boolean = false,
    singleLine: Boolean = true
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, fontSize = 12.sp) },
        singleLine = singleLine,
        visualTransformation = if (secret && !visible) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        leadingIcon = if (leading != null) ({ Icon(leading, null, tint = TextMuted, modifier = Modifier.size(18.dp)) }) else null,
        trailingIcon = if (secret) ({ IconButton(onClick = { visible = !visible }) { Icon(if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null, tint = TextMuted) } }) else null,
        shape = RoundedCornerShape(13.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Gold,
            unfocusedBorderColor = Line,
            focusedLabelColor = Gold,
            unfocusedLabelColor = TextMuted,
            cursorColor = Gold,
            focusedTextColor = TextMain,
            unfocusedTextColor = TextMain
        )
    )
}

@Composable
private fun TransferScreen(amount: String, onAmountChange: (String) -> Unit, onContinue: () -> Unit, onContacts: () -> Unit) {
    var recipient by remember { mutableStateOf("۰۹۱۲ ۳۴۵ ۶۷۸۹") }
    var note by remember { mutableStateOf("") }
    PageColumn {
        Panel(Modifier.fillMaxWidth()) {
            Text("انتقال از", color = TextMuted, fontSize = 11.sp)
            Spacer(Modifier.height(7.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(GoldSoft), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Wallet, null, tint = Gold) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("کیف پول اصلی", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("موجودی: ۲۴٬۸۵۰٬۰۰۰ تومان", color = TextMuted, fontSize = 10.sp)
                }
                Icon(Icons.Filled.ExpandMore, null, tint = TextMuted)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("گیرنده")
            Text("انتخاب از مخاطبین", color = Gold, fontSize = 11.sp, modifier = Modifier.clickable(onClick = onContacts).padding(5.dp))
        }
        Spacer(Modifier.height(7.dp))
        InputField("شماره موبایل یا شناسه کاربر", recipient, { recipient = it }, leading = Icons.Filled.PersonSearch)
        Spacer(Modifier.height(15.dp))
        SectionTitle("مبلغ انتقال")
        Spacer(Modifier.height(7.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("مبلغ به تومان", fontSize = 12.sp) },
            trailingIcon = { Text("تومان", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(end = 10.dp)) },
            shape = RoundedCornerShape(13.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Line, focusedLabelColor = Gold, unfocusedLabelColor = TextMuted, cursorColor = Gold, focusedTextColor = TextMain, unfocusedTextColor = TextMain)
        )
        Spacer(Modifier.height(14.dp))
        InputField("توضیحات (اختیاری)", note, { note = it }, leading = Icons.Filled.Notes)
        Spacer(Modifier.height(17.dp))
        Panel(Modifier.fillMaxWidth()) {
            InfoLine("کارمزد", "رایگان")
            InfoLine("زمان انجام", "آنی")
            InfoLine("محدودیت روزانه", "۵۰٬۰۰۰٬۰۰۰ تومان")
        }
        Spacer(Modifier.height(20.dp))
        PrimaryButton("بررسی و ادامه", enabled = recipient.isNotBlank() && amount.isNotBlank(), onClick = onContinue)
    }
}

@Composable
private fun MoneyFormScreen(heading: String, hint: String, button: String, icon: ImageVector, onDone: (String) -> Unit) {
    var amount by remember { mutableStateOf("۵۰۰٬۰۰۰") }
    var method by remember { mutableStateOf("کارت بانکی") }
    PageColumn {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(GoldSoft).padding(18.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, null, tint = Gold, modifier = Modifier.size(35.dp))
                Spacer(Modifier.height(9.dp))
                Text(heading, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(hint, color = TextMuted, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(20.dp))
        InputField("مبلغ به تومان", amount, { amount = it }, leading = Icons.Filled.Payments)
        Spacer(Modifier.height(18.dp))
        SectionTitle("روش انجام")
        Spacer(Modifier.height(8.dp))
        listOf("کارت بانکی", "انتقال از حساب دیگر", "اعتبار هدیه").forEach { option ->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(SurfaceDark).clickable { method = option }.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (option == "کارت بانکی") Icons.Filled.CreditCard else Icons.Filled.AccountBalance, null, tint = Gold)
                Spacer(Modifier.width(10.dp))
                Text(option, color = TextMain, fontSize = 12.sp, modifier = Modifier.weight(1f))
                RadioButton(method == option, { method = option }, colors = RadioButtonDefaults.colors(selectedColor = Gold, unselectedColor = TextMuted))
            }
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height(12.dp))
        PrimaryButton(button, enabled = amount.isNotBlank(), onClick = { onDone(amount) })
    }
}

@Composable
private fun PayScreen(onOpen: (AppScreen) -> Unit, onDone: (String) -> Unit) {
    var amount by remember { mutableStateOf("۲۵۰٬۰۰۰") }
    var merchant by remember { mutableStateOf("") }
    PageColumn {
        Panel(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.QrCodeScanner, null, tint = Gold, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("پرداخت سریع", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("با اسکن کد پذیرنده یا وارد کردن اطلاعات", color = TextMuted, fontSize = 10.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            PrimaryButton("اسکن کد QR", onClick = { onOpen(AppScreen.Scan) })
        }
        Spacer(Modifier.height(17.dp))
        SectionTitle("یا ورود دستی اطلاعات")
        Spacer(Modifier.height(9.dp))
        InputField("نام پذیرنده یا شناسه پرداخت", merchant, { merchant = it }, leading = Icons.Filled.Store)
        Spacer(Modifier.height(12.dp))
        InputField("مبلغ (تومان)", amount, { amount = it }, leading = Icons.Filled.Payments)
        Spacer(Modifier.height(19.dp))
        PrimaryButton("پرداخت", enabled = merchant.isNotBlank() && amount.isNotBlank(), onClick = { onDone(amount) })
        Spacer(Modifier.height(18.dp))
        SectionTitle("پرداخت‌های اخیر")
        Spacer(Modifier.height(8.dp))
        listOf("فروشگاه شهروند", "کافه بلوط", "سوپرمارکت آنلاین").forEachIndexed { i, name ->
            MenuRow(Icons.Filled.Store, name, "پذیرنده ذخیره‌شده •••• ${120 + i}") { merchant = name }
        }
    }
}

@Composable
private fun ScanScreen(onDone: () -> Unit) {
    PageColumn {
        Text("کد QR پذیرنده را داخل کادر قرار دهید.", color = TextMuted, fontSize = 12.sp)
        Spacer(Modifier.height(22.dp))
        Box(Modifier.fillMaxWidth().height(330.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF0D1118)), contentAlignment = Alignment.Center) {
            Box(Modifier.size(230.dp).border(2.dp, Gold, RoundedCornerShape(22.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.QrCodeScanner, null, tint = Gold.copy(alpha = .8f), modifier = Modifier.size(85.dp))
            }
            Text("نمای دوربین", color = TextMuted, fontSize = 10.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
        }
        Spacer(Modifier.height(18.dp))
        SecondaryButton("وارد کردن کد به‌صورت دستی", onClick = onDone)
        Spacer(Modifier.height(12.dp))
        PrimaryButton("استفاده از کد آزمایشی", onClick = onDone)
        Text("دسترسی دوربین در نسخهٔ نمایشی فعال نشده است.", color = TextMuted, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(12.dp))
    }
}

@Composable
private fun MyQrScreen() {
    PageColumn {
        Panel(Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                NovaLogo(45)
                Spacer(Modifier.height(11.dp))
                Text("کد پرداخت شخصی شما", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("برای دریافت وجه، کد زیر را نمایش دهید.", color = TextMuted, fontSize = 10.sp)
                Spacer(Modifier.height(18.dp))
                FakeQr(216)
                Spacer(Modifier.height(15.dp))
                Text("NP-4821-0912", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                Text("شناسه کاربری", color = TextMuted, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        SecondaryButton("کپی شناسه", onClick = {})
        Spacer(Modifier.height(9.dp))
        PrimaryButton("اشتراک‌گذاری کد", onClick = {})
    }
}

@Composable
private fun FakeQr(size: Int) {
    val cells = remember { List(29 * 29) { i -> ((i * 31 + (i / 7) * 11 + (i % 13) * 3) % 9) < 4 } }
    Canvas(Modifier.size(size.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(9.dp)) {
        val cell = size.minDimension / 29f
        cells.forEachIndexed { index, active ->
            if (active) {
                val x = (index % 29) * cell
                val y = (index / 29) * cell
                drawRect(Color(0xFF171A22), Offset(x, y), androidx.compose.ui.geometry.Size(cell * .92f, cell * .92f))
            }
        }
        // Finder-like corner blocks
        val positions = listOf(Offset(0f, 0f), Offset(size.minDimension - cell * 7, 0f), Offset(0f, size.minDimension - cell * 7))
        positions.forEach { p ->
            drawRect(Color(0xFF171A22), p, androidx.compose.ui.geometry.Size(cell * 7, cell * 7))
            drawRect(Color.White, p + Offset(cell, cell), androidx.compose.ui.geometry.Size(cell * 5, cell * 5))
            drawRect(Color(0xFF171A22), p + Offset(cell * 2, cell * 2), androidx.compose.ui.geometry.Size(cell * 3, cell * 3))
        }
    }
}

@Composable
private fun CardsScreen(onOpen: (AppScreen) -> Unit) {
    PageColumn {
        SectionTitle("کارت‌های فعال", "افزودن کارت") { onOpen(AppScreen.CardDetail) }
        Spacer(Modifier.height(12.dp))
        BankCardVisual("NOVA BLACK", "••••  ••••  ••••  ۴۸۲۱", "۰۸/۲۸", Gold)
        Spacer(Modifier.height(10.dp))
        BankCardVisual("NOVA PLATINUM", "••••  ••••  ••••  ۱۹۰۲", "۰۴/۲۹", SurfaceRaised)
        Spacer(Modifier.height(20.dp))
        SectionTitle("تنظیمات کارت")
        Spacer(Modifier.height(8.dp))
        MenuRow(Icons.Filled.Lock, "مسدودسازی موقت", "غیرفعال‌سازی کارت در صورت نیاز") { onOpen(AppScreen.CardDetail) }
        MenuRow(Icons.Filled.Tune, "مدیریت محدودیت‌ها", "سقف خرید و برداشت") { onOpen(AppScreen.CardDetail) }
        MenuRow(Icons.Filled.ReceiptLong, "تراکنش‌های کارت", "مشاهده پرداخت‌های اخیر") { onOpen(AppScreen.Activity) }
    }
}

@Composable
private fun CardDetailScreen(onDone: () -> Unit) {
    var cardName by remember { mutableStateOf("کارت اصلی") }
    var cardNumber by remember { mutableStateOf("") }
    PageColumn {
        BankCardVisual("NOVA BLACK", "••••  ••••  ••••  ۴۸۲۱", "۰۸/۲۸", Gold)
        Spacer(Modifier.height(18.dp))
        InputField("عنوان کارت", cardName, { cardName = it }, leading = Icons.Filled.CreditCard)
        Spacer(Modifier.height(12.dp))
        InputField("شماره کارت", cardNumber, { cardNumber = it }, leading = Icons.Filled.Numbers)
        Spacer(Modifier.height(18.dp))
        Panel(Modifier.fillMaxWidth()) {
            InfoLine("وضعیت کارت", "فعال")
            InfoLine("سقف خرید روزانه", "۲۰٬۰۰۰٬۰۰۰ تومان")
            InfoLine("برداشت روزانه", "۵٬۰۰۰٬۰۰۰ تومان")
        }
        Spacer(Modifier.height(19.dp))
        PrimaryButton("ثبت تغییرات / درخواست کارت", onClick = onDone)
    }
}

@Composable
private fun BillsScreen(onOpen: (AppScreen) -> Unit) {
    PageColumn {
        Panel(Modifier.fillMaxWidth()) {
            Text("پرداخت قبوض", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("قبض‌های خدماتی خود را یکجا مدیریت کنید.", color = TextMuted, fontSize = 11.sp)
            Spacer(Modifier.height(14.dp))
            PrimaryButton("پرداخت قبض جدید", onClick = { onOpen(AppScreen.BillDetail) })
        }
        Spacer(Modifier.height(18.dp))
        SectionTitle("خدمات پرکاربرد")
        Spacer(Modifier.height(9.dp))
        val services = listOf(
            Triple("برق", Icons.Filled.Bolt, "قبض برق"),
            Triple("آب", Icons.Filled.WaterDrop, "قبض آب"),
            Triple("گاز", Icons.Filled.LocalFireDepartment, "قبض گاز"),
            Triple("تلفن", Icons.Filled.Phone, "تلفن ثابت"),
            Triple("اینترنت", Icons.Filled.Wifi, "اینترنت"),
            Triple("موبایل", Icons.Filled.Smartphone, "تلفن همراه")
        )
        services.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                row.forEach { (name, icon, _) ->
                    Column(Modifier.weight(1f).clip(RoundedCornerShape(15.dp)).background(SurfaceDark).clickable { onOpen(AppScreen.BillDetail) }.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icon, null, tint = Gold, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(7.dp))
                        Text(name, color = TextMain, fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(9.dp))
        }
        Spacer(Modifier.height(10.dp))
        SectionTitle("قبوض اخیر")
        Spacer(Modifier.height(9.dp))
        MenuRow(Icons.Filled.ReceiptLong, "قبض اینترنت", "شناسه •••• ۸۴۱۲ • پرداخت‌شده") { onOpen(AppScreen.BillDetail) }
        MenuRow(Icons.Filled.ReceiptLong, "قبض برق", "شناسه •••• ۲۳۱۰ • پرداخت‌نشده") { onOpen(AppScreen.BillDetail) }
    }
}

@Composable
private fun BillDetailScreen(onDone: () -> Unit) {
    var billId by remember { mutableStateOf("۱۲۳۴۵۶۷۸۹۰") }
    var paymentId by remember { mutableStateOf("۹۸۷۶۵۴۳۲۱") }
    PageColumn {
        Panel(Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.ReceiptLong, null, tint = Gold, modifier = Modifier.size(35.dp))
            Spacer(Modifier.height(8.dp))
            Text("اطلاعات قبض", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("نوع قبض را انتخاب و شناسه‌ها را وارد کنید.", color = TextMuted, fontSize = 10.sp)
        }
        Spacer(Modifier.height(16.dp))
        var type by remember { mutableStateOf("برق") }
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("برق", "آب", "گاز", "تلفن").forEach { item ->
                Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (type == item) Gold else SurfaceDark).clickable { type = item }.padding(vertical = 11.dp), contentAlignment = Alignment.Center) {
                    Text(item, color = if (type == item) Color.Black else TextMuted, fontSize = 11.sp)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        InputField("شناسه قبض", billId, { billId = it }, leading = Icons.Filled.Numbers)
        Spacer(Modifier.height(12.dp))
        InputField("شناسه پرداخت", paymentId, { paymentId = it }, leading = Icons.Filled.Numbers)
        Spacer(Modifier.height(16.dp))
        Panel(Modifier.fillMaxWidth()) {
            InfoLine("مبلغ قبض", "۳۲۰٬۰۰۰ تومان")
            InfoLine("مهلت پرداخت", "۳۰ آبان ۱۴۰۵")
            InfoLine("نوع خدمت", type)
        }
        Spacer(Modifier.height(18.dp))
        PrimaryButton("پرداخت قبض", onClick = onDone)
    }
}

@Composable
private fun NotificationsScreen() {
    PageColumn {
        SectionTitle("امروز")
        Spacer(Modifier.height(9.dp))
        NoticeItem(Icons.Filled.CheckCircle, "پرداخت با موفقیت انجام شد", "تراکنش ۲۴۵٬۰۰۰ تومانی شما ثبت شد.", "۱۴:۳۲", true)
        NoticeItem(Icons.Filled.Security, "ورود جدید به حساب", "ورود از دستگاه اندرویدی شناسایی شد.", "۱۰:۰۸", false)
        Spacer(Modifier.height(15.dp))
        SectionTitle("دیروز")
        Spacer(Modifier.height(9.dp))
        NoticeItem(Icons.Filled.LocalOffer, "پیشنهاد ویژه نوواپی", "از پیشنهادهای این هفته استفاده کنید.", "۱۷:۴۵", false)
        NoticeItem(Icons.Filled.AccountBalanceWallet, "افزایش موجودی", "کیف پول شما ۵۰۰٬۰۰۰ تومان شارژ شد.", "۰۹:۴۰", true)
    }
}

@Composable
private fun NoticeItem(icon: ImageVector, title: String, body: String, time: String, success: Boolean) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(SurfaceDark).padding(13.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(if (success) GoldSoft else SurfaceRaised), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = if (success) Gold else TextMuted)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(body, color = TextMuted, fontSize = 10.sp, lineHeight = 16.sp)
        }
        Text(time, color = TextMuted, fontSize = 9.sp)
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun ContactsScreen(onOpen: () -> Unit) {
    var search by remember { mutableStateOf("") }
    PageColumn {
        InputField("جست‌وجوی نام یا شماره", search, { search = it }, leading = Icons.Filled.Search)
        Spacer(Modifier.height(16.dp))
        SectionTitle("مخاطبین اخیر")
        Spacer(Modifier.height(8.dp))
        val people = listOf("سارا محمدی" to "۰۹۱۲ ۳۴۵ ۶۷۸۹", "علی رضایی" to "۰۹۳۵ ۱۲۳ ۴۵۶۷", "نگار احمدی" to "۰۹۱۹ ۷۷۷ ۱۱۲۲", "امیر حسینی" to "۰۹۱۰ ۲۲۲ ۳۳۴۴", "مریم کریمی" to "۰۹۳۶ ۸۸۸ ۹۹۰۰")
        people.filter { it.first.contains(search) || it.second.contains(search) }.forEachIndexed { index, person ->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(SurfaceDark).clickable(onClick = onOpen).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(listOf(GoldSoft, SurfaceRaised, Color(0xFF263B4A))[index % 3]), contentAlignment = Alignment.Center) {
                    Text(person.first.take(1), color = Gold, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(person.first, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(person.second, color = TextMuted, fontSize = 10.sp)
                }
                Icon(Icons.Filled.Call, null, tint = Gold, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(7.dp))
        }
    }
}

@Composable
private fun ContactDetailScreen(onTransfer: () -> Unit) {
    PageColumn {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(76.dp).clip(CircleShape).background(GoldSoft), contentAlignment = Alignment.Center) { Text("س", color = Gold, fontSize = 32.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(10.dp))
            Text("سارا محمدی", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("۰۹۱۲ ۳۴۵ ۶۷۸۹", color = TextMuted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(22.dp))
        PrimaryButton("انتقال وجه به این مخاطب", onClick = onTransfer)
        Spacer(Modifier.height(12.dp))
        Panel(Modifier.fillMaxWidth()) {
            InfoLine("شناسه نوواپی", "NP-4821")
            InfoLine("وضعیت مخاطب", "تأییدشده")
            InfoLine("آخرین انتقال", "۲ روز پیش")
        }
    }
}

@Composable
private fun SettingsScreen(onOpen: (AppScreen) -> Unit) {
    var darkMode by remember { mutableStateOf(true) }
    var notifications by remember { mutableStateOf(true) }
    PageColumn {
        SectionTitle("تنظیمات عمومی")
        Spacer(Modifier.height(9.dp))
        ToggleRow(Icons.Filled.DarkMode, "حالت تیره", "نمایش با تم تیره", darkMode) { darkMode = it }
        ToggleRow(Icons.Filled.Notifications, "اعلان‌ها", "دریافت پیام‌های حساب", notifications) { notifications = it }
        MenuRow(Icons.Filled.Language, "زبان برنامه", "فارسی") { }
        MenuRow(Icons.Filled.Person, "اطلاعات حساب", "نام، شماره و مشخصات") { onOpen(AppScreen.Profile) }
        Spacer(Modifier.height(14.dp))
        SectionTitle("امنیت")
        Spacer(Modifier.height(8.dp))
        MenuRow(Icons.Filled.Security, "امنیت حساب", "رمز و تأیید هویت") { onOpen(AppScreen.Security) }
        MenuRow(Icons.Filled.Devices, "دستگاه‌های متصل", "مدیریت نشست‌های فعال") { onOpen(AppScreen.Security) }
        Spacer(Modifier.height(14.dp))
        SectionTitle("پشتیبانی")
        Spacer(Modifier.height(8.dp))
        MenuRow(Icons.Filled.Help, "راهنما و پشتیبانی", "پاسخ به سؤالات متداول") { onOpen(AppScreen.Support) }
    }
}

@Composable
private fun ToggleRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(SurfaceDark).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Gold)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextMuted, fontSize = 10.sp)
        }
        Switch(checked, onChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = Gold, uncheckedThumbColor = TextMuted, uncheckedTrackColor = SurfaceRaised))
    }
    Spacer(Modifier.height(7.dp))
}

@Composable
private fun SecurityScreen() {
    var biometric by remember { mutableStateOf(false) }
    var twoFactor by remember { mutableStateOf(true) }
    PageColumn {
        Panel(Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Shield, null, tint = Gold, modifier = Modifier.size(33.dp))
            Spacer(Modifier.height(9.dp))
            Text("حساب شما محافظت می‌شود", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("برای امنیت بیشتر، روش‌های ورود و تأیید را مدیریت کنید.", color = TextMuted, fontSize = 10.sp, lineHeight = 17.sp)
        }
        Spacer(Modifier.height(15.dp))
        ToggleRow(Icons.Filled.Fingerprint, "ورود بیومتریک", "استفاده از اثر انگشت یا چهره", biometric) { biometric = it }
        ToggleRow(Icons.Filled.MarkEmailRead, "تأیید دومرحله‌ای", "ارسال کد در ورودهای جدید", twoFactor) { twoFactor = it }
        MenuRow(Icons.Filled.Password, "تغییر رمز عبور", "آخرین تغییر: ۴۵ روز پیش") { }
        MenuRow(Icons.Filled.Devices, "نشست‌های فعال", "۲ دستگاه متصل") { }
        Spacer(Modifier.height(15.dp))
        PrimaryButton("ذخیره تنظیمات امنیتی", onClick = {})
    }
}

@Composable
private fun SupportScreen(onChat: () -> Unit) {
    PageColumn {
        Panel(Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.SupportAgent, null, tint = Gold, modifier = Modifier.size(38.dp))
            Spacer(Modifier.height(8.dp))
            Text("چطور می‌توانیم کمک کنیم؟", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("پاسخ پرسش خود را پیدا کنید یا با پشتیبانی در ارتباط باشید.", color = TextMuted, fontSize = 11.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(14.dp))
            PrimaryButton("گفت‌وگو با پشتیبانی", onClick = onChat)
        }
        Spacer(Modifier.height(18.dp))
        SectionTitle("سؤالات متداول")
        Spacer(Modifier.height(9.dp))
        listOf("چطور موجودی کیف پول را افزایش دهم؟", "چقدر طول می‌کشد انتقال انجام شود؟", "چطور کارت خود را مسدود کنم؟", "چگونه رسید تراکنش را دریافت کنم؟").forEach { question ->
            var expanded by remember { mutableStateOf(false) }
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(SurfaceDark).clickable { expanded = !expanded }.padding(13.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(question, color = TextMain, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = Gold)
                }
                if (expanded) {
                    Spacer(Modifier.height(8.dp))
                    Text("از بخش کیف پول یا دسترسی سریع، گزینه مربوط را انتخاب کنید و مراحل نمایش داده‌شده را دنبال کنید.", color = TextMuted, fontSize = 10.sp, lineHeight = 16.sp)
                }
            }
            Spacer(Modifier.height(7.dp))
        }
    }
}

@Composable
private fun ChatScreen() {
    var message by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf("سلام! به پشتیبانی نوواپی خوش آمدید.", "چطور می‌توانیم کمکتان کنیم؟") }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 9.dp)) {
        Panel(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(GoldSoft), contentAlignment = Alignment.Center) { Icon(Icons.Filled.SupportAgent, null, tint = Gold) }
                Spacer(Modifier.width(9.dp))
                Column {
                    Text("پشتیبانی نوواپی", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("آنلاین • پاسخ‌گویی آزمایشی", color = Green, fontSize = 9.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Bottom) {
            messages.forEachIndexed { index, item ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (index % 2 == 0) Arrangement.Start else Arrangement.End) {
                    Box(Modifier.widthIn(max = 290.dp).clip(RoundedCornerShape(14.dp)).background(if (index % 2 == 0) SurfaceDark else GoldSoft).padding(12.dp)) {
                        Text(item, color = TextMain, fontSize = 11.sp, lineHeight = 17.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = message, onValueChange = { message = it }, modifier = Modifier.weight(1f), placeholder = { Text("پیام خود را بنویسید", fontSize = 11.sp) }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Line, cursorColor = Gold, focusedTextColor = TextMain, unfocusedTextColor = TextMain))
            Spacer(Modifier.width(7.dp))
            IconButton(onClick = { if (message.isNotBlank()) { messages.add(message); messages.add("پیام شما ثبت شد. کارشناس پشتیبانی به‌زودی پاسخ می‌دهد."); message = "" } }, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(14.dp)).background(Gold)) {
                Icon(Icons.Filled.Send, null, tint = Color.Black)
            }
        }
    }
}

@Composable
private fun StatementScreen() {
    PageColumn {
        Panel(Modifier.fillMaxWidth()) {
            Text("گزارش عملکرد", color = TextMuted, fontSize = 11.sp)
            Text("آبان ۱۴۰۵", color = TextMain, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            InfoLine("مجموع واریز", "+ ۱۲٬۴۵۰٬۰۰۰ تومان", Green)
            InfoLine("مجموع برداشت", "− ۸٬۴۲۰٬۰۰۰ تومان", Red)
            InfoLine("مانده خالص", "۴٬۰۳۰٬۰۰۰ تومان", TextMain)
        }
        Spacer(Modifier.height(18.dp))
        SectionTitle("روند تراکنش‌ها")
        Spacer(Modifier.height(10.dp))
        SpendingPanel()
        Spacer(Modifier.height(18.dp))
        SectionTitle("خلاصه دسته‌بندی‌ها")
        Spacer(Modifier.height(9.dp))
        listOf("خرید روزانه" to "۳۵٪", "قبوض و خدمات" to "۲۵٪", "حمل‌ونقل" to "۲۰٪", "سایر" to "۲۰٪").forEachIndexed { i, item ->
            Column(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Row {
                    Text(item.first, color = TextMain, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text(item.second, color = TextMuted, fontSize = 11.sp)
                }
                Spacer(Modifier.height(5.dp))
                Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(SurfaceRaised)) {
                    Box(Modifier.fillMaxWidth(listOf(.35f, .25f, .2f, .2f)[i]).fillMaxHeight().clip(CircleShape).background(if (i == 0) Gold else TextMuted))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton("دریافت فایل گزارش", onClick = {})
    }
}

@Composable
private fun TransactionDetailScreen(onReceipt: () -> Unit) {
    PageColumn {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(65.dp).clip(CircleShape).background(GoldSoft), contentAlignment = Alignment.Center) { Icon(Icons.Filled.ShoppingBag, null, tint = Gold, modifier = Modifier.size(29.dp)) }
            Spacer(Modifier.height(12.dp))
            Text("پرداخت فروشگاهی", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("۲۴۵٬۰۰۰ تومان", color = TextMain, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("تراکنش موفق", color = Green, fontSize = 11.sp)
        }
        Spacer(Modifier.height(20.dp))
        Panel(Modifier.fillMaxWidth()) {
            InfoLine("وضعیت", "موفق", Green)
            InfoLine("تاریخ", "۱۷ آبان ۱۴۰۵")
            InfoLine("ساعت", "۱۴:۳۲:۱۸")
            InfoLine("شماره پیگیری", "۸۴۱۲۹۰۳۷۱۲")
            InfoLine("پذیرنده", "فروشگاه شهروند")
            InfoLine("شماره کارت", "•••• ۴۸۲۱")
            InfoLine("کارمزد", "رایگان")
        }
        Spacer(Modifier.height(17.dp))
        PrimaryButton("مشاهده و دریافت رسید", onClick = onReceipt)
        Spacer(Modifier.height(8.dp))
        SecondaryButton("گزارش مشکل", onClick = {})
    }
}

@Composable
private fun SuccessScreen(action: String, amount: String, onReceipt: () -> Unit, onHome: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(88.dp).clip(CircleShape).background(GoldSoft), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.CheckCircle, null, tint = Gold, modifier = Modifier.size(54.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text("عملیات با موفقیت انجام شد", color = TextMain, fontSize = 19.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(action, color = TextMuted, fontSize = 12.sp)
        Spacer(Modifier.height(20.dp))
        Panel(Modifier.fillMaxWidth()) {
            Text("مبلغ", color = TextMuted, fontSize = 11.sp)
            Text(amount, color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(9.dp))
            InfoLine("شماره پیگیری", "۸۴۱۲۹۰۳۷۱۲")
            InfoLine("وضعیت", "موفق", Green)
        }
        Spacer(Modifier.height(22.dp))
        PrimaryButton("مشاهده رسید", onClick = onReceipt)
        Spacer(Modifier.height(9.dp))
        SecondaryButton("بازگشت به خانه", onClick = onHome)
    }
}

@Composable
private fun ReceiptScreen(action: String, amount: String, onHome: () -> Unit) {
    PageColumn {
        Panel(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                NovaLogo(48)
                Spacer(Modifier.height(9.dp))
                Text("رسید تراکنش", color = TextMain, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("NOVAPAY • پرداخت امن", color = TextMuted, fontSize = 9.sp)
            }
            Spacer(Modifier.height(17.dp))
            HorizontalDivider(color = Line)
            Spacer(Modifier.height(13.dp))
            Text(action, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(amount, color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(10.dp))
            InfoLine("وضعیت", "موفق", Green)
            InfoLine("تاریخ", "۱۷ آبان ۱۴۰۵")
            InfoLine("ساعت", "۱۴:۳۲:۱۸")
            InfoLine("شماره پیگیری", "۸۴۱۲۹۰۳۷۱۲")
            InfoLine("مبدأ", "کیف پول اصلی")
            InfoLine("مقصد", "حساب مقصد")
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Line)
            Spacer(Modifier.height(10.dp))
            Text("این رسید به‌صورت آزمایشی در اپ تولید شده است.", color = TextMuted, fontSize = 9.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(14.dp))
        PrimaryButton("اشتراک‌گذاری رسید", onClick = {})
        Spacer(Modifier.height(9.dp))
        SecondaryButton("بازگشت به خانه", onClick = onHome)
    }
}

@Composable
private fun InfoLine(label: String, value: String, valueColor: Color = TextMain) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextMuted, fontSize = 10.sp, modifier = Modifier.weight(1f))
        Text(value, color = valueColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
    }
}
