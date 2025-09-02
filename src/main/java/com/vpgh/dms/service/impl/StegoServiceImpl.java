package com.vpgh.dms.service.impl;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.vpgh.dms.service.StegoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class StegoServiceImpl implements StegoService {
    private static final String AES_CIPHER = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;
    @Value("${stego.base64-secret}")
    private String secretKey;

    @Override
    public ByteArrayOutputStream hideData(InputStream input, String content, String password) throws Exception {
        byte[] inputBytes = input.readAllBytes();
        PdfDocument pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(inputBytes)).setUnethicalReading(true));

        boolean containsImage = hasImage(pdf);
        pdf.close();

        if (containsImage) {
            return hideDataImage(new ByteArrayInputStream(inputBytes), content, password);
        } else {
            return hideDataText(new ByteArrayInputStream(inputBytes), content, password);
        }
    }

    @Override
    public String extractData(InputStream input, String password) throws Exception {
        byte[] inputBytes = input.readAllBytes();
        PdfDocument pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(inputBytes)).setUnethicalReading(true));

        boolean containsImage = hasImage(pdf);
        pdf.close();

        if (containsImage) {
            return extractDataImage(new ByteArrayInputStream(inputBytes), password);
        } else {
            return extractDataText(new ByteArrayInputStream(inputBytes), password);
        }
    }

    private ByteArrayOutputStream hideDataText(InputStream input, String content, String password) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String encrypted = encrypt(content, password);

        PdfDocument pdf = new PdfDocument(new PdfReader(input).setUnethicalReading(true), new PdfWriter(out));
        PdfPage page = pdf.getFirstPage();

        Rectangle rect = new Rectangle(0, 0, 1, 1);
        PdfFormXObject formXObject = new PdfFormXObject(rect);
        formXObject.getPdfObject().put(new PdfName("img"), new PdfString(encrypted));

        PdfName stegoName = new PdfName("img");
        PdfDictionary resources = page.getResources().getPdfObject();
        if (resources.getAsDictionary(PdfName.XObject) == null) {
            resources.put(PdfName.XObject, new PdfDictionary());
        }
        resources.getAsDictionary(PdfName.XObject).put(stegoName, formXObject.getPdfObject());

        pdf.close();
        return out;
    }

    private String extractDataText(InputStream input, String password) throws Exception {
        try (PdfDocument pdf = new PdfDocument(new PdfReader(input).setUnethicalReading(true))) {
            PdfPage page = pdf.getFirstPage();
            PdfDictionary resources = page.getResources().getPdfObject();

            PdfDictionary xObjects = resources.getAsDictionary(PdfName.XObject);
            if (xObjects != null) {
                for (PdfName name : xObjects.keySet()) {
                    PdfStream stream = xObjects.getAsStream(name);
                    if (stream != null) {
                        PdfString stegoData = stream.getAsString(new PdfName("img"));
                        if (stegoData != null) {
                            String content = stegoData.toUnicodeString();
                            return decrypt(content.trim(), password);
                        }
                    }
                }
            }
        }
        return null;
    }

    private ByteArrayOutputStream hideDataImage(InputStream input, String content, String password) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfReader(input).setUnethicalReading(true), new PdfWriter(out));

        String encrypted = encrypt(content, password);
        boolean foundImage = false;

        outerLoop:
        for (int i = 1; i <= pdf.getNumberOfPages(); i++) {
            PdfPage page = pdf.getPage(i);
            PdfDictionary resources = page.getResources().getPdfObject();
            PdfDictionary xObjects = resources.getAsDictionary(PdfName.XObject);

            if (xObjects != null) {
                for (PdfName name : xObjects.keySet()) {
                    PdfStream stream = xObjects.getAsStream(name);
                    if (stream != null && PdfName.Image.equals(stream.getAsName(PdfName.Subtype))) {
                        PdfName filter = stream.getAsName(PdfName.Filter);

                        if (PdfName.FlateDecode.equals(filter) || PdfName.JPXDecode.equals(filter)) {
                            PdfImageXObject imgObject = new PdfImageXObject(stream);
                            BufferedImage image = imgObject.getBufferedImage();
                            if (image == null) continue;

                            BufferedImage stegoImg = encodeLSB(image, encrypted);
                            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

                            if (PdfName.FlateDecode.equals(filter)) {
                                ImageIO.write(stegoImg, "png", byteOut);
                            } else if (PdfName.JPXDecode.equals(filter)) {
                                ImageIO.write(stegoImg, "jp2", byteOut);
                            }

                            PdfImageXObject newImage = new PdfImageXObject(ImageDataFactory.create(byteOut.toByteArray()));
                            xObjects.put(name, newImage.getPdfObject());

                            foundImage = true;
                            break outerLoop;
                        }
                    }
                }
            }
        }

        pdf.close();

        if (!foundImage) {
            throw new IllegalStateException("No lossless image (FlateDecode or JPXDecode) found in PDF.");
        }

        return out;
    }

    private String extractDataImage(InputStream input, String password) throws Exception {
        try (PdfDocument pdf = new PdfDocument(new PdfReader(input).setUnethicalReading(true))) {
            for (int i = 1; i <= pdf.getNumberOfPages(); i++) {
                PdfPage page = pdf.getPage(i);
                PdfDictionary res = page.getResources().getPdfObject();
                if (res == null) continue;

                PdfDictionary xObjects = res.getAsDictionary(PdfName.XObject);
                if (xObjects == null) continue;

                for (PdfName name : xObjects.keySet()) {
                    PdfStream stream = xObjects.getAsStream(name);
                    if (stream != null && PdfName.Image.equals(stream.getAsName(PdfName.Subtype))) {
                        PdfName filter = stream.getAsName(PdfName.Filter);

                        if (PdfName.FlateDecode.equals(filter) || PdfName.JPXDecode.equals(filter)) {
                            PdfImageXObject imgObject = new PdfImageXObject(stream);
                            BufferedImage img = imgObject.getBufferedImage();
                            if (img != null) {
                                String extracted = decodeLSB(img);
                                String decrypted = extracted != null ? decrypt(extracted, password) : null;
                                if (decrypted != null) {
                                    return decrypted;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private BufferedImage encodeLSB(BufferedImage image, String text) {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);

        byte[] lengthBytes = ByteBuffer.allocate(4).putInt(data.length).array();
        byte[] allData = new byte[lengthBytes.length + data.length];
        System.arraycopy(lengthBytes, 0, allData, 0, lengthBytes.length);
        System.arraycopy(data, 0, allData, lengthBytes.length, data.length);

        int totalBits = allData.length * 8;
        int width = image.getWidth();
        int height = image.getHeight();

        if (totalBits > width * height) {
            throw new IllegalArgumentException("Data too large to hide in image");
        }

        BufferedImage stegoImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int bitIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);

                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                if (bitIndex < totalBits) {
                    int byteIndex = bitIndex / 8;
                    int bitInByte = 7 - (bitIndex % 8);
                    int bit = (allData[byteIndex] >> bitInByte) & 1;

                    blue = (blue & 0xFE) | bit;
                    bitIndex++;
                }

                int newRgb = (red << 16) | (green << 8) | blue;
                stegoImage.setRGB(x, y, newRgb);
            }
        }

        return stegoImage;
    }

    private String decodeLSB(BufferedImage img) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int bitIndex = 0;
        int curByte = 0;
        int bitsCollected = 0;
        int dataLength = -1;

        int width = img.getWidth();
        int height = img.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int blue = rgb & 0xFF;
                int bit = blue & 1;

                curByte = (curByte << 1) | bit;
                bitsCollected++;

                if (bitsCollected == 8) {
                    bos.write(curByte);
                    bitsCollected = 0;
                    curByte = 0;

                    if (dataLength == -1 && bos.size() == 4) {
                        byte[] lenBytes = bos.toByteArray();
                        dataLength = ByteBuffer.wrap(lenBytes).getInt();
                        bos.reset();
                    }

                    if (dataLength != -1 && bos.size() == dataLength) {
                        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
                    }
                }
            }
        }

        return null;
    }

    private boolean hasImage(PdfDocument pdfDoc) {
        for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
            PdfPage page = pdfDoc.getPage(i);
            PdfDictionary resources = page.getResources().getPdfObject();
            if (resources == null) continue;

            PdfDictionary xObjects = resources.getAsDictionary(PdfName.XObject);
            if (xObjects == null) continue;

            for (PdfName name : xObjects.keySet()) {
                PdfStream xObject = xObjects.getAsStream(name);
                if (xObject == null) continue;

                PdfName subtype = xObject.getAsName(PdfName.Subtype);
                PdfName filter = xObject.getAsName(PdfName.Filter);

                if (PdfName.Image.equals(subtype) && (PdfName.FlateDecode.equals(filter) || PdfName.JPXDecode.equals(filter))) {
                    return true;
                }
            }
        }
        return false;
    }

    private SecretKeySpec createKey(String password) throws Exception {
        byte[] decodedSecret = Base64.getDecoder().decode(secretKey);

        ByteArrayOutputStream keyStream = new ByteArrayOutputStream();
        keyStream.write(decodedSecret);
        if (password != null) {
            keyStream.write(password.getBytes(StandardCharsets.UTF_8));
        }

        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] hashed = sha.digest(keyStream.toByteArray());
        byte[] finalKey = Arrays.copyOf(hashed, 16);

        return new SecretKeySpec(finalKey, "AES");
    }

    private String encrypt(String data, String password) throws Exception {
        SecretKeySpec secretKey = createKey(password);
        Cipher cipher = Cipher.getInstance(AES_CIPHER);

        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    private String decrypt(String encryptedData, String password) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedData);

        if (combined.length < IV_LENGTH) return null;

        byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
        byte[] encrypted = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

        SecretKeySpec secretKey = createKey(password);
        Cipher cipher = Cipher.getInstance(AES_CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        try {
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (BadPaddingException e) {
            return null;
        }
    }

}
