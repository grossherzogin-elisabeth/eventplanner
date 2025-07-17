<p>
    Wir haben deine Anmeldung für die Reise ${event.name} am
    <a class="this-is-not-a-phone-number">${event_start_date}</a>
    erhalten. Bitte beachte: Du stehst bisher nur auf der Warteliste und bist noch nicht für
    diese Reise eingeplant! Wenn du zur Crew hinzugefügt wirst, werden wir dich umgehend
    darüber informieren.
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
    <#if registration??>
        <tr>
            <td>Übernachtung an Bord</td>
            <#if registration.overnightStay == true>
                <td><b>Ja</b></td>
            <#else>
                <td><b>Nein</b></td>
            </#if>
        </tr>
        <tr>
            <td>Anreise am Vortag</td>
            <#if registration.arrival??>
                <td><b>Ja</b></td>
            <#else>
                <td><b>Nein</b></td>
            </#if>
        </tr>
        <#if registration.note??>
            <tr>
                <td>Notiz fürs Büro</td>
                <td><b>${registration.note}</b></td>
            </tr>
        </#if>
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