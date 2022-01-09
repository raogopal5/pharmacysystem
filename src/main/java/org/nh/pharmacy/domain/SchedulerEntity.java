package org.nh.pharmacy.domain;

import javax.persistence.*;

@SqlResultSetMapping(name = "schedulerMapping",
    classes = {
        @ConstructorResult(targetClass = SchedulerEntity.class,
            columns = {@ColumnResult(name = "instanceName"),
                @ColumnResult(name = "scheduler"),
                @ColumnResult(name = "description"),
                @ColumnResult(name = "expression"),
                @ColumnResult(name = "repeatInterval", type = Long.class),
                @ColumnResult(name = "startTime", type = Long.class),
                @ColumnResult(name = "nextFireTime", type = Long.class),
                @ColumnResult(name = "previousFireTime", type = Long.class),
                @ColumnResult(name = "triggerState"),}
        )}
)
@Entity
public class SchedulerEntity {
    @Id
    int id;
    private String instanceName;
    private String scheduler;
    private String description;
    private String expression;
    private Long repeatInterval;
    private long startTime;
    private long nextFireTime;
    private long previousFireTime;
    private String triggerState;

    public SchedulerEntity() {
    }

    public SchedulerEntity(String instanceName, String scheduler, String description, String expression, Long repeatInterval, long startTime, long nextFireTime, long previousFireTime, String triggerState) {
        this.instanceName = instanceName;
        this.scheduler = scheduler;
        this.description = description;
        this.expression = expression;
        this.repeatInterval = repeatInterval;
        this.startTime = startTime;
        this.nextFireTime = nextFireTime;
        this.previousFireTime = previousFireTime;
        this.triggerState = triggerState;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getScheduler() {
        return scheduler;
    }

    public void setScheduler(String scheduler) {
        this.scheduler = scheduler;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(Long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(long nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public long getPreviousFireTime() {
        return previousFireTime;
    }

    public void setPreviousFireTime(long previousFireTime) {
        this.previousFireTime = previousFireTime;
    }

    public String getTriggerState() {
        return triggerState;
    }

    public void setTriggerState(String triggerState) {
        this.triggerState = triggerState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SchedulerEntity that = (SchedulerEntity) o;

        if (id != that.id)
            return false;
        if (repeatInterval != that.repeatInterval)
            return false;
        if (startTime != that.startTime)
            return false;
        if (nextFireTime != that.nextFireTime)
            return false;
        if (previousFireTime != that.previousFireTime)
            return false;
        if (instanceName != null ? !instanceName.equals(that.instanceName) : that.instanceName != null)
            return false;
        if (scheduler != null ? !scheduler.equals(that.scheduler) : that.scheduler != null)
            return false;
        if (description != null ? !description.equals(that.description) : that.description != null)
            return false;
        if (expression != null ? !expression.equals(that.expression) : that.expression != null)
            return false;
        return triggerState != null ? triggerState.equals(that.triggerState) : that.triggerState == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (instanceName != null ? instanceName.hashCode() : 0);
        result = 31 * result + (scheduler != null ? scheduler.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        result = 31 * result + (int) (repeatInterval ^ (repeatInterval >>> 32));
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        result = 31 * result + (int) (nextFireTime ^ (nextFireTime >>> 32));
        result = 31 * result + (int) (previousFireTime ^ (previousFireTime >>> 32));
        result = 31 * result + (triggerState != null ? triggerState.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SchedulerEntity{" +
            "instanceName='" + instanceName + '\'' +
            ", scheduler='" + scheduler + '\'' +
            ", description='" + description + '\'' +
            ", expression='" + expression + '\'' +
            ", repeatInterval=" + repeatInterval +
            ", startTime=" + startTime +
            ", nextFireTime=" + nextFireTime +
            ", previousFireTime=" + previousFireTime +
            ", triggerState='" + triggerState + '\'' +
            '}';
    }
}


