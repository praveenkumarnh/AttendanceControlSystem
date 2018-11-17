import alertify from "alertifyjs";

import {TableHandler} from "../tracks";

export default class Notification {

    constructor(code, firstName, lastName, email, action, createdAt, avatar) {

        const type = action === 'entrance' ? 'entrar' : 'salir';

        TableHandler.row.add({
            "code": code,
            "firstname": firstName,
            "lastname": lastName,
            "email": email,
            "createdAt": createdAt,
            "action": type
        }).draw();

        const html = `<div class="track-entry text-center">
                            <img class="rounded mx-auto d-block" src="${avatar}"/>
                            <p>
                                <strong>${firstName} ${lastName}</strong> acaba de ${type}
                            </p>
                        </div>\n`;

        alertify.notify(html, action, 5);
    }
}