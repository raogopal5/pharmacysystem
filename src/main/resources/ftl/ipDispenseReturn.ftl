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
<table class="width-64 commonfont topDown">
    <tr>
        <td class="headfirst">DRUG RETURN</td>
    </tr>
    <tr>
        <td class="headtop topblank"><span class="boldDisplay"><#if unitDisplayName??>${unitDisplayName} - </#if> ${hscDisplayName}</span></td>
    </tr>
     <tr>
            <td class="unit-grey">
               <#if unitAddress??><#list unitAddress.line as line>
                                            <#if line??> <#if line?has_content> ${line},</#if> <#else>  </#if>
                                            </#list> <#if unitAddress.city??> ${unitAddress.city},</#if>
                                            <#if unitAddress.district??> ${unitAddress.district},</#if>
                                            <#if unitAddress.state??> ${unitAddress.state},</#if>
                                            <#if unitAddress.country??> ${unitAddress.country}</#if>
                                            <#if unitAddress.postalCode??  && unitAddress.postalCode?has_content>-${unitAddress.postalCode} <#else> </#if><#else> - - - </#if>
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
        <td width="49%" class="topAlign">
          <table border="0" align="left">
            <tr>
                <td colspan="3" class="topAlign" style="height:28px;"><#if barcodeImage??>${barcodeImage}</#if></td>
            </tr>
            <tr>
              <td width="95px" class="boldDisplay topAlign">Patient Name</td>
              <td width="2px" class="topAlign">:</td>
              <td><#if patientName??>${patientName}</#if></td>
            </tr>
            <tr>
              <td width="95px" class="boldDisplay topAlign">MRN</td>
              <td class="topAlign" width="2px">:</td>
              <td><#if patientMrn??>${patientMrn} <#else> - </#if><#if genderAge??>( ${genderAge})</#if></td>
            </tr>
             <tr>
                          <td width="95px" class="boldDisplay topAlign">Mobile No.</td>
                          <td class="topAlign" width="2px">:</td>
                          <td><#if patientPhoneNo??> ${patientPhoneNo} </#if></td>
                        </tr>
                                                   <tr>
                  <td width="107px" class="boldDisplay topAlign">Ward/Bed</td>
                  <td class="topAlign" width="2px">:</td>
                  <td> <#if ward??> ${ward} / <#if bed??> ${bed} </#if> </#if></td>
                  </tr>
                    <tr>
                                 <td width="95px" class="boldDisplay topAlign">Consultant</td>
                                 <td class="topAlign" width="2px">:</td>
                                 <td> <#if consultantName??> ${consultantName} <#else> - </#if></td>
                              </tr>
                              <tr>
                                            <td width="95px" class="boldDisplay topAlign">Department</td>
                                            <td class="topAlign " width="2px">:</td>
                                            <td><#if department??> ${department} <#else> - </#if></td>
                                          </tr>
          </table>
        </td>
        <!--Second Table-->
        <td width="2%">&nbsp;</td>
        <td  class="topAlign">
          <table border="0" align="left" style="width:100%;">
          <tr>
            <td colspan="3" class="topAlign" style="height:28px;"><#if returnBarcodeImage??>${returnBarcodeImage}</#if></td>

          </tr>
          <tr>
            <td width="95px" class="boldDisplay topAlign">Return No.</td>
            <td class="topAlign " width="2px">:</td>
            <td><#if returnNo??> ${returnNo}</#if></td>
          </tr>
          <tr>
            <td width="95px" class="boldDisplay topAlign">Return Date</td>
            <td class="topAlign ">:</td>
            <td><#if returnDate??> ${returnDate} </#if></td>
          </tr>
          <tr>
              <td width="95px" class="boldDisplay topAlign">Return By</td>
              <td class="topAlign ">:</td>
              <td><#if returnBy??> ${returnBy} </#if></td>
          </tr>
          <tr>
             <td width="95px" class="boldDisplay topAlign">Return Store</td>
             <td class="topAlign ">:</td>
             <td><#if returnStore??>${returnStore}  </#if></td>
             </tr>
          <tr>
             <td width="115px" class="boldDisplay topAlign">Return Request No </td>
             <td class="topAlign ">:</td>
             <td><#if returnRequestNumber??>${returnRequestNumber} </#if></td>
          </tr>
            <tr>
              <td width="95px" class="boldDisplay topAlign">Requested By</td>
              <td class="topAlign ">:</td>
              <td><#if requestedBy??>${requestedBy}</#if> </td>
           </tr>
             </table>
        </td>
      </tr>
    </table>

