package org.nh.pharmacy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Pharmacy.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = true)
public class ApplicationProperties {

    private final AthmaBucket athmaBucket = new AthmaBucket();

    private final Configs configs = new Configs();

    private final SendGrid sendGrid = new SendGrid();

    private final QuartzScheduler quartzScheduler = new QuartzScheduler();

    private final RedisCacheProperties redisCache = new RedisCacheProperties();

    public AthmaBucket getAthmaBucket() {
        return athmaBucket;
    }

    public SendGrid getSendGrid() {
        return sendGrid;
    }

    public Configs getConfigs() {
        return configs;
    }

    public QuartzScheduler getQuartzScheduler() {
        return quartzScheduler;
    }

    public RedisCacheProperties getRedisCache() {
        return redisCache;
    }

    public static class AthmaBucket {

        private String masterExport;
        private String template;
        private String tempExport;
        private String printSaveFile;
        private String docBasePath;

        public String getStoragePath(String pathReference) {

            switch (pathReference) {
                case "masterExport":
                    return getMasterExport();
                default:
                    throw new IllegalArgumentException("Invalid path reference.");
            }
        }

        public String getMasterExport() {
            return masterExport;
        }

        public void setMasterExport(String masterExport) {
            this.masterExport = masterExport;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }

        public String getTempExport() {
            return tempExport;
        }

        public void setTempExport(String tempExport) {
            this.tempExport = tempExport;
        }

        public String getPrintSaveFile() {
            return printSaveFile;
        }

        public void setPrintSaveFile(String printSaveFile) {
            this.printSaveFile = printSaveFile;
        }

        public String getDocBasePath() {
            return docBasePath;
        }

        public void setDocBasePath(String docBasePath) {
            this.docBasePath = docBasePath;
        }
    }

    public static class Configs {

        private Integer indexPageSize;
        private Integer exportRowsCount;

        public Integer geIndexPageSize() {
            return indexPageSize != null ? indexPageSize : 200;
        }

        public void setIndexPageSize(Integer indexPageSize) {
            this.indexPageSize = indexPageSize;
        }

        public Integer getExportRowsCount() {
            return exportRowsCount != null ? exportRowsCount : 2000;
        }

        public void setExportRowsCount(Integer exportRowsCount) {
            this.exportRowsCount = exportRowsCount;
        }
    }

    public static class SendGrid {

        private String apiKey;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }

    public static class QuartzScheduler {
        private boolean enabled = Boolean.TRUE;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class RedisCacheProperties {
        private String redisHost;
        private int redisPort;
        private boolean cacheEnabled;
        private  boolean clustered;
        private boolean taxMappingCacheEnabled;

        public String getRedisHost() {
            return redisHost;
        }

        public void setRedisHost(String redisHost) {
            this.redisHost = redisHost;
        }

        public int getRedisPort() {
            return redisPort;
        }

        public void setRedisPort(int redisPort) {
            this.redisPort = redisPort;
        }

        public boolean isCacheEnabled() {
            return cacheEnabled;
        }

        public void setCacheEnabled(boolean cacheEnabled) {
            this.cacheEnabled = cacheEnabled;
        }

        public boolean isClustered() {
            return clustered;
        }

        public void setClustered(boolean clustered) {
            this.clustered = clustered;
        }

        public boolean isTaxMappingCacheEnabled() {
            return taxMappingCacheEnabled;
        }

        public void setTaxMappingCacheEnabled(boolean taxMappingCacheEnabled) {
            this.taxMappingCacheEnabled = taxMappingCacheEnabled;
        }
    }

}
