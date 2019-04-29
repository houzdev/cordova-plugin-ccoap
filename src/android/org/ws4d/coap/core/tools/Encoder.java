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

package org.ws4d.coap.core.tools;

import java.io.UnsupportedEncodingException;

/**
 * @author Bjorn Butzin <bjoern.butzin@uni-rostock.de>
 */
public class Encoder {
	public static String ByteToString(byte[] bytes) {
		if (null != bytes) {
			try {
				return new String(bytes, "UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
		}
		return null;
	}

	public static byte[] StringToByte(String string) {
		if (null != string) {
			try {
				return string.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
		}
		return null;
	}
}