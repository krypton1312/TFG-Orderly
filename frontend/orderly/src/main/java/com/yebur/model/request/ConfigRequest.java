package com.yebur.model.request;

public class ConfigRequest {
    private String theme;
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private String smtpReceiverEmail;
    private Boolean smtpUseTls;
    private String printerName;

    public ConfigRequest() {}

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getSmtpHost() { return smtpHost; }
    public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }

    public Integer getSmtpPort() { return smtpPort; }
    public void setSmtpPort(Integer smtpPort) { this.smtpPort = smtpPort; }

    public String getSmtpUsername() { return smtpUsername; }
    public void setSmtpUsername(String smtpUsername) { this.smtpUsername = smtpUsername; }

    public String getSmtpPassword() { return smtpPassword; }
    public void setSmtpPassword(String smtpPassword) { this.smtpPassword = smtpPassword; }

    public String getSmtpReceiverEmail() { return smtpReceiverEmail; }
    public void setSmtpReceiverEmail(String smtpReceiverEmail) { this.smtpReceiverEmail = smtpReceiverEmail; }

    public Boolean getSmtpUseTls() { return smtpUseTls; }
    public void setSmtpUseTls(Boolean smtpUseTls) { this.smtpUseTls = smtpUseTls; }

    public String getPrinterName() { return printerName; }
    public void setPrinterName(String printerName) { this.printerName = printerName; }
}
