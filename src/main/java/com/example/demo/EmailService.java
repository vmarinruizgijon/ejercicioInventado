package com.example.demo;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarNotaEnPdf(String correoDestino, Nota nota) throws Exception {
        
        // 1. Crear el PDF en memoria
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        
        document.open();
        Font fontTitulo = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font fontCuerpo = new Font(Font.HELVETICA, 12, Font.NORMAL);
        
        document.add(new Paragraph("Título: " + nota.getTitulo(), fontTitulo));
        document.add(new Paragraph("Fecha: " + nota.getFecha().toString(), fontCuerpo));
        document.add(new Paragraph("\n")); 
        document.add(new Paragraph(nota.getContenido(), fontCuerpo));
        document.close();

        // 2. Crear el mensaje de correo con adjunto [cite: 20, 21, 22]
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);
        
        helper.setTo(correoDestino);
        helper.setSubject("Tu nota: " + nota.getTitulo());
        helper.setText("Hola,\n\nAdjunto encontrarás tu nota en formato PDF.\n\nUn saludo!");
        helper.setFrom("victor.marin-manzanares@iesruizgijon.com"); // Reemplaza con tu correo

        // 3. Adjuntar el PDF [cite: 23]
        helper.addAttachment("Nota_" + nota.getId() + ".pdf", new ByteArrayResource(out.toByteArray()));

        // 4. Enviar [cite: 24]
        mailSender.send(mensaje);
    }
}