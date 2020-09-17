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

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import java.net.URISyntaxException


class MainActivity : AppCompatActivity() {
    private val recruiterUrl = "https://gamewith.tw/monsterstrike/lobby"

    private val playStoreUrl = "https://play.google.com/store/apps/details?id=org.sourcekey.strikerquickrecruiter"

    private val sourceCodeUrl = "https://github.com/iHad169/StrikerQuickRecruiter"

    private var saver: SharedPreferences? = null

    private var webView: WebView? = null

    private var extremeLuckOnlyRadioButton: RadioButton? = null

    private var appropriateRoleOnlyRadioButton: RadioButton? = null

    private var anyoneCanRadioButton: RadioButton? = null

    private var recruitEditText: EditText? = null

    private val recruitButtonClickWaitTime = 3

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
            .setNeutralButton(R.string.disagree) { dialogInterface, which ->
                saver?.edit()?.putBoolean("isAgreeUseConditions", false)?.commit()
                finish()
                System.exit(0)
            }
            .create().show()
        val privacyPolicyWebView: WebView = useConditionsDialogLayout.findViewById(R.id.privacyPolicyWebView)
        val termsConditionsWebView: WebView = useConditionsDialogLayout.findViewById(R.id.termsConditionsWebView)
        privacyPolicyWebView.loadUrl("file:///android_asset/privacyPolicy.html")
        termsConditionsWebView.loadUrl("file:///android_asset/termsConditions.html")
    }

    /**
     * 從Intent獲取招募訊息
     * */
    private fun getRecruitText(intent: Intent): String? {
        val data: Uri = intent.data?:return null
        when(data.scheme.toString()){
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
     * 招募條件
     * */
    private enum class ParticipationCondition{
        extremeLuckOnly,        //僅限極運
        appropriateRoleOnly,    //僅限適正角色
        anyoneCan;              //誰都可以
        companion object{
            fun valueOf(value: Int): ParticipationCondition? {
                return values().getOrNull(value)
            }
        }
    }

    /**
     * 選擇招募條件
     * */
    private fun selectRecruitConditions(participationCondition: ParticipationCondition){
        saver?.edit()?.putString(
            "participationCondition",
            participationCondition.ordinal.toString()
        )?.commit()
        webView?.loadUrl(
            """javascript:
            |window.setTimeout(function(){
                |document.getElementsByName("js-recruiting-tags")[${participationCondition.ordinal}].checked = true;
            |}, 0);
        """.trimMargin()
        )
    }

    /**
     * 填上招募訊息
     * */
    private fun pasteRecruit(){
        val recruitText = recruitEditText?.text.toString()
        saver?.edit()?.putString("recruitText", recruitText)?.commit()
        webView?.loadUrl(
            """javascript:
            |window.setTimeout(function(){
                |document.getElementsByClassName("js-recruiting-line-message")[0].value = "${recruitText}";
            |}, 0);
        """.trimMargin()
        )
    }

    /**
     * 進行招募
     * */
    private fun executeRecruit(){
        webView?.loadUrl(
            """javascript:
            |window.setTimeout(function(){
                |document.getElementsByClassName("js-recruiting-button")[0].click();
            |}, 1000);
        """.trimMargin()
        )
    }

    /**
     * 進入返遊戲
     * */
    private fun startGame(){
        webView?.loadUrl(
            """javascript:
            |window.setTimeout(function(){
                |document.getElementsByClassName("js-recruiting-modal-launch")[0].click();
            |}, 4000);
        """.trimMargin()
        )
    }

    /**
     * 快速招募
     *
     * 一次過填寫招募所需嘅資訊然後進行招募
     * */
    private fun quickRecruit(){
        pasteRecruit()
        executeRecruit()
        startGame()
    }

    /**
     * 初始化WebView
     * */
    private fun initWebView(){
        webView = findViewById(R.id.webView)
        val webViewSettings = webView?.settings
        webViewSettings?.domStorageEnabled = true
        webViewSettings?.javaScriptEnabled = true//設定同JavaScript互Call權限
        webViewSettings?.javaScriptCanOpenWindowsAutomatically = true//設定允許畀JavaScript彈另一個window
        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(webView: WebView, url: String) {
                super.onPageFinished(webView, url)
                //初始化上次所選嘅RadioButton
                val beforeSelectParticipationCondition = ParticipationCondition.valueOf(
                    saver?.getString("participationCondition", "0")?.toIntOrNull()?:0
                )?:ParticipationCondition.extremeLuckOnly
                selectRecruitConditions(beforeSelectParticipationCondition)
                //如由怪物彈殊啟動此程式就進行 快速招募
                if(getRecruitText(intent) != null){
                    quickRecruit()
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if(url!!.startsWith("intent://")) {
                    try {
                        val context = view!!.context
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        if(intent != null) {
                            view!!.stopLoading()
                            val packageManager = context.packageManager
                            val info = packageManager.resolveActivity(
                                intent,
                                PackageManager.MATCH_DEFAULT_ONLY
                            )
                            if(info != null) {
                                context.startActivity(intent)
                            } else {
                                val fallbackUrl =
                                    intent.getStringExtra("browser_fallback_url")
                                view!!.loadUrl(fallbackUrl)

                                // or call external broswer
                                // Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
                                // context.startActivity(browserIntent);
                            }
                            return true
                        }
                    } catch (e: URISyntaxException) { }
                }
                return false
            }
        }
        webView?.loadUrl(recruiterUrl)
    }

    /**
     * 更新參加條件RadioButton
     * */
    private fun updateParticipationConditionRadioButton(participationCondition: ParticipationCondition? = null){
        val beforeSelectParticipationCondition =
            participationCondition?:
            ParticipationCondition.valueOf(
                saver?.getString("participationCondition", "0")?.toIntOrNull()?:0
            )?:
            ParticipationCondition.extremeLuckOnly

        when(beforeSelectParticipationCondition){
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
    private fun initParticipationConditionRadioButton(){
        extremeLuckOnlyRadioButton      = findViewById(R.id.extremeLuckOnlyRadioButton)
        appropriateRoleOnlyRadioButton  = findViewById(R.id.appropriateRoleOnlyRadioButton)
        anyoneCanRadioButton            = findViewById(R.id.anyoneCanRadioButton)
        extremeLuckOnlyRadioButton?.setOnClickListener { selectRecruitConditions(
            ParticipationCondition.extremeLuckOnly
        ) }
        appropriateRoleOnlyRadioButton?.setOnClickListener { selectRecruitConditions(
            ParticipationCondition.appropriateRoleOnly
        ) }
        anyoneCanRadioButton?.setOnClickListener { selectRecruitConditions(ParticipationCondition.anyoneCan) }
    }

    /**
     * 更新招募EditText
     * */
    private fun updateRecruitEditText(text: String? = null){
        recruitEditText?.setText(text?:saver?.getString("recruitText", ""))
    }

    /**
     * 初始化招募Button
     * */
    private fun initRecruitButton(){
        val recruitButton: Button = findViewById(R.id.recruitButton)
        var clickWaitTime = 0
        val handle = Handler()
        fun setClickTimer(){
            clickWaitTime = recruitButtonClickWaitTime
            recruitButton.setText("${getString(R.string.recruit)} ($clickWaitTime)")
            handle.removeCallbacksAndMessages(null)
            fun loop(){
                handle.postDelayed({
                    clickWaitTime--
                    if(0 < clickWaitTime) {
                        recruitButton.setText("${getString(R.string.recruit)} ($clickWaitTime)")
                        loop()
                    } else {
                        recruitButton.setText(R.string.recruit)
                    }
                }, 1000)
            }
            loop()
        }
        recruitButton.setOnClickListener{
            if(clickWaitTime <= 0){
                quickRecruit()
                setClickTimer()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.shareItem -> {
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "text/plain"
                val shareBody = "${playStoreUrl}"
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share)))
            }
            R.id.supportUsItem -> {
                val supportUsAd = InterstitialAd(this@MainActivity)
                supportUsAd.adUnitId = "ca-app-pub-2319576034906153/7505233198"
                supportUsAd.adListener = object: AdListener() {
                    // Code to be executed when an ad finishes loading.
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        supportUsAd.show()
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
                        Toast.makeText(this@MainActivity, R.string.thankYou, Toast.LENGTH_LONG).show()
                    }
                }
                supportUsAd.loadAd(AdRequest.Builder().build())
            }
            R.id.sourceCodeItem -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(sourceCodeUrl))
                startActivity(browserIntent)
            }
            R.id.useConditionsItem -> {
                showUseConditions()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //設定資料saver
        saver = getSharedPreferences("MonsterStrikeQuickRecruiter", 0)
        //顯示使用條款
        if(saver?.getBoolean("isAgreeUseConditions", false) != true){showUseConditions()}
        //設定webView
        initWebView()
        //設定參加條件RadioButton
        initParticipationConditionRadioButton()
        //設定招募文字
        recruitEditText = findViewById(R.id.recruitEditText)
        //設定招募鍵
        initRecruitButton()
        //設定底下廣告
        MobileAds.initialize(this){}
        val bottomAdView: AdView = findViewById(R.id.bottomAdView)
        bottomAdView.loadAd(AdRequest.Builder().build())
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
        webView?.resumeTimers()//請注意，此調用不會暫停JavaScript。要全局暫停JavaScript，請使用pauseTimers()
        //更新介面資訊
        updateParticipationConditionRadioButton()
        updateRecruitEditText(getRecruitText(intent))
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
        val recruitText = getRecruitText(intent?:return)?:return
        updateRecruitEditText(recruitText)
        quickRecruit()
    }
}