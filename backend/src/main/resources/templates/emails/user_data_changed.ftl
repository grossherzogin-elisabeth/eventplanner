<h1>
    <#if user.nickName??>
        Moin ${user.nickName},
    <#else>
        Moin ${user.firstName},
    </#if>
</h1>
<p>
    ${userName} hat seine persönlichen Daten geändert.
</p>
<p>
    Mit freundlichen Grüßen<br>
    Dein Lissi Büro-Team
</p>
