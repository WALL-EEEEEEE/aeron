/*
 *  Copyright 2014-2019 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.aeron.cluster.service;

import io.aeron.Image;
import io.aeron.Publication;
import io.aeron.cluster.codecs.CloseReason;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;

/**
 * Interface which a service must implement to be contained in the cluster.
 */
public interface ClusteredService
{
    /**
     * Start event for the service where the service can perform any initialisation required and load snapshot state.
     * The snapshot image can be null if no previous snapshot exists.
     * <p>
     * <b>Note:</b> As this is a potentially long running operation the implementation should occasional call
     * {@link Cluster#idle()} or {@link Cluster#idle(int)}, especially when polling the snapshot {@link Image}
     * returns 0.
     *
     * @param cluster with which the service can interact.
     * @param snapshotImage from which the service can load its archived state which can be null when no snapshot.
     */
    void onStart(Cluster cluster, Image snapshotImage);

    /**
     * A session has been opened for a client to the cluster.
     *
     * @param session     for the client which have been opened.
     * @param timestampMs at which the session was opened.
     */
    void onSessionOpen(ClientSession session, long timestampMs);

    /**
     * A session has been closed for a client to the cluster.
     *
     * @param session     that has been closed.
     * @param timestampMs at which the session was closed.
     * @param closeReason the session was closed.
     */
    void onSessionClose(ClientSession session, long timestampMs, CloseReason closeReason);

    /**
     * A message has been received to be processed by a clustered service.
     *
     * @param session     for the client which sent the message. This can be null if the client was a service.
     * @param timestampMs for when the message was received.
     * @param buffer      containing the message.
     * @param offset      in the buffer at which the message is encoded.
     * @param length      of the encoded message.
     * @param header      aeron header for the incoming message.
     */
    void onSessionMessage(
        ClientSession session,
        long timestampMs,
        DirectBuffer buffer,
        int offset,
        int length,
        Header header);

    /**
     * A scheduled timer has expired.
     *
     * @param correlationId for the expired timer.
     * @param timestampMs   at which the timer expired.
     */
    void onTimerEvent(long correlationId, long timestampMs);

    /**
     * The service should take a snapshot and store its state to the provided archive {@link Publication}.
     * <p>
     * <b>Note:</b> As this is a potentially long running operation the implementation should occasional call
     * {@link Cluster#idle()} or {@link Cluster#idle(int)}, especially in the event of back pressure.
     *
     * @param snapshotPublication to which the state should be recorded.
     */
    void onTakeSnapshot(Publication snapshotPublication);

    /**
     * Notify that the cluster node has changed role.
     *
     * @param newRole that the node has assumed.
     */
    void onRoleChange(Cluster.Role newRole);

    /**
     * Called when the container is going to terminate.
     *
     * @param cluster with which the service can interact.
     */
    void onTerminate(Cluster cluster);
}
