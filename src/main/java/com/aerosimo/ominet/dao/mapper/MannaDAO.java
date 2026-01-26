/******************************************************************************
 * This piece of work is to enhance manna project functionality.              *
 *                                                                            *
 * Author:    eomisore                                                        *
 * File:      MannaDAO.java                                                   *
 * Created:   26/01/2026, 13:50                                               *
 * Modified:  26/01/2026, 13:50                                               *
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

package com.aerosimo.ominet.dao.mapper;

import com.aerosimo.ominet.core.config.Connect;
import com.aerosimo.ominet.core.models.Spectre;
import com.aerosimo.ominet.dao.impl.MannaResponseDTO;
import oracle.jdbc.OracleTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class MannaDAO {

    private static final Logger log = LogManager.getLogger(MannaDAO.class.getName());

    public static String saveManna(String passage, String verse, String version) {
        log.info("Preparing to save new daily bread from OurManna to database");
        String response;
        String sql = "{call manna_pkg.saveManna(?,?,?,?,?)}";
        try (Connection con = Connect.dbase();
             CallableStatement stmt = con.prepareCall(sql)) {
            stmt.setString(1, passage);
            stmt.setString(2, verse);
            stmt.setString(3, version);
            stmt.setString(4, "OurManna");
            stmt.registerOutParameter(5, Types.VARCHAR);
            stmt.execute();
            response = stmt.getString(5);
            if(response.equalsIgnoreCase("success")){
                log.info("Successfully write daily bread update from OurManna to database");
                return response;
            } else {
                log.error("Fail to save daily bread update from OurManna to database");
                return response;
            }
        } catch (SQLException err) {
            log.error("Error in manna_pkg (SAVE MANNA)", err);
            try {
                Spectre.recordError("TE-20001", "Error in manna_pkg (SAVE MANNA): " + err.getMessage(), MannaDAO.class.getName());
                response = "internal server error";
                return response;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static MannaResponseDTO dailyBread() {
        log.info("Preparing to retrieve daily bread details");
        MannaResponseDTO response = null;
        String sql = "{call manna_pkg.dailyBread(?)}";
        try (Connection con = Connect.dbase();
             CallableStatement stmt = con.prepareCall(sql)) {
            stmt.registerOutParameter(1, OracleTypes.CURSOR);
            stmt.execute();
            try (ResultSet rs = (ResultSet) stmt.getObject(2)) {
                if (rs != null && rs.next()) {
                    response = new MannaResponseDTO();
                    response.setPassage(rs.getString("passage"));
                    response.setVerse(rs.getString("verse"));
                    response.setVersion(rs.getString("version"));
                    response.setModifiedBy(rs.getString("modifiedBy"));
                    response.setModifiedDate(rs.getString("modifiedDate"));
                }
            }
        } catch (SQLException err) {
            log.error("Error in manna_pkg (GET DAILY BREAD)", err);
            try {
                Spectre.recordError("TE-20001", err.getMessage(), MannaDAO.class.getName());
            } catch (Exception ignored) {}
        }
        return response;
    }
}