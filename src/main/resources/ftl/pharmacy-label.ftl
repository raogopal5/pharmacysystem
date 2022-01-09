^Q25,3
^W50
^H10
^P1
^S3
^AD
^C1
^R0
~Q+0
^O0
^D0
^E0
~R200
^XSET,ROTATION,0
^L
Dy2-me-dd
Th:m:s

AB,0,44,1,1,0,0E,     ${NAME}
AB,0,70,1,1,0,0E,     ${NAME2}

AB,0,96,1,1,0,0E,     <#if STRENGTH??>${STRENGTH}<#else> - </#if>
AB,0,122,1,1,0,0E,     <#if FORM??>${FORM}<#else> - </#if>
AB,0,148,1,1,0,0E,     Batch: <#if BATCH??>${BATCH}<#else> - </#if> / Expiry: ${EXPIRY_DATE}
E
