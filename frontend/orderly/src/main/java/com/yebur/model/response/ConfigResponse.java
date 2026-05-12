package com.yebur.model.response;

public class ConfigResponse {
    private String theme;
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    // smtpPassword intentionally ABSENT — never returned to client
    private String smtpReceiverEmail;
    private Boolean smtpUseTls;
    private String printerName;

    public ConfigResponse() {}

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getSmtpHost() { return smtpHost; }
    public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }

    public Integer getSmtpPort() { return smtpPort; }
    public void setSmtpPort(Integer smtpPort) { this.smtpPort = smtpPort; }

    public String getSmtpUsername() { return smtpUsername; }
    public void setSmtpUsername(String smtpUsername) { this.smtpUsername = smtpUsername; }

    public String getSmtpReceiverEmail() { return smtpReceiverEmail; }
    public void setSmtpReceiverEmail(String smtpReceiverEmail) { this.smtpReceiverEmail = smtpReceiverEmail; }

    public Boolean getSmtpUseTls() { return smtpUseTls; }
    public void setSmtpUseTls(Boolean smtpUseTls) { this.smtpUseTls = smtpUseTls; }

    public String getPrinterName() { return printerName; }
    public void setPrinterName(String printerName) { this.printerName = printerName; }
}
