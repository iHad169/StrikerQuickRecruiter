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

import android.content.Context

/**
 * 招募條件
 * */
enum class ParticipationCondition(val resourceId: Int) {
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