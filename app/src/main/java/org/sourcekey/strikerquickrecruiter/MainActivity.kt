/*
 * MonsterStrikeQuickRecruiter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MonsterStrikeQuickRecruiter is distributed in the hope that it will be useful,
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MonsterStrikeQuickRecruiter.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.sourcekey.strikerquickrecruiter

//import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

import android.app.AlertDialog
import android.content.*
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val playStoreUrl =
        "https://play.google.com/store/apps/details?id=org.sourcekey.strikerquickrecruiter"

    private val teachingURL = "https://youtube.com/watch?v=syl0QVo05ks"

    private val sourceCodeUrl = "https://github.com/iHad169/StrikerQuickRecruiter"

    private var saver: SharedPreferences? = null

    private var drawerLayout: DrawerLayout? = null

    private var recentStartList: ArrayListWithListenEvent<String> = ArrayListWithListenEvent()

    private var navView: NavigationView? = null

    private var recruiterView: RecruiterView? = null

    private var extremeLuckOnlyRadioButton: RadioButton? = null

    private var appropriateRoleOnlyRadioButton: RadioButton? = null

    private var anyoneCanRadioButton: RadioButton? = null

    private var recruitTextView: TextView? = null

    private val recruitButtonClickWaitTime: Long = 5000

    private val recruitWillAutomaticSelectWaitTime: Long = 5000

    private var currentTheme: Int = R.style.OrdinalTheme

    /**
     * 初始化 招募條件 類 對此Activity提取
     * */
    private val initParticipationCondition = run {
        ParticipationCondition.context = this
    }

    /**
     * 展開插頁式廣告
     * */
    private fun openInterstitialAd(onAdClosed: () -> Unit = fun() {}){
        InterstitialAd.load(
            this@MainActivity,
            "ca-app-pub-2319576034906153/1180884454",
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    onAdClosed()
                }
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    interstitialAd.show(this@MainActivity)
                }
            }
        )
        /*
        val interstitialAd = InterstitialAd(this@MainActivity)
        interstitialAd.adUnitId = "ca-app-pub-2319576034906153/1180884454"
        interstitialAd.adListener = object : AdListener() {
            // Code to be executed when an ad finishes loading.
            override fun onAdLoaded() {
                super.onAdLoaded()
                interstitialAd.show()
            }
            // Code to be executed when an ad request fails.
            //override fun onAdFailedToLoad(adError: LoadAdError) { super.onAdFailedToLoad(adError) }
            // Code to be executed when the ad is displayed.
            //override fun onAdOpened() { super.onAdOpened() }
            // Code to be executed when the user clicks on an ad.
            //override fun onAdClicked() { super.onAdClicked() }
            // Code to be executed when the user has left the app.
            //override fun onAdLeftApplication() { super.onAdLeftApplication() }
            // Code to be executed when the interstitial ad is closed.
            override fun onAdClosed() {
                super.onAdClosed()
                onAdClosed()
            }
        }
        interstitialAd.loadAd(AdRequest.Builder().build())*/
    }

    /**
     * 顯示使用條款
     */
    private fun showUseConditions() {
        val useConditionsDialogLayout = layoutInflater.inflate(
            R.layout.use_conditions_dialog_layout,
            null
        )
        AlertDialog.Builder(this)
            .setView(useConditionsDialogLayout)
            .setCancelable(false)
            .setPositiveButton(R.string.agree) { dialogInterface, which ->
                saver?.edit()?.putBoolean("isAgreeUseConditions", true)?.commit()
            }
            .setNegativeButton(R.string.disagree) { dialogInterface, which ->
                saver?.edit()?.putBoolean("isAgreeUseConditions", false)?.commit()
                finish()
                System.exit(0)
            }
            .create().show()
        val privacyPolicyWebView: WebView =
            useConditionsDialogLayout.findViewById(R.id.privacyPolicyWebView)
        val termsConditionsWebView: WebView =
            useConditionsDialogLayout.findViewById(R.id.termsConditionsWebView)
        privacyPolicyWebView.loadUrl("file:///android_asset/privacyPolicy.html")
        termsConditionsWebView.loadUrl("file:///android_asset/termsConditions.html")
    }

    /**
     * 顯示教學
     */
    private fun showTeaching() {
        /**
        val teachingLayout = layoutInflater.inflate(
        R.layout.teaching_layout,
        null
        )
        AlertDialog.Builder(this)
        .setTitle(R.string.teaching)
        .setView(teachingLayout)
        .setCancelable(true)
        .setPositiveButton(R.string.ok) { dialogInterface, which ->
        saver?.edit()?.putBoolean("isWatchTeaching", true)?.commit()
        }
        .create().show()
        val youTubePlayerView = findViewById<YouTubePlayerView>(R.id.teachingYouTubePlayerView)
        //lifecycle.addObserver(youTubePlayerView)
         */
        AlertDialog.Builder(this)
            .setTitle(R.string.teaching)
            .setMessage(R.string.youNeedWatchTeaching)
            .setCancelable(true)
            .setPositiveButton(R.string.need) { dialogInterface, which ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(teachingURL)))
                saver?.edit()?.putBoolean("isWatchTeaching", true)?.commit()
            }
            .setNegativeButton(R.string.notNeed) { dialogInterface, which ->
                saver?.edit()?.putBoolean("isWatchTeaching", true)?.commit()
            }
            .create().show()
    }

    /**
     * 從Intent獲取招募訊息
     * */
    private fun getRecruitText(intent: Intent): String? {
        val data: Uri = intent.data ?: return null
        when (data.scheme.toString()) {
            "line" -> {
                return data.path
            }
            "whatsapp" -> {
                return data.query
            }
            "http" -> {
                return data.path
            }
            "https" -> {
                return data.path
            }
            "null" -> {
                return null
            }
            "" -> {
                return null
            }
            else -> {
                return null
            }
        }
    }

    /**
     * 招募
     *
     * 一次過填寫招募所需嘅資訊然後進行招募
     * */
    private fun recruit(recruitText: String? = null) {
        recruiterView?.pasteRecruit(recruitText?: recruitTextView?.text.toString())
        recruiterView?.executeRecruit()
    }

    /**
     * 獲取上次所選嘅參加條件RadioButton
     * */
    private fun getLastTimeSelectParticipationCondition(): ParticipationCondition {
        return ParticipationCondition.valueOf(
            saver?.getString("participationCondition", "0")?.toIntOrNull() ?: 0
        ) ?: ParticipationCondition.extremeLuckOnly
    }

    /**
     * 更新參加條件RadioButton
     * */
    private fun updateParticipationConditionRadioButton(participationCondition: ParticipationCondition? = null) {
        when (participationCondition ?: getLastTimeSelectParticipationCondition()) {
            ParticipationCondition.extremeLuckOnly -> {
                extremeLuckOnlyRadioButton?.isChecked = true
            }
            ParticipationCondition.appropriateRoleOnly -> {
                appropriateRoleOnlyRadioButton?.isChecked = true
            }
            ParticipationCondition.anyoneCan -> {
                anyoneCanRadioButton?.isChecked = true
            }
        }
    }

    /**
     * 初始化參加條件RadioButton
     * */
    private fun initParticipationConditionRadioButton() {
        fun selectRecruitConditions(participationCondition: ParticipationCondition) {
            saver?.edit()?.putString("participationCondition", participationCondition.ordinal.toString())?.apply()
            recruiterView?.selectRecruitConditions(participationCondition)
        }
        extremeLuckOnlyRadioButton = findViewById(R.id.extremeLuckOnlyRadioButton)
        appropriateRoleOnlyRadioButton = findViewById(R.id.appropriateRoleOnlyRadioButton)
        anyoneCanRadioButton = findViewById(R.id.anyoneCanRadioButton)
        extremeLuckOnlyRadioButton?.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                selectRecruitConditions(ParticipationCondition.extremeLuckOnly)
            }
        }
        appropriateRoleOnlyRadioButton?.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                selectRecruitConditions(ParticipationCondition.appropriateRoleOnly)
            }
        }
        anyoneCanRadioButton?.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                selectRecruitConditions(ParticipationCondition.anyoneCan)
            }
        }
    }

    /**
     * 顯示招募條件選擇
     * */
    private fun showSelectParticipationCondition() {
        //新增底下廣告Layout
        val bottomAdDialogLayout = layoutInflater.inflate(
            R.layout.bottom_ad_dialog_layout,
            null
        )
        //設置Dialog
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.teaching)
            .setView(bottomAdDialogLayout)
            .setCancelable(true)
            .setSingleChoiceItems(
                ParticipationCondition.strings(),
                getLastTimeSelectParticipationCondition().ordinal
            ) { di, i ->
                updateParticipationConditionRadioButton(ParticipationCondition.valueOf(i))
                di?.dismiss()
            }
            .setOnDismissListener { recruit() }
            .setOnCancelListener { recruit() }
            .create()
        dialog.show()
        //設定底下廣告
        MobileAds.initialize(dialog.context) {}
        val bottomAdView: AdView = bottomAdDialogLayout.findViewById(R.id.bottomAdView)
        bottomAdView.loadAd(AdRequest.Builder().build())
        //設置倒時器
        setTimer(fun(rt) {
            val waitTime = rt.toInt() / 1000
            val message =
                "${getString(R.string.willAutomaticSelect)} (${waitTime}${getString(R.string.seconds)})"
            dialog.setTitle(message)
        }, fun() { dialog.dismiss() }, recruitWillAutomaticSelectWaitTime)
    }

    /**
     * 更新招募TextView
     * */
    private fun updateRecruitTextView(text: String? = null) {
        recruitTextView?.setText(text ?: saver?.getString("recruitText", ""))
        if(text != null){saver?.edit()?.putString("recruitText", text)?.apply()}
    }

    /**
     * 設定招募文字
     * */
    private fun initRecruitTextView() {
        recruitTextView = findViewById(R.id.recruitTextView)
        recruitTextView?.setOnClickListener {
            //設定Click-To-Copy功能
            val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("TextView", recruitTextView?.text.toString())
            clipboard.setPrimaryClip(clip)
            //提示 已複製 成功
            Toast.makeText(this@MainActivity, getString(R.string.copied), Toast.LENGTH_SHORT).show()
            //展開插頁式廣告
            openInterstitialAd()
        }
    }

    /**
     * 初始化招募Button
     * */
    private fun initRecruitButton() {
        val recruitButton: Button = findViewById(R.id.recruitButton)
        var clickWaitTime = 0
        recruitButton.setOnClickListener {
            if (clickWaitTime <= 0) {
                recruit()
                setTimer(fun(rt) {
                    clickWaitTime = rt.toInt() / 1000
                    recruitButton.setText("${getString(R.string.recruit)} ($clickWaitTime)")
                }, fun() {
                    clickWaitTime = 0
                    recruitButton.setText(R.string.recruit)
                }, recruitButtonClickWaitTime)
            }else{ openInterstitialAd{ recruit() } }
        }
    }

    /**
     * 更新NavigationView
     * */
    private fun updateNavigationView(addUrl: String? = null){
        //增加Url
        if(addUrl != null){
            //更新 最近啟動連結表
            recentStartList.remove(addUrl)
            recentStartList.add(0, addUrl)
            val maxSize = 20
            if(maxSize < recentStartList.size){
                try{ recentStartList.removeAt(maxSize) }catch (e: Exception){}
            }
            //儲存 最近啟動連結表
            saver?.edit()?.putString("recentStartLinkList", Gson().toJson(recentStartList))?.apply()
        }
        //更新NavigationView
        navView?.menu?.clear()
        recentStartList.forEachIndexed { index, url ->
            val s = SpannableString(url)
            s.setSpan(ForegroundColorSpan(url.rendRGB()), 0, s.length, 0)
            navView?.menu?.add(0, index, index, s)
        }
    }

    /**
     * 初始化NavigationView
     * */
    private fun initNavigationView(){
        //新增 顯示NavigationView 按鈕
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        //初始化NavigationView相關值
        drawerLayout = findViewById(R.id.drawer_layout)
        recentStartList = try{Gson().fromJson(
            saver?.getString("recentStartLinkList", ""),
            object : TypeToken<ArrayListWithListenEvent<String>>(){}.type
        )}catch (e: Exception){ArrayListWithListenEvent()}
        navView = findViewById(R.id.navView)
        //設定NavigationView
        navView?.setNavigationItemSelectedListener {
            try{
                val url = recentStartList.getOrNull(it.itemId)
                Toast.makeText(this@MainActivity, url, Toast.LENGTH_SHORT).show()
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                recruiterView?.stopLoading()
                startActivity(intent)
            }catch(e: Exception){Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()}
            return@setNavigationItemSelectedListener true
        }
        recentStartList.onElementChange = fun(){ updateNavigationView() }
        updateNavigationView()
    }

    /**
     * 初始化招募器
     * */
    private fun initRecruiterView(){
        recruiterView = findViewById(R.id.recruiterView)
        recruiterView?.onPageFinished = fun(webView, url){
            //初始化上次所選嘅RadioButton
            recruiterView?.selectRecruitConditions(getLastTimeSelectParticipationCondition())
            //如由怪物彈殊啟動此程式就顯示招募條件選擇
            if (getRecruitText(intent) != null) { showSelectParticipationCondition() }
        }
        recruiterView?.onReturnGame = fun(webView, url){
            url?:return
            updateNavigationView()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout?.openDrawer(GravityCompat.START)
            }
            R.id.restoreItem -> {
                recruiterView?.restore()
            }
            R.id.shareItem -> {
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "text/plain"
                val shareBody = playStoreUrl
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share)))
            }
            R.id.supportUsItem -> {
                openInterstitialAd {
                    Toast.makeText(this@MainActivity, R.string.thankYou, Toast.LENGTH_LONG).show()
                }
            }
            R.id.sourceCodeItem -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(sourceCodeUrl))
                startActivity(browserIntent)
            }
            R.id.useConditionsItem -> {
                showUseConditions()
            }
            R.id.teachingItem -> {
                showTeaching()
            }
            /*R.id.switchThemeItem -> {
                setTheme(R.style.OldTheme)
                //recreate()
                setContentView(R.layout.activity_main)
                AlertDialog.Builder(this)
                    .setTitle(R.string.switchTheme)
                    .setCancelable(true)
                    .setSingleChoiceItems(
                        R.style,
                        getLastTimeSelectParticipationCondition().ordinal
                    ) { di, i ->
                        updateParticipationConditionRadioButton(ParticipationCondition.valueOf(i))
                        di?.dismiss()
                    }
                    .setPositiveButton(R.string.need) { dialogInterface, which ->
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(teachingURL)))
                        saver?.edit()?.putBoolean("isWatchTeaching", true)?.commit()
                    }
                    .setNegativeButton(R.string.notNeed) { dialogInterface, which ->
                        saver?.edit()?.putBoolean("isWatchTeaching", true)?.commit()
                    }
                    .create().show()
            }*/
            R.id.about -> {
                val switchActivityIntent = Intent(this, AboutActivity::class.java)
                startActivity(switchActivityIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //設定資料saver
        saver = getSharedPreferences("MonsterStrikeQuickRecruiter", 0)
        //顯示教學
        if (saver?.getBoolean("isWatchTeaching", false) != true) {
            showTeaching()
        }
        //顯示使用條款
        if (saver?.getBoolean("isAgreeUseConditions", false) != true) {
            showUseConditions()
        }
        //初始化NavigationView
        initNavigationView()
        //初始化參加條件RadioButton
        initParticipationConditionRadioButton()
        //初始化招募文字
        initRecruitTextView()
        //初始化招募鍵
        initRecruitButton()
        //初始化招募器
        initRecruiterView()
        //設定底下廣告
        MobileAds.initialize(this) {}
        val bottomAdView: AdView = findViewById(R.id.bottomAdView)
        bottomAdView.loadAd(AdRequest.Builder().build())
    }

    override fun onResume() {
        super.onResume()
        recruiterView?.resume()
        //更新介面資訊
        updateParticipationConditionRadioButton()
        updateRecruitTextView(getRecruitText(intent))
    }

    override fun onPause() {
        recruiterView?.pause()
        super.onPause()
    }

    override fun onStop() {
        recruiterView?.stop()
        super.onStop()
    }

    override fun onDestroy() {
        recruiterView?.destroy()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val recruitText = getRecruitText(intent ?: return) ?: return
        updateRecruitTextView(recruitText)
        recruit()
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        //theme.applyStyle(R.style.OldTheme, true)
        return theme
    }
}