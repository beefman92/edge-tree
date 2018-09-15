package com.my.edge.common.job;

/**
 * Creator: Beefman
 * Date: 2018/9/14
 */
public class FileRecord {
    private String fileName;
    private byte[] fileContent;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }
}
