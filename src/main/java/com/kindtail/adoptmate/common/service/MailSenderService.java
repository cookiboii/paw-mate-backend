package com.kindtail.adoptmate.common.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MailSenderService {
    private final JavaMailSender mailSender;

    public MailSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public String  joinMail (String email) throws MessagingException {
        String setFrom = "luo1998@gmail.com";
        String  id = UUID.randomUUID().toString().replace("-", "").substring(0,8);  //인증 번호 랜덤 메서드 이걸한이유가 겹치는게 없어서 이걸 했음
        String toMail =email;
        String title = " 회원가입 인증 이메일";
        String content = "홈페이지 가입을 신청해 주셔서 감사합니다." +
                "<br><br>" +
                "인증 번호는 <strong>" + id+ "</strong> 입니다. <br>" +
                "해당 인증 번호를 인증번호 확인란에 기입해 주세요.";

        mailSend(setFrom, toMail, title, content);

        return id;

    }

    private void mailSend(String setFrom, String toMail, String title, String content)throws MessagingException {

         MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "utf-8");
        mimeMessage.setFrom(setFrom);
        mimeMessage.setSubject(title);
        helper.setTo(toMail);
        helper.setText(content, true);
        mailSender.send(mimeMessage);



    }

    public void sendAuthCode (String email, String authCode) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
        helper.setTo(email);
        helper.setSubject("[adopmate] 비밀번호 재설정 인증코드 안내");
        String content = """
        <html>
            <body>
                <h3>비밀번호 재설정을 위한 인증 코드입니다.</h3>
                <p>아래의 인증 코드를 입력해 주세요:</p>
                <div style="font-size: 24px; font-weight: bold; color: #2E86C1; margin-top: 10px;">
                    %s
                </div>
                <p style="margin-top: 20px; color: #555;">※ 인증 코드는 5분 동안만 유효합니다.</p>
            </body>
        </html>
    """.formatted(authCode);

        helper.setText(content, true); // true → HTML 사용

        mailSender.send(mimeMessage);

    }

}
