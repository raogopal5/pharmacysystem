<html>

<head>
    <style>
            table {
                border-spacing: 0px;
                font-family: "Calibri";
                font-size: 13px;
                font-color: #000;
                border-collapse: collapse;
            }

            .topDown {
                margin-top: 40px;
            }

            .commonfont {
                font-family: "Calibri";
		font-size: 13px;
            }

            .text-right {
                text-align: right;
            }

            .headfirst {
                font-size: 18px;
            }

            .headtop {
                font-size: 12px;
                font-weight: bold;
            }

            .topblank {
                padding-top: 10px;
            }

            .unit-grey {
                color: #717171;
            }

            .text-center {
                text-align: center;
            }

            .footfirst {
                font-size: 13px;
                color: #717171;
            }

            .footfont {
                font-size: 8px;
                color: #717171;
            }

            .topline {
                line-height: 90%;
            }

            .leftcolumn {
                margin-left: 50%;
            }

            .leftcolumns {
                width: 60%;
                float: left;
                font-size: 13px;
                line-height: 15px;
            }

            .rightcolumns {
                width: 40%;
                float: left;
                font-size: 13px;
                line-height: 15px;
                padding-top: 4px;
            }

            .rightcolumnslast {
                width: 30%;
                float: left;
                font-size: 13px;
                line-height: 19px;
                padding-top: 4px;
                margin-top: -9px;
            }

            .sgst {
                color: #717171;
                display: block;
                font-size: 8px;
            }

            .subsection {
                font-size: 8px;
            }

            .amount {
                font-weight: bold;
                font-size: 16px;
            }

            .colorapply {
                color: red;
            }

            .headalign {
                text-align: right;
            }

            .borderapply tr td {
                border: 1px solid #DEDEDE;
            }

            hr {
                display: block;
                margin-top: 4em;
                margin-bottom: -0.5em;
                border-style: inset;
                border-width: 0.5px;
                border-color: #000;
            }
            p{
                padding: 0px;
                margin: 0px;
            }

            .ind-items {
                -fs-table-paginate: paginate;
            }

            .ind-items tr {
                 page-break-inside: avoid;
            }
        </style>
</head>

<body>
    <div class="headfirst text-center">Stock Consumption</div>
    <table width="100%" class="fcommonfont">
        <tr>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td>&nbsp;</td>
        </tr>
    </table>
    <div width="100%" class="commonfont">
        <div class="leftcolumns">
            <p><strong>Consumption No. : </strong><#if consumptionNo??> ${consumptionNo} <#else> - </#if></p>
        </div>
    <div class="rightcolumns">
            <p><strong>Issue Store : </strong><#if issueStore??> ${issueStore} <#else> - </#if></p>
        </div>
       <div class="leftcolumns">
            <p><strong>Consumption Date : </strong><#if consumptionDate??> ${consumptionDate} <#else> - </#if></p>
        </div>
       <div class="rightcolumns">
            <p><strong>Consumption Store : </strong><#if consumptionStore??> ${consumptionStore} <#else> - </#if></p>
        </div>
      <div class="leftcolumns">
            <p><strong>Consumption Unit : </strong><#if consumptionUnit??> ${consumptionUnit} <#else> - </#if></p>
        </div>
        <div class="rightcolumns">
            <p><strong>For Department : </strong>
                <#if forDept??> ${forDept} <#else> - </#if>
            </p>
        </div>
      <div class="leftcolumns">
            <p><strong> Status : </strong><#if status??> ${status} <#else> - </#if></p>
        </div>
      <div class="rightcolumns">
            <p><strong>For Patient : </strong><#if forPatient??> ${forPatient} <#else> - </#if></p>
        </div>
         <div class="leftcolumns">
                   <p><strong>.</p>
        </div>
       <div class="rightcolumns">
            <p><strong>Issuer : </strong>
                <#if forPerson??> ${forPerson} <#else> - </#if>
            </p>
       </div>
    <div>&nbsp;</div>
    <table width="100%" cellpadding="6" class="borderapply commonfont ind-items">
        <tr>
            <td width="4%"><strong>S.No. </strong></td>
            <td width="40%"><strong> Item Name </strong></td>
            <td width="5%"><strong>Consumed Quantity </strong></td>
            <td width="10%"><strong>Unit MRP</strong></td>
            <td width="10%"><strong>Unit Cost </strong></td>
            <td width="10%"><strong>Total Cost</strong></td>
            <td width="10%"><strong> Batch No. </strong> </td>
            <td width="11%"><strong> Expiry Date </strong> </td>
        </tr>

        <#list issueLines as issueLine>
        <tr><#assign item = issueLine.item>
            <td>${issueLine?counter}</td>
            <td><#if item.getName()??>${item.getName()}<#else>-</#if></td>
            <td><#if issueLine.quantity??>${issueLine.quantity.value}<#else>-</#if></td>
            <td><#if issueLine.mrp??>${issueLine.mrp}<#else>-</#if></td>
            <td><#if issueLine.cost??>${issueLine.cost}<#else>-</#if></td>
             <#assign quantity = issueLine.quantity.value>
             <#assign cost = issueLine.cost>
             <#assign totalCost = (quantity * cost)>
             <td><#if totalCost ??>${totalCost}</td><#else>-</#if>
            <td><#if issueLine.batchNumber??>${issueLine.batchNumber} <#else>-</#if></td>
            <td><#if issueLine.expiryDate??>${issueLine.expiryDate.format(datetimeformatter)} <#else> </#if></td>
        </tr>
        </#list>
    </table>

    <table width="100%" class="fcommonfont">
        <tr>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td>&nbsp;</td>
        </tr>
    </table>

    <table width="100%" class="commonfont">
        <tr>
            <td width="40%"><strong>Created By:</strong> ${createdBy}</td>
            <td width="40%"><strong>Created On:</strong> ${createdOn}</td>
        </tr>
        <tr>
            <td><strong>Approved By:</strong> ${approvedBy} </td>
            <td><strong>Approved On: </strong> ${approvedOn}</td>

        </tr>

        <tr>
            <td><strong>Printed On:</strong> ${publishedOn}</td>
        </tr>

    </table>
</body>

</html>
