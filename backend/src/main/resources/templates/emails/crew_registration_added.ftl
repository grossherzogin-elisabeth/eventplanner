<h1>
    <#if user.nickName??>
        Moin ${user.nickName},
    <#else>
        Moin ${user.firstName},
    </#if>
</h1>
<p>
    ${userName} hat sich für die Reise ${event.name} am
    <a class="this-is-not-a-phone-number">${event_start_date}</a>
    als ${position} angemeldet.
</p>
<p>
    Mit freundlichen Grüßen<br>
    Dein Lissi Büro-Team
</p>