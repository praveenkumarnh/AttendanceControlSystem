import alertify from "alertifyjs";
import { TableHandler } from "../tracks";
import "./index.css";

export default class Notification {

    /**
     * 
     * @param {Number} code 
     * @param {String} firstName 
     * @param {String} lastName 
     * @param {String} email 
     * @param {String} action 
     * @param {String} createdAt 
     * @param {String} avatar 
     */
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