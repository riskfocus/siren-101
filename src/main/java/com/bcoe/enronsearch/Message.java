package com.bcoe.enronsearch;

import java.util.Enumeration;
import java.io.IOException;

import javax.mail.*;
import javax.mail.internet.*;

/*
Munge MIME message data into
an easier to work with object
for indexing.
*/
public class Message
{

  private String id;
  private String dateString;
  private String[] to;
  private String from;
  private String subject;
  private String body = "";
  private String[] cc;
  private String[] bcc;
  private String xFrom;
  private String xTo;
  private String xBcc;
  private String xCc;
  private String xOrigin;
  private String xFolder;
  private String[] recipients;

  public Message(MimeMessage rawMessage) {
    extractHeaders(rawMessage);
    extractBody(rawMessage);
  }

  private void extractHeaders(MimeMessage rawMessage) {
    try {

      StringBuilder recipients = null;
      for (Enumeration<Header> e = rawMessage.getAllHeaders(); e.hasMoreElements();) {
        Header h = e.nextElement();
        String[] tokens;
        switch (h.getName()) {
          case "From":
            from = h.getValue(); 
            break;
          case "To":
            recipients = addRecipients(recipients, h.getValue());
            tokens = h.getValue().split("[,\\s]+");
            to = tokens;
            break;
          case "Subject":
            subject = h.getValue(); 
            break;
          case "Message-ID":
            id = h.getValue(); 
            break;
          case "Cc":
            recipients = addRecipients(recipients, h.getValue());
            tokens = h.getValue().split("[,\\s]+");
        	cc = tokens;
        	break;
          case "Bcc":
            recipients = addRecipients(recipients, h.getValue());
            tokens = h.getValue().split("[,\\s]+");
        	bcc = tokens;
        	break;
          case "Date":
        	dateString = h.getValue();
        	break;
          case "X-Origin":
            xOrigin = h.getValue().toLowerCase();
            break;
          case "X-From":
            xFrom = h.getValue();
            break;
          case "X-To":
            xTo = h.getValue();
            break;
          case "X-bcc":
            xBcc = h.getValue();
            break;
          case "X-cc":
            xCc = h.getValue();
            break;
          case "X-Folder":
            xFolder = h.getValue();
            break;
        }
      }
      if (recipients != null)
        this.recipients = recipients.toString().split("[,\\s]+");

    } catch (MessagingException e) {
      System.out.println("failed to extract message headers: " + e);
    }
  }

  private StringBuilder addRecipients(StringBuilder recipients, String values) {
      if (recipients == null) {
        recipients = new StringBuilder(values);
      } else {
        recipients.append(", ").append(values);
      }

      return recipients;
  }

  private void extractBody(MimeMessage rawMessage) {
    try {
      
      Object contentObject = rawMessage.getContent();

      if(contentObject instanceof Multipart) {

        BodyPart clearTextPart = null;
        Multipart content = (Multipart)contentObject;

        for(int i = 0; i < content.getCount(); i++) {
          BodyPart part =  content.getBodyPart(i);

          if(part.isMimeType("text/plain")) {
            body = (String) part.getContent();
            break;
          }

        }
      } else if (contentObject instanceof String) {
        body = (String) contentObject;
      }
    } catch (MessagingException e) {
      System.out.println("Failed to parse body from MIME message: " + e);
    } catch (IOException e) {
      System.out.println("Failed to read MIME part: " + e);
    }
  }

  public String getId() {
    return id;
  }

  public String[] getTo() {
    return to;
  }

  public String getFrom() {
    return from;
  }

  public String getSubject() {
    return subject;
  }

  public String getBody() {
    return body;
  }

  public String[] getCc() {
    return cc;
  }

  public String[] getBcc() {
    return bcc;
  }

  public String getDateString() {
    return dateString;
  }

  public String getxFrom() {
    return xFrom;
  }

  public String getxTo() {
    return xTo;
  }

  public String getxBcc() {
    return xBcc;
  }

  public String getxCc() {
    return xCc;
  }

  public String getxOrigin() {
    return xOrigin;
  }

  public String getxFolder() {
    return xFolder;
  }

  public String toString() {
    return "to: " + getTo() + "\nfrom: " + getFrom() + "\nsubject: " + getSubject() + "\n\n" + body + "\n\n";
  }

  public String[] getRecipients() {
    return recipients;
  }
}
