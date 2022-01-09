INSERT INTO user_master(id, login, first_name, last_name, email, active, display_name, prefix, employee_no, status, date_of_birth, designation,mobile_no, organization_unit_id, department_id, user_type)
	VALUES (nextval('user_master_id_seq'), 'admin', 'Administrator',null, 'admin@localhost', 'true','Administrator',null, 'admin','Active',null,null,null,null, null,'User');
INSERT INTO user_master(id, login, first_name, last_name, email, active, display_name, prefix, employee_no, status, date_of_birth, designation,mobile_no, organization_unit_id, department_id, user_type)
	VALUES (nextval('user_master_id_seq'), '90011Y', 'Manager',null, 'manager@localhost', 'true','Manager',null, '90011Y','Active',null,null,null,null, null,'User');
INSERT INTO user_master(id, login, first_name, last_name, email, active, display_name, prefix, employee_no, status, date_of_birth, designation,mobile_no, organization_unit_id, department_id, user_type)
	VALUES (nextval('user_master_id_seq'), '90011X', 'Aprrover',null, 'aprrover@localhost', 'true','aprrover',null, '90011X','Active',null,null,null,null, null,'User');
INSERT INTO user_master(id, login, first_name, last_name, email, active, display_name, prefix, employee_no, status, date_of_birth, designation,mobile_no, organization_unit_id, department_id, user_type)
	VALUES (nextval('user_master_id_seq'), '90011Z', 'Creater',null, 'creater@localhost', 'true','creater',null, '90011Z','Active',null,null,null,null, null,'User');

INSERT INTO group_master(id, name, code, active, actual, members, group_type, part_of_id,context)
	VALUES (nextval('group_master_id_seq'), 'Administrators','Administrators', 'true', 'true','[{"member":{"code":"admin","name":"Administrator"},"inactive":false}]','{"active":true,"code":"administrator","id":52,"display":"Administrator"}' , null,'Issue_Approval_Committee');
INSERT INTO group_master(id, name, code, active, actual, members, group_type, part_of_id)
	VALUES (nextval('group_master_id_seq'), 'Manangers','Manangers', 'true', 'true','[{"member":{"code":"90011Y","name":"Manager"},"inactive":false}]','{"active":true,"code":"manager","id":52,"display":"Manager"}' , null);
INSERT INTO group_master(id, name, code, active, actual, members, group_type, part_of_id, context)
	VALUES (nextval('group_master_id_seq'), 'IssueApprovalGroup','IssueApprovalGroup', 'true', 'true','[{"member":{"code":"90011X","name":"Aprrover"},"inactive":false}]','{"active":true,"code":"issueApprovalGroup","id":52,"display":"IssueApprovalGroup"}' , null, 'Issue_Approval_Committee');
INSERT INTO group_master(id, name, code, active, actual, members, group_type, part_of_id, context)
	VALUES (nextval('group_master_id_seq'), 'IssueNotificationGroup','IssueNotificationGroup', 'true', 'true','[{"member":{"code":"90011X","name":"Aprrover"},"inactive":false}]','{"active":true,"code":"issueNotificationGroup","id":52,"display":"IssueNotificationGroup"}' , null, 'Issue_Notification_Committee');
INSERT INTO group_master(id, name, code, active, actual, members, group_type, part_of_id, context)
	VALUES (nextval('group_master_id_seq'), 'ConsumptionApprovalGroup','ConsumptionApprovalGroup', 'true', 'true','[{"member":{"code":"90011X","name":"Aprrover"},"inactive":false}]','{"active":true,"code":"consumptionApprovalGroup","id":52,"display":"ConsumptionApprovalGroup"}' , null, 'Consumption_Approval_Committee');
INSERT INTO group_master(id, name, code, active, actual, members, group_type, part_of_id, context)
	VALUES (nextval('group_master_id_seq'), 'ReceiptApprovalGroup','ReceiptApprovalGroup', 'true', 'true','[{"member":{"code":"90011X","name":"Aprrover"},"inactive":false}]','{"active":true,"code":"receiptApprovalGroup","id":52,"display":"ReceiptApprovalGroup"}' , null, 'Receipt_Approval_Committee');
INSERT INTO group_master(id, name, code, active, actual, members, group_type, part_of_id, context)
	VALUES (nextval('group_master_id_seq'), 'DiscountApprovalGroup','DiscountApprovalGroup', 'true', 'true','[{"member":{"code":"90011X","name":"Aprrover"},"inactive":false}]','{"active":true,"code":"discountApprovalGroup","id":52,"display":"DiscountApprovalGroup"}' , null, 'Discount_Approval_Committee');
