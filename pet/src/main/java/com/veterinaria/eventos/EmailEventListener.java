package com.veterinaria.eventos;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.veterinaria.servicios.EmailServicio;

@Component
public class EmailEventListener {

    private final EmailServicio emailServicio;

    public EmailEventListener(EmailServicio emailServicio) {
        this.emailServicio = emailServicio;
    }

    @Async
    @EventListener
    public void handleRegistroCorreoEvent(RegistroCorreoEvent event) {
        emailServicio.enviarCorreoRegistroCliente(event.email(), event.tokenStr());
    }
}
