<!DOCTYPE html>
<html>
<head>
<style>
h3 {text-align: center;}
table1, th,tr {
  border: 1px solid black;
  border-collapse: collapse;
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
                    .commonfont {
                font-family: "Calibri";
		font-size: 13px;
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

            .borderapply  td {
                border: 1px solid black;
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
              .left-margin {
                margin-left: 300px;
              }
              #div-class {
                border: 1px solid black;
                margin: 0;
              }

      #test, th, #test td {
        border: 1px solid black;
        border-collapse: collapse;
      }
          #footer{
                  display: block;
                  position: running(footer);
                  padding:0px;
                  width: 100%;
              }
         @page {
                        size: A4 ;
                        margin: 1.4in 0.5in 1.0in 0.4in;
                         width: 200px;
                         border-top:solid 1px black;
                         overflow-x: auto;
                    @bottom-center {
                        font-size:7pt;
                        color: #717171;
                        vertical-align:top;
                        font-family: "Calibri";
                        content:element(lastFooter) "Page " counter(page) " of " counter(pages);
                    }
                     @top-left {
                        font-family: "Calibri";
                        content: element(pageHeaderWithPatientInfo);
                                }

                }
                @page:first {
                size: A4 ;
                margin: 1.5in 0.5in 1.0in 0.4in;
                border-top:solid 1px white;
                    @top-left {
                         font-family: "Calibri";
                         content: element(pageHeader);
                            }
                   }
        		table {
                    border-spacing: 0px;
        			font-size: 8pt;
        			font-color: #000;
                    border-collapse: collapse !important;
                }
       @Media print{
         	.running{display:block;}
         }

         .preventingTagsFromSpanningMultiplePages{

                     page-break-inside: avoid !important;

                 }

    .footer-info-border {
             border-top: 1px solid #333;
             margin: 10px 0px 0px 0px;
         }

         #table2 {
           border: 1px solid black;
           border-collapse: collapse;
         }
</style>
</head>
<body>
<table id="test" style="width:100%">
  <tr>
    <th style="text-align: center"><b>Tax Invoice</b></th>
  </tr>
   <tr>
    <th style="text-align: center">[Rule 10 of the Revised Invoice Rules, 2017]</th>
  </tr>
</table>

<table id="test" style="width:100%" class="borderapply">
         <tr>
    <td><b>Invoice No.</b></td><td><#if invoiceNumber??>${invoiceNumber} <#else> - </#if></td>
    <td><b>E- Way Bill No</b></td><td style="padding-left:5px;"><div style="width: 60px; overflow:hidden;"><#if eWayBillNumber??>${eWayBillNumber} <#else> - </#if></div></td>
    <td><b>Name of transpoter</b></td><td style="padding-left:5px;"><div style="width: 60px; overflow:hidden;"><#if nameOfTranspoter??>${nameOfTranspoter} <#else> - </#if></div></td>
  </tr>

         <tr>
    <td><b>Invoice Date</b></td><td><#if invoiceDate??>${invoiceDate} <#else> - </#if></td>
    <td><b>E- Way Bill Date</b></td><td><#if eWayBillDate??>${eWayBillDate} <#else> - </#if></td>
    <td><b>Way Bill/L.R No.</b></td><td><#if eWayBillNumber??>${eWayBillNumber} <#else> - </#if></td>
  </tr>
        </table>
<table id="test" style="width:100%" class="borderapply">
 <tr>
 <td><b>Name of Consignor</b></td><td><#if issueUnitConsignor??>${issueUnitConsignor} <#else> - </#if></td>
 <td><b>Name of Consignor</b></td><td><#if indentUnitConsignor??>${indentUnitConsignor} <#else> - </#if></td>
 </tr>
 <tr>
 <td><b>Address</b></td><td><#if issueUnitAddress??><#list issueUnitAddress.line as line>
                                                                                <#if line??> <#if line?has_content> ${line},</#if> <#else>  </#if>
                                                                                </#list> <#if issueUnitAddress.city??> ${issueUnitAddress.city},</#if>
                                                                                <#if issueUnitAddress.district??> ${issueUnitAddress.district},</#if>
                                                                                <#if issueUnitAddress.state??> ${issueUnitAddress.state},</#if>
                                                                                <#if issueUnitAddress.country??> ${issueUnitAddress.country}</#if>
                                                                                <#if issueUnitAddress.postalCode?? && issueUnitAddress.postalCode?has_content>-${issueUnitAddress.postalCode} <#else> </#if><#else> - - - </#if><br/>
                                                                                <span>Ph:</span><span><#if issueTelComePhone??><#if issueTelComePhone.value??> ${issueTelComePhone.value}</#if><#else> - </#if></span><br/>
