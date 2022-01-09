package org.nh.pharmacy.domain.dto;

import org.nh.pharmacy.domain.enumeration.NotificationOf;
import org.nh.pharmacy.domain.enumeration.NotificationType;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * A Notification
 */
public class Notification implements Serializable {

    private String title;
    private String body;
    private String link;
    private NotificationType notificationType;
    private NotificationOf notificationOf;
    private Map<String, Object> params;
    private List<Member> memberDetails;

    public Notification() {
        // Default constructor
    }

    public Notification(String title, String body, String link, NotificationType notificationType, NotificationOf notificationOf, Map<String, Object> params, List<Member> memberDetails) {
        this.title = title;
        this.body = body;
        this.link = link;
        this.notificationType = notificationType;
        this.notificationOf = notificationOf;
        this.params = params;
        this.memberDetails = memberDetails;
    }

    public Notification(String title, String body, NotificationType notificationType, NotificationOf notificationOf, Map<String, Object> params, List<Member> memberDetails) {
        this.title = title;
        this.body = body;
        this.notificationType = notificationType;
        this.notificationOf = notificationOf;
        this.params = params;
        this.memberDetails = memberDetails;
    }

    public Notification(String title, String body, NotificationType notificationType, NotificationOf notificationOf, List<Member> memberDetails) {
        this.title = title;
        this.body = body;
        this.notificationType = notificationType;
        this.notificationOf = notificationOf;
        this.memberDetails = memberDetails;
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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public NotificationOf getNotificationOf() {
        return notificationOf;
    }

    public void setNotificationOf(NotificationOf notificationOf) {
        this.notificationOf = notificationOf;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public List<Member> getMemberDetails() {
        return memberDetails;
    }

    public void setMemberDetails(List<Member> memberDetails) {
        this.memberDetails = memberDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Notification that = (Notification) o;

        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (body != null ? !body.equals(that.body) : that.body != null) return false;
        if (link != null ? !link.equals(that.link) : that.link != null) return false;
        if (notificationType != that.notificationType) return false;
        if (notificationOf != that.notificationOf) return false;
        if (params != null ? !params.equals(that.params) : that.params != null) return false;
        return memberDetails != null ? memberDetails.equals(that.memberDetails) : that.memberDetails == null;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (link != null ? link.hashCode() : 0);
        result = 31 * result + (notificationType != null ? notificationType.hashCode() : 0);
        result = 31 * result + (notificationOf != null ? notificationOf.hashCode() : 0);
        result = 31 * result + (params != null ? params.hashCode() : 0);
        result = 31 * result + (memberDetails != null ? memberDetails.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Notification{" +
            "title='" + title + '\'' +
            ", body='" + body + '\'' +
            ", link='" + link + '\'' +
            ", notificationType=" + notificationType +
            ", notificationOf=" + notificationOf +
            ", params=" + params +
            ", memberDetails=" + memberDetails +
            '}';
    }
}
