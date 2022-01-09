package org.nh.pharmacy.web.rest.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.nh.billing.domain.PlanRule;
import org.nh.billing.domain.PlanRules;
import org.nh.billing.domain.dto.*;
import org.nh.billing.domain.enumeration.*;
import org.nh.common.dto.GroupDTO;
import org.nh.pharmacy.web.rest.errors.CustomParameterizedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransformWorkBookToRuleUtils {

    /**
     * @return
     * @throws IOException
     */
    public static PlanRule uploadPlanRuleExcel(InputStream fis, PlanRule planRule) throws IOException {

        Map<String, Object> result = new HashMap<>();
        String[] visitTypeList = null;
        List<String> visitTypes = null;
        List<PlanRuleDetail> planDetailList = null;
        List<Long> ruleIds = new ArrayList<Long>();
        PlanRule createNewPlanRule = null;
        PlanRules planRules = null;
        int cellIndex = 0;
        if (fis != null) {
            //copy plan and create with new version
            if (null == planRule) {
                throw new CustomParameterizedException("10068", "Plan Rule doesn't exist with id:" + planRule);
            } else {
                createNewPlanRule = new PlanRule();
                planRules = new PlanRules();
                createNewPlanRule.setType(planRule.getType());
                createNewPlanRule.setTypeCode(planRule.getTypeCode() == null ? null : planRule.getTypeCode().toString());
                createNewPlanRule.setLevel(planRule.getLevel());
                createNewPlanRule.setUuid(planRule.getUuid());

                Integer versionNumer = planRule.getVersion();
                createNewPlanRule.setVersion(versionNumer + 1);
                createNewPlanRule.setTypeId(planRule.getTypeId());
                createNewPlanRule.effectiveFrom(planRule.getEffectiveFrom());
                createNewPlanRule.effectiveTo(planRule.getEffectiveTo());
                planDetailList = new ArrayList<PlanRuleDetail>();
                XSSFWorkbook workbook = new XSSFWorkbook(fis);
                XSSFSheet sheet = workbook.getSheetAt(0);
                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    PlanRuleDetail planRuleDetail = new PlanRuleDetail();
                    Row row = sheet.getRow(i);
                    cellIndex = 0;
                    PlanRuleComponent component = new PlanRuleComponent();
                    GroupDTO group = new GroupDTO();
                    ItemGroup itemGroup = new ItemGroup();
                    ItemCategory itemCategory = new ItemCategory();
                    ItemType itemType = new ItemType();
                    Cell planRuleCell = row.getCell(cellIndex++);//0
                    Number planRuleId = planRuleCell == null ? null : planRuleCell.getNumericCellValue();
                    planRuleDetail.setId((planRuleId == null) ? null : planRuleId.longValue());
                    Cell vplanRuleTypeCell = row.getCell(cellIndex++);//1
                    String planRuleType = vplanRuleTypeCell == null ? null : vplanRuleTypeCell.getStringCellValue();
                    if (null == planRuleType || planRuleType.isEmpty()) {
                        break;
                    }
                    planRuleDetail.setPlanRuleType(((planRuleType == null) || (planRuleType.isEmpty())) ? null : (PlanRuleType.valueOf(planRuleType)));
                    Cell visitTypeCell = row.getCell(cellIndex++);//2
                    String visitType = visitTypeCell == null ? null : visitTypeCell.getStringCellValue();
                    if (null != visitType && !visitType.isEmpty()) {
                        visitTypes = new ArrayList<>();
                        visitTypeList = visitType.split(",");
                        for (String vlues : visitTypeList) {
                            visitTypes.add(vlues);
                        }
                    }
                    planRuleDetail.setVisitType(visitTypes);
                    Cell genderCell = row.getCell(cellIndex++);//3
                    String gender = genderCell == null ? null : genderCell.getStringCellValue();
                    planRuleDetail.setGender(((gender == null) || (gender.isEmpty())) ? null : gender);
                    Cell minAgeCell = row.getCell(cellIndex++);//4
                    if (null != minAgeCell && minAgeCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        Number minAge = minAgeCell == null ? null : minAgeCell.getNumericCellValue();
                        planRuleDetail.setMinAge((minAge == null || minAge.toString().isEmpty()) ? null : minAge.intValue());
                    } else {
                        Integer minAge = (minAgeCell == null || minAgeCell.getStringCellValue().trim().isEmpty()) ? null : Integer.valueOf(minAgeCell.getStringCellValue());
                        planRuleDetail.setMinAge((minAge == null || minAge.toString().isEmpty()) ? null : minAge.intValue());
                    }
                    Cell maxAgeCell = row.getCell(cellIndex++);//5
                    if (null != maxAgeCell && maxAgeCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        Number maxAge = maxAgeCell == null ? null : maxAgeCell.getNumericCellValue();
                        planRuleDetail.setMaxAge((maxAge == null || maxAge.toString().isEmpty()) ? null : maxAge.intValue());
                    } else {
                        Integer maxAge = (maxAgeCell == null || maxAgeCell.getStringCellValue().trim().isEmpty()) ? null : Integer.valueOf(maxAgeCell.getStringCellValue());
                        planRuleDetail.setMaxAge((maxAge == null || maxAge.toString().isEmpty()) ? null : maxAge.intValue());
                        ;
                    }
                    Cell daysCell = row.getCell(cellIndex++);//6
                    String days = daysCell == null ? null : daysCell.getStringCellValue();
                    planRuleDetail.setDays(((days == null) || (days.isEmpty())) ? null : Days.valueOf(days));
                    Cell minAmountCell = row.getCell(cellIndex++);//7
                    if (null != minAmountCell && minAmountCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        Number minAmount = minAmountCell == null ? null : minAmountCell.getNumericCellValue();
                        planRuleDetail.setMinAmount(minAmount == null ? null : minAmount.floatValue());
                    } else {
                        Float minAmount = (minAmountCell == null || minAmountCell.getStringCellValue().isEmpty()) ? null : Float.valueOf(minAmountCell.getStringCellValue());
                        planRuleDetail.setMinAmount(minAmount == null ? null : minAmount.floatValue());
                    }
                    Cell maxAmountCell = row.getCell(cellIndex++);//8
                    if (null != maxAmountCell && maxAmountCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        Number maxAmount = maxAmountCell == null ? null : maxAmountCell.getNumericCellValue();
                        planRuleDetail.setMaxAmount(maxAmount == null ? null : maxAmount.floatValue());
                    } else {
                        Float maxAmount = (maxAmountCell == null || maxAmountCell.getStringCellValue().isEmpty()) ? null : Float.valueOf(maxAmountCell.getStringCellValue());
                        planRuleDetail.setMaxAmount(maxAmount == null ? null : maxAmount.floatValue());
                    }
                    Cell minQuantityCell = row.getCell(cellIndex++);//9
                    if (null != minQuantityCell && minQuantityCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        Number minQuantity = minQuantityCell == null ? null : minQuantityCell.getNumericCellValue();
                        planRuleDetail.setMinQuantity(minQuantity == null ? null : minQuantity.intValue());
                    } else {
                        Integer minQuantity = (minQuantityCell == null || minQuantityCell.getStringCellValue().isEmpty()) ? null : Integer.valueOf(minQuantityCell.getStringCellValue());
                        planRuleDetail.setMinQuantity(minQuantity == null ? null : minQuantity.intValue());
                    }
                    Cell maxQuantityCell = row.getCell(cellIndex++);//10
                    if (null != maxQuantityCell && maxQuantityCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        Number maxQuantity = maxQuantityCell == null ? null : maxQuantityCell.getNumericCellValue();
                        planRuleDetail.setMaxQuantity(maxQuantity == null ? null : maxQuantity.intValue());
                        ;
                    } else {
                        Integer maxQuantity = (maxQuantityCell == null || maxQuantityCell.getStringCellValue().isEmpty()) ? null : Integer.valueOf(maxQuantityCell.getStringCellValue());
                        planRuleDetail.setMaxQuantity(maxQuantity == null ? null : maxQuantity.intValue());
                    }
                    Cell activeCell = row.getCell(cellIndex++);//11
                    if (null != activeCell && activeCell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
                        Boolean active = activeCell == null ? null : activeCell.getBooleanCellValue();
                        planRuleDetail.setActive(active == null ? null : active);
                    } else {
                        String active = (activeCell == null || activeCell.getStringCellValue().isEmpty()) ? null : activeCell.getStringCellValue();
                        planRuleDetail.setActive(active == null ? null : Boolean.valueOf(active));
                    }
                    Cell authorizationExclusionCell = row.getCell(cellIndex++);//12
                    if (null != authorizationExclusionCell && authorizationExclusionCell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
                        Boolean authorizationExclusion = authorizationExclusionCell == null ? null : authorizationExclusionCell.getBooleanCellValue();
                        planRuleDetail.setAuthorizationExclusion(authorizationExclusion == null ? null : authorizationExclusion);
                    } else {
                        String authorizationExclusion = (authorizationExclusionCell == null || authorizationExclusionCell.getStringCellValue().isEmpty()) ? null : authorizationExclusionCell.getStringCellValue();
                        planRuleDetail.setAuthorizationExclusion(authorizationExclusion == null ? null : Boolean.valueOf(authorizationExclusion));
                    }
                    Cell exclusionCell = row.getCell(cellIndex++);//13
                    if (null != exclusionCell && exclusionCell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
                        Boolean exclusion = exclusionCell == null ? null : exclusionCell.getBooleanCellValue();
                        planRuleDetail.setExclusion(exclusion == null ? null : exclusion);
                    } else {
                        String exclusion = (exclusionCell == null || exclusionCell.getStringCellValue().isEmpty()) ? null : exclusionCell.getStringCellValue();
                        planRuleDetail.setExclusion(exclusion == null ? null : Boolean.valueOf(exclusion));
                    }

                    Cell patientCopaymentCell = row.getCell(cellIndex++);//14
                    Cell sponsorPaymentCell = row.getCell(cellIndex++);//15

                    if ((exclusionCell.getCellType() == Cell.CELL_TYPE_BOOLEAN && !exclusionCell.getBooleanCellValue()) || (exclusionCell.getCellType() == Cell.CELL_TYPE_STRING && !Boolean.valueOf(exclusionCell.getStringCellValue()))) {
                        Number patientCopayment = patientCopaymentCell == null ? null : patientCopaymentCell.getNumericCellValue();
                        Number sponsorPayment = sponsorPaymentCell == null ? null : sponsorPaymentCell.getNumericCellValue();
                        if ((sponsorPayment != null && patientCopayment != null) && (sponsorPayment.intValue()) + (patientCopayment.intValue()) == 100) {
                            planRuleDetail.setPatientCopayment(patientCopayment == null ? 0 : patientCopayment.floatValue());
                            planRuleDetail.setSponsorPayment(sponsorPayment == null ? null : sponsorPayment.floatValue());
                        } else {
                            throw new CustomParameterizedException("10071", "Sum of Patient Payment and Sponser Copayment must be 100% -Row Number :" + i);
                        }
                    } else {
                        planRuleDetail.setPatientCopayment(0F);
                        planRuleDetail.setSponsorPayment(0F);
                    }
                    AppliedOnBasePatientSponsor appliedOnBase = new AppliedOnBasePatientSponsor();
                    Cell appliedOnBaseTypeCell = row.getCell(cellIndex++);//16
                    String appliedOnBaseType = appliedOnBaseTypeCell == null ? null : appliedOnBaseTypeCell.getStringCellValue();
                    appliedOnBase.setAppliedType((appliedOnBaseType == null || appliedOnBaseType.isEmpty()) ? null : BaseSponsorPatientType.valueOf(appliedOnBaseType.toString()));
                    Cell appliedOnBasevalueCell = row.getCell(cellIndex++);//17
                    if (null != appliedOnBasevalueCell && appliedOnBasevalueCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        Number appliedOnBasevalue = appliedOnBasevalueCell == null ? null : appliedOnBasevalueCell.getNumericCellValue();
                        appliedOnBase.setValue(appliedOnBasevalue == null ? null : appliedOnBasevalue.floatValue());
                    } else {
                        Float appliedOnBasevalue = (appliedOnBasevalueCell == null || appliedOnBasevalueCell.getStringCellValue().isEmpty()) ? null : Float.valueOf(appliedOnBasevalueCell.getStringCellValue());
                        appliedOnBase.setValue(appliedOnBasevalue == null ? null : appliedOnBasevalue.floatValue());
                    }
                    Cell appliedOnBasevalueTypeCell = row.getCell(cellIndex++);//18
                    String appliedOnBasevalueType = appliedOnBasevalueTypeCell == null ? null : appliedOnBasevalueTypeCell.getStringCellValue();
                    appliedOnBase.setValueType((appliedOnBasevalueType == null || appliedOnBasevalueType.isEmpty()) ? null : BaseSponsorPatientValueType.valueOf(appliedOnBasevalueType));
                    if (null != appliedOnBase && ((appliedOnBase.getValue() != null && !appliedOnBase.getValue().toString().isEmpty()) && (appliedOnBase.getValueType() != null && !appliedOnBase.getValueType().toString().isEmpty()) && (appliedOnBase.getAppliedType() != null && !appliedOnBase.getAppliedType().toString().isEmpty()))) {
                        planRuleDetail.setAppliedOnBase(appliedOnBase);
                    } else {
                        planRuleDetail.setAppliedOnBase(null);
                    }
                    AppliedOnBasePatientSponsor appliedOnPatientAmount = new AppliedOnBasePatientSponsor();
                    Cell appliedOnPatientAmountTypeCell = row.getCell(cellIndex++);//19
                    String appliedOnPatientAmountType = appliedOnPatientAmountTypeCell == null ? null : appliedOnPatientAmountTypeCell.getStringCellValue();
                    appliedOnPatientAmount.setAppliedType((appliedOnPatientAmountType == null || appliedOnPatientAmountType.isEmpty()) ? null : BaseSponsorPatientType.valueOf(appliedOnPatientAmountType.toString()));
                    Cell appliedOnPatientAmountValueCell = row.getCell(cellIndex++);//20
                    if (null != appliedOnPatientAmountValueCell && appliedOnPatientAmountValueCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        Number appliedOnPatientAmountValue = appliedOnPatientAmountValueCell == null ? null : appliedOnPatientAmountValueCell.getNumericCellValue();
                        appliedOnPatientAmount.setValue(appliedOnPatientAmountValue == null ? null : appliedOnPatientAmountValue.floatValue());
                    } else {
                        Float appliedOnPatientAmountValue = (appliedOnPatientAmountValueCell == null || appliedOnPatientAmountValueCell.getStringCellValue().isEmpty()) ? null : Float.valueOf(appliedOnPatientAmountValueCell.getStringCellValue());
                        appliedOnPatientAmount.setValue(appliedOnPatientAmountValue == null ? null : appliedOnPatientAmountValue.floatValue());
                    }

                    Cell appliedOnPatientAmountValueTypeCell = row.getCell(cellIndex++);//21
                    String appliedOnPatientAmountValueType = appliedOnPatientAmountValueTypeCell == null ? null : appliedOnPatientAmountValueTypeCell.getStringCellValue();
                    appliedOnPatientAmount.setValueType((appliedOnPatientAmountValueType == null || appliedOnPatientAmountValueType.isEmpty()) ? null : BaseSponsorPatientValueType.valueOf(appliedOnPatientAmountValueType.toString()));
                    if (null != appliedOnPatientAmount && ((appliedOnPatientAmount.getValue() != null && !appliedOnPatientAmount.getValue().toString().isEmpty()) && (appliedOnPatientAmount.getValueType() != null && !appliedOnPatientAmount.getValueType().toString().isEmpty()) && (appliedOnPatientAmount.getAppliedType() != null && !appliedOnPatientAmount.getAppliedType().toString().isEmpty()))) {
                        planRuleDetail.setAppliedOnPatientAmount(appliedOnPatientAmount);
                    } else {
                        planRuleDetail.setAppliedOnPatientAmount(null);
                    }
                    AppliedOnBasePatientSponsor appliedOnSponsorAmount = new AppliedOnBasePatientSponsor();
                    Cell appliedOnSponsorAmountTypeCell = row.getCell(cellIndex++);//22
                    String appliedOnSponsorAmountType = appliedOnSponsorAmountTypeCell == null ? null : appliedOnSponsorAmountTypeCell.getStringCellValue();
                    appliedOnSponsorAmount.setAppliedType((appliedOnSponsorAmountType == null || appliedOnSponsorAmountType.isEmpty()) ? null : BaseSponsorPatientType.valueOf(appliedOnSponsorAmountType.toString()));
                    Cell appliedOnSponsorAmountValueCell = row.getCell(cellIndex++);//23
                    if (null != appliedOnSponsorAmountValueCell && appliedOnSponsorAmountValueCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        Number appliedOnSponsorAmountValue = appliedOnSponsorAmountValueCell == null ? null : appliedOnSponsorAmountValueCell.getNumericCellValue();
                        appliedOnSponsorAmount.setValue(appliedOnSponsorAmountValue == null ? null : appliedOnSponsorAmountValue.floatValue());
                    } else {
                        Float appliedOnSponsorAmountValue = (appliedOnSponsorAmountValueCell == null || appliedOnSponsorAmountValueCell.getStringCellValue().isEmpty()) ? null : Float.valueOf(appliedOnSponsorAmountValueCell.getStringCellValue());
                        appliedOnSponsorAmount.setValue(appliedOnSponsorAmountValue == null ? null : appliedOnSponsorAmountValue.floatValue());
                    }

                    Cell appliedOnSponsorAmountValueTypeCell = row.getCell(cellIndex++);//24
                    String appliedOnSponsorAmountValueType = appliedOnSponsorAmountValueTypeCell == null ? null : appliedOnSponsorAmountValueTypeCell.getStringCellValue();
                    appliedOnSponsorAmount.setValueType((appliedOnSponsorAmountValueType == null || appliedOnSponsorAmountValueType.isEmpty()) ? null : BaseSponsorPatientValueType.valueOf(appliedOnSponsorAmountValueType.toString()));
                    if (null != appliedOnSponsorAmount && ((appliedOnSponsorAmount.getValue() != null && !appliedOnSponsorAmount.getValue().toString().isEmpty()) && (appliedOnSponsorAmount.getValueType() != null && !appliedOnSponsorAmount.getValueType().toString().isEmpty()) && (appliedOnSponsorAmount.getAppliedType() != null && !appliedOnSponsorAmount.getAppliedType().toString().isEmpty()))) {
                        planRuleDetail.setAppliedOnSponsorAmount(appliedOnSponsorAmount);
                    } else {
                        planRuleDetail.setAppliedOnSponsorAmount(null);
                    }
                    Cell genericCell = row.getCell(cellIndex++);//25
                    if (null != genericCell && genericCell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
                        Boolean generic = genericCell == null ? null : genericCell.getBooleanCellValue();
                        planRuleDetail.setIsGeneric(generic == null ? null : generic);
                    } else {
                        String generic = (genericCell == null || genericCell.getStringCellValue().isEmpty()) ? null : genericCell.getStringCellValue();
                        planRuleDetail.setIsGeneric(generic == null ? null : Boolean.valueOf(generic));
                    }
                    Cell groupNameCell = row.getCell(cellIndex++);
                    String groupName = (groupNameCell == null || groupNameCell.getStringCellValue().isEmpty()) ? null : groupNameCell.getStringCellValue();
                    group.setName(groupName == null ? null : groupName);

                    Cell groupcodeCell = row.getCell(cellIndex++);

                    Cell groupIdCell = row.getCell(cellIndex++);

                    if(null!=groupName && ! groupName.isEmpty()) {
                        String groupCode = (groupcodeCell == null || groupcodeCell.getStringCellValue().isEmpty()) ? null : groupcodeCell.getStringCellValue();
                        group.setCode(groupCode == null ? null : groupCode);


                        Number groupId = null;

                        if (null != groupIdCell && groupIdCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            groupId = groupIdCell == null ? null : groupIdCell.getNumericCellValue();
                            group.setId(groupId == null ? null : groupId.longValue());
                        } else if (null != groupIdCell && groupIdCell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                            FormulaEvaluator formulaEval = workbook.getCreationHelper().createFormulaEvaluator();
                            String data = formulaEval.evaluate(groupIdCell).formatAsString();
                            Number groupIds = Double.valueOf(data);
                            groupId = (groupIdCell == null || data.isEmpty()) ? null : groupIds.longValue();
                            group.setId(groupId == null ? null : groupId.longValue());
                            ;
                        } else {
                            groupId = (groupIdCell == null || groupIdCell.getStringCellValue().isEmpty()) ? null : Long.valueOf(groupIdCell.getStringCellValue());
                            group.setId(groupId == null ? null : groupId.longValue());
                            ;
                        }
                        if(null!=groupId && !groupId.toString().isEmpty()) {
                            planRuleDetail.setGroup(group);
                        }
                    }
                    Cell aliasNameCell = row.getCell(cellIndex++);//27
                    String aliasName = aliasNameCell == null ? null : aliasNameCell.getStringCellValue();
                    planRuleDetail.setAliasName(((aliasName == null) || (aliasName.isEmpty())) ? null : aliasName);
                    Cell aliasCodeCell = row.getCell(cellIndex++);//28
                    String aliasCode = aliasCodeCell == null ? null : aliasCodeCell.getStringCellValue();
                    planRuleDetail.setAliasCode(((aliasCode == null) || (aliasCode.isEmpty())) ? null : aliasCode);
                    Cell sponsorPayTaxCell = row.getCell(cellIndex++);//29
                    if (null != sponsorPayTaxCell && sponsorPayTaxCell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
                        Boolean sponsorPayTax = sponsorPayTaxCell == null ? null : sponsorPayTaxCell.getBooleanCellValue();
                        planRuleDetail.setSponsorPayTax(sponsorPayTax == null ? false : sponsorPayTax);
                    } else {
                        String sponsorPayTax = (sponsorPayTaxCell == null || sponsorPayTaxCell.getStringCellValue().isEmpty()) ? null : sponsorPayTaxCell.getStringCellValue();
                        planRuleDetail.setSponsorPayTax(sponsorPayTax == null ? false : Boolean.valueOf(sponsorPayTax));
                    }
                    Cell tarrifClassCell = row.getCell(cellIndex++);//30
                    String tarrifClass = tarrifClassCell == null ? null : tarrifClassCell.getStringCellValue();
                    planRuleDetail.setTariffClass(((tarrifClass == null) || (tarrifClass.isEmpty())) ? null : tarrifClass);
                    Cell tarrifClassValueCell = row.getCell(cellIndex++);//31
                    String tarrifClassValue = tarrifClassValueCell == null ? null : tarrifClassValueCell.getStringCellValue();
                    planRuleDetail.setTariffClassValue(((tarrifClassValue == null) || (tarrifClassValue.isEmpty())) ? null : tarrifClassValue);
                    Cell parentRuleIdCell = row.getCell(cellIndex++);//32
                    if (null != parentRuleIdCell && parentRuleIdCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        Number parentRuleId = parentRuleIdCell == null ? null : parentRuleIdCell.getNumericCellValue();
                        planRuleDetail.setParentRuleId(parentRuleId == null ? null : parentRuleId.longValue());
                    } else {
                        Long parentRuleId = (parentRuleIdCell == null || parentRuleIdCell.getStringCellValue().isEmpty()) ? null : Long.valueOf(parentRuleIdCell.getStringCellValue());
                        planRuleDetail.setParentRuleId(parentRuleId == null ? null : parentRuleId.longValue());
                    }
                    Cell levelCell = row.getCell(cellIndex++);//33
                    if (null != levelCell && levelCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        Number level = levelCell == null ? null : levelCell.getNumericCellValue();
                        planRuleDetail.setLevel(level == null ? null : level.longValue());
                    } else {
                        Long level = (levelCell == null || levelCell.getStringCellValue().isEmpty()) ? null : Long.valueOf(levelCell.getStringCellValue());
                        planRuleDetail.setLevel(level == null ? null : level.longValue());
                    }
                    Cell componentNameCell = row.getCell(cellIndex++);//34
                    String componentName = (componentNameCell == null || componentNameCell.getStringCellValue().isEmpty()) ? null : componentNameCell.getStringCellValue();
                    component.setName(componentName == null ? null : componentName);
                    Cell componentcodeCell = row.getCell(cellIndex++);

                    Cell componentIdCell = row.getCell(cellIndex++);

                    if (null != componentName && !componentName.isEmpty()) {
                        String componentCpde = (componentcodeCell == null || componentcodeCell.getStringCellValue().isEmpty()) ? null : componentcodeCell.getStringCellValue();
                        component.setCode(componentCpde == null ? null : componentCpde);


                        Number componentId = null;

                        if (null != componentIdCell && componentIdCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            componentId = componentIdCell == null ? null : componentIdCell.getNumericCellValue();
                            component.setId(componentId == null ? null : componentId.longValue());
                        } else if (null != componentIdCell && componentIdCell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                            FormulaEvaluator formulaEval = workbook.getCreationHelper().createFormulaEvaluator();
                            String data = formulaEval.evaluate(componentIdCell).formatAsString();
                            Number componentids = Double.valueOf(data);
                            componentId = (componentIdCell == null || data.isEmpty()) ? null : componentids.longValue();
                            component.setId(componentId == null ? null : componentId.longValue());
                            ;
                        } else {
                            componentId = (componentIdCell == null || componentIdCell.getStringCellValue().isEmpty()) ? null : Long.valueOf(componentIdCell.getStringCellValue());
                            component.setId(componentId == null ? null : componentId.longValue());
                            ;
                        }
                        if (null != componentId && !componentId.toString().isEmpty()) {
                            planRuleDetail.setComponent(component);
                        }
                    }

                    Cell itemGroupNameCell = row.getCell(cellIndex++);
                    String itemGroupName = (itemGroupNameCell == null || itemGroupNameCell.getStringCellValue().isEmpty()) ? null : itemGroupNameCell.getStringCellValue();
                    itemGroup.setName(itemGroupName == null ? null : itemGroupName);

                    Cell itemGroupCodeCell = row.getCell(cellIndex++);

                    Cell itemGroupIdCell = row.getCell(cellIndex++);

                    if(null!=itemGroupName && ! itemGroupName.isEmpty()) {
                        String itemGroupCode = (itemGroupCodeCell == null || itemGroupCodeCell.getStringCellValue().isEmpty()) ? null : itemGroupCodeCell.getStringCellValue();
                        itemGroup.setCode(itemGroupCode == null ? null : itemGroupCode);


                        Number itemGroupId = null;

                        if (null != itemGroupIdCell && itemGroupIdCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            itemGroupId = itemGroupIdCell == null ? null : itemGroupIdCell.getNumericCellValue();
                            itemGroup.setId(itemGroupId == null ? null : itemGroupId.longValue());
                        } else if (null != itemGroupIdCell && itemGroupIdCell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                            FormulaEvaluator formulaEval = workbook.getCreationHelper().createFormulaEvaluator();
                            String data = formulaEval.evaluate(itemGroupIdCell).formatAsString();
                            Number itemGroupIds = Double.valueOf(data);
                            itemGroupId = (itemGroupIdCell == null || data.isEmpty()) ? null : itemGroupIds.longValue();
                            itemGroup.setId(itemGroupId == null ? null : itemGroupId.longValue());
                            ;
                        } else {
                            itemGroupId = (itemGroupIdCell == null || itemGroupIdCell.getStringCellValue().isEmpty()) ? null : Long.valueOf(itemGroupIdCell.getStringCellValue());
                            itemGroup.setId(itemGroupId == null ? null : itemGroupId.longValue());
                            ;
                        }
                        if(null!=itemGroupId && !itemGroupId.toString().isEmpty()) {
                            planRuleDetail.setItemGroup(itemGroup);
                        }
                    }

                    Cell itemCategoryNameCell = row.getCell(cellIndex++);
                    String itemCategoryName = (itemCategoryNameCell == null || itemCategoryNameCell.getStringCellValue().isEmpty()) ? null : itemCategoryNameCell.getStringCellValue();
                    itemCategory.setDescription(itemCategoryName == null ? null : itemCategoryName);

                    Cell itemCategoryCodeCell = row.getCell(cellIndex++);

                    Cell itemCategoryIdCell = row.getCell(cellIndex++);

                    if(null!=itemCategoryName && ! itemCategoryName.isEmpty()) {
                        String itemCategoryCode = (itemCategoryCodeCell == null || itemCategoryCodeCell.getStringCellValue().isEmpty()) ? null : itemCategoryCodeCell.getStringCellValue();
                        itemCategory.setCode(itemCategoryCode == null ? null : itemCategoryCode);


                        Number itemCategoryId = null;

                        if (null != itemCategoryIdCell && itemCategoryIdCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            itemCategoryId = itemCategoryIdCell == null ? null : itemCategoryIdCell.getNumericCellValue();
                            itemCategory.setId(itemCategoryId == null ? null : itemCategoryId.longValue());
                        } else if (null != itemCategoryIdCell && itemCategoryIdCell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                            FormulaEvaluator formulaEval = workbook.getCreationHelper().createFormulaEvaluator();
                            String data = formulaEval.evaluate(itemCategoryIdCell).formatAsString();
                            Number itemCategoryIds = Double.valueOf(data);
                            itemCategoryId = (itemCategoryIdCell == null || data.isEmpty()) ? null : itemCategoryIds.longValue();
                            itemCategory.setId(itemCategoryId == null ? null : itemCategoryId.longValue());
                            ;
                        } else {
                            itemCategoryId = (itemCategoryIdCell == null || itemCategoryIdCell.getStringCellValue().isEmpty()) ? null : Long.valueOf(itemCategoryIdCell.getStringCellValue());
                            itemCategory.setId(itemCategoryId == null ? null : itemCategoryId.longValue());
                            ;
                        }
                        if(null!=itemCategoryId && !itemCategoryId.toString().isEmpty()) {
                            planRuleDetail.setItemCategory(itemCategory);
                        }
                    }

                    Cell itemTypeNameCell = row.getCell(cellIndex++);
                    String itemTypeName = (itemTypeNameCell == null || itemTypeNameCell.getStringCellValue().isEmpty()) ? null : itemTypeNameCell.getStringCellValue();
                    itemType.setName(itemTypeName == null ? null : itemTypeName);

                    Cell itemTypeCodeCell = row.getCell(cellIndex++);

                    Cell itemTypeIdCell = row.getCell(cellIndex++);

                    if(null!=itemTypeName && ! itemTypeName.isEmpty()) {
                        String itemTypeCode = (itemTypeCodeCell == null || itemTypeCodeCell.getStringCellValue().isEmpty()) ? null : itemTypeCodeCell.getStringCellValue();
                        itemType.setCode(itemTypeCode == null ? null : itemTypeCode);


                        Number itemTypeId = null;

                        if (null != itemTypeIdCell && itemTypeIdCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            itemTypeId = itemTypeIdCell == null ? null : itemTypeIdCell.getNumericCellValue();
                            itemType.setId(itemTypeId == null ? null : itemTypeId.longValue());
                        } else if (null != itemTypeIdCell && itemTypeIdCell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                            FormulaEvaluator formulaEval = workbook.getCreationHelper().createFormulaEvaluator();
                            String data = formulaEval.evaluate(itemTypeIdCell).formatAsString();
                            Number itemTypeIds = Double.valueOf(data);
                            itemTypeId = (itemTypeIdCell == null || data.isEmpty()) ? null : itemTypeIds.longValue();
                            itemType.setId(itemTypeId == null ? null : itemTypeId.longValue());
                            ;
                        } else {
                            itemTypeId = (itemTypeIdCell == null || itemTypeIdCell.getStringCellValue().isEmpty()) ? null : Long.valueOf(itemTypeIdCell.getStringCellValue());
                            itemType.setId(itemTypeId == null ? null : itemTypeId.longValue());
                            ;
                        }
                        if(null!=itemTypeId && !itemTypeId.toString().isEmpty()) {
                            planRuleDetail.setItemType(itemType);
                        }
                    }

                    planDetailList.add(planRuleDetail);

                }
                createNewPlanRule.setPlanRules(planRules);
                createNewPlanRule.getPlanRules().setPlanRuleDetailsList(planDetailList);
            }
        }

        return createNewPlanRule;
    }

}
