<html>
<head>
    <style>
        @font-face {
          font-family: "Calibri";
          src: url(calibri.ttf); format("truetype");
          -fs-pdf-font-embed: embed;
          -fs-pdf-font-encoding: Identity-H;
        }
        @font-face {
          font-family: "Calibri-Bold";
          src: url(calibrib.ttf); format("truetype");
          -fs-pdf-font-embed: embed;
          -fs-pdf-font-encoding: Identity-H;
        }
        body {
            font-family: "Calibri";
        }
        div.page-header-remaining {display:none;}
        @page {
            size: A4; margin: 2in 0.4in;
            @bottom-center {
                font-size:11px;
                color: #717171;
                vertical-align:top;
                font-family: "Calibri";
                content:element(lastFooter) "Page " counter(page) " of " counter(pages);
            }
            @top-center {
                font-family: "Calibri";
                content: element(page-header-remaining, last-except);
            }
        }
        @page:first { margin-top: 0.4in; }
		table {
            border-spacing: 0px;
			font-size: 12px;
			font-color: #000;
            border-collapse: collapse;
        }
		div p{
			margin: 0px;
		}
		.commonfont{

		}
		.width-100{
			width: 100%;
		}
		.width-64{
			width: 64%;
		}
		.boldDisplay{
			font-weight:bold;
		}
		.topDown{
             margin-top: 40px;
        }
        .text-right {
            text-align: right;
        }
		.headfirst{
            font-size: 24px;
        }
        .headtop{
            font-size: 16px;
			font-weight: bold;
        }
		.topblank{
			padding-top: 10px;
		}
        .unit-grey{
            color: #717171;
        }
        .text-center {
            text-align: center;
        }
		.footfirst{
            font-size: 12px;
            color: #717171;
        }
        .footfont{
            font-size: 11px;
            color: #717171;
        }
        .topline{
            line-height: 90%;
        }
        .leftcolumn{
            margin-left: 50%;
        }
        .leftcolumns{
            width:50%;
            font-size: 12px;
            line-height: 16px;
        }
        .rightcolumns{
            width: 50%;
            font-size: 12px;
            text-align : right;
        }
		.rightcolumnslast{
            width: 50%;
            font-size: 12px;
            line-height: 16px;
            text-align: right;
        }
        .sgst{
            color: #717171;
            display: block;
            font-size: 11px;
        }
		.subsection{
			font-size: 11px;
		}
		.amount{
			font-weight: bold;
			font-size: 22px;
		}
		.headalign{
			text-align: right;
		}
		.borderapply td {
			border-top: 0.5px solid #C8C8C8;
			border-left: 0.5px solid #C8C8C8;
		}
		.borderapply .right {
			border-right: 0.5px solid #C8C8C8;
		}
		.borderapply tfoot td {
		    border:0px;
			border-top: 0.5px solid #C8C8C8;
		}
		hr {
			display: block;
			margin-top: 1.1em;
			margin-bottom: -1.2em;
			border-style: inset;
			border-width: 0.5px;
			border-color: #DEDEDE;
		}
		.inv-items {
		   -fs-table-paginate: paginate;
		}
		.inv-items tr {
		    page-break-inside: avoid;
		}
		.running{
            position: running(page-header-remaining);
            display: none;
        }
        @Media print{
        	.running{display:block;}
        }
        .h-value { min-width:150px; display:inline-block; text-align:left;}
		.topAlign{vertical-align: top;}
    </style>
</head>
<body>
<table align="center" style="position:running(lastFooter); margin-bottom:10px;">
    <tr class="footfirst">
        <td class="text-center"><span class="boldDisplay">DL NO </span>: ${dlNo}  | <span class="boldDisplay">GSTIN </span>: ${gstin}
            | <span class="boldDisplay">E &amp; OE |</span></td>
    </tr>
 </table>
