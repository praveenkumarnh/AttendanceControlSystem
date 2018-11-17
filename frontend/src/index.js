import {Config} from "./config";
import EventBus from "./services/eventbus/index";

import Home from "./components/home";
import Notification from "./components/notification/index";

(function () {
    function Bootstrap() {
        Home.render();
        registerEventBusHandler();
    }

    function registerEventBusHandler() {
        const eventBus = new EventBus(Config.apiUrl + "/eventbus/");

        eventBus.onopen = function () {
            eventBus.registerHandler('tracked.employee', function (err, msg) {
                new Notification(
                    msg.body.code,
                    msg.body.firstName,
                    msg.body.lastName,
                    msg.body.email,
                    msg.body.action,
                    msg.body.createdAt,
                    msg.body.avatar
                );
            });
        };
    }

    return Bootstrap;
}())();
