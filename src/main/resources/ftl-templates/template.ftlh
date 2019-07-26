Hallo ${stockPriceAlarm.user.fullName}! <br><br>

Der folgende Alarm wurde aktiviert:<br><br>
<hr>
<b>Produkt</b>: ${stockPriceAlarm.name} (${stockPriceAlarm.holding.stockIdentifier})<br>
<b>Typ</b>: ${stockPriceAlarm.type.label} <br>
<#if stockPriceAlarm.type.percent><b>Referenzpreis</b>: ${stockPriceAlarm.referencePrice}<br></#if>
<b>Limit</b>: ${stockPriceAlarm.amount} <#if stockPriceAlarm.type.percent>%</#if><br>
<b>Auslöser</b>: ${price} ${stockPriceAlarm.currency}<br>
<hr>
<br>
Mit freundlichen Grüßen.