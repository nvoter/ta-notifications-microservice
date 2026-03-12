package org.fcs.notifications.microservice.services.impl;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfirmationCodeEmailTemplateServiceImplTest {
    private final ConfirmationCodeEmailTemplateServiceImpl service = new ConfirmationCodeEmailTemplateServiceImpl();

    @Test
    void buildHtml_whenNamesAndCodeProvided_thenBuildEscapedTemplate() {
        ReflectionTestUtils.setField(service, "confirmationCodeTtlMinutes", 15);

        String html = service.buildHtml("Иван", "Петров", "<script>", "12&3");

        assertTrue(html.contains("Иван &lt;script&gt;"));
        assertTrue(html.contains("12"));
        assertTrue(html.contains("&amp;"));
        assertTrue(html.contains("15 минут"));
    }

    @Test
    void buildHtml_whenNamesAndCodeBlank_thenUseFallbacks() {
        ReflectionTestUtils.setField(service, "confirmationCodeTtlMinutes", 10);

        String html = service.buildHtml(" ", null, " ", " ");

        assertTrue(html.contains("Здравствуйте"));
        assertFalse(html.contains("class=\"code-box\""));
    }

    @Test
    void buildHtml_whenNamesAndCodeNull_thenUseFallbacksAndEmptyCode() {
        ReflectionTestUtils.setField(service, "confirmationCodeTtlMinutes", 5);

        String html = service.buildHtml(null, null, null, null);

        assertTrue(html.contains("Здравствуйте"));
        assertFalse(html.contains("class=\"code-box\""));
    }

    @Test
    void escapeHtml_whenNull_thenReturnEmptyString() {
        String escaped = ReflectionTestUtils.invokeMethod(service, "escapeHtml", (String) null);

        assertTrue(escaped.isEmpty());
    }
}
