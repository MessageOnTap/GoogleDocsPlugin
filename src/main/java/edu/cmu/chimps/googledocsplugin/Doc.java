package edu.cmu.chimps.googledocsplugin;


public class Doc {
    private String DocName;
    private String DocUrl;
    private Long CreatedTime;

    public Long getCreatedTime() {
        return CreatedTime;
    }

    public void setCreatedTime(Long createdTime) {
        CreatedTime = createdTime;
    }

    public String getDocName() {
        return DocName;
    }

    public void setDocName(String docName) {
            DocName = docName;
        }

    public String getDocUrl() {
            return DocUrl;
        }

    public void setDocUrl(String docUrl) {
            DocUrl = docUrl;
        }
}
