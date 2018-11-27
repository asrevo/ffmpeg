package org.revo.Domain;

public class Bucket {
    private String name;
    private String domainName;

    public Bucket() {
    }

    public Bucket(String name, String domainName) {
        this.name = name;
        this.domainName = domainName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    @Override
    public String toString() {
        return name;
    }

}
