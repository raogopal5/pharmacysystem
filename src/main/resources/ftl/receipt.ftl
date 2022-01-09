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
        <td class="headfirst">RECEIPT</td>
    </tr>
        <td class="headtop topblank"><span class="boldDisplay"><#if unitDisplayName??>${unitDisplayName}</#if><#if hscDisplayName??> - ${hscDisplayName}</#if></span></td>
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
<table border="0" width="100%" class="table">
  <tr>
    <!--First Table-->
    <td width="500px" class="topAlign">
      <table border="0" class="table">
        <tr>
          <td width="95px" class="boldDisplay">
            Patient Name
          </td>
           <td>
            :
          </td>
          <td width="400px">
            ${patientName} (${patientGenderAndAge})
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
            <#if patientMrn??>${patientMrn} </#if>
          </td>
        </tr>
        <tr>
          <td width="150px" class="boldDisplay">
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
            Address
          </td>
          <td>
            :
          </td>
          <td width="200px">
           <#if address??> ${address} <#else> - </#if>
          </td>
        </tr>

		      </table>
      <table>
			  <tr class="colorapply">
				<#if receiptPlans??>
				<td class="topAlign boldDisplay" >
					  Plan:&nbsp
					</td>

				  <td class="topAlign boldDisplay" >
					  <#if receiptPlans??> ${receiptPlans} <#else> - </#if>
					</td>
					</#if>
					<#if receiptSponsor??>
				  <td  class="topAlign">
					   &nbsp|&nbsp
					</td>
					<td class="boldDisplay topAlign" style="align:left">
					 Sponsor:&nbsp
					</td>
				  <td class="topAlign boldDisplay" >
					 <#if receiptSponsor??> ${receiptSponsor} <#else> - </#if>
				</td>
				</#if>
			  </tr>
      </table>
    </td>
    <!--Second Table-->
   <td width="500px" class="topAlign">
    <table border="0" align="right">
           <tr>
             <td width="95px" class="boldDisplay">
               Receipt No
             </td>
             <td>
               :
             </td>
             <td width="200px">
               <#if receiptNo??> ${receiptNo} <#else> - </#if>
             </td>
           </tr>
           <tr>
             <td width="95px" class="boldDisplay">
               Receipt Date
             </td>
             <td>
               :
             </td>
             <td width="200px">
               <#if receiptDate??> ${receiptDate} <#else> - </#if>
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
    <tr>
         <td>Received from <b>${patientName}</b> the sum of Rupees ${amountInWords} Only as per details given below <b>On Account</b> dated <b><#if receiptDate??> ${receiptDate} <#else> - </#if></b></td>
    </tr>
    <tr>
        <td>&#160;</td>
    </tr>
</table>
<table cellpadding="6" class="width-100 borderapply commonfont inv-items">
    <thead>
        <tr>
            <td style="width:25px;"><span class="boldDisplay">Sl. No</span></td>
            <td style="width:250px;"><span class="boldDisplay">Receipt No</span></td>
            <td style="width:100px;"><span class="boldDisplay">Mode</span></td>
            <td style="width:100px;text-align: right;"><span class="boldDisplay">Amount</span></td>
<#if transactionCurrency?? >
            <td style="width:100px;text-align: right;"><span class="boldDisplay">Currency</span></td>
            <td style="width:100px;text-align: right;"><span class="boldDisplay">Conversion Rate</span></td>
            <td style="width:100px;text-align: right;"><span class="boldDisplay">Eqv Amount(in INR)</span></td>
</#if>
            <td style="width:550px;text-align: right;"><span class="boldDisplay">Transaction Details</span></td>
        </tr>
    </thead>
    <tbody>
    <tr>
		<td style="width:25px;">1</td>
		<td style="width:250px;"><#if receiptNo??>${receiptNo}</#if></td>
		<td style="width:100px;"><#if mode??>${mode}</#if></td>
		<td style="width:100px;text-align: right;"><#if amount??>${amount?string(",##0.00")}</#if></td>
<#if transactionCurrency?? >
		<td style="width:100px;text-align: right;"><#if transactionCurrency??>${transactionCurrency}</#if></td>
		<td style="width:100px;text-align: right;"><#if conversionRate??>${conversionRate?string(",##0.00")}</#if></td>
		<td style="width:100px;text-align: right;"><#if eqvAmountInINR??>${eqvAmountInINR?string(",##0.00")}</#if></td>
</#if>
		<td style="width:550px;text-align: right;"><#list transactionDetails as transactionDetail><span style="display:block">${transactionDetail}</span></#list></td>
    </tr>
    </tbody>
    <tfoot>
        <tr>
            <td colspan="10"></td>
        </tr>
    </tfoot>
</table>
<table>
  <tr>
            <td style="width:25px;"><span class="boldDisplay"></span></td>
            <td style="width:250px;"><span class="boldDisplay"></span></td>
            <td style="width:100px;"><span class="boldDisplay"></span></td>
            <td style="width:100px;text-align: right;"><span class="boldDisplay"></span></td>
           <#if transactionCurrency?? >
            <td style="width:100px;text-align: right;"><span class="boldDisplay"></span></td>
            <td style="width:100px;text-align: right;"><span class="boldDisplay"></span></td>
            <td style="width:100px;text-align: right;"><span class="boldDisplay"></span></td>
             <td style="width:100px;text-align: right;"><span class="boldDisplay"></span></td>
              <td style="width:300px;text-align: right;"><span class="boldDisplay"></span></td>
                        <td style="width:100px;text-align: left;"><span class="boldDisplay"><#if eqvAmountInINR??>Total:&nbsp;${eqvAmountInINR?string(",##0.00")}<#else><#if amount??>Total:&nbsp;${amount?string(",##0.00")}<#else> - </#if></#if></span></td>
                    <#else>
                          <td style="width:100px;text-align: center;"><span class="boldDisplay"><#if eqvAmountInINR??>Total:&nbsp;${eqvAmountInINR?string(",##0.00")}<#else><#if amount??>Total:&nbsp;${amount?string(",##0.00")}<#else> - </#if></#if></span></td>
                   </#if>
            <td style="width:550px;text-align: right;"><span class="boldDisplay"></span></td>
        </tr>
</table>
<table border="0" class="table" width="100%">
        <tr>
          <td class="boldDisplay topAlign" style="width: 50px;">
            Remarks:
          </td>

          <td>
          <#if remarks??> ${remarks} <#else> - </#if>

          </td>
        </tr>
</table>
<table class="width-100 commonfont" style="page-break-inside: avoid;" >
    <tr>
        <td>&#160;</td>
    </tr>
    <tr>
        <td>&#160;</td>
    </tr>
    <tr>
<#if transactionCurrency?? >
        <td><b>Note:</b> Amounts received subject to final realization from the foreign exchange dealer or the issuing banker</td>
               <#else>
     <#if modeType??>
        <td><b>Note:</b> Receipt is valid subject to realisation of the amount.</td>
     </#if>
     </#if>

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
        <td>&#160;</td>
    </tr>
     <tr>
        <td>&#160;</td>
    </tr>


</table>
<table>
        <tr>
            <td><b>Prepared By:&nbsp;</b><#if preparedBy??> ${preparedBy}</#if><span>|</span> <b>Prepared On:&nbsp;</b><#if preparedOn??> ${preparedOn}</#if></td>
        </tr>
        <tr>
            <td><b>Generated By:&nbsp;</b><#if generatedBy??> ${generatedBy}</#if><span> | </span><b>Generated On:&nbsp;</b><#if generatedOn??> ${generatedOn}</#if></td>
        </tr>
      </table>
</body>
</html>
