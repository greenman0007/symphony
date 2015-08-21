/*
 * Copyright (c) 2012-2015, b3log.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.symphony.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Common;
import org.json.JSONObject;

/**
 * Geography utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 21, 2015
 * @since 1.3.0
 */
public final class Geos {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Geos.class.getName());

    /**
     * Gets province, city of the specified IP.
     *
     * @param ip the specified IP
     * @return address info, for example      <pre>
     * {
     *     "province": "",
     *     "city": ""
     * }
     * </pre>, returns {@code null} if not found
     */
    public static JSONObject getAddress(final String ip) {
        final String ak = Symphonys.get("baidu.lbs.ak");

        if (StringUtils.isBlank(ak) || !Networks.isIPv4(ip)) {
            return null;
        }

        HttpURLConnection conn = null;

        try {
            final URL url = new URL("http://api.map.baidu.com/location/ip?ip=" + ip
                    + "&ak=" + ak);

            conn = (HttpURLConnection) url.openConnection();
            final BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line = null;

            final StringBuilder sb = new StringBuilder();
            while (null != (line = bufferedReader.readLine())) {
                sb.append(line);
            }

            final JSONObject data = new JSONObject(sb.toString());
            if (0 != data.optInt("status")) {
                return null;
            }

            final JSONObject content = data.optJSONObject("content");
            final JSONObject addressDetail = content.optJSONObject("address_detail");
            final String province = addressDetail.optString(Common.PROVINCE);
            final String city = addressDetail.optString(Common.CITY);

            final JSONObject ret = new JSONObject();
            ret.put(Common.PROVINCE, province);
            ret.put(Common.CITY, city);

            return ret;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Can't get location [ip=" + ip + "]", e);

            return null;
        } finally {
            if (null != conn) {
                try {
                    conn.disconnect();
                } catch (final Exception e) {
                    LOGGER.log(Level.ERROR, "Close HTTP connection error", e);
                }
            }
        }
    }

    /**
     * Private constructor.
     */
    private Geos() {
    }
}