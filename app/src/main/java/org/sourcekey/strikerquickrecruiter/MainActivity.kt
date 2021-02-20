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
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import java.net.URISyntaxException


class MainActivity : AppCompatActivity() {

    private val recruiterUrl = "https://gamewith.tw/monsterstrike/lobby"

    private val playStoreUrl =
        "https://play.google.com/store/apps/details?id=org.sourcekey.strikerquickrecruiter"

    private val teachingURL = "https://youtube.com/watch?v=syl0QVo05ks"

    private val sourceCodeUrl = "https://github.com/iHad169/StrikerQuickRecruiter"

    private var saver: SharedPreferences? = null

    private var webView: WebView? = null

    private var extremeLuckOnlyRadioButton: RadioButton? = null

    private var appropriateRoleOnlyRadioButton: RadioButton? = null

    private var anyoneCanRadioButton: RadioButton? = null

    private var recruitTextView: TextView? = null

    private val recruitButtonClickWaitTime: Long = 5000

    private val recruitWillAutomaticSelectWaitTime: Long = 5000

    /**
     * 招募條件
     * */
    private enum class ParticipationCondition(val resourceId: Int) {
        //僅限極運
        extremeLuckOnly(R.string.extremeLuckOnly) {
            override fun toString(): String = context?.getString(resourceId) ?: toString()
        },

        //僅限適正角色
        appropriateRoleOnly(R.string.appropriateRoleOnly) {
            override fun toString(): String = context?.getString(resourceId) ?: toString()
        },

        //誰都可以
        anyoneCan(R.string.anyoneCan) {
            override fun toString(): String = context?.getString(resourceId) ?: toString()
        };

        companion object {
            var context: Context? = null
            fun valueOf(value: Int): ParticipationCondition? {
                return values().getOrNull(value)
            }

            fun strings(): Array<String> {
                return values().map { it.toString() }.toTypedArray()
            }
        }
    }

    /**
     * 初始化 招募條件 類 對此Activity提取
     * */
    private val initParticipationCondition = { ParticipationCondition.context = this }()

    /**
     * 展開插頁式廣告
     * */
    private fun openInterstitialAd(onAdClosed: () -> Unit = fun() {}){
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
        interstitialAd.loadAd(AdRequest.Builder().build())
    }

