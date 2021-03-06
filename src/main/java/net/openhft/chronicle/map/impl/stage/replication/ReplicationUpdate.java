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

package net.openhft.chronicle.map.impl.stage.replication;

import net.openhft.chronicle.hash.impl.stage.entry.SegmentStages;
import net.openhft.chronicle.hash.impl.stage.hash.CheckOnEachPublicOperation;
import net.openhft.chronicle.hash.replication.RemoteOperationContext;
import net.openhft.chronicle.map.impl.ReplicatedChronicleMapHolder;
import net.openhft.chronicle.map.impl.stage.entry.ReplicatedMapEntryStages;
import net.openhft.sg.Stage;
import net.openhft.sg.StageRef;
import net.openhft.sg.Staged;

@Staged
public abstract class ReplicationUpdate<K> implements RemoteOperationContext<K> {
    @StageRef SegmentStages s;
    @StageRef ReplicatedMapEntryStages<K, ?, ?> e;
    @StageRef ReplicatedChronicleMapHolder<?, ?, ?, ?, ?, ?, ?> mh;
    @StageRef CheckOnEachPublicOperation checkOnEachPublicOperation;

    @Stage("ReplicationUpdate") public long innerRemoteTimestamp;
    @Stage("ReplicationUpdate") public byte innerRemoteIdentifier = (byte) 0;

    public abstract boolean replicationUpdateInit();

    public void initReplicationUpdate(long timestamp, byte identifier) {
        innerRemoteTimestamp = timestamp;
        if (identifier == 0)
            throw new IllegalStateException("identifier can't be 0");
        innerRemoteIdentifier = identifier;
    }
    
    public void dropChange() {
        mh.m().dropChange(s.segmentIndex, e.pos);
    }

    public void moveChange(long oldPos, long newPos) {
        mh.m().moveChange(s.segmentIndex, oldPos, newPos, e.timestamp());
    }
    
    public void updateChange() {
        if (!replicationUpdateInit()) {
            raiseChange();
        }
    }

    public void raiseChange() {
        mh.m().raiseChange(s.segmentIndex, e.pos, e.timestamp());
    }

    @Override
    public long remoteTimestamp() {
        checkOnEachPublicOperation.checkOnEachPublicOperation();
        return innerRemoteTimestamp;
    }

    @Override
    public byte remoteIdentifier() {
        checkOnEachPublicOperation.checkOnEachPublicOperation();
        return innerRemoteIdentifier;
    }
}