<span>Fax:</span><span><#if issueTelComeFax??><#if issueTelComeFax.value??> ${issueTelComeFax.value}</#if><#else> - </#if></span><br/>
<span>Email ID:</span><span><#if issueTelComeEmail??><#if issueTelComeEmail.value??> ${issueTelComeEmail.value}</#if><#else> - </#if></span><br/></td>
 <td><b>Address</b></td><td><#if indentUnitAddress??><#list indentUnitAddress.line as line>
                                                                                <#if line??> <#if line?has_content> ${line},</#if> <#else>  </#if>
                                                                                </#list> <#if indentUnitAddress.city??> ${indentUnitAddress.city},</#if>
                                                                                <#if indentUnitAddress.district??> ${indentUnitAddress.district},</#if>
                                                                                <#if indentUnitAddress.state??> ${indentUnitAddress.state},</#if>
                                                                                <#if indentUnitAddress.country??> ${indentUnitAddress.country}</#if>
                                                                                <#if indentUnitAddress.postalCode?? && indentUnitAddress.postalCode?has_content>-${indentUnitAddress.postalCode} <#else> </#if><#else> - - - </#if><br/>
                                                                                <span>Ph:</span><span><#if indentUnitTelComePhone??><#if indentUnitTelComePhone.value??> ${indentUnitTelComePhone.value}</#if><#else> - </#if></span><br/>
 <span>Fax:</span><span><#if indentUnitTelComeEmail??><#if indentUnitTelComeEmail.value??> ${indentUnitTelComeEmail.value}</#if><#else> - </#if></span><br/>
 <span>Email ID:</span><span><#if indentUnitTelComeFax??><#if indentUnitTelComeFax.value??> ${indentUnitTelComeFax.value}</#if><#else> - </#if></span><br/></td>
  </tr>
  <tr><td><b>State Code & Name</b></td><td><#if StateCodeName?? && StateCodeName?has_content>${StateCodeName} <#else> - </#if></td>
   <td><b>State Code & Name</b></td><td><#if indentStateCodeName?? && indentStateCodeName?has_content>${indentStateCodeName} <#else> - </#if></td></tr>

     <tr><td><b>GSTIN</b></td><td><#if issueGSTIN??>${issueGSTIN} <#else> - </#if></td>
   <td><b>GSTIN</b></td><td><#if indentGSTIN??>${indentGSTIN} <#else> - </#if></td></tr>
        </table>
 <#assign totaltaxableAmount=0>
 <table class="width-100 borderapply commonfont inv-items"  border="1">
        <tr bgcolor="#d0f7da">
            <th width="8%"><strong><b>S.No.</b> </strong></th>
            <th width="50%"><strong> <b>Material Code &
Description Of supply</b> </strong></th>
            <th width="11%"><strong><b>HSN Code</b></strong></th>
            <th width="10%"><strong><b>Qty.</b></strong></th>
            <th width="10%"><strong><b>Unit Rate</b> </strong> </th>
            <th width="11%"><strong> <b>Taxable Value</b> </strong> </th>
        </tr>
        <#list issueLines as issueLine>
        <#if issueLine.getIssuedQuantity().getValue() gt 0 >
<#assign taxableAmount=0>
        <tr><#assign item = issueLine.item>
            <td>${issueLine?counter}</td>
        <td>${item.getCode()}/${item.getName()}</td>
        <td><#if hsnData??>
        <#list hsnData as key,value>
        <#if key ==issueLine.getItem().getId()>
            ${value}
            </#if>
            </#list><#else> - </#if></td>
            <td>${issueLine.getIssuedQuantity().getValue()}</td>
            <td><#if issueLine.getCost()??>${issueLine.getCost()} <#else> </#if></td>
         <#if issueLine.getCost()??><#assign taxableAmount += (issueLine.getIssuedQuantity().getValue()*issueLine.getCost())></#if>
         <#if issueLine.getCost()??><#assign totaltaxableAmount += (issueLine.getIssuedQuantity().getValue()*issueLine.getCost())></#if>
            <td><#if taxableAmount??>${taxableAmount}</#if></td>
        </tr>
        </#if>
        </#list>
    </table>
    <div class="preventingTagsFromSpanningMultiplePages">
    <table id="test" style="width:100%" class="borderapply">
    <tr><td><span><b>Total Taxable Value</b></span></td><td style="text-align:left"><#if totaltaxableAmount??>${totaltaxableAmount} <#else> - </#if></td></tr>
    <tr><td><span><b>Total Net Amount</b></span></td><td style="text-align:left"><#if totaltaxableAmount??>${totaltaxableAmount?string(",##0.00")} <#else> - </#if></td></tr>
    </table>
    </div>
    <div id="div-class" class="preventingTagsFromSpanningMultiplePages">
    <table style="width:100%">
      <tr><td><span class="left-margin"><b>Signature</b></span></td></tr>
       <tr><td><span class="left-margin"><b>Name of the Signatory</b></span></td></tr>
        <tr><td><span class="left-margin"><b>Designation/Status</b></span></td></tr>
       </table>
       </div>
 <table>
  <tr>
        <td>&#160;</td>
    </tr>
</table>
<table class="width-100 commonfont" cellpadding="1" class="width-100" style="margin-top:10px;">
<tr>
<td><span><b>Generated By:</b></span><span class="generatedBy"><#if generatedBy??> ${generatedBy}</#if></span></td>
</tr>
<tr>
<td><span><b>Generated On:</b><#if generatedOn??>${generatedOn}</#if></span></td>
</tr>
</table>
</body>
</html>
