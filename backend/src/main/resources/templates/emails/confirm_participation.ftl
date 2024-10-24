<span class="preheader">Du wurdest von der Crew der Reise ${event.name} am ${event_start_date} entfernt.</span>
<table role="presentation" border="0" cellpadding="0" cellspacing="0" class="main">
    <tr>
        <td class="wrapper">
            <p>
                Moin ${user.firstName},
            </p>
            <p>
                Du bist füt die Reise <b>${event.name}</b> vom <b>${event_start_date}</b> bis zum
                <b>${event_end_date}</b> als Crew eingeplant. Bitte bestätige deine Teilnahme <b>bis spätestens
                ${deadline}</b> durch Klick auf den unten stehenden Link. Wir wünschen dir eine schöne Reise!
            </p>
            <p>
                <a href="${confirm_link}">Ja, ich nehme teil!</a>
            </p>
            <p>
                <a href="${deny_link}">Ich kann leider nicht teilnehmen und muss die Reise absagen.</a>
            </p>
            <p>
                Tipp: Du kannst den Status deiner Reisen jederzeit in der App unter
                <a href="${app_link}">${app_link}</a> einsehen und dich dort direkt zu Reisen an- und abmelden. Außerdem
                kannst du deine persönlichen Daten in der App einsehen und auch selbst bearbeiten.
                Dazu musst dir nur einen Lissi Account mit dieser Email Adresse erstellen.
            </p>
            <p>
                Mit freundlichen Grüßen<br>
                Dein Lissi Büro-Team
            </p>
        </td>
    </tr>
</table>