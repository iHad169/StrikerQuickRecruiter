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

import android.R.string
import android.graphics.Color
import android.os.Handler
import android.view.View
import android.widget.ListView


/**
 * 設置倒時器
 * */
fun setTimer(
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
 * 可以在 ScrollView 中使用 ListView
 * */
fun setListViewHeightBasedOnChildren(listView: ListView) {
    val listAdapter = listView.adapter ?: return // pre-condition
    var totalHeight = 0
    val desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.width, View.MeasureSpec.AT_MOST)
    for (i in 0 until listAdapter.count) {
        val listItem: View = listAdapter.getView(i, null, listView)
        listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
        totalHeight += listItem.getMeasuredHeight()
    }
    val params = listView.layoutParams
    params.height = totalHeight + listView.dividerHeight * (listAdapter.count - 1)
    listView.layoutParams = params
    listView.requestLayout()
}
/**
 *
 * */
fun String.rendRGB(): Int{
    val hash: Int = this.hashCode()
    val r = hash and 0xFF0000 shr 16
    val g = hash and 0x00FF00 shr 8
    val b = hash and 0x0000FF
    return Color.rgb(r, g, b)
}