package com.example.notification_service.service;

import com.example.notification_service.dto.DepositEvent;
import com.example.notification_service.dto.PaymentEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendDepositSuccessEmail(DepositEvent event) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(event.getEmail());
        helper.setSubject("🎉 [Xác nhận] Nạp tiền vào ví thành công!");

        // Định dạng tiền tệ VND & Ngày tháng
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedAmount = currencyFormatter.format(event.getAmount());

        // Template Email dạng HTML
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px;">
                <h2 style="color: #2e7d32; text-align: center;">Thông Báo Nạp Tiền Thành Công</h2>
                <p>Xin chào <strong>%s</strong>,</p>
                <p>Giao dịch nạp tiền của bạn đã được hệ thống xử lý thành công. Dưới đây là thông tin chi tiết giao dịch:</p>
                
                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                    <tr style="background-color: #f9f9f9;">
                        <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Mã giao dịch:</strong></td>
                        <td style="padding: 10px; border-bottom: 1px solid #ddd; text-align: right;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Số tiền nạp:</strong></td>
                        <td style="padding: 10px; border-bottom: 1px solid #ddd; text-align: right; color: #2e7d32; font-weight: bold;">+%s</td>
                    </tr>
                    <tr style="background-color: #f9f9f9;">
                        <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Số dư khả dụng:</strong></td>
                        <td style="padding: 10px; border-bottom: 1px solid #ddd; text-align: right; font-weight: bold;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Thời gian:</strong></td>
                        <td style="padding: 10px; border-bottom: 1px solid #ddd; text-align: right;">%s</td>
                    </tr>
                </table>

                <p style="font-size: 0.9em; color: #666;">Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!</p>
            </div>
            """.formatted(
                event.getUserName(),
                event.getTransactionId(),
                formattedAmount,
                event.getCurrentBalance(),
                event.getTime()
        );

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    public void sendPaymentSuccessEmail(PaymentEvent event) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(event.getEmail());
        helper.setSubject("🎉 [Xác nhận] Thanh toán thành công!");

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedAmount = currencyFormatter.format(event.getAmount());

        String htmlContent = """
            <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px;">
                <h2 style="color: #1565c0; text-align: center;">Thông Báo Thanh Toán Thành Công</h2>
                <p>Xin chào <strong>%s</strong>,</p>
                <p>Giao dịch thanh toán của bạn đã được hệ thống xử lý thành công. Dưới đây là thông tin chi tiết:</p>

                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                    <tr style="background-color: #f9f9f9;">
                        <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Mã giao dịch:</strong></td>
                        <td style="padding: 10px; border-bottom: 1px solid #ddd; text-align: right;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Số tiền thanh toán:</strong></td>
                        <td style="padding: 10px; border-bottom: 1px solid #ddd; text-align: right; color: #1565c0; font-weight: bold;">-%s</td>
                    </tr>
                    <tr style="background-color: #f9f9f9;">
                        <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Số dư khả dụng:</strong></td>
                        <td style="padding: 10px; border-bottom: 1px solid #ddd; text-align: right; font-weight: bold;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 10px; border-bottom: 1px solid #ddd;"><strong>Thời gian:</strong></td>
                        <td style="padding: 10px; border-bottom: 1px solid #ddd; text-align: right;">%s</td>
                    </tr>
                </table>

                <p style="font-size: 0.9em; color: #666;">Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!</p>
            </div>
            """.formatted(
                event.getUserName(),
                event.getTransactionId(),
                formattedAmount,
                event.getCurrentBalance(),
                event.getTime()
        );

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}