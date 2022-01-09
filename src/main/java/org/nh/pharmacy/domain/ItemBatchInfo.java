package org.nh.pharmacy.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A ItemBatchInfo.
 */
@Entity
@Table(name = "item_batch_info")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "itembatchinfo", type = "itembatchinfo")
@Setting(settingPath = "/es/settings.json")
public class ItemBatchInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "batch_no")
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "whitespace_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private String batchNo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public ItemBatchInfo itemId(Long itemId) {
        this.itemId = itemId;
        return this;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public ItemBatchInfo batchNo(String batchNo) {
        this.batchNo = batchNo;
        return this;
    }

    public ItemBatchInfo(){}

    public ItemBatchInfo(Long itemId, String batchNo) {
        this.itemId = itemId;
        this.batchNo = batchNo;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemBatchInfo that = (ItemBatchInfo) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(itemId, that.itemId) &&
            Objects.equals(batchNo, that.batchNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, itemId, batchNo);
    }

    @Override
    public String toString() {
        return "ItemBatchInfo{" +
            "id=" + id +
            ", itemId=" + itemId +
            ", batchNo='" + batchNo + '\'' +
            '}';
    }
}