<table class="width-100 commonfont">
    <tr>
        <td>&#160;</td>
    </tr>
    <#if sponsorInvoices??>
     <#list sponsorInvoices as sponsorInvoice >
        <tr class="colorapply">
            <td><span class="n boldDisplay">Plan</span> : ${sponsorInvoice.getSponsorDocument().getPlanRef().getName()} |  <span class="n "><span class="boldDisplay" >Sponsor</span> : <#if sponsorInvoice.getSponsorDocument().getSponsorRef()??> ${sponsorInvoice.getSponsorDocument().getSponsorRef().getName()} (${sponsorInvoice.getSponsorDocument().getSponsorRef().getCode()})</#if></span> | <span class="n "><span class="boldDisplay"> Sponsor Bill No </span> : ${sponsorInvoice.getSponsorInvoiceNumber()}</span> |  <span class="n "><span class="boldDisplay"> Sponsor Return Amt </span> : ${sponsorInvoice.getSponsorDocument().getSponsorPayable()?string(",##0.00")}</span></td>
        </tr>
        </#list>
        </#if>
</table>
<table cellpadding="6" class="width-100 borderapply commonfont inv-items">
    <thead>
        <tr>
            <td style="width:25px;"><span class="boldDisplay">Sl No.</span></td>
            <td style="width:200px;"><span class="boldDisplay">Item Name</span></td>
            <td style="width:90px;"><span class="boldDisplay">Mfr</span></td>
            <td style="width:70px;"><span class="boldDisplay">Batch/Exp</span></td>
            <td style="width:40px;" class="headalign"><span class="boldDisplay">Return Qty</span></td>
            <td style="width:40px;" class="headalign"><span class="boldDisplay">Unit Price</td>
            <td style="width:40px;" class="headalign right"><span class="boldDisplay">Total Amount</span></td>
        </tr>
    </thead>
    <tbody>
    <#assign x = 1>
     <#assign totalAmt = 0>
		<#list dispenseItems as dispenseItem>
		<tr>
		 <td width="2px"  class="topAlign" >${x}.</td>
			<td>${dispenseItem.name} </td>
			<td style="overflow-wrap: break-word;word-wrap:break-word;"><#if dispenseItem.getMedication()??><#if dispenseItem.getMedication().getManufacturer()??>${dispenseItem.getMedication().getManufacturer()}</#if> </#if> </td>
			<td>${dispenseItem.batchNumber}/${dispenseItem.expiryDate}</td>
			<td class="text-right">${dispenseItem.returnQuantity}</td>
			<td class="text-right">${convertDecimal.decimalFormater(dispenseItem.mrp)}</td>
			<td class="text-right right">${convertDecimal.decimalFormater(dispenseItem.returnQuantity*dispenseItem.mrp)}</td>
			 <#assign totalAmt+=(dispenseItem.returnQuantity*dispenseItem.mrp)>
			 <#assign x++>
		</tr>

		</#list>
    </tbody>
    <tfoot>
        <tr>
            <td colspan="7"></td>
        </tr>
    </tfoot>
</table>
<table class="width-100 commonfont">
    <tr>
     <td colspan="2">&#160; </td>
        <td width="50%" ><span class="boldDisplay"></span></td>
        <td width="50%" class="text-right"><span class="boldDisplay">Total </span>: <#if totalAmt??>${totalAmt?string(",##0.00")}</#if></td>
         </tr>
    <tr>
        <td colspan="2">&#160; </td>
 <td width="50%" ><span class="boldDisplay"></span></td>
        <td  width="40%" class="text-right"><span class="boldDisplay">Round Off </span>:<#if roundOff??>${roundOff?string(",##0.00")} </#if> </td>
                   </tr>
    <tr>
     <td colspan="2">&#160; </td>
     <td width="50%" ><span class="boldDisplay"></span></td>
         <td  width="50%" class="text-right"><span class="boldDisplay">Net Amount</span>:${(totalAmt+roundOff)?string(",##0.00")} </td>

    </tr>
</table>
<div style="page-break-inside: avoid;">
<table class="width-100 commonfont">
<tr>
        <td>&#160;</td>
    </tr>

    <tr>
            <td>&#160;</td>
        </tr>
    <tr>
        <td style="text-align: left;"><span class="boldDisplay">Remarks:</span><#if remarks??> ${remarks}</#if></td>
        <td></td>
        <td></td>
        <td style="text-align: right;"><span class="boldDisplay">Authorized Signatory</span></td>
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
        <tr>
            <td>&#160;</td>
        </tr>
        <tr>
            <td>&#160;</td>
        </tr>

    <tr>
            <td><span><b>Prepared By<b>:</span>&nbsp;${preparedBy} | <span class="boldDisplay"><b>Prepared On</b>:</span>&nbsp;<span>&nbsp;<#if preparedOn??>${preparedOn}</#if></span> | </td>
        </tr>
        <tr>
            <td><span><b>Generated By</b>:&nbsp;${generatedBy} | <span><b>Generated On</bold>:</span> <span class="generatedOn">&nbsp;${generatedOn}</span> | </td>

</table>

</div>
</body>
</html>
