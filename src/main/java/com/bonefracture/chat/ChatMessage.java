package com.bonefracture.chat;

import com.bonefracture.detection.AnalysisResult;

public class ChatMessage {

    private String question;
    private String answer;
    private AnalysisResult about; // null for general questions

    public ChatMessage(String question, AnalysisResult about) {
        this.question = question;
        this.about = about;
    }

    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public AnalysisResult getAbout() { return about; }
}
