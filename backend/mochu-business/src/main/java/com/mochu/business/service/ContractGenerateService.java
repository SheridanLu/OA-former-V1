package com.mochu.business.service;

import com.mochu.business.entity.BizContract;
import com.mochu.business.entity.BizContractFieldValue;
import com.mochu.business.entity.SysContractTplField;
import com.mochu.business.entity.SysContractTplVersion;
import com.mochu.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 合同生成服务 — 模板渲染 + 水印 + 打印适配
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractGenerateService {

    private final ContractTplService tplService;

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([a-zA-Z_][a-zA-Z0-9_]*)(?::[^}]+)?}}");

    /**
     * 渲染模板：用字段值替换占位符，生成合同正文HTML
     */
    public String renderTemplate(Integer tplVersionId, Map<String, String> fieldValues) {
        SysContractTplVersion version = tplService.getVersionById(tplVersionId);
        if (version == null) throw new BusinessException("模板版本不存在");

        String html = version.getHtmlCache();
        if (html == null || html.isBlank()) {
            throw new BusinessException("模板内容为空");
        }

        // 替换占位符
        Matcher matcher = PLACEHOLDER.matcher(html);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = fieldValues != null ? fieldValues.getOrDefault(key, "") : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(
                    "<span class=\"field-value\">" + escapeHtml(value) + "</span>"));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 生成可打印合同HTML（含水印 + 打印样式）
     */
    public String generatePrintableHtml(BizContract contract, String contentHtml) {
        String watermarkText = "墨初科技";

        return "<!DOCTYPE html>\n" +
                "<html>\n<head>\n<meta charset=\"utf-8\">\n" +
                "<title>" + escapeHtml(contract.getContractName()) + "</title>\n" +
                "<style>\n" +
                "@page { size: A4; margin: 25mm 20mm; }\n" +
                "@media print { .no-print { display: none; } body { -webkit-print-color-adjust: exact; } }\n" +
                "body { font-family: 'SimSun','宋体',serif; font-size: 14px; line-height: 2; color: #000; }\n" +
                ".contract-wrapper { position: relative; max-width: 210mm; margin: 0 auto; padding: 20px; }\n" +
                ".watermark { position: fixed; top: 0; left: 0; width: 100%; height: 100%; z-index: -1; pointer-events: none; " +
                "background-repeat: repeat; background-size: 200px 150px; " +
                "background-image: url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='150'%3E" +
                "%3Ctext x='50%25' y='50%25' text-anchor='middle' dominant-baseline='middle' " +
                "transform='rotate(-30,100,75)' font-size='18' fill='%23e0e0e0' opacity='0.5'%3E" +
                escapeHtml(watermarkText) + "%3C/text%3E%3C/svg%3E\"); }\n" +
                ".contract-header { text-align: center; margin-bottom: 30px; }\n" +
                ".contract-header h1 { font-size: 22px; margin-bottom: 10px; }\n" +
                ".contract-header .contract-no { color: #666; font-size: 13px; }\n" +
                ".contract-meta { display: flex; justify-content: space-between; margin-bottom: 20px; border-bottom: 1px solid #333; padding-bottom: 10px; }\n" +
                ".contract-meta span { font-size: 13px; }\n" +
                ".contract-body { min-height: 500px; }\n" +
                ".field-value { border-bottom: 1px solid #333; padding: 0 4px; }\n" +
                ".contract-footer { margin-top: 60px; display: flex; justify-content: space-between; }\n" +
                ".sign-block { width: 45%; }\n" +
                ".sign-block h4 { margin-bottom: 40px; }\n" +
                ".sign-line { border-bottom: 1px solid #333; margin-bottom: 15px; height: 30px; }\n" +
                ".print-btn { position: fixed; top: 20px; right: 20px; padding: 10px 30px; " +
                "background: #409eff; color: #fff; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; }\n" +
                "</style>\n</head>\n<body>\n" +
                "<button class=\"print-btn no-print\" onclick=\"window.print()\">打印合同</button>\n" +
                "<div class=\"watermark\"></div>\n" +
                "<div class=\"contract-wrapper\">\n" +
                "  <div class=\"contract-header\">\n" +
                "    <h1>" + escapeHtml(contract.getContractName()) + "</h1>\n" +
                "    <div class=\"contract-no\">合同编号：" + escapeHtml(contract.getContractNo()) + "</div>\n" +
                "  </div>\n" +
                "  <div class=\"contract-meta\">\n" +
                "    <span>甲方：" + escapeHtml(contract.getPartyA() != null ? contract.getPartyA() : "") + "</span>\n" +
                "    <span>乙方：" + escapeHtml(contract.getPartyB() != null ? contract.getPartyB() : "") + "</span>\n" +
                "  </div>\n" +
                "  <div class=\"contract-body\">\n" + contentHtml + "\n  </div>\n" +
                "  <div class=\"contract-footer\">\n" +
                "    <div class=\"sign-block\">\n" +
                "      <h4>甲方（盖章）：</h4>\n" +
                "      <div class=\"sign-line\"></div>\n" +
                "      <div>日期：_____年___月___日</div>\n" +
                "    </div>\n" +
                "    <div class=\"sign-block\">\n" +
                "      <h4>乙方（盖章）：</h4>\n" +
                "      <div class=\"sign-line\"></div>\n" +
                "      <div>日期：_____年___月___日</div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</div>\n</body>\n</html>";
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
