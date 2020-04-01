/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.internal.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import org.apache.geode.cache.Cache;
import org.apache.geode.logging.internal.log4j.api.LogService;

public class TXLastEventInTransactionUtils {
  private static final Logger logger = LogService.getLogger();

  /**
   *
   * @param callbacks list of events belonging to a transaction
   * @return list containing typically the last event of the
   *         transaction taken from the events passed.
   *         Returns an empty list if none of the senders to which the events
   *         are to be sent are configured to group transactions.
   *         In case there are senders configured to group transactions but the
   *         senders do not fulfill the conditions needed to group events, all the
   *         events passed are returned.
   */
  public static List<EntryEventImpl> getLastTransactionEvents(List<EntryEventImpl> callbacks,
      Cache cache) {
    if (callbacks.size() == 0) {
      return Collections.emptyList();
    }

    if (checkNoSendersGroupTransactionEvents(callbacks, cache)) {
      return Collections.emptyList();
    }

    if (!checkAllSendersGroupTransactionEvents(callbacks, cache)) {
      logger.warn("ERROR some senders group transaction events but others do not");
      return callbacks;
    }

    if (!checkAllEventsGoToSameSenders(callbacks)) {
      logger.warn("ERROR Not all events go to the same sender in transaction");
      return callbacks;
    }

    List events = new ArrayList<EntryEventImpl>();
    events.add(callbacks.get(callbacks.size() - 1));
    return events;
  }

  private static boolean checkNoSendersGroupTransactionEvents(List<EntryEventImpl> callbacks,
      Cache cache) {
    for (Object senderId : getSenderIdsForEvents(callbacks)) {
      if (cache.getGatewaySender((String) senderId).isGroupTransactionEvents()) {
        return false;
      }
    }
    return true;
  }

  private static boolean checkAllSendersGroupTransactionEvents(List<EntryEventImpl> callbacks,
      Cache cache) {
    for (Object senderId : getSenderIdsForEvents(callbacks)) {
      if (!cache.getGatewaySender((String) senderId).isGroupTransactionEvents()) {
        return false;
      }
    }
    return true;
  }

  private static Set<String> getSenderIdsForEvents(List<EntryEventImpl> callbacks) {
    return callbacks
        .stream()
        .map(event -> event.getRegion().getAllGatewaySenderIds())
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  private static boolean checkAllEventsGoToSameSenders(List<EntryEventImpl> callbacks) {
    List<Set> senderIdsPerEvent = callbacks
        .stream()
        .map((event) -> event.getRegion().getAllGatewaySenderIds()).collect(Collectors.toList());

    return senderIdsPerEvent.stream().distinct().count() <= 1;
  }

}
