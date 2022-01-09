package org.nh.pharmacy.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.nh.billing.domain.dto.Medication;
import org.nh.common.dto.*;
import org.nh.pharmacy.util.UOMDeserializer;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocumentLine implements Serializable {
    private Long lineNumber;
    private Long itemId;
    private String owner;
    private Long medicationId;
    private Long stockId;
    private Long stockQuantity;
    private Long batchQuantity;
    private String code;
    @Field(type = FieldType.Text)
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "word_analyzer"),
        otherFields = {
            @InnerField(suffix = "raw", type = FieldType.Keyword),
            @InnerField(suffix = "sort", type = FieldType.ICU_Collation_Keyword)
        }
    )
    private String name;
    private String batchNumber;
    private String instruction;
    private String note;
    private LocalDate expiryDate;
    private LocatorDTO locator;
    private Boolean substitute;
    private String supplier;
    @JsonDeserialize(using = UOMDeserializer.class)
    private UOMDTO uom;
    private String barCode;
    private String sku;
    private ValueSetCodeDTO group;
    private Boolean consignment = Boolean.FALSE;
    private OrderItem orderItem;
    @Field(type = FieldType.Object)
    private ConsultantDTO consultant;
    private UOMDTO trackUOM;
    private ValueSetCodeDTO itemType;
    private ItemCategoryDTO itemCategory;
    private ValueSetCodeDTO itemGroup;
    private GroupDTO materialGroup;
    private Medication medication;
    private HealthcareServiceCenterDTO renderingHSC;
    private List<TariffAddOnParametersDTO> addOnParams=new ArrayList<>();
    //private DispenseSource dispenseSource;
    //private DispenseReturnDetails dispenseReturnDetails;


    public Long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Long lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Long getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(Long medicationId) {
        this.medicationId = medicationId;
    }

    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    public Long getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Long stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Long getBatchQuantity() {
        return batchQuantity;
    }

    public void setBatchQuantity(Long batchQuantity) {
        this.batchQuantity = batchQuantity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocatorDTO getLocator() {
        return locator;
    }

    public void setLocator(LocatorDTO locator) {
        this.locator = locator;
    }

    public Boolean getSubstitute() {
        return substitute;
    }

    public void setSubstitute(Boolean substitute) {
        this.substitute = substitute;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public UOMDTO getUom() {
        return uom;
    }

    public void setUom(UOMDTO uom) {
        this.uom = uom;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public ValueSetCodeDTO getGroup() {
        return group;
    }

    public void setGroup(ValueSetCodeDTO group) {
        this.group = group;
    }

    public Boolean getConsignment() {
        return consignment;
    }

    public void setConsignment(Boolean consignment) {
        this.consignment = consignment;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public ConsultantDTO getConsultant() {
        return consultant;
    }

    public void setConsultant(ConsultantDTO consultant) {
        this.consultant = consultant;
    }

    public UOMDTO getTrackUOM() {
        return trackUOM;
    }

    public void setTrackUOM(UOMDTO trackUOM) {
        this.trackUOM = trackUOM;
    }

    public ValueSetCodeDTO getItemType() {
        return itemType;
    }

    public void setItemType(ValueSetCodeDTO itemType) {
        this.itemType = itemType;
    }

    public ItemCategoryDTO getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(ItemCategoryDTO itemCategory) {
        this.itemCategory = itemCategory;
    }

    public ValueSetCodeDTO getItemGroup() {
        return itemGroup;
    }

    public void setItemGroup(ValueSetCodeDTO itemGroup) {
        this.itemGroup = itemGroup;
    }

    public GroupDTO getMaterialGroup() {
        return materialGroup;
    }

    public void setMaterialGroup(GroupDTO materialGroup) {
        this.materialGroup = materialGroup;
    }

    public Medication getMedication() {
        return medication;
    }

    public void setMedication(Medication medication) {
        this.medication = medication;
    }

    public HealthcareServiceCenterDTO getRenderingHSC() {
        return renderingHSC;
    }

    public void setRenderingHSC(HealthcareServiceCenterDTO renderingHSC) {
        this.renderingHSC = renderingHSC;
    }

    public List<TariffAddOnParametersDTO> getAddOnParams() {
        return addOnParams;
    }

    public void setAddOnParams(List<TariffAddOnParametersDTO> addOnParams) {
        this.addOnParams = addOnParams;
    }
}
