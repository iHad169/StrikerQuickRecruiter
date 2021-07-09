
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

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Comparator
import java.util.function.Predicate

open class ArrayListWithListenEvent<E> : ArrayList<E>() {

    var onElementChange: (()->Unit)? = null

    override fun add(element: E): Boolean {
        val added = super.add(element)
        onElementChange?.invoke()
        return added
    }

    override fun add(index: Int, element: E) {
        super.add(index, element)
        onElementChange?.invoke()
    }

    override fun addAll(elements: Collection<E>): Boolean {
        val r = super.addAll(elements)
        onElementChange?.invoke()
        return r
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        val r = super.addAll(index, elements)
        onElementChange?.invoke()
        return r
    }

    override fun removeAt(index: Int): E {
        val element = super.removeAt(index)
        onElementChange?.invoke()
        return element
    }

    override fun remove(element: E): Boolean {
        val removed = super.remove(element)
        onElementChange?.invoke()
        return removed
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        val r = super.removeAll(elements)
        onElementChange?.invoke()
        return r
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun removeIf(filter: Predicate<in E>): Boolean {
        val r = super.removeIf(filter)
        onElementChange?.invoke()
        return r
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        super.removeRange(fromIndex, toIndex)
        onElementChange?.invoke()
    }

    override fun set(index: Int, element: E): E {
        val r = super.set(index, element)
        onElementChange?.invoke()
        return r
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun sort(c: Comparator<in E>?) {
        super.sort(c)
        onElementChange?.invoke()
    }

    override fun clear() {
        super.clear()
        onElementChange?.invoke()
    }
}


/*
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

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Comparator
import java.util.function.Predicate

open class ArrayListWithListenEvent<E> : LinkedHashSet<E>() {

    var onElementChange: (()->Unit)? = null

    override fun add(element: E): Boolean {
        val added = super.add(element)
        onElementChange?.invoke()
        return added
    }

    override fun addAll(elements: Collection<E>): Boolean {
        val r = super.addAll(elements)
        onElementChange?.invoke()
        return r
    }

    override fun remove(element: E): Boolean {
        val removed = super.remove(element)
        onElementChange?.invoke()
        return removed
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        val r = super.removeAll(elements)
        onElementChange?.invoke()
        return r
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun removeIf(filter: Predicate<in E>): Boolean {
        val r = super.removeIf(filter)
        onElementChange?.invoke()
        return r
    }

    override fun clear() {
        super.clear()
        onElementChange?.invoke()
    }
}

*/