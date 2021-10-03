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

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import java.net.URISyntaxException
import android.webkit.WebChromeClient




@SuppressLint("SetJavaScriptEnabled")
class RecruiterView : WebView{

    private val recruiterUrl = "https://gamewith.tw/monsterstrike/lobby"

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        privateBrowsing: Boolean
    ) : super(context, attrs, defStyleAttr, privateBrowsing)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP) constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    /**
     * 包裝成 設置Node改變左嘅監聽器 嘅JS程序
     * */
    private fun wrapSetNodeChangedListener(
        listenClassName: String,
        attributeName: String,
        infinityLoop: Boolean = false,
        runJsThenListened: ()->String
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
                        ${runJsThenListened()}
                        if(!$infinityLoop){clearInterval(timer);}
                    }
                }, 1000);
            });
        """
    }

    /**
     * 響WebView執行JavaScript程序
     *
     * 發現如直接寫JS程序
     * 唔加setTimeout()
     * 會執行無效
     * */
    private fun runJS(delay: Int = 0, jsCode: ()->String) {
        loadUrl("""javascript:window.setTimeout(function(){${jsCode()}}, ${delay});""")
    }

    /**
     * 選擇招募條件
     * */
    fun selectRecruitConditions(participationCondition: ParticipationCondition) {
        runJS{
            """document.getElementsByName("js-recruiting-tags")[${participationCondition.ordinal}].checked = true;"""
        }
    }

    /**
     * 填上招募訊息
     * */
    fun pasteRecruit(recruitText: String) {
        runJS{
            """document.getElementsByClassName("js-recruiting-line-message")[0].value = "${recruitText}";"""
        }
    }

    /**
     * 進入返遊戲
     * */
    private fun returnGame() {
        runJS(1000){
            wrapSetNodeChangedListener("js-recruiting-modal-content-loading", "style"){
                """document.getElementsByClassName("js-recruiting-modal-launch")[0].click();"""
            }
        }
    }

    /**
     * 進行招募
     * */
    fun executeRecruit() {
        runJS{
            wrapSetNodeChangedListener("js-recruiting-conditions", "style") {
                """document.getElementsByClassName("js-recruiting-button")[0].click();"""
            }
        }
        //runJS("""document.getElementsByClassName("js-recruiting-button")[0].click();""", 1000)
        returnGame()
    }

    /**
     * 還原招募器頁面
     * */
    fun restore() {
        loadUrl(recruiterUrl)
    }

    /**
     * 當 招募器 載入完成
     * */
    var onPageFinished: ((webView: WebView, url: String)->Unit)? = null

    var onReturnGame: ((webView: WebView?, url: String?)->Unit)? = null

    /**
     * 初始化 参加招募者團隊 按鈕
     * */
    private fun initParticipateButton() {
        runJS {
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
        }
    }

    /**
     *
     * */
    private fun testJsIsWork(){
        runJS{"""alert("Hello! I am an alert box!");"""}
    }

    /**
     * 初始化WebView
     * */
    init {
        settings.domStorageEnabled = true
        settings.javaScriptEnabled = true//設定同JavaScript互Call權限
        settings.javaScriptCanOpenWindowsAutomatically = true//設定允許畀JavaScript彈另一個window
        webViewClient = object : WebViewClient() {

            override fun onPageFinished(webView: WebView, url: String) {
                super.onPageFinished(webView, url)
                //初始化 参加招募者團隊 按鈕
                initParticipateButton()
                //執行外部onPageFinished程序
                onPageFinished?.invoke(webView, url)
            }

            override fun shouldOverrideUrlLoading(webView: WebView?, url: String?): Boolean {
                if (url!!.startsWith("intent://")) {
                    //Toast.makeText(this@MainActivity, url, Toast.LENGTH_SHORT).show()
                    try {
                        val context = webView?.context
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        if (intent != null) {
                            //
                            onReturnGame?.invoke(webView, url)
                            // 震動
                            val vibrator = context?.getSystemService(Service.VIBRATOR_SERVICE) as? Vibrator
                            vibrator?.cancel()
                            vibrator?.vibrate(longArrayOf(0, 2000), VibrationEffect.DEFAULT_AMPLITUDE)
                            // 轉到url指定程式
                            webView?.stopLoading()
                            context?.startActivity(intent)
                            val packageManager = context?.packageManager
                            val info = packageManager?.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                            if (info == null) {
                                val fallbackUrl = intent.getStringExtra("browser_fallback_url") ?: return false
                                webView?.loadUrl(fallbackUrl)
                                // or call external broswer
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl))
                                context?.startActivity(browserIntent)
                            }
                            return true
                        }
                    } catch (e: URISyntaxException) { }
                }
                return false
            }
        }
        loadUrl(recruiterUrl)
    }

    fun resume() {
        onResume()
        resumeTimers()//請注意，此調用不會暫停JavaScript。要全局暫停JavaScript，請使用pauseTimers()
    }

    fun pause() {
        onPause()
    }

    fun stop() {
        pauseTimers()//如果應用程序已暫停，這可能很有用。
    }
}