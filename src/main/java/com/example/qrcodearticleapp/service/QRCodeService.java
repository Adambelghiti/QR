package com.example.qrcodearticleapp.service;

import com.example.qrcodearticleapp.entity.Article;
import com.example.qrcodearticleapp.entity.Entrepot;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class QRCodeService {

    public Article decodeQRCode(byte[] qrCodeData) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(qrCodeData);
        BufferedImage bufferedImage = ImageIO.read(bis);

        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result result = new QRCodeReader().decode(bitmap);
            String[] parts = result.getText().split(",");

            Article article = new Article();
            article.setNom(parts[0].split(":")[1].trim());
            article.setLongueur(parts[1].split(":")[1].trim());
            article.setLargeur(parts[2].split(":")[1].trim());
            article.setHauteur(parts[3].split(":")[1].trim());
            article.setCategorie(parts[4].split(":")[1].trim());

            // Initialize Entrepot if not null
            if (parts.length > 5) {
                Entrepot entrepot = new Entrepot();
                entrepot.setNom(parts[5].split(":")[1].trim());
                article.setEntrepot(entrepot);
            } else {
                article.setEntrepot(new Entrepot());
            }

            return article;
        } catch (NotFoundException e) {
            System.out.println("There is no QR code in the image");
            return null;
        } catch (ChecksumException | FormatException e) {
            System.out.println("Error decoding QR code: " + e.getMessage());
            return null;
        }
    }
}
