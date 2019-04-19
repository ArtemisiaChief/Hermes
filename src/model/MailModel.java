package model;

import java.util.Date;

public class MailModel {
    private int idx;
    private String to;
    private String subject;
    private String content;
    private String from;
    private Date date;

    public MailModel(int idx, String to, String subject, String from, Date date) {
        this.idx=idx;
        this.to = to;
        this.subject = subject;
        this.content = content;
        this.from = from;
        this.date=date;
    }

    public int getIdx() {
        return idx;
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public String getFrom() {
        return from;
    }

    public Date getDate() {
        return date;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
