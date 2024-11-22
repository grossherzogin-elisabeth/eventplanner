<h1>
    <#if user.nickName??>
        Moin ${user.nickName},
    <#else>
        Moin ${user.firstName},
    </#if>
</h1>
<p>
    Du hast bisher noch nicht deine Teilname an ${event.name} am
    <a class="this-is-not-a-phone-number">${event_start_date}</a>
    bestätigt. Bitte bestätige deine Teilnahme an der Reise
    <b>bis spätestens <a class="this-is-not-a-phone-number">${deadline}</a></b>
    durch Klick auf den unten stehenden Link. Wir wünschen dir eine schöne Reise!
</p>
<p>
    <a href="${confirm_link}">Ja, ich nehme teil!</a>
</p>
<p>
    <a href="${deny_link}">Ich kann leider nicht teilnehmen und muss die Reise absagen.</a>
</p>
<p>
    Hier noch einmal alle Informationen zur Reise:
</p>
<table class="facts-table" cellpadding="0" cellspacing="0">
    <tr>
        <td>Reise</td>
        <td><b>${event.name}</b></td>
    </tr>
    <#if position??>
        <tr>
            <td>Position</td>
            <td><b>${position}</b></td>
        </tr>
    </#if>
    <#if event.description??>
        <tr>
            <td>Beschreibung</td>
            <td><b>${event.description}</b></td>
        </tr>
    </#if>
    <#if event_crew_on_board_datetime??>
        <tr>
            <td>Crew an Board</td>
            <td><b><a class="this-is-not-a-phone-number">${event_crew_on_board_datetime}</a></b></td>
        </tr>
    </#if>
    <#if event_start_datetime??>
        <tr>
            <td>Reisebeginn</td>
            <td><b><a class="this-is-not-a-phone-number">${event_start_datetime}</a></b></td>
        </tr>
    </#if>
    <tr>
        <td>Reiseroute</td>
        <td>
            <b>
                <#list event.locations as location>
                    ${location.name()}<#sep>, </#sep>
                </#list>
            </b>
        </td>
    </tr>
</table>
<p>
    Tipp: Du kannst den Status deiner Reisen jederzeit in der App unter
    <a href="${app_link}">${app_link}</a> einsehen und dich dort direkt zu Reisen an- und abmelden.
</p>
<p>
    Mit freundlichen Grüßen<br>
    Dein Lissi Büro-Team
</p>