<table class="width-64 commonfont topDown">
    <tr>
        <td class="headfirst">PHARMACY RETURN CUM REFUND</td>
    </tr>
    <tr>
        <td class="headtop topblank"><span class="boldDisplay">${unitDisplayName}- ${hscDisplayName}</span></td>
    </tr>
    <tr>
        <td class="unit-grey">
			<#if unitAddress??><#list unitAddress.line as line>
                                                    <#if line??> <#if line?has_content> ${line},</#if> <#else>  </#if>
                                                    </#list> <#if unitAddress.city??> ${unitAddress.city},</#if>
                                                    <#if unitAddress.district??> ${unitAddress.district},</#if>
                                                    <#if unitAddress.state??> ${unitAddress.state},</#if>
                                                    <#if unitAddress.country??> ${unitAddress.country}</#if>
                                                    <#if unitAddress.postalCode?? && unitAddress.postalCode?has_content>-${unitAddress.postalCode} <#else> </#if><#else> - - - </#if>
		</td>
    </tr>
</table>
<hr />
<table  class="width-100 fcommonfont">
    <tr>
        <td>&#160;</td>
    </tr>
    <tr>
        <td>&#160;</td>
    </tr>
</table>
<div  class="running">
    <table class="commonfont" style="margin-left:9px;">
        <tbody>
        <tr>
            <td class="leftcolumns">
                <span class="boldDisplay">Patient Name </span>: ${patientName}
            </td>
        </tr>
        <tr>
            <td class="leftcolumns colorapply">
                <span class="boldDisplay">Patient MRN </span>: <#if patientMrn??>${patientMrn} <#else> - </#if>
            </td>
        </tr>
        <tr>
                    <td class="leftcolumns colorapply">
                        <span class="boldDisplay">Invoice No (C) </span>: <#if invoiceNo??> ${invoiceNo} <#else> - </#if>
                    </td>
                </tr>
        </tbody>
    </table>
</div>

<table border="0" width="100%">
  <tr>
    <!--First Table-->
    <td width="500px" class="topAlign">
      <table border="0">
        <tr>
          <td width="95px" class="boldDisplay">
            Patient Name
          </td>
           <td>
            :
          </td>
          <td width="200px">
            ${patientName}
          </td>
        </tr>
        <tr>
          <td width="95px" class="boldDisplay">
            Patient MRN
          </td>
          <td>
            :
          </td>
          <td width="200px">
            <#if patientMrn??>${patientMrn} <#else> - </#if>
          </td>
        </tr>
        <tr>
          <td width="95px" class="boldDisplay">
            Patient Phone No
          </td>
          <td>
            :
          </td>
          <td width="200px">
           <#if patientPhoneNo??> ${patientPhoneNo} </#if>
          </td>
        </tr>
        <tr>
          <td width="95px" class="boldDisplay">
            Date
          </td>
          <td>
            :
          </td>
          <td width="200px">
            ${date}
          </td>
       </tr>
      </table>
    </td>
    <!--Second Table-->
    <td width="500px" class="topAlign">
      <table border="0" align="right">
        <tr>
          <td width="95px" class="boldDisplay">
            Consultant Name
          </td>
          <td>
            :
          </td>
          <td width="200px">
            <#if consultantName??> ${consultantName} <#else> - </#if>
          </td>
        </tr>
        <tr>
          <td width="95px" class="boldDisplay">
            Invoice No (C)
          </td>
          <td>
            :
          </td>
          <td width="200px">
            ${invoiceNo}
          </td>
        </tr>
        <tr>
          <td width="95px" class="boldDisplay">
            Refund No
          </td>
         <td>
            :
          </td>
          <td width="200px">
            ${refundNo}
          </td>
        </tr>
        <tr>
                  <td width="95px" class="boldDisplay">
                    Visit No
                  </td>
                  <td class="topAlign">
                    :
                  </td>
                  <td width="200px">
                    <#if visitNumber??>${visitNumber}<#else> </#if>
                  </td>
                </tr>
      </table>
    </td>
  </tr>
