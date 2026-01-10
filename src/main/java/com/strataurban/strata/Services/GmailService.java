//package com.strataurban.strata.Services;
//
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.json.gson.GsonFactory;
//import com.google.api.services.gmail.Gmail;
//import com.google.api.services.gmail.model.Message;
//import com.google.auth.http.HttpCredentialsAdapter;
//import com.google.auth.oauth2.GoogleCredentials;
//
//import jakarta.mail.Session;
//import jakarta.mail.internet.InternetAddress;
//import jakarta.mail.internet.MimeMessage;
//import java.io.ByteArrayOutputStream;
//import java.io.FileInputStream;
//import java.util.Base64;
//import java.util.Collections;
//import java.util.Properties;
//
//public class GmailService {
//
//    public static void sendEmail(String to, String subject, String bodyText) throws Exception {
//        GoogleCredentials credentials = GoogleCredentials
//                .fromStream(new FileInputStream("credentials.json"))
//                .createScoped(Collections.singleton("https://www.googleapis.com/auth/gmail.send"));
//
//        Gmail service = new Gmail.Builder(
//                GoogleNetHttpTransport.newTrustedTransport(),
//                GsonFactory.getDefaultInstance(),
//                new HttpCredentialsAdapter(credentials))
//                .setApplicationName("StrataUrban Email Service")
//                .build();
//
//        MimeMessage email = new MimeMessage(Session.getDefaultInstance(new Properties()));
//        email.setFrom(new InternetAddress("onboarding@strataurban.com"));
//        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
//        email.setSubject(subject);
//        email.setText(bodyText);
//
//        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//        email.writeTo(buffer);
//
//        Message message = new Message();
//        message.setRaw(Base64.getUrlEncoder().encodeToString(buffer.toByteArray()));
//
//        service.users().messages().send("me", message).execute();
//        System.out.println("Email sent to " + to);
//    }
//}