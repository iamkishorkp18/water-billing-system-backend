package water_billing_platform.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendInvoiceEmail(String toEmail, String subject, String body, byte[] pdfBytes, String fileName)
            throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);

        helper.setSubject(subject);

        helper.setText(body);

        helper.addAttachment(
                fileName,
                new org.springframework.core.io.ByteArrayResource(pdfBytes)
        );

        mailSender.send(message);
    }

    public void sendSimpleEmail(String toEmail, String subject, String body) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, false);

        helper.setTo(toEmail);

        helper.setSubject(subject);

        helper.setText(body);

        try {
            helper.setFrom("kishorkishukp18@gmail.com", "AquaLedger");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new MessagingException("Unable to set sender name.", e);
        }

        mailSender.send(message);
    }
}