/*
 *      Copyright (C) 2015  higherfrequencytrading.com
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Lesser General Public License as published by
 *      the Free Software Foundation, either version 3 of the License.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public License
 *      along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.hash.impl.stage.query;

import net.openhft.chronicle.bytes.BytesUtil;
import net.openhft.chronicle.hash.Data;
import net.openhft.chronicle.hash.impl.stage.entry.HashEntryStages;
import net.openhft.chronicle.hash.impl.stage.entry.SegmentStages;
import net.openhft.sg.Stage;
import net.openhft.sg.StageRef;
import net.openhft.sg.Staged;

import static net.openhft.chronicle.hash.impl.stage.query.KeySearch.SearchState.DELETED;
import static net.openhft.chronicle.hash.impl.stage.query.KeySearch.SearchState.PRESENT;

@Staged
public abstract class KeySearch<K> {

    @StageRef public SegmentStages s;
    @StageRef public HashLookupSearch hashLookupSearch;
    @StageRef public HashEntryStages<K> entry;

    public Data<K> inputKey = null;

    public void initInputKey(Data<K> inputKey) {
        this.inputKey = inputKey;
    }

    public enum SearchState {
        PRESENT,
        DELETED,
        ABSENT
    }

    @Stage("KeySearch") protected SearchState searchState = null;

    abstract boolean keySearchInit();

    @Stage("KeySearch")
    public void setSearchState(SearchState newSearchState) {
        this.searchState = newSearchState;
    }

    void initKeySearch() {
        for (long pos; (pos = hashLookupSearch.nextPos()) >= 0L;) {
            entry.readExistingEntry(pos);
            if (!keyEquals())
                continue;
            hashLookupSearch.found();
            keyFound();
            return;
        }
        searchState = SearchState.ABSENT;
    }

    boolean keyEquals() {
        return inputKey.size() == entry.keySize &&
                BytesUtil.bytesEqual(entry.entryBS, entry.keyOffset,
                        inputKey.bytes(), inputKey.offset(), entry.keySize);
    }

    @Stage("KeySearch")
    void keyFound() {
        searchState = PRESENT;
    }

    abstract void closeKeySearch();

    public boolean searchStatePresent() {
        return searchState == PRESENT;
    }

    public boolean searchStateDeleted() {
        return searchState == DELETED && !s.concurrentSameThreadContexts &&
                s.innerUpdateLock.isHeldByCurrentThread();
    }

    public boolean searchStateAbsent() {
        return !searchStatePresent() && !searchStateDeleted();
    }
}
