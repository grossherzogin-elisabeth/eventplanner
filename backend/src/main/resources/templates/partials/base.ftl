<!doctype html>
<html lang="en">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="format-detection" content="telephone=no">
    <title>${title}</title>
    <style media="all" type="text/css">
        a[x-apple-data-detectors] {
            pointer-eventDetails: none !important;
            color: inherit !important;
            text-decoration: none !important;
            font-size: inherit !important;
            font-family: inherit !important;
            font-weight: inherit !important;
            line-height: inherit !important;
        }

        .this-is-not-a-phone-number {
            pointer-eventDetails: none !important;
            color: inherit !important;
            text-decoration: none !important;
            font-size: inherit !important;
            font-family: inherit !important;
            font-weight: inherit !important;
            line-height: inherit !important;
        }

        /* GLOBAL RESETS */
        body {
            font-family: Helvetica, sans-serif;
            -webkit-font-smoothing: antialiased;
            font-size: 16px;
            line-height: 1.3;
            -ms-text-size-adjust: 100%;
            -webkit-text-size-adjust: 100%;
        }

        table {
            border-collapse: separate;
            mso-table-lspace: 0pt;
            mso-table-rspace: 0pt;
            width: 100%;
        }

        table td {
            font-family: Helvetica, sans-serif;
            font-size: 16px;
            vertical-align: top;
        }

        /* BODY & CONTAINER */

        body {
            background-color: transparent;
            margin: 0;
            padding: 0;
        }

        .body {
            background-color: transparent;
            width: 100%;
        }

        .container {
            margin: 0 auto !important;
            max-width: 600px;
            width: 600px;
        }

        .content {
            padding: 24px;
            box-sizing: border-box;
            display: block;
            margin: 0 auto;
            max-width: 600px;
        }

        .footer {
            clear: both;
            padding: 24px;
            text-align: left;
            width: 100%;
        }

        .footer td,
        .footer p,
        .footer span,
        .footer a {
            color: #9a9ea6;
            font-size: 12px;
            text-align: left;
        }

        /* TYPOGRAPHY */

        h1 {
            font-family: Helvetica, sans-serif;
            font-size: 16px;
            font-weight: normal;
            margin: 0;
            margin-bottom: 16px;
        }

        p {
            font-family: Helvetica, sans-serif;
            font-size: 16px;
            font-weight: normal;
            margin: 0;
            margin-bottom: 16px;
        }

        .facts-table {
            table-layout: fixed;
            width: auto;
            margin: 0;
            margin-bottom: 16px;
        }

        .facts-table td {
            padding-right: 8px;
        }

        @media only screen and (max-width: 640px) {
            .main p,
            .main td,
            .main span {
                font-size: 16px !important;
            }

            .content {
                padding: 8px !important;
            }

            .container {
                padding-top: 8px !important;
                width: 100% !important;
            }

            .footer {
                padding: 8px !important;
            }
        }

        /* PRESERVE THESE STYLES IN THE HEAD */

        @media all {
            .ExternalClass {
                width: 100%;
            }

            .ExternalClass,
            .ExternalClass p,
            .ExternalClass span,
            .ExternalClass font,
            .ExternalClass td,
            .ExternalClass div {
                line-height: 100%;
            }

            .apple-link a {
                color: inherit !important;
                font-family: inherit !important;
                font-size: inherit !important;
                font-weight: inherit !important;
                line-height: inherit !important;
                text-decoration: none !important;
            }

            #MessageViewBody a {
                color: inherit;
                text-decoration: none;
                font-size: inherit;
                font-family: inherit;
                font-weight: inherit;
                line-height: inherit;
            }
        }
    </style>
</head>
<body>
<table role="presentation" border="0" cellpadding="0" cellspacing="0" class="body">
    <tr>
        <td>&nbsp;</td>
        <td class="container">
            <div class="content">
                <h1>
                    <#if user.nickName??>
                        Moin ${user.nickName},
                    <#else>
                        Moin ${user.firstName},
                    </#if>
                </h1>
                ${content}
                <p>
                    Mit freundlichen Grüßen<br>
                    Dein Lissi Büro-Team
                </p>
            </div>
            <div class="footer">
                <p>
                    Schulschiffverein Großherzogin Elisabeth e.V.<br>
                    Rathausplatz 5, 26931 Elsfleth<br>
                    Sitz Elsfleth / Weser Vereinsregister Oldenburg 100123
                </p>
                <p>
                    Email: <a href="mailto:office@grossherzogin-elisabeth.de">office@grossherzogin-elisabeth.de</a><br>
                    Tel. <a href="tel:+49 4404 988672">04404 - 988672</a><br>
                    Bürozeiten: Mo. - Do. 09:00 - 12:00 Uhr und 14:00 - 16:30 Uhr, Fr. 09:00 - 12:00 Uhr
                </p>
                <p>
                    Dies ist eine automatisch generierte Email. Du erhältst diese Email, weil mit deiner Email Adresse
                    ein Nutzer beim Schulschiffverein Großherzogin Elisabeth e.V. als Stammcrew Mitglied registriert
                    ist. Wenn du keine weiteren Emails erhalten möchtest, schreib uns gerne eine Email an
                    <a href="mailto:admin@grossherzogin-elisabeth.de">admin@grossherzogin-elisabeth.de</a>.
                </p>
            </div>
        </td>
        <td>&nbsp;</td>
    </tr>
</table>
</body>
</html>