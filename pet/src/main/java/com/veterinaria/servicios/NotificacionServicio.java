package com.veterinaria.servicios;

import java.time.LocalDate;
import java.util.List;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.veterinaria.modelos.Vacuna;
import com.veterinaria.respositorios.VacunaRepositorio;

@Service
public class NotificacionServicio {
    private final VacunaRepositorio vacunaRepositorio;
    private final JavaMailSender mailSender;

    public NotificacionServicio(VacunaRepositorio vacunaRepositorio, JavaMailSender mailSender) {
        this.vacunaRepositorio = vacunaRepositorio;
        this.mailSender = mailSender;
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void enviarRecordatoriosVacunas() {
        LocalDate fechaObjetivo = LocalDate.now().plusDays(3);
        List<Vacuna> vacunasAVencer = vacunaRepositorio.findByFechaProximaDosis(fechaObjetivo);
        for (Vacuna v : vacunasAVencer) {
            if (v.getPaciente().getCliente().getUsuario() != null && v.getPaciente().getCliente().getUsuario().getEmail() != null) {
                enviarEmail(v.getPaciente().getCliente().getUsuario().getEmail(),
                        "Recordatorio de Vacuna - Clínica Veterinaria",
                        "Hola, te recordamos que a " + v.getPaciente().getNombre()
                                + " le toca su vacuna (" + v.getNombreVacuna() + ") el " + fechaObjetivo
                                + ". ¡Te esperamos!");
            }
        }
    }

    private void enviarEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}