</table>


<table class="width-100 commonfont">
    <tr>
        <td>&#160;</td>
    </tr>
     <#list sponsorInvoices as sponsorInvoice >
        <tr class="colorapply">
            <td><span class="n boldDisplay">Plan</span> : ${sponsorInvoice.getSponsorDocument().getPlanRef().getName()} |  <span class="n "><span class="boldDisplay" >Sponsor</span> : <#if sponsorInvoice.getSponsorDocument().getSponsorRef()??> ${sponsorInvoice.getSponsorDocument().getSponsorRef().getName()} (${sponsorInvoice.getSponsorDocument().getSponsorRef().getCode()})</#if></span> | <span class="n "><span class="boldDisplay"> Sponsor Bill No </span> : ${sponsorInvoice.getSponsorInvoiceNumber()}</span> |  <span class="n "><span class="boldDisplay"> Sponsor Return Amt </span> : ${sponsorInvoice.getSponsorDocument().getSponsorPayable()?string(",##0.00")}</span></td>
        </tr>
        </#list>
</table>
<table cellpadding="6" class="width-100 borderapply commonfont inv-items">
    <thead>
        <tr>
            <td style="width:200px;"><span class="boldDisplay">Particulars</span></td>
            <td style="width:40px;"><span class="boldDisplay">Manufacturer</span></td>
            <td style="width:30px;"><span class="boldDisplay">Sch.</span></td>
            <td style="width:70px;"><span class="boldDisplay">Batch/Exp</span></td>
            <td style="width:50px;" class="headalign"><span class="boldDisplay">Qty</span></td>
            <td style="width:50px;" class="headalign"><span class="boldDisplay">Unit <br /> Rate </td>
            <td style="width:50px;" class="headalign"><span class="boldDisplay">SGST<br /><span class="subsection">(%)</span></span> </td>
            <td style="width:50px;" class="headalign"><span class="boldDisplay">CGST<br /><span class="subsection">(%)</span></span></td>
            <td style="width:40px;" class="headalign right"><span class="boldDisplay">Amount<br />(Rs)</span></td>
        </tr>
    </thead>
    <tbody>
		<#list dispenseItems as dispenseItem>
		<#if dispenseItem.quantity != 0>
		<tr>
			<td>${dispenseItem.name} <#if dispenseItem.dispenseTaxes?size != 0><#if dispenseItem.dispenseTaxes[0].attributes.hsnCode??> (${dispenseItem.dispenseTaxes[0].attributes.hsnCode})</#if></#if></td>
			<#list manufacutererAndSchedule[dispenseItem.code] as data>
			<td style="overflow-wrap: break-word;word-wrap:break-word;">${data}</td>
			<#else>
			<td>-</td>
			<td>-</td>
			</#list>
			<td>${dispenseItem.batchNumber}/${dispenseItem.expiryDate}</td>
			<td class="text-right">${dispenseItem.quantity}</td>
			<td class="text-right">${convertDecimal.decimalFormater(dispenseItem.saleRate)}</td>
			<#if dispenseItem.dispenseTaxes?size != 0>
			<#assign noTax = true>
			<#list dispenseItem.dispenseTaxes as taxes>
			<#if taxes.taxCode?contains("SGST")>
			<#assign noTax = false>
			<td class="text-right"><#assign taxCal = taxes.taxDefinition>${taxCal.getTaxCalculation().getPercentage()}</td>
			</#if>
			</#list>
			<#if noTax == true>
            <td class="text-right">-</td>
            </#if>
            <#assign noTax = true>
			<#list dispenseItem.dispenseTaxes as taxes>
			<#if taxes.taxCode?contains("CGST")>
			<#assign noTax = false>
			<td class="text-right"><#assign taxCal = taxes.taxDefinition>${taxCal.getTaxCalculation().getPercentage()}</td>
			</#if>
			</#list>
			<#if noTax == true>
            <td class="text-right">-</td>
            </#if>
			<#else>
			<td class="text-right">-</td>
			<td class="text-right">-</td>
			</#if>
			<td class="text-right right">${convertDecimal.decimalFormater(dispenseItem.saleAmount)}</td>
		</tr>
		</#if>
		</#list>
    </tbody>
    <tfoot>
        <tr>
            <td colspan="10"></td>
        </tr>
    </tfoot>
