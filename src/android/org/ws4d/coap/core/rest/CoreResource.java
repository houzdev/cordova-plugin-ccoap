/* Copyright 2016 University of Rostock
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

package org.ws4d.coap.core.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ws4d.coap.core.CoapConstants;
import org.ws4d.coap.core.enumerations.CoapMediaType;
import org.ws4d.coap.core.rest.api.CoapResource;
import org.ws4d.coap.core.rest.api.ResourceServer;
import org.ws4d.coap.core.tools.Encoder;

/**
 * Well-Known CoRE support (rfc6690 - ietf-core-link-format)
 * 
 * @author Nico Laum <nico.laum@uni-rostock.de>
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Bjorn Butzin <bjoern.butzin@uni-rostock.de>
 */
public class CoreResource extends BasicCoapResource {
	private static final String uriPath = "/.well-known/core";
	private static final CoapMediaType mediaType = CoapMediaType.link_format;
	private ResourceServer server = null;

	/** stores the size of the last non-filtered core link string */
	private int lastSize = -1;

	public CoreResource(ResourceServer server) {
		super(uriPath, "", mediaType);
		this.setReadable(true);
		this.setPostable(false);
		this.setPutable(false);
		this.setDeletable(false);
		this.setObservable(true);
		this.server = server;
	}

	@Override
	public synchronized CoapData get(List<CoapMediaType> mediaTypesAccepted) {
		return new CoapData(Encoder.StringToByte(buildCoreString(null)), CoapMediaType.link_format);
	}

	@Override
	public synchronized CoapData get(List<String> queries, List<CoapMediaType> mediaTypesAccepted) {
		return new CoapData(Encoder.StringToByte(buildCoreString(queries)), CoapMediaType.link_format);
	}

	/**
	 * Creates the core link format string out of the registered resources with
	 * respect to the query parameters.
	 * 
	 * @param queries
	 *            - The list query strings to filter the results.
	 * @return
	 */
	private String buildCoreString(List<String> queries) {
		// set up filters
		Set<String> rtFilter = new HashSet<String>();
		Set<String> ifFilter = new HashSet<String>();
		Set<String> hrefFilter = new HashSet<String>();

		if (null != queries) {
			// query parameter needs to be decoded from URL encoding
			for (String query : queries) {
				try {
					query = URLDecoder.decode(query, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					break;
				}
				// each query parameter can contain spaces to separate
				// individual values
				// parameter values need to be split to test against them later
				if (query.startsWith("rt="))
					for (String part : query.substring(3).split(" ")) {
						rtFilter.add(part);
					}
				if (query.startsWith("if="))
					for (String part : query.substring(3).split(" ")) {
						ifFilter.add(part);
					}
				if (query.startsWith("href="))
					for (String part : query.substring(5).split(" ")) {
						hrefFilter.add(part);
					}
			}
		}

		Map<String, CoapResource> resources = this.server.getResources();
		// used to optimize string builder behavior; '+' as string append would
		// render less optimal
		StringBuilder returnString = new StringBuilder();
		boolean first = true;

		for (CoapResource resource : resources.values()) {
			// meets the filter?
			if (matchFilter(rtFilter, resource.getResourceType())
					&& matchFilter(ifFilter, resource.getInterfaceDescription())
					&& matchFilter(hrefFilter, resource.getPath())) {

				// add ',' if this is not the first entry
				if (!first) {
					returnString.append(", ");
				} else {
					first = false;
				}
				// resource path
				returnString.append("<");
				returnString.append(resource.getPath());
				returnString.append(">");

				// resource tags
				Iterator<String> it = resource.getTags().keySet().iterator();
				while(it.hasNext()){
					String key = it.next();
					String value = resource.getTags().get(key);
					returnString.append("; ");
					returnString.append(key);
					if(null != value){
						returnString.append("=\"");
						returnString.append(value);
						returnString.append("\"");
					}
				}
				// size estimate to be displayed?
				// only display sz when larger than MTU
				if (resource.getSizeEstimate() > CoapConstants.COAP_PAYLOAD_SIZE_MAX) {
					returnString.append("; sz=\"");
					returnString.append(resource.getSizeEstimate());
					returnString.append("\"");
				}
			}
		}
		String result = returnString.toString();
		if (null == queries) {
			this.lastSize = result.length();
		}
		return result;
	}

	/**
	 * Checks if all words out of the filter set are contained in the string.
	 * Words in the filter set ending with '*' are assumed to be prefixes.
	 * 
	 * @param filterSet
	 *            - The list of words to be contained in string.
	 * @param string
	 *            - The string to be checked against the filter set.
	 * @return true if and only if all words and prefixes out of the filter set
	 *         are contained in the string
	 */
	private static boolean matchFilter(Set<String> filterSet, String string) {
		// is there a filter
		if (!filterSet.isEmpty()) {

			// null can not match any filter
			if (null == string)
				return false;

			// gather individual space separated entries
			Set<String> words = new HashSet<String>();
			for (String word : string.split(" ")) {
				words.add(word);
			}

			// every filter needs to be fulfilled
			for (String filter : filterSet) {
				if (!filter.endsWith("*")) { // '*' at the end indicate prefix
												// filter
					// if no prefix filter -> compare full words
					if (!words.contains(filter))
						return false; // no match contained
				} else {
					// if prefix filter -> compare if any word starts with
					// prefix
					boolean match = false;
					for (String word : words) {
						if (word.startsWith(filter.substring(0, filter.length() - 1))) {
							match = true;
							break;
						}
					}
					if (!match)
						return false; // no word has matched the prefix-filter
				}
			} // go on with next filter
		}
		return true; // met all filters otherwise we would already have returned
						// false
	}

	@Override
	public synchronized boolean post(byte[] data, CoapMediaType type) {
		/* nothing happens in case of a post */
		return true;
	}

	@Override
	public synchronized boolean put(byte[] data, CoapMediaType type) {
		/* nothing happens in case of a post */
		return true;
	}

	@Override
	public synchronized int getSizeEstimate() {
		if (this.lastSize < 0) {
			// only the case on startup
			// otherwise the lastSize is set by the last call of buildCoreString
			this.lastSize = 0; // lastSize need to be set to 0 to prevent
								// infinite recursion
			buildCoreString(null); // init last size value
		}
		return this.lastSize;
	}
}