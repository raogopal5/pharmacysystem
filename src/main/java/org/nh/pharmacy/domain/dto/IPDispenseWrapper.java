package org.nh.pharmacy.domain.dto;

import org.nh.billing.domain.ChargeRecord;
import org.nh.common.dto.SourceDTO;
import org.nh.pharmacy.domain.Dispense;

import java.io.Serializable;
import java.util.List;

/**
 * this class used to publish ip dispense result to pharmacy charge record service impl class
 */
public class IPDispenseWrapper implements Serializable {
    private List<ChargeRecord> chargeRecordList;
    private boolean isDirectDispense;
    //field used to publish dispensed items data to amb
    private SourceDTO sourceDTO;
    private Dispense dispense;

    public List<ChargeRecord> getChargeRecordList() {
        return chargeRecordList;
    }

    public void setChargeRecordList(List<ChargeRecord> chargeRecordList) {
        this.chargeRecordList = chargeRecordList;
    }

    public boolean isDirectDispense() {
        return isDirectDispense;
    }

    public void setDirectDispense(boolean directDispense) {
        isDirectDispense = directDispense;
    }

    public SourceDTO getSourceDTO() {
        return sourceDTO;
    }

    public void setSourceDTO(SourceDTO sourceDTO) {
        this.sourceDTO = sourceDTO;
    }

    public Dispense getDispense() {
        return dispense;
    }

    public void setDispense(Dispense dispense) {
        this.dispense = dispense;
    }
}
