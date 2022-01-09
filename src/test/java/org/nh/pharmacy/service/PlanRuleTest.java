package org.nh.pharmacy.service;

import org.nh.billing.domain.PlanRule;
import org.nh.billing.domain.PlanRules;
import org.nh.billing.domain.dto.*;
import org.nh.billing.domain.enumeration.BaseSponsorPatientType;
import org.nh.billing.domain.enumeration.BaseSponsorPatientValueType;
import org.nh.billing.domain.enumeration.PlanRuleType;
import org.nh.billing.domain.enumeration.Type;
import org.nh.common.dto.GroupDTO;
import org.nh.common.dto.HealthcareServiceCenterDTO;
import org.nh.common.dto.OrganizationDTO;
import org.nh.common.dto.UserDTO;
import org.nh.pharmacy.domain.dto.DispenseDocument;
import org.nh.pharmacy.domain.dto.DispenseDocumentLine;
import org.nh.pharmacy.domain.dto.DispensePlan;
import org.nh.pharmacy.domain.dto.DispenseTax;
import org.nh.pharmacy.domain.enumeration.DispenseStatus;

import java.util.ArrayList;
import java.util.List;

import static org.nh.common.util.BigDecimalUtil.getBigDecimal;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class PlanRuleTest {

    /**
     * Disc on MRP,Sponsor Discount, Patient Discount,
     *
     * @return
     */
    public static PlanRule populatePlanRuleWithOneRuleDetailsRule() {
        PlanRule planRule = new PlanRule();
        planRule.setId(1l);
        planRule.setType("plan-1");
        planRule.setLevel(1);
        planRule.setVersion(1);

        List<PlanRuleDetail> planRuleDetails = new ArrayList<>();
        PlanRuleDetail planRuleDetail = new PlanRuleDetail();
        //Base discount
        AppliedOnBasePatientSponsor baseDiscount = new AppliedOnBasePatientSponsor();
        baseDiscount.setAppliedType(BaseSponsorPatientType.Discount);
        baseDiscount.setValue(1.0f);
        baseDiscount.setValueType(BaseSponsorPatientValueType.Percentage);
        planRuleDetail.setAppliedOnBase(baseDiscount);

        //Sponsor discount
        AppliedOnBasePatientSponsor sponsorDiscount = new AppliedOnBasePatientSponsor();
        sponsorDiscount.setAppliedType(BaseSponsorPatientType.Discount);
        sponsorDiscount.setValue(1.0f);
        sponsorDiscount.setValueType(BaseSponsorPatientValueType.Percentage);
        planRuleDetail.setAppliedOnSponsorAmount(sponsorDiscount);

        //Patient discount
        AppliedOnBasePatientSponsor patientDiscount = new AppliedOnBasePatientSponsor();
        patientDiscount.setAppliedType(BaseSponsorPatientType.Discount);
        patientDiscount.setValue(2.0f);
        patientDiscount.setValueType(BaseSponsorPatientValueType.Percentage);
        planRuleDetail.setAppliedOnPatientAmount(patientDiscount);

        planRuleDetails.add(planRuleDetail);
        PlanRules planRules = new PlanRules(planRuleDetails);

        planRule.setPlanRules(planRules);

        return planRule;
    }

    /**
     * BaseAddOn on MRP,No Sponsor Discount, Patient Discount,
     *
     * @return
     */
    public static PlanRule populatePlanRuleWithRuleDetailsRule() {
        PlanRule planRule = new PlanRule();
        planRule.setId(1l);
        planRule.setType("plan-2");
        planRule.setLevel(1);
        planRule.setVersion(1);

        List<PlanRuleDetail> planRuleDetails = new ArrayList<>();
        PlanRuleDetail planRuleDetail = new PlanRuleDetail();
        //Base discount
        AppliedOnBasePatientSponsor baseAddOn = new AppliedOnBasePatientSponsor();
        baseAddOn.setAppliedType(BaseSponsorPatientType.Addon);
        baseAddOn.setValue(1.0f);
        baseAddOn.setValueType(BaseSponsorPatientValueType.Amount);
        planRuleDetail.setAppliedOnBase(baseAddOn);

        //Patient discount
        AppliedOnBasePatientSponsor patientDiscount = new AppliedOnBasePatientSponsor();
        patientDiscount.setAppliedType(BaseSponsorPatientType.Discount);
        patientDiscount.setValue(2.0f);
        patientDiscount.setValueType(BaseSponsorPatientValueType.Percentage);
        planRuleDetail.setAppliedOnPatientAmount(patientDiscount);

        planRuleDetails.add(planRuleDetail);
        PlanRules planRules = new PlanRules(planRuleDetails);

        planRule.setPlanRules(planRules);

        return planRule;
    }


    //baseDiscount, sponsorDiscount, patientDiscount, sponsor pay tax
    static DispensePlan populatePlanWithOneRuleDetail() {

        //Document level Plan
        DispensePlan dispensePlan = new DispensePlan();
        PlanRuleDetail planRuleDetail = new PlanRuleDetail();
        planRuleDetail.setId(101l);
        planRuleDetail.setActive(true);
        GroupDTO group = new GroupDTO();
        group.setId(1l);
        group.setName("DRUG");
        group.setCode("123DRUG");
        PlanRuleComponent components = new PlanRuleComponent();
        components.setId(1l);
        components.setName("Crocin");
        components.setCode("crocin-1");
        components.setGroups(group);
        //planRuleDetail.setComponent(components);
        planRuleDetail.setPatientCopayment(60.0f);
        planRuleDetail.setSponsorPayment(40.0f);
        planRuleDetail.setPlanRuleType(PlanRuleType.Item);

        //Base discount
        AppliedOnBasePatientSponsor baseDiscount = new AppliedOnBasePatientSponsor();
        baseDiscount.setAppliedType(BaseSponsorPatientType.Discount);
        baseDiscount.setValue(2.0f);
        baseDiscount.setValueType(BaseSponsorPatientValueType.Percentage);
        planRuleDetail.setAppliedOnBase(baseDiscount);

        //Sponsor discount
        AppliedOnBasePatientSponsor sponsorDiscount = new AppliedOnBasePatientSponsor();
        sponsorDiscount.setAppliedType(BaseSponsorPatientType.Discount);
        sponsorDiscount.setValue(1.0f);
        sponsorDiscount.setValueType(BaseSponsorPatientValueType.Percentage);
        planRuleDetail.setAppliedOnSponsorAmount(sponsorDiscount);

        //Patient discount
        AppliedOnBasePatientSponsor patientDiscount = new AppliedOnBasePatientSponsor();
        patientDiscount.setAppliedType(BaseSponsorPatientType.Discount);
        patientDiscount.setValue(5.0f);
        patientDiscount.setValueType(BaseSponsorPatientValueType.Percentage);
        planRuleDetail.setAppliedOnPatientAmount(patientDiscount);

        //Tax paid by sponsor
        planRuleDetail.setSponsorPayTax(true);

        List<PlanRuleDetail> planRuleDetails = new ArrayList<>();
        planRuleDetails.add(planRuleDetail);
        PlanRules planRules = new PlanRules(planRuleDetails);

        PlanRule planRule = new PlanRule();
        planRule.setPlanRules(planRules);
        dispensePlan.setPlanRule(planRule);

        return dispensePlan;
    }

    //Test case
    public static DispenseDocument getDispenseWithoutPlan() {
        return getDispenseDocument();
    }

    //Test case
    public static DispenseDocument getDispenseWithPlan() {
        DispenseDocument dispenseDocument = getDispenseDocument();
        List<DispensePlan> dispensePlans = new ArrayList<>();
        dispensePlans.add(populatePlanWithOneRuleDetail());
        dispenseDocument.setDispensePlans(dispensePlans);

        return dispenseDocument;
    }

    public static DispenseDocument getDispenseDocument() {
        DispenseDocument dispenseDocument = new DispenseDocument();

        HealthcareServiceCenterDTO hsc = new HealthcareServiceCenterDTO();
        hsc.setId(1l);
        hsc.setCode("hsc-1");
        hsc.setName("Healthcare service center HSR Layout");
        OrganizationDTO org = new OrganizationDTO();
        org.setId(11l);
        org.setCode("Org_1");
        org.setName("Organization Name");
        hsc.setPartOf(org);
        dispenseDocument.setHsc(hsc);

        //dispense unit
        OrganizationDTO unit = new OrganizationDTO();
        unit.setId(2001l);
        unit.setCode("unit-1001");
        dispenseDocument.setDispenseUnit(unit);

        UserDTO user = new UserDTO();
        user.setId(1001l);
        user.setDisplayName("User 1");
        user.setLogin("Admin");
        user.setEmployeeNo("335036");
        dispenseDocument.setDispenseUser(user);

        dispenseDocument.setUnitDiscountPercentage(2f);
        //dispenseDocument.setUserDiscountPercentage(1f);
        //dispenseDocument.setUserDiscountAmount(5.0f);
        dispenseDocument.setDiscountPercentage(false);

        //Dispense items
        DispenseDocumentLine dispenseDocLine = new DispenseDocumentLine();
        dispenseDocLine.setCode("orderitem-1");
        dispenseDocLine.setName("Crocin");
        dispenseDocLine.setQuantity(2f);
        dispenseDocLine.setItemId(1l);
        dispenseDocLine.setLineNumber(1l);
        dispenseDocLine.setMrp(getBigDecimal(100f));


        List<DispenseTax> dispenseLineTaxes = new ArrayList<>();
        dispenseLineTaxes.add(getTaxDefinition("SGST", 5.0f));
        dispenseLineTaxes.add(getTaxDefinition("CGST", 5.0f));
        dispenseDocLine.setDispenseTaxes(dispenseLineTaxes);

        dispenseDocLine.setItemDiscount(true);
        dispenseDocLine.setPercentDiscount(true);
        dispenseDocLine.setUserDiscount(getBigDecimal(2f));
        dispenseDocLine.setTotalTaxInPercent(10.0f);
        //dispenseDocLine.setUnitDiscount(2f);

        //Dispense document line
        List<DispenseDocumentLine> dispenseDocumentLines = new ArrayList<>();
        dispenseDocumentLines.add(dispenseDocLine);
        dispenseDocument.setDispenseDocumentLines(dispenseDocumentLines);
        dispenseDocument.setDispenseStatus(DispenseStatus.DRAFT);

        return dispenseDocument;
    }

    static DispenseTax getTaxDefinition(String taxCode, float percent) {
        DispenseTax dispenseTax = new DispenseTax();
        dispenseTax.setTaxCode(taxCode);
        dispenseTax.setDefinition(taxCode);

        TaxDefinition taxDefinition = new TaxDefinition();
        taxDefinition.setId(1l);
        taxDefinition.setName(taxCode);

        TaxCalculation taxCalculation = new TaxCalculation();
        taxCalculation.setType(Type.Percentage);
        taxCalculation.setPercentage(percent);
        taxDefinition.setTaxCalculation(taxCalculation);
        dispenseTax.setTaxDefinition(taxDefinition);

        return dispenseTax;
    }

}
