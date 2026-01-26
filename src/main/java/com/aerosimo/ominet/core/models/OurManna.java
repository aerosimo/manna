/******************************************************************************
 * This piece of work is to enhance manna project functionality.              *
 *                                                                            *
 * Author:    eomisore                                                        *
 * File:      OurManna.java                                                   *
 * Created:   26/01/2026, 13:23                                               *
 * Modified:  26/01/2026, 13:23                                               *
 *                                                                            *
 * Copyright (c)  2026.  Aerosimo Ltd                                         *
 *                                                                            *
 * Permission is hereby granted, free of charge, to any person obtaining a    *
 * copy of this software and associated documentation files (the "Software"), *
 * to deal in the Software without restriction, including without limitation  *
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,   *
 * and/or sell copies of the Software, and to permit persons to whom the      *
 * Software is furnished to do so, subject to the following conditions:       *
 *                                                                            *
 * The above copyright notice and this permission notice shall be included    *
 * in all copies or substantial portions of the Software.                     *
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,            *
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES            *
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                   *
 * NONINFINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT                 *
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,               *
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING               *
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE                 *
 * OR OTHER DEALINGS IN THE SOFTWARE.                                         *
 *                                                                            *
 ******************************************************************************/

package com.aerosimo.ominet.core.models;

import com.aerosimo.ominet.dao.mapper.MannaDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OurManna {

    private static final Logger log = LogManager.getLogger(OurManna.class.getName());

    public static void updateManna(){
        String apiUrl, passage, verse, version, feedback;
        BufferedReader br;
        StringBuilder response;
        HttpURLConnection conn;
        URL url;
        int responseCode;
        JSONObject data;
        JSONObject manna;
        try {
            apiUrl = "https://beta.ourmanna.com/api/v1/get?format=json&order=daily";
            url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                manna = new JSONObject(response.toString());
                data = manna.getJSONObject("data");
                passage = data.getString("text");
                verse = data.getString("reference");
                version = data.getString("version");
                feedback = MannaDAO.saveManna(passage, verse, version);
                log.info("{} in updating manna for today with: {} {} {}", feedback, passage, verse, version);
            } else {
                log.error("Failed to fetch data for today's manna with HTTP Code: {}", responseCode);
                try {
                    Spectre.recordError("TE-20003", "Failed to fetch data for today's manna with HTTP Code: " + responseCode, OurManna.class.getName());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            conn.disconnect();
        } catch (Exception err) {
            log.error("OurManna service failed with adaptor error {}", String.valueOf(err));
            try {
                Spectre.recordError("TE-20004", "OurManna service failed with adaptor error " + err, OurManna.class.getName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}