/* Copyright 2015 University of Rostock
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/

package org.ws4d.coap.core.tools;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This class provides a thread safe hash map implementation that automatically removes
 * added items after a fixed amount of miliseconds.
 * 
 * @author Bjorn Butzin <bjoern.butzin@uni-rostock.de>
 *
 * @param <K>
 *            the data type to be used as hash map key
 * @param <V>
 *            the data type to be used as hash map value
 */
public class TimeoutHashMap<K, V> implements Map<K, V> {

	/**
	 * This map that actually keeps the key value pairs. The original value is
	 * wrapped into a TimedEntry. A TimedEntry enhances the original value with
	 * the point in time when the item has to be deleted. This point in time is
	 * represented as milliseconds since the same point in time as used by
	 * System.currentTimeMillis().
	 */
	private Map<K, TimedEntry<V>> map = new Hashtable<K, TimedEntry<V>>();

	/**
	 * the timeout of every new entry in ms
	 */
	private Long timeout;

	/**
	 * this timer calls the update method when the next entry is timed out
	 */
	private Thread thread = new Thread(new TimeoutHashMapTimer(this));

	/**
	 * Creates a new TimeoutHashMap2 object
	 * 
	 * @param timeout
	 *            - the amount of milliseconds after which a newly added item
	 *            will automatically be removed
	 */
	public TimeoutHashMap(long timeout) {
		this.timeout = timeout;
		this.thread.start();
	}

	public synchronized void clear() {
		this.map.clear();
	}

	public synchronized boolean containsKey(Object key) {
		return this.map.containsKey(key);
	}

	public synchronized boolean containsValue(Object value) {
		for (TimedEntry<V> entry : this.map.values()) {
			if (entry.getValue().equals(value)) {
				return true;
			}
		}
		return false;
	}

	public synchronized Set<java.util.Map.Entry<K, V>> entrySet() {
		Set<Entry<K, V>> set = new HashSet<Entry<K, V>>();
		for (Entry<K, TimedEntry<V>> entry : this.map.entrySet()) {
			set.add(new AbstractMap.SimpleEntry<K, V>(entry.getKey(), entry.getValue().getValue()));
		}
		return set;
	}

	public synchronized V get(Object key) {
		TimedEntry<V> e = this.map.get(key);
		if(e!= null){
			return this.map.get(key).getValue();
		}
		return null;
		
	}

	public synchronized boolean isEmpty() {
		return this.map.isEmpty();
	}

	public synchronized Set<K> keySet() {
		return this.map.keySet();
	}

	public synchronized V put(K key, V value) {
		TimedEntry<V> entry = this.map.put(key, new TimedEntry<V>(System.currentTimeMillis() + this.timeout, value));
		V old = null != entry ? entry.getValue() : null;
		// important to wake the timer, especially if map was empty before
		this.thread.interrupt();
		return old;
	}

	public synchronized void putAll(Map<? extends K, ? extends V> m) {
		for (Entry<? extends K, ? extends V> e : m.entrySet()) {
			this.put(e.getKey(), e.getValue());
		}
	}

	public synchronized V remove(Object key) {
		 TimedEntry<V> t = this.map.remove(key);
		 return null==t ? null : t.getValue();
	}

	public synchronized int size() {
		return this.map.size();
	}

	public synchronized Collection<V> values() {
		Collection<V> result = new ArrayList<V>();
		for (TimedEntry<V> entry : this.map.values()) {
			result.add(entry.getValue());
		}
		return result;
	}

	synchronized Long update() {
		long time = Long.MAX_VALUE;

		// search for next expiring entry & already expired entries
		// iterator is required to prevent concurrent modification exception
		Iterator<Entry<K, TimedEntry<V>>> it = this.map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<K, TimedEntry<V>> entry = it.next();
			long newTime = entry.getValue().getExpires() - System.currentTimeMillis();
			if (newTime < 1) {
				// Immediately remove expired entries
				it.remove();
			} else if (newTime < time) {
				// set next expire time
				time = newTime;
			}
		}
		return time;
	}

	private class TimedEntry<A> {

		private final Long expires;
		private final A value;

		TimedEntry(Long expires, A value) {
			this.expires = expires;
			this.value = value;
		}

		public Long getExpires() {
			return this.expires;
		}

		public A getValue() {
			return this.value;
		}
	}

	private class TimeoutHashMapTimer implements Runnable {

		private TimeoutHashMap<K, V> timerMap;

		TimeoutHashMapTimer(TimeoutHashMap<K, V> map) {
			this.timerMap = map;
		}

		public void run() {
			while (true) {
				if (!this.timerMap.isEmpty()) {
					long time;

					// remove expired entries & get next expire time
					time = this.timerMap.update();
					// next delete in <time> milliseconds -> sleep
					try {
						Thread.sleep(time);
					} catch (@SuppressWarnings("unused") InterruptedException e) {
						// do nothing, InterruptedException is expected on a put
					}
				} else {
					// timerMap is empty we can sleep until something is added
					// we expect an interrupt when something is added
					try {
						Thread.sleep(Long.MAX_VALUE);
					} catch (@SuppressWarnings("unused") InterruptedException e) {
						// do nothing, InterruptedException is expected on a put
					}
				}
			}
		}
	}
}
