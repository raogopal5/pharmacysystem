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
                font-size:7pt;
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
			font-size: 8pt;
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
		.topblank{
			padding-top: 10px;
		}
        .unit-grey{
            font-size: 8pt;
            color: #717171;
        }
        .text-center {
            text-align: center;
        }
		.footfirst{
            font-size: 8pt;
            color: #717171;
        }
        .footfont{
            font-size: 8pt;
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
            font-size: 8pt;
            line-height: 16px;
        }
        .rightcolumns{
            width: 50%;
            font-size: 8pt;
            text-align : right;
        }
		.rightcolumnslast{
            width: 50%;
            font-size: 8pt;
            line-height: 16px;
            text-align: right;
        }
        .sgst{
            color: #717171;
            display: block;
            font-size: 8pt;
        }
		.subsection{
			font-size: 8pt;
		}
		.amount{
			font-weight: bold;
			font-size: 8pt;
		}
		.headalign{
			text-align: left;
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
		.headfirst{
                    font-size: 14pt;
                }
                .headtop{
                    font-size: 10pt;
        			font-weight: bold;
                }
    </style>
</head>
<body>
<table class="width-64 commonfont topDown">
    <tr>
        <td class="headfirst">INPATIENT ISSUE SLIP</td>
    </tr>
    <tr>
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
</table>
<hr />
<table  class="width-100 commonfont">
    <tr>
        <td>&#160;</td>
    </tr>
    <tr>
        <td>&#160;</td>
    </tr>
</table>
<div class = "running">
    <table class="commonfont" style="margin-left:9px;">
        <tbody>
        <tr>
            <td class="leftcolumns">
                <span class="boldDisplay">Patient Name </span>: <#if patientName??>${patientName}</#if>
            </td>
        </tr>
        <tr>
                    <td class="leftcolumns colorapply">
                        <span class="boldDisplay">Patient MRN </span>: <#if patientMrn??>${patientMrn} <#else> - </#if>
                    </td>
        </tr>
        <tr>
            <td class="leftcolumns colorapply">
                <span class="boldDisplay">Dept </span>: <#if dept??> ${dept} <#else> - </#if>
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
              <td width="95px" class="boldDisplay topAlign">Dept</td>
              <td class="topAlign " width="2px">:</td>
              <td><#if dept??> ${dept} <#else> - </#if></td>
            </tr>
             <tr>
               <td width="95px" class="boldDisplay topAlign">Consultant</td>
               <td class="topAlign" width="2px">:</td>
               <td> <#if consultantName??> ${consultantName} <#else> - </#if></td>
            </tr>
            <tr>
                 <td width="95px" class="boldDisplay topAlign">Visit No.</td>
                 <td class="topAlign" width="2px">:</td>
                 <td> <#if visitNo??> ${visitNo} <#else> - </#if></td>
             </tr>
               <tr>
                  <td width="95px" class="boldDisplay topAlign">Ward</td>
                  <td class="topAlign" width="2px">:</td>
                  <td> <#if ward??> ${ward} <#else> <#if currentWard??> ${currentWard} <#else> - </#if> </#if></td>
                  </tr>
                    <tr>
                     <td width="95px" class="boldDisplay topAlign">Recent Location</td>
                     <td class="topAlign" width="2px">:</td>
                     <td>  <#if currentWard??> ${currentWard} <#else> - </#if> / <#if currentBed??> ${currentBed} <#else> - </#if></td>
                   </tr>
          </table>
        </td>
        <!--Second Table-->
        <td width="2%">&nbsp;</td>
        <td  class="topAlign">
          <table border="0" align="left" style="width:100%;">
          <tr>
            <td colspan="3" class="topAlign" style="height:28px;"><#if orderBarcodeImage??>${orderBarcodeImage}</#if></td>

          </tr>
          <tr>
            <td width="95px" class="boldDisplay topAlign">Order No</td>
            <td class="topAlign " width="2px">:</td>
            <td><#if orderNo??> ${orderNo} <#else> - </#if></td>
          </tr>
          <tr>
            <td width="95px" class="boldDisplay topAlign">Ordered On</td>
            <td class="topAlign ">:</td>
            <td><#if orderedOn??> ${orderedOn} <#else> - </#if></td>
          </tr>
          <tr>
              <td width="95px" class="boldDisplay topAlign">Store</td>
              <td class="topAlign ">:</td>
              <td><#if store??>${store} <#else> - </#if></td>
          </tr>
          <tr>
             <td width="95px" class="boldDisplay topAlign">Ordered By</td>
             <td class="topAlign ">:</td>
             <td><#if orderedBy??>${orderedBy} <#else> - </#if></td>
             </tr>
          <tr>
             <td width="95px" class="boldDisplay topAlign">Issue No</td>
             <td class="topAlign ">:</td>
             <td><#if issueNo??>${issueNo} <#else> - </#if></td>
          </tr>
            <tr>
              <td width="95px" class="boldDisplay topAlign">Issue On</td>
              <td class="topAlign ">:</td>
              <td><#if issueOn??>${issueOn} <#else> - </#if></td>
           </tr>
            <tr>
                 <td width="95px" class="boldDisplay topAlign">Issued By</td>
                 <td class="topAlign ">:</td>
                 <td><#if issuedBy??>${issuedBy} <#else> - </#if></td>
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
<table class="width-100 commonfont">
    <tr>
        <td class="boldDisplay">NOTE: This is not a receipt of any cash transaction</td>
    </tr>

</table>
<table cellpadding="6" class="width-100 borderapply commonfont inv-items">
    <thead>
           <tr>
            <td style="width:25px;"><span class="boldDisplay">Sl No.</span></td>
            <td style="width:300px;"><span class="boldDisplay">Item Name</span></td>
            <td style="width:100px;"><span class="boldDisplay">Item Code</span></td>
            <td style="width:120px;"><span class="boldDisplay">Mfr</span></td>
            <td style="width:50px;"><span class="boldDisplay">Unit Price</span></td>
            <td style="width:100px;"><span class="boldDisplay">Batch/Exp</span></td>
            <td style="width:40px;" class="headalign"><span class="boldDisplay">Req Qty</span></td>
            <td style="width:40px;" class="headalign"><span class="boldDisplay">Issue Qty</span></td>
        </tr>
    </thead>
    <tbody>
            <#assign x = 1>
		<#list documentLines as documentLine>
		<tr>
		   <td width="2px"  class="topAlign" >${x}.</td>
		    <td  class="topAlign"><#if documentLine.getName()??> ${documentLine.getName()} <#else> - </#if></td>
		   <td  class="topAlign"><#if documentLine.getCode()??> ${documentLine.getCode()} <#else> - </#if></td>
		   <td  class="topAlign"><#if documentLine.getMedication()?? ><#if documentLine.getMedication().getManufacturer()??> ${documentLine.getMedication().getManufacturer()}</#if>  <#else> - </#if></td>
		   <td  class="topAlign"><#if documentLine.getMrp()??> ${documentLine.getMrp()} <#else> - </#if></td>
		    <td  class="topAlign"><#if documentLine.getBatchNumber()??> ${documentLine.getBatchNumber()} <#else> - </#if>/<#if documentLine.getExpiryDate()??> ${documentLine.getExpiryDate()} <#else> - </#if></td>
		  	   <td  class="topAlign"><#if documentLine.getOrderItem()?? ><#if documentLine.getOrderItem().getQuantity()??> ${documentLine.getOrderItem().getQuantity()}</#if>  <#else> - </#if></td>
          	   <td  class="topAlign"><#if documentLine.getQuantity()??> ${documentLine.getQuantity()} <#else> - </#if></td>
                   <#assign x++>
		</tr>
		</#list>
    </tbody>
    <tfoot>
        <tr>
            <td colspan="10"></td>
        </tr>
    </tfoot>
</table>
<table class="width-100 commonfont">
<tr>
        <td>&#160;</td>
    </tr>
    <tr>
            <td>&#160;</td>
        </tr>
    <tr>
        <td style="text-align: left;"><span class="boldDisplay">Remarks:</span><#if remarks??> ${remarks}</#if></td>
        <td style="text-align: left;"></td>
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
            <td><span><b>Prepared By<b>:</span>&nbsp;${preparedBy} | <span class="boldDisplay"><b>Prepared On</b>:</span>&nbsp;${preparedOn} | </td>
        </tr>
        <tr>
            <td><span><b>Generated By</b>:&nbsp;${generatedBy} | <span><b>Generated On</bold>:</span> <span class="generatedOn">&nbsp;${generatedOn}</span> | </td>

</table>
</body>
</html>