INSERT INTO group_master(id, name, code, active, actual, members, group_type, part_of_id, context)
	VALUES (nextval('group_master_id_seq'), 'CorrectionApprovalGroup','CorrectionApprovalGroup', 'true', 'true','[{"member":{"code":"90011X","name":"Aprrover"},"inactive":false}]','{"active":true,"code":"correctionApprovalGroup","id":52,"display":"CorrectionApprovalGroup"}' , null, 'Correction_Approval_Committee');
INSERT INTO group_master(id, name, code, active, actual, members, group_type, part_of_id, context)
	VALUES (nextval('group_master_id_seq'), 'DirectTransferApprovalGroup','DirectTransferApprovalGroup', 'true', 'true','[{"member":{"code":"90011X","name":"Aprrover"},"inactive":false}]','{"active":true,"code":"directTransferApprovalGroup","id":52,"display":"DirectTransferApprovalGroup"}' , null, 'DirectTransfer_Approval_Committee');
INSERT INTO group_master(id, name, code, active, actual, members, group_type, part_of_id, context)
	VALUES (nextval('group_master_id_seq'), 'DirectTransferNotificationGroup','DirectTransferNotificationGroup', 'true', 'true','[{"member":{"code":"90011X","name":"Aprrover"},"inactive":false}]','{"active":true,"code":"directTransferNotificationGroup","id":52,"display":"DirectTransferNotificationGroup"}' , null, 'DirectTransfer_Notification_Committee');
INSERT INTO group_master(id, name, code, active, actual, members, group_type, part_of_id, context)
	VALUES (nextval('group_master_id_seq') + 1, 'DispenseReturnApprovalGroup','DispenseReturnApprovalGroup', 'true', 'true','[{"member":{"code":"90011X","name":"Aprrover"},"inactive":false}]','{"active":true,"code":"dispenseReturnApprovalGroup","id":52,"display":"DispenseReturnApprovalGroup"}' , null, 'DispenseReturn_Approval_Committee');
INSERT INTO group_master(id, name, code, active, actual, members, group_type, part_of_id, context)
	VALUES (nextval('group_master_id_seq') + 1, 'AuditApprovalGroup','AuditApprovalGroup', 'true', 'true','[{"member":{"code":"90011X","name":"Aprrover"},"inactive":false}]','{"active":true,"code":"auditApprovalGroup","id":52,"display":"AuditApprovalGroup"}' , null, 'Audit_Approval_Committee');
INSERT INTO group_master(id, name, code, active, actual, members, group_type, part_of_id, context)
	VALUES (nextval('group_master_id_seq') + 1, 'AdjustmentApprovalGroupLevelOne','AdjustmentApprovalGroupLevelOne', 'true', 'true','[{"member":{"code":"90011X","name":"Aprrover"},"inactive":false}]','{"active":true,"code":"adjustmentApprovalGroupLevelOne","id":52,"display":"AdjustmentApprovalGroupLevelOne"}' , null, 'Adjustment_Level_One_Approval_Committee');
INSERT INTO group_master(id, name, code, active, actual, members, group_type, part_of_id, context)
	VALUES (nextval('group_master_id_seq') + 1, 'AdjustmentApprovalGroupLevelTwo','AdjustmentApprovalGroupLevelTwo', 'true', 'true','[{"member":{"code":"90011Y","name":"Manager"},"inactive":false}]','{"active":true,"code":"adjustmentApprovalGroupLevelTwo","id":52,"display":"AdjustmentApprovalGroupLevelTwo"}' , null, 'Adjustment_Level_Two_Approval_Committee');
INSERT INTO group_master(id, name, code, active, actual, members, group_type, part_of_id, context)
	VALUES (nextval('group_master_id_seq') + 1, 'IndentApprovalGroup-Unit1','IndentApprovalGroup-Unit1', 'true', 'true','[{"member":{"code":"90011X","name":"Aprrover"},"inactive":false}]','{"active":true,"code":"indentApprovalGroup","id":52,"display":"IndentApprovalGroup"}' ,1, 'Indent_Approval_Committee');
INSERT INTO group_master(id, name, code, active, actual, members, group_type, part_of_id, context)
	VALUES (nextval('group_master_id_seq') + 1, 'IndentApprovalGroup-Global','IndentApprovalGroup-Global', 'true', 'true','[{"member":{"code":"90011X","name":"Aprrover"},"inactive":false}]','{"active":true,"code":"indentApprovalGroup","id":52,"display":"IndentApprovalGroup"}' ,null, 'Indent_Approval_Committee');
INSERT INTO group_master(id, name, code, active, actual, members, group_type, part_of_id, context)
	VALUES (nextval('group_master_id_seq') + 1, 'IndentApprovalGroup-Unit1-Final','IndentApprovalGroup-Unit1-Final', 'true', 'true','[{"member":{"code":"90011X","name":"Aprrover"},"inactive":false}]','{"active":true,"code":"indentApprovalGroup","id":52,"display":"IndentApprovalGroup"}' ,1, 'Indent_Approval_Committee');