</table>
<div style="page-break-inside: avoid;">
<table class="width-100 commonfont">
    <tr>
        <td width="50%" colspan="2"><span class="boldDisplay"><u>Summary</u></span></td>
        <td width="50%" class="text-right"><span class="boldDisplay">Total </span>: </td>
        <td width="10%" class="text-right">${convertDecimal.decimalFormater(total)}</td>
    </tr>
    <tr>
        <td colspan="2"><span class="boldDisplay">Tax </span>- <#list invoiceTaxes as taxes>${taxes.taxCode}: ${taxes.taxAmount + taxes.patientTaxAmount},</#list></td>
        <td class="text-right"><span class="boldDisplay">Discount </span>: </td>
        <td class="text-right">${convertDecimal.decimalFormater(totalDiscount)}</td>
    </tr>
    <tr>
        <td colspan="2"><span class="boldDisplay">Discount </span>- Patient: ${convertDecimal.decimalFormater(patientDiscount)}, Sponsor: ${convertDecimal.decimalFormater(sponsorDiscount)} <#if taxDiscount != 0 >, Tax: ${convertDecimal.decimalFormater(taxDiscount)}</#if></td>
        <td class="text-right"><span class="boldDisplay">Net Amount</span>: </td>
        <td class="text-right">${convertDecimal.decimalFormater(netAmount)}</td>
    </tr>
    <tr>
        <td colspan="2"><span class="boldDisplay">Return Mode </span>- <#if refundAmount != 0>Return via ${refundType} Rs ${convertDecimal.decimalFormater(refundAmount)}<#else> Not Applicable </#if></td>
        <td class="text-right"><span class="boldDisplay">Sponsor Return </span>: </td>
                <td class="text-right">${convertDecimal.decimalFormater(totalSponsorAmount)}</td>
    </tr>
    <tr>
        <td colspan="2">&#160; </td>
        <td class="text-right"><span class="boldDisplay">Patient Round Off </span>: </td>
                <td class="text-right"><#if roundOff??>${convertDecimal.decimalFormater(roundOff)}<#else> 0.00 </#if></td>
    </tr>
    <tr>
            <td colspan="2">&#160; </td>
            <td class="text-right"><span class="boldDisplay">Patient Return Amt </span>: </td>
            <td class="text-right"><span class="amount">${convertDecimal.decimalFormater(returnAmount)}</span></td>
     </tr>
</table>
<table class="width-100 commonfont" style="page-break-inside: avoid;">
    <tr>
        <td>&#160;</td>
    </tr>
    <tr>
        <td><span class="boldDisplay">Prepared By</span>: ${preparedBy}</td>
    </tr>
    <tr>
        <td><span class="boldDisplay">Qualified Pharmacist</span>: <#if qualifiedPharmacist??><#list qualifiedPharmacist as pharmacist> <#if pharmacist.contactEntityType.code == "PHARMACIST"> ${pharmacist.name}<#sep>,</#if></#list> <#else>-</#if> </td>
    </tr>
    <tr>
        <td><span class="boldDisplay">Generated By</span>: <span class="generatedBy">${generatedBy}</span> | <span class="boldDisplay">Generated On</span>: <span class="generatedOn">${generatedOn} </span>| <span class="boldDisplay">Signature</span>: </td>
    </tr>
    <tr>
        <td>&#160;</td>
    </tr>
    <tr>
        <td>&#160;</td>
    </tr>
    <tr>
        <td>&#160;</td>
    </tr>
    <tr>
        <td>&#160;</td>
    </tr>
    </table>

</div>
</body>
</html>
