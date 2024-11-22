<h1>
    <#if user.nickName??>
        Moin ${user.nickName},
    <#else>
        Moin ${user.firstName},
    </#if>
</h1>
<p>
    Deine Teilnahme an der Reise ${event.name} am
    <a class="this-is-not-a-phone-number">${event_start_date}</a>
    wurde storniert. Du bist nun nicht mehr als Crew eingeplant.
    Bitte versuche kurzfristige Absagen wenn möglich zu vermeiden,
    da es dann schwierig ist noch einen Ersatz für dich zu finden.
</p>
<p>
    Tipp: Du kannst den Status deiner Reisen jederzeit in der App unter
    <a href="${app_link}">${app_link}</a> einsehen und dich dort direkt zu Reisen an- und abmelden.
</p>
<p>
    Mit freundlichen Grüßen<br>
    Dein Lissi Büro-Team
</p>