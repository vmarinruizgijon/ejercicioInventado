package com.example.demo;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class QrService {

    public String generarQrBase64(String texto) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            // Generamos una matriz de 250x250 píxeles
            BitMatrix bitMatrix = qrCodeWriter.encode(texto, BarcodeFormat.QR_CODE, 250, 250);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            // Convertimos los bytes de la imagen a una cadena Base64
            byte[] imagenBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(imagenBytes);
            
        } catch (Exception e) {
            return null;
        }
    }
}