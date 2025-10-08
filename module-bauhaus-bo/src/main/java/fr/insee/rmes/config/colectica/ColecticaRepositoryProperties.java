package fr.insee.rmes.config.colectica;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.colectica")
public class ColecticaRepositoryProperties {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    @NotNull
    private URI baseUrl;

    private Duration connectTimeout = DEFAULT_TIMEOUT;

    private Duration readTimeout = DEFAULT_TIMEOUT;

    @NotBlank
    private String itemPath = "/Repository/api/items/{itemType}/{agency}/{identifier}/{version}";

    @NotBlank
    private String latestItemPath = "/Repository/api/items/{itemType}/{agency}/{identifier}/latest";

    private String username;

    private String password;

    public URI getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(URI baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout != null ? connectTimeout : DEFAULT_TIMEOUT;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout != null ? readTimeout : DEFAULT_TIMEOUT;
    }

    public String getItemPath() {
        return itemPath;
    }

    public void setItemPath(String itemPath) {
        this.itemPath = itemPath;
    }

    public String getLatestItemPath() {
        return latestItemPath;
    }

    public void setLatestItemPath(String latestItemPath) {
        this.latestItemPath = latestItemPath;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
