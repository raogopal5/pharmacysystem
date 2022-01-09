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
                 <div class="headfirst text-center">Stock Receipt</div>
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
                                   <p><strong>Receipt No.  : </strong>${receiptNumber} </p>
                               </div>
                               <div class="rightcolumns">
                                   <p><strong>Receipt Date : </strong>${receiptDate} </p>
                               </div>
                               <div class="leftcolumns">
                                   <p><strong>Receiving Unit : </strong> ${receivingUnit} </p>
                               </div>
                               <div class="rightcolumns">
                                   <p><strong>Receiving Store : </strong>${receivingStore}</p>
                               </div>
                               <div class="leftcolumns">
                                   <p><strong>Issue Unit : </strong> ${issueUnit}</p>
                               </div>
                               <div class="rightcolumns">
                                   <p><strong>Issue Store : </strong>${issueStore}</p>
                               </div>
                               <div class="leftcolumns">
                                   <p><strong>Status : </strong>${status}</p>
                               </div>
                           </div>
                           <div>&nbsp;</div>
                           <table width="100%" cellpadding="6" class="borderapply commonfont">
                               <tr>
                                   <td width="10%"><strong>S.No. </strong></td>
                                   <td width="45%"><strong> Item Name </strong></td>
                                   <td width="15%"><strong>Requested<br> Quantity </strong></td>
                                   <td width="15%"><strong>Issued <br>Quantity</strong></td>
                                   <td width="15%"><strong> Batch <br> No.</strong> </td>
                                   <td width="15%"><strong> Accepted <br> Quantity</strong> </td>
                                   <td width="15%"><strong> Rejected <br> Quantity</strong> </td>
                                   <td width="16%"><strong> Reason</strong> </td>
                               </tr>
                               <#if lineItems??>
                               <#list lineItems as lineItem>
                               <tr>
                                   <td>${lineItem?counter}</td>
                                   <td><#if lineItem.item??>${lineItem.item.getName()}</#if></td>
                                   <#if lineItem.sourceDocument??>
                                        <#if lineItem.sourceDocument[0].getType() == "Stock_Direct_Transfer">
                                           <td>-</td>
                                            <td>${lineItem.sourceDocument[0].getQuantity().getValue()}</td>
                                         </#if>
                                         <#if lineItem.sourceDocument[0].getType() == "Stock_Indent" || lineItem.sourceDocument[0].getType() == "Inter_Unit_Stock_Indent">
                                                <td>${lineItem.sourceDocument[0].getQuantity().getValue()}</td>
                                                <td>${lineItem.sourceDocument[1].getQuantity().getValue()}</td>
                                          </#if>
                                   </#if>
                                   <td><#if lineItem.batchNumber??>${lineItem.batchNumber}</#if></td>
                                   <td><#if lineItem.acceptedQuantity??>${lineItem.acceptedQuantity.getValue()}</#if></td>
                                   <td><#if lineItem.rejectedQuantity??>${lineItem.rejectedQuantity.getValue()}</#if></td>
                                   <td><#if lineItem.reason??>${lineItem.reason.getDisplay()}</#if></td>
                               </tr>
                               </#list>
                                </#if>
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
                                    <td><strong>Published By:</strong> <span class="generatedBy">${publishedBy}</span></td>
                                    <td><strong>Published On:</strong> <span class="generatedOn">${publishedOn}</span></td>
                               </tr>

                           </table>
                       </body>

                       </html>
