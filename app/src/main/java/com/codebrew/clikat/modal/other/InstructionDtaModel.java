package com.codebrew.clikat.modal.other;

public class InstructionDtaModel {

    private String title;
    private String body;
    private int image;

    public InstructionDtaModel(String title, String body, int image) {
        this.title = title;
        this.body = body;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