    /**
     * 設置倒時器
     * */
    private fun setTimer(
        onUpdate: (remainingTime: Long) -> Unit,
        onTimeout: () -> Unit,
        delayTime: Long,
        updateTime: Long = 1000,
        infinityLoop: Boolean = false
    ) {
        var waitTime = delayTime
        val handler = Handler()
        handler.removeCallbacksAndMessages(null)
        fun loop() {
            handler.postDelayed({
                waitTime -= updateTime
                if (0 < waitTime || infinityLoop) {
                    onUpdate(waitTime)
                    loop()
                } else {
                    onTimeout()
                }
            }, updateTime)
        }
        onUpdate(waitTime)
        loop()
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
     * 響WebView執行JavaScript程序
     *
     * 發現如直接寫JS程序
     * 唔加setTimeout()
     * 會執行無效
     * */
    private fun WebView.runJS(jsCode: String, delay: Int = 0) {
        loadUrl("""javascript:window.setTimeout(function(){$jsCode}, ${delay});""")
    }

    /**
     * 包裝成 設置Node改變左嘅監聽器 嘅JS程序
     * */
    private fun wrapSetNodeChangedListener(
        listenClassName: String,
        attributeName: String,
        runJsThenListened: String,
        infinityLoop: Boolean = false
    ): String {
        /*"""
            document.querySelectorAll(".$listClassName").forEach(function(element){
                var mutationObserver = new MutationObserver(function(mutationsList, observer){
                    mutationsList.forEach(function(mutation){
                        $runJsThenListed
                        mutationObserver.disconnect();
                    })
                });
                mutationObserver.observe(element, { attributes: true });
            });
        """*/
        return """
            document.querySelectorAll(".$listenClassName").forEach(function(element){
                var originalValue = element.getAttribute("$attributeName");
                var timer = window.setInterval(function(){
                    var currentValue = element.getAttribute("$attributeName");
                    if(originalValue !== currentValue){
                        $runJsThenListened
                        if(!$infinityLoop){clearInterval(timer);}
                    }
                }, 1000);
            });
        """
    }

    /**
     * 選擇招募條件
     * */
    private fun selectRecruitConditions(participationCondition: ParticipationCondition) {
        saver?.edit()?.putString(
            "participationCondition",
            participationCondition.ordinal.toString()
        )?.commit()
        webView?.runJS("""document.getElementsByName("js-recruiting-tags")[${participationCondition.ordinal}].checked = true;""")
    }

    /**
     * 填上招募訊息
     * */
    private fun pasteRecruit() {
        val recruitText = recruitTextView?.text.toString()
        saver?.edit()?.putString("recruitText", recruitText)?.commit()
        webView?.runJS("""document.getElementsByClassName("js-recruiting-line-message")[0].value = "${recruitText}";""")
    }

    /**
     * 進行招募
     * */
    private fun executeRecruit() {
        webView?.runJS(
            """document.getElementsByClassName("js-recruiting-button")[0].click();""",
            1000
        )
    }

    /**
     * 進入返遊戲
     * */
    private fun startGame() {
        webView?.runJS(
            wrapSetNodeChangedListener(
                "js-recruiting-modal-content-loading", "style",
                """document.getElementsByClassName("js-recruiting-modal-launch")[0].click();"""
            ), 1000
        )
    }

    /**
     * 初始化 参加招募者團隊 按鈕
     * */
    private fun initParticipateButton() {
        webView?.runJS(
            """
                window.setInterval(function(){
                    document.querySelectorAll(".js-participate-anchor-launch").forEach(function(element){
                        if(element.href.startsWith("intent://")){
                            element.click();
                            element.parentNode.removeChild(element);
                        }
                    });
                }, 1000);
            """
        )
    }

    /**
     * 快速招募
     *
     * 一次過填寫招募所需嘅資訊然後進行招募
     * */
    private fun quickRecruit() {
        pasteRecruit()
        executeRecruit()
        startGame()
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
            .setOnDismissListener { quickRecruit() }
            .setOnCancelListener { quickRecruit() }
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
     * 初始化WebView
     * */
    private fun initWebView() {
        webView = findViewById(R.id.webView)
        val webViewSettings = webView?.settings
        webViewSettings?.domStorageEnabled = true
        webViewSettings?.javaScriptEnabled = true//設定同JavaScript互Call權限
        webViewSettings?.javaScriptCanOpenWindowsAutomatically = true//設定允許畀JavaScript彈另一個window
        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(webView: WebView, url: String) {
                super.onPageFinished(webView, url)
                //初始化 参加招募者團隊 按鈕
                initParticipateButton()
                //初始化上次所選嘅RadioButton
                selectRecruitConditions(getLastTimeSelectParticipationCondition())
                //如由怪物彈殊啟動此程式就顯示招募條件選擇
                if (getRecruitText(intent) != null) {
                    showSelectParticipationCondition()
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url!!.startsWith("intent://")) {
                    //Toast.makeText(this@MainActivity, url, Toast.LENGTH_SHORT).show()
                    try {
                        val context = view!!.context
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        if (intent != null) {
                            view.stopLoading()
                            val packageManager = context.packageManager
                            val info = packageManager.resolveActivity(
                                intent,
                                PackageManager.MATCH_DEFAULT_ONLY
                            )
                            if (info != null) {
                                context.startActivity(intent)
                            } else {
                                val fallbackUrl =
                                    intent.getStringExtra("browser_fallback_url") ?: return false
                                view.loadUrl(fallbackUrl)

                                // or call external broswer
                                // Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
                                // context.startActivity(browserIntent);
                            }
                            return true
                        }
                    } catch (e: URISyntaxException) {
                    }
                }
                return false
            }
        }
        webView?.loadUrl(recruiterUrl)
    }

    /**
     * 更新招募TextView
     * */
    private fun updateRecruitTextView(text: String? = null) {
        recruitTextView?.setText(text ?: saver?.getString("recruitText", ""))
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
                quickRecruit()
                setTimer(fun(rt) {
                    clickWaitTime = rt.toInt() / 1000
                    recruitButton.setText("${getString(R.string.recruit)} ($clickWaitTime)")
                }, fun() {
                    clickWaitTime = 0
                    recruitButton.setText(R.string.recruit)
                }, recruitButtonClickWaitTime)
            }else{ openInterstitialAd{ quickRecruit() } }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refreshItem -> {
                //webView?.reload()
                webView?.loadUrl(recruiterUrl)
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
        //設定webView
        initWebView()
        //設定參加條件RadioButton
        initParticipationConditionRadioButton()
        //設定招募文字
        initRecruitTextView()
        //設定招募鍵
        initRecruitButton()
        //設定底下廣告
        MobileAds.initialize(this) {}
        val bottomAdView: AdView = findViewById(R.id.bottomAdView)
        bottomAdView.loadAd(AdRequest.Builder().build())
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
        webView?.resumeTimers()//請注意，此調用不會暫停JavaScript。要全局暫停JavaScript，請使用pauseTimers()
        //更新介面資訊
        updateParticipationConditionRadioButton()
        updateRecruitTextView(getRecruitText(intent))
    }

    override fun onPause() {
        webView?.onPause()
        super.onPause()
    }

    override fun onStop() {
        webView?.pauseTimers()//如果應用程序已暫停，這可能很有用。
        super.onStop()
    }

    override fun onDestroy() {
        webView?.destroy()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val recruitText = getRecruitText(intent ?: return) ?: return
        updateRecruitTextView(recruitText)
        quickRecruit()
    }
}