/* Copyright (c) 2023, LegoSoft Soluciones, S.C.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are not permitted.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *  TestController.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.auditservice.web;

import com.ailegorreta.auditservice.domain.Event;
import com.ailegorreta.auditservice.domain.EventRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * This is a dummy controller for testing Webmvc purpose only, i.e., all REST calls for audit-service are done using
 * Kafka as listener (to insert Events) and GraphQL for queries. Therefore justtesting the service are enough for
 * insertion new Events and for queries use GraphqlTester class.
 *
 * TODO validate is we can use Spring Data REST for MongoDB
 *
 * So the purpose to test this controller is security and future direct REST API for this microservice
 *
 * @project: audit-service
 * @author: rlh
 * @date: September 2023
 */
@Controller
@RequestMapping("audit/tests")
public class TestController {
    private final EventRepository eventRepository;
    public TestController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @PostMapping("all")
    Iterable<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @PostMapping("telefono")
    List<Event> getEventsByTelefono(@Valid @RequestBody EventInput eventInput) {
        return eventRepository.findEventsDetail(eventInput.idUsuario, eventInput.telefono);
    }
    public record EventInput (Integer idUsuario, String telefono) {}

}