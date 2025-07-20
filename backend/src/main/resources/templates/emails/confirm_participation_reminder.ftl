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
<#include "../components/registration_details_table.ftl">