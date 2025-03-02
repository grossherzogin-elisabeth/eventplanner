<p>
    Du wurdest für die Reise ${event.name} am
    <a class="this-is-not-a-phone-number">${event_start_date}</a>
    als Crew eingeplant. Hier sind noch einmal alle Informationen zur Reise:
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
    Solltest du doch nicht an der Reise teilnehmen können, melde dich bitte bis spätestens eine Woche
    vor der Reise wieder ab, damit wir noch Zeit haben einen Ersatz für dich zu finden.
</p>