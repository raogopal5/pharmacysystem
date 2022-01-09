package org.nh.pharmacy.web.rest.mapper;

import org.mapstruct.Mapper;
import org.nh.billing.domain.Receipt;
import org.nh.common.dto.ConsultantDTO;
import org.nh.common.dto.OrganizationDTO;
import org.nh.common.dto.PatientDTO;
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.dto.PaymentDetail;
import org.nh.pharmacy.exception.FieldValidationException;
import org.nh.pharmacy.exception.constants.PharmacyErrorCodes;
import org.nh.pharmacy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nirbhay on 6/22/17.
 */
@Mapper(componentModel = "spring")
public abstract class DispenseToReceiptMapper {

    @Autowired
    InvoiceMapper invoiceMapper;

    @Autowired
    UserService userService;

    public List<Receipt> convertDispenseToReceipts(Dispense dispense) throws FieldValidationException {
        List<Receipt> receipts = new ArrayList<>();
        List<PaymentDetail> paymentDetails = dispense.getDocument().getPaymentDetails();

        if (paymentDetails == null || paymentDetails.isEmpty()) {
            throw new FieldValidationException(PharmacyErrorCodes.PAYMENT_DETAILS_NOT_FOUND);
        }

        OrganizationDTO unit = dispense.getDocument().getDispenseUnit();
        PatientDTO patient = dispense.getDocument().getPatient();
        ConsultantDTO consultant = dispense.getDocument().getConsultant();
        paymentDetails.forEach(paymentDetail -> {
            Receipt receipt = new Receipt();

            receipt.setUnitMaster(unit);
            receipt.setUnit(unit.getId());
            receipt.setReceiptAmount(paymentDetail.getTotalAmount());
            receipt.setUnsettledAmount(paymentDetail.getUnsettledAmount());
            receipt.setAdjustmentStatus(paymentDetail.getAdjustmentStatus());
            receipt.setPaymentMode(paymentDetail.getPaymentMode());
            if (paymentDetail.getReceivedBy() != null) {
                receipt.setReceivedBy(paymentDetail.getReceivedBy().getId());
                receipt.setReceivedByUser(getUserDto(paymentDetail.getReceivedBy().getId()));
            }
            receipt.setReceiptDate(paymentDetail.getReceivedDate());
            receipt.setReceiptType(paymentDetail.getReceiptType());
            if (paymentDetail.getApprovedBy() != null) {
                receipt.setApprovedBy(paymentDetail.getApprovedBy().getId());
            }
            //receipt.setReceiptDate(paymentDetail.getApprovedDate());
            receipt.setReceiptDate(LocalDateTime.now());
            receipt.setBank(paymentDetail.getBankDetails());
            receipt.setMachine(paymentDetail.getMachineDetails());
            receipt.setPatient(patient);
            receipt.setPatientId(patient.getId());
            receipt.setInstrumentNumber(paymentDetail.getInstrumentNumber());
            receipt.setInstrumentValidityDate(paymentDetail.getInstrumentValidityDate());
            receipt.setHsc(paymentDetail.getHsc());
            receipt.setTransactionInfo(paymentDetail.getTransactionInfo());
            receipt.setBaseCurrency(paymentDetail.getBaseCurrency());
            receipt.setExchangeRate(paymentDetail.getExchangeRate());
            receipt.setTransactionAmount(paymentDetail.getTransactionAmount());
            receipt.setTransactionCurrency(paymentDetail.getTransactionCurrency());
            //receipt.setApprovedDateTime(paymentDetail.getApprovedDate());
            receipt.setApprovedDateTime(LocalDateTime.now());
            receipt.setConsultant(consultant);
            receipts.add(receipt);
        });

        return receipts;
    }

    private org.nh.common.dto.UserDTO getUserDto(Long id) {
        org.nh.pharmacy.domain.User user = userService.findOne(id);
        org.nh.common.dto.UserDTO userDto = new org.nh.common.dto.UserDTO();
        userDto.setId(user.getId());
        userDto.setDisplayName(user.getDisplayName());
        userDto.setEmployeeNo(user.getEmployeeNo());
        userDto.setLogin(user.getLogin());
        return userDto;
    }
}
