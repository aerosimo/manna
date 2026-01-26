/******************************************************************************
 * This piece of work is to enhance manna project functionality.              *
 *                                                                            *
 * Author:    eomisore                                                        *
 * File:      MannaREST.java                                                  *
 * Created:   26/01/2026, 14:15                                               *
 * Modified:  26/01/2026, 14:15                                               *
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

package com.aerosimo.ominet.api;

import com.aerosimo.ominet.core.models.OurManna;
import com.aerosimo.ominet.core.models.Spectre;
import com.aerosimo.ominet.dao.impl.APIResponseDTO;
import com.aerosimo.ominet.dao.impl.MannaResponseDTO;
import com.aerosimo.ominet.dao.mapper.MannaDAO;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MannaREST {

    private static final Logger log = LogManager.getLogger(MannaREST.class.getName());

    @POST
    @Path("/overhaul")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveHoroscope() {
        try{
            OurManna.updateManna();
            log.info("Successfully initiate the process of getting daily bread.");
            return Response.ok(new APIResponseDTO("success", "manna (daily bread) update initiated successfully")).build();
        } catch (Exception err) {
            log.error("❌ Error while updating manna (daily bread): {}", err.getMessage(), err);
            try {
                Spectre.recordError("TE-20001", "❌ Error while updating manna " + err.getMessage(), MannaREST.class.getName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return Response.serverError()
                    .entity(new APIResponseDTO("unsuccessful", "internal server error"))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @GET
    @Path("/dailybread")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHoroscope() {
        MannaResponseDTO resp = MannaDAO.dailyBread();
        if (resp == null || resp.getPassage() == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new APIResponseDTO("unsuccessful", "no manna found"))
                    .build();
        }
        return Response.ok(resp).build();
    }
}