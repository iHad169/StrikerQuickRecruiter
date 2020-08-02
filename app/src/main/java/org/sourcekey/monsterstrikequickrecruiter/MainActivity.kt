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

package org.sourcekey.monsterstrikequickrecruiter

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import java.net.URISyntaxException


class MainActivity : AppCompatActivity() {
    val recruiterUrl = "https://gamewith.tw/monsterstrike/lobby"

    private var saver: SharedPreferences? = null

    private var webView: WebView? = null

    private var extremeLuckOnlyRadioButton: RadioButton? = null

    private var appropriateRoleOnlyRadioButton: RadioButton? = null

    private var anyoneCanRadioButton: RadioButton? = null

    private var recruitEditText: EditText? = null

    private var recruitButton: Button? = null

    /**
     * 顯示使用條款
     */
    private fun showUseConditions() {
        val useConditionsDialogLayout = layoutInflater.inflate(R.layout.use_conditions_dialog_layout, null)
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

    enum class ParticipationCondition{
        extremeLuckOnly,        //僅限極運
        appropriateRoleOnly,    //僅限適正角色
        anyoneCan;              //誰都可以
        companion object{
            fun valueOf(value: Int): ParticipationCondition? {
                return values().getOrNull(value)
            }
        }
    }

    private fun selectRecruitConditions(participationCondition: ParticipationCondition){
        Log.i("屌", participationCondition.name)
        saver?.edit()?.putString("participationCondition", participationCondition.ordinal.toString())?.commit()
        webView?.loadUrl("""javascript:
            |window.setTimeout(function(){
                |document.getElementsByName("js-recruiting-tags")[${participationCondition.ordinal}].checked = true;
            |}, 0);
        """.trimMargin())
    }

    private fun pasteRecruit(){
        val recruitText = recruitEditText?.text.toString()
        saver?.edit()?.putString("recruitText", recruitText)?.commit()
        webView?.loadUrl("""javascript:
            |window.setTimeout(function(){
                |document.getElementsByClassName("js-recruiting-line-message")[0].value = "${recruitText}";
            |}, 0);
        """.trimMargin())
    }

    private fun executeRecruit(){
        webView?.loadUrl("""javascript:
            |window.setTimeout(function(){
                |document.getElementsByClassName("js-recruiting-button")[0].click();
            |}, 1000);
        """.trimMargin())
    }

    private fun startGame(){
        webView?.loadUrl("""javascript:
            |window.setTimeout(function(){
                |document.getElementsByClassName("js-recruiting-modal-launch")[0].click();
            |}, 4000);
        """.trimMargin())
    }

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
        webViewSettings?.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK)//開啟離線網頁功能,為之後上唔到個網都可以用到
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
                if (url!!.startsWith("intent://")) {
                    try {
                        val context = view!!.context
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        if (intent != null) {
                            view!!.stopLoading()
                            val packageManager = context.packageManager
                            val info = packageManager.resolveActivity(
                                intent,
                                PackageManager.MATCH_DEFAULT_ONLY
                            )
                            if (info != null) {
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
     * 初始化參加條件RadioButton
     * */
    private fun initParticipationConditionRadioButton(){
        extremeLuckOnlyRadioButton      = findViewById(R.id.extremeLuckOnlyRadioButton)
        appropriateRoleOnlyRadioButton  = findViewById(R.id.appropriateRoleOnlyRadioButton)
        anyoneCanRadioButton            = findViewById(R.id.anyoneCanRadioButton)
        extremeLuckOnlyRadioButton?.setOnClickListener { selectRecruitConditions(ParticipationCondition.extremeLuckOnly) }
        appropriateRoleOnlyRadioButton?.setOnClickListener { selectRecruitConditions(ParticipationCondition.appropriateRoleOnly) }
        anyoneCanRadioButton?.setOnClickListener { selectRecruitConditions(ParticipationCondition.anyoneCan) }
        val beforeSelectParticipationCondition = ParticipationCondition.valueOf(
            saver?.getString("participationCondition", "0")?.toIntOrNull()?:0
        )?:ParticipationCondition.extremeLuckOnly
        when(beforeSelectParticipationCondition){
            ParticipationCondition.extremeLuckOnly -> {extremeLuckOnlyRadioButton?.isChecked = true}
            ParticipationCondition.appropriateRoleOnly -> {appropriateRoleOnlyRadioButton?.isChecked = true}
            ParticipationCondition.anyoneCan -> {anyoneCanRadioButton?.isChecked = true}
        }
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
        recruitEditText?.setText(getRecruitText(intent)?:saver?.getString("recruitText", ""))
        //設定招募鍵
        recruitButton = findViewById(R.id.recruitButton)
        recruitButton?.setOnClickListener{ quickRecruit() }
        //設定橫幅廣告
        MobileAds.initialize(this){}
        val bottomAdView: AdView = findViewById(R.id.bottomAdView)
        bottomAdView.loadAd(AdRequest.Builder().build())
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
        webView?.resumeTimers()//請注意，此調用不會暫停JavaScript。要全局暫停JavaScript，請使用pauseTimers()
    }

    override fun onPause() {
        super.onPause()
        webView?.onPause()
        webView?.pauseTimers()//如果應用程序已暫停，這可能很有用。
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val recruitText = getRecruitText(intent?:return)?:return
        recruitEditText?.setText(recruitText)
        quickRecruit()
    }
}