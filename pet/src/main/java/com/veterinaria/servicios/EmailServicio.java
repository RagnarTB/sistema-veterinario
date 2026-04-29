package com.veterinaria.servicios;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServicio {

    private final JavaMailSender mailSender;

    public EmailServicio(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCorreoConfirmacion(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@veterinaria.com");
            message.setTo(toEmail);
            message.setSubject("Activa tu cuenta de Cliente - VetCare");

            String urlConfirmacion = "http://localhost:4200/confirmar?token=" + token;

            message.setText("Hola,\n\n"
                    + "Tu registro en nuestra clínica ha comenzado. "
                    + "Por favor, haz clic en el siguiente enlace para crear tu contraseña y activar tu cuenta:\n\n"
                    + urlConfirmacion + "\n\n"
                    + "Si no solicitaste este correo, puedes ignorarlo.");

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error enviando el correo SMTP: " + e.getMessage());
            // No bloqueamos todo si el smtp falla, pero en prod deberíamos lanzar una excepcion
        }
    }

    public void enviarCorreoRegistroCliente(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@veterinaria.com");
            message.setTo(toEmail);
            message.setSubject("Completa tu registro - VetCare");

            String urlConfirmacion = "http://localhost:4200/completar-registro?token=" + token + "&email=" + java.net.URLEncoder.encode(toEmail, "UTF-8");

            message.setText("Hola,\n\n"
                    + "Para terminar de configurar tu cuenta, haz clic en el siguiente enlace y completa tus datos:\n\n"
                    + urlConfirmacion + "\n\n"
                    + "Si no solicitaste este correo, puedes ignorarlo.");

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error enviando el correo SMTP: " + e.getMessage());
        }
    }
}
