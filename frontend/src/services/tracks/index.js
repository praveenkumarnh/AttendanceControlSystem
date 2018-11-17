import $ from 'jquery';
import {Config} from "../../config";

export default class TrackService {
    static fetchAll() {
        let def = $.Deferred();

        $.ajax(Config.apiUrl + "/api/tracks").done(function (data) {
            def.resolve(data.tracks);
        }).fail(function () {
            def.reject();
        });

        return def.promise();
    }
}