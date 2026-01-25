package org.fcs.notifications.microservice.services.impl;

import org.fcs.notifications.microservice.services.ConfirmationCodeEmailTemplateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfirmationCodeEmailTemplateServiceImpl implements ConfirmationCodeEmailTemplateService {
    @Value("${app.confirmation-code.ttl-minutes}")
    private int confirmationCodeTtlMinutes;

    @Override
    public String buildHtml(String firstName, String lastName, String middleName, String confirmationCode) {
        String recipientName = displayName(firstName, middleName);
        String codeBoxes = buildCodeBoxes(confirmationCode);

        return """
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                  <meta charset="UTF-8">
                  <style>
                    body {
                      margin: 0;
                      padding: 40px 16px;
                      background:
                        radial-gradient(circle at top left, rgba(16, 45, 105, 0.08), transparent 28%%),
                        linear-gradient(180deg, #f7f9fc 0%%, #eef2f8 100%%);
                      font-family: Roboto, Arial, sans-serif;
                      color: #102d69;
                    }

                    .shell {
                      max-width: 520px;
                      margin: 0 auto;
                    }

                    .card {
                      width: 100%%;
                      box-sizing: border-box;
                      border: 1px solid rgba(16, 45, 105, 0.12);
                      border-radius: 28px;
                      padding: 36px;
                      background-color: rgba(255, 255, 255, 0.94);
                      box-shadow: 0 28px 80px rgba(16, 45, 105, 0.1);
                    }

                    .title {
                      margin: 0;
                      font-size: 36px;
                      line-height: 1.05;
                      letter-spacing: -0.03em;
                      font-weight: 700;
                      color: #102d69;
                    }

                    .description {
                      margin: 12px 0 0;
                      color: #56637f;
                      font-size: 16px;
                      line-height: 1.5;
                    }

                    .code-table {
                      margin: 32px auto;
                      border-collapse: separate;
                      border-spacing: 10px 0;
                    }

                    .code-box {
                      width: 64px;
                      height: 64px;
                      border: 1px solid #102d69;
                      border-radius: 18px;
                      background-color: #ffffff;
                      color: #102d69;
                      font-size: 22px;
                      font-weight: 500;
                      letter-spacing: 0.02em;
                      line-height: 64px;
                      text-align: center;
                      box-shadow: 0 10px 20px rgba(16, 45, 105, 0.06);
                    }

                    .footnotes {
                      margin-top: 32px;
                    }

                    .footnote {
                      margin: 8px 0 0;
                      text-align: left;
                      color: #56637f;
                      font-size: 16px;
                      line-height: 1.5;
                    }
                  </style>
                </head>
                <body>
                  <div class="shell">
                    <div class="card">
                      <h1 class="title">Код подтверждения</h1>
                      <p class="description">%s, используйте код ниже для входа в аккаунт</p>
                      <table class="code-table" role="presentation" cellpadding="0" cellspacing="0">
                        %s
                      </table>
                      <div class="footnotes">
                        <p class="footnote" style="margin-top:22px;">Код действует в течение %d минут</p>
                        <p class="footnote">Если Вы не запрашивали код подтверждения, проигнорируйте это письмо</p>
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                escapeHtml(recipientName),
                codeBoxes,
                confirmationCodeTtlMinutes
        );
    }

    private String buildCodeBoxes(String confirmationCode) {
        String safeCode = safe(confirmationCode);
        if (safeCode.isBlank()) {
            return "";
        }

        StringBuilder html = new StringBuilder("<tr>");
        for (int index = 0; index < safeCode.length(); index++) {
            String symbol = escapeHtml(String.valueOf(safeCode.charAt(index)));
            html.append("<td class=\"code-box\">")
                    .append(symbol)
                    .append("</td>");
        }
        html.append("</tr>");
        return html.toString();
    }

    private String displayName(String firstName, String middleName) {
        String value = String.join(" ",
                safe(firstName),
                safe(middleName)
        ).trim();
        return value.isBlank() ? "Здравствуйте" : value;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
