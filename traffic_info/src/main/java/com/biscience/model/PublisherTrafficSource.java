package com.biscience.model;

import com.biscience.TrafficInfoProperties;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by Anna Kuranda on 5/21/2017.
 */
public class PublisherTrafficSource {
    private int entityId;
    private Integer eType;
    private String srcType;
    private Double srcShare;
    private String domain;

    public PublisherTrafficSource(int entityId, String srcType, Double srcShare, String domain) {
        this.entityId = entityId;
        this.srcType = srcType;
        this.srcShare = srcShare;
        this.domain = domain;
        this.eType = 1;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public Integer geteType() {
        return eType;
    }

    public void seteType(Integer eType) {
        this.eType = eType;
    }

    public String getSrcType() {
        return srcType;
    }

    public void setSrcType(String srcType) {
        this.srcType = srcType;
    }

    public Double getSrcShare() {
        return srcShare;
    }

    public void setSrcShare(Double srcShare) {
        this.srcShare = srcShare;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }


    public String toCsv(){
        StringBuilder sb = new StringBuilder();

        String separator = TrafficInfoProperties.CSV_SEPARATOR.getValue();
        if(StringUtils.isEmpty(separator))
        {
            separator = "\t";
        }

        sb.append(entityId).append(separator);
        sb.append(eType).append(separator);
        sb.append(srcType).append(separator);
        sb.append(srcShare).append(separator);
        sb.append(domain);

        return sb.toString();
    }
}
