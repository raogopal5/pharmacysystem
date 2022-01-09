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
           size: A4; margin: 2in 0.4in 1.57in 0.4in;
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
        .line-height{
          line-height: 1;
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
			border-right: 0.5px solid #C8C8C8;
		}
		.borderapply .right {
			border-right: 0.5px solid #C8C8C8;
		}
		.borderapply tfoot td {
		    border:0px;
			border-top: 0.5px solid #C8C8C8;
		}
		.align-right{
        	text-align:right;
        }
        .p{
        font-size: 12px;
        }
        .table{
            border-collapse:separate;
            border-spacing: 0 0.5em;
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
<table  style="position:running(lastFooter); margin-bottom:0px;" align="center">
    <tr class="footfirst">
         <td align="center"></td>
    </tr>
    <tr class="footfont">
        <td align="center"></td>
    </tr>
</table>
<div id="pageHeader">
    <table class="width-64 commonfont" >
        <tr>
        <td></td>
         <td></td>
            <td class="headfirst">${isRefundDone?then('REFUND', 'REFUND REQUEST')}</td>
        </tr>
        <tr>
        <td></td>
         <td></td>
            <td class="headtop topblank"><span><span class="boldDisplay">${unitDisplayName}</span>-<span class="boldDisplay">${hscDisplayName}</span></span></td>
        </tr>
        <tr>
        <td></td>
         <td></td>
            <td class="unit-grey">
               <#if unitAddress??><#list unitAddress.line as line><#if line??> <#if line?has_content> ${line},</#if> <#else></#if></#list> <#if unitAddress.city??> ${unitAddress.city},</#if><#if unitAddress.district??> ${unitAddress.district},</#if><#if unitAddress.state??> ${unitAddress.state},</#if><#if unitAddress.country??> ${unitAddress.country}</#if><#if unitAddress.postalCode??  && unitAddress.postalCode?has_content>-${unitAddress.postalCode} <#else> </#if><#else> - - - </#if>
            </td>
        </tr>
    </table>
    <hr />
</div>

<table  class="width-100 fcommonfont">
    <tr>
        <td>&#160;</td>
    </tr>

</table>
<table border="0" width="100%" class="table">
  <tr>
    <!--First Table-->
    <td width="500px" class="topAlign line-height">
      <table border="0" class="table">
        <tr>
          <td width="95px" class="boldDisplay">
            Patient Name
          </td>
           <td>
            :
          </td>
          <td width="400px">
             ${patientName} <#if patientGenderAndAge??>(${patientGenderAndAge})</#if>
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
             Refund No &nbsp
             </td>
             <td>
               :
             </td>
             <td width="200px">
                <#if refundNo??> ${refundNo} <#else> - </#if>
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
               <#if date??> ${date} <#else> - </#if>
             </td>
       </tr>
        <tr>
                    <td width="95px" class="boldDisplay">
                     Visit No &nbsp
                    </td>
                    <td>
                      :
                    </td>
                    <td width="200px">
                     <#if visitNo??> ${visitNo} <#else> - </#if>
                    </td>
              </tr>
               <tr>
                    <td width="95px" class="boldDisplay">
                     Request Status &nbsp
                     </td>
                     <td>
                       :
                     </td>
                     <td width="200px">
                         <#if refundStatus??> ${refundStatus} <#else> - </#if>
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
</table>
<table cellpadding="6" class="width-100 borderapply commonfont inv-items">
    <thead>
        <tr>
            <td style="width:150px;"><span class="boldDisplay">Sl. No</span></td>
            <td style="width:250px;"><span class="boldDisplay">Receipt No</span></td>
            <td style="width:350px;"><span class="boldDisplay">Receipt Date & Time</span></td>
            <td style="width:250px;text-align: right;"><span class="boldDisplay">Receipt Amount</span></td>
            <td style="width:250px;text-align: right;"><span class="boldDisplay">Refund Amount</span></td>
        </tr>
    </thead>
    <tbody>
    <#list refundDocumentLines as refundDocumentLineItem>
    <tr>
		<td>${refundDocumentLineItem?counter}</td>
		<td><#if (refundDocumentLineItem.getReceiptDetail().getReceiptNumber())??>${refundDocumentLineItem.getReceiptDetail().getReceiptNumber()}</#if></td>
		<td><#if (refundDocumentLineItem.getReceiptDetail().getReceiptDate())??>${refundDocumentLineItem.receiptDetail.receiptDate?datetime.iso?string("dd-MM-yyyy hh:mm a")}</#if></td>
		<td style="text-align: right;"><#if (refundDocumentLineItem.getReceiptDetail().getReceiptAmount())??>${refundDocumentLineItem.getReceiptDetail().getReceiptAmount()?string(",##0.00")}</#if></td>
		<td style="text-align: right;"><#if refundDocumentLineItem.getRefundAmount()??>${refundDocumentLineItem.getRefundAmount()?string(",##0.00")}</#if></td>
    </tr>
    </#list>
    </tbody>
    <tfoot>
        <tr>
            <td colspan="10"></td>
        </tr>
    </tfoot>
</table>
<p class="p" style="text-align: right;">${isRefundDone?then('Total Refunded Amount:', 'Refund Requested Amount:')}&nbsp <span class="boldDisplay"><#if refundRequestedAmount??>${refundRequestedAmount?string(",##0.00")}<#else> - </#if></span></p>
<table border="0" class="table line-height" width="100%"  cellspacing="0">
        <tr>
          <td class="boldDisplay topAlign" style="width: 95px;">
            Amount in words
          </td>
		  <td class="boldDisplay" style="text-align: left;width: 5px;">
            :
          </td>
          <td>
            <#if refundRequestedAmountWords??>${refundRequestedAmountWords} <#else> - </#if>
          </td>
        </tr>
        <tr>
          <td class="boldDisplay" style="width: 95px;">
            Refund Mode
          </td>
		  <td class="boldDisplay topAlign" style="text-align: left;width: 5px;">
            :
          </td>
          <td>
          <#if refundMode??> ${refundMode} <#else> - </#if>
          </td>
       </tr>
        <tr>
          <td class="boldDisplay topAlign" style="width: 95px;">
            Remarks
          </td>
		  <td class="boldDisplay topAlign" style="text-align: left;width: 5px;">
            :
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
        <td><span class="boldDisplay"> Authorised Signature:</span></td>
        <td style="text-align: right;"><span class="boldDisplay">${isRefundDone?then('Refund Received By', '')}</span></td>
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
            <td><b>Prepared By:</b>&nbsp;${preparedBy} <span>|</span> <b>Prepared On:</b>&nbsp;${preparedOn}</td>
        </tr>
        <tr>
            <td><b>Generated By:</b>&nbsp;<#if generatedBy??> ${generatedBy} </#if><span> | </span><b>Generated On:</b>&nbsp;${generatedOn}</td>
        </tr>

      </table>
</body>
</html>
