/*jshint esversion: 6 */

'use strict';

import $ from 'jquery';
import 'datatables.net-bs4';
import 'datatables.net-responsive-bs4';
import 'datatables.net-scroller-bs4';
import 'datatables.net-select-bs4';
import {EventBus} from "vertx-eventbus";
import alertify from "alertifyjs";
import TableTemplate from "html-loader!./table.html";
import "./style.css";

(function () {
    let _url = "http://192.168.1.56:8083";
    let _table;
    const _actions = {"entrance": "Entró", "exit": "Salió"};

    function Asiscontrol() {
        rendertable();
        registerEventBus();
        loadRecords();
    }

    function rendertable() {
        $("body").html(TableTemplate);
        _table = $('#data-table').DataTable({
            "order": [[4, "desc"]],
            rowReorder: true,
            "language": {
                "sProcessing": "Procesando...",
                "sLengthMenu": "Mostrar _MENU_ registros",
                "sZeroRecords": "No se encontraron resultados",
                "sEmptyTable": "Ningún dato disponible en esta tabla",
                "sInfo": "Mostrando registros del _START_ al _END_ de un total de _TOTAL_ registros",
                "sInfoEmpty": "Mostrando registros del 0 al 0 de un total de 0 registros",
                "sInfoFiltered": "(filtrado de un total de _MAX_ registros)",
                "sInfoPostFix": "",
                "sSearch": "Buscar:",
                "sUrl": "",
                "sInfoThousands": ",",
                "sLoadingRecords": "Cargando...",
                "oPaginate": {
                    "sFirst": "Primero",
                    "sLast": "Último",
                    "sNext": "Siguiente",
                    "sPrevious": "Anterior"
                },
                "oAria": {
                    "sSortAscending": ": Activar para ordenar la columna de manera ascendente",
                    "sSortDescending": ": Activar para ordenar la columna de manera descendente"
                }
            },
            columns: [
                {data: 'code', width: "50px"},
                {data: 'firstname'},
                {data: 'lastname'},
                {data: 'email'},
                {data: 'createdAt'},
                {data: 'action', width: "30px"}
            ]
        });
    }

    function registerEventBus() {

        const eventBus = new EventBus(_url + "/eventbus/");

        eventBus.onopen = function () {
            eventBus.registerHandler('tracked.employee', function (err, msg) {
                const action = msg.body.action === 'entrance' ? 'entrar' : 'salir';

                _table.row.add({
                    "code": msg.body.code,
                    "firstname": msg.body.firstName,
                    "lastname": msg.body.lastName,
                    "email": msg.body.email,
                    "createdAt": msg.body.createdAt,
                    "action": _actions[msg.body.action]
                }).draw();

                const html = `<div class="track-entry text-center">
                        <img class="rounded mx-auto d-block" src="${msg.body.avatar}"/>
                        <p>
                            <strong>${msg.body.firstName} ${msg.body.lastName}</strong> acaba de ${action}
                        </p>
                    </div>\n`;

                alertify.notify(html, msg.body.action, 5);
            });
        };
    }

    function loadRecords() {
        $.ajax({
            url: _url + "/api/tracks",
            success: function (result, status) {
                if (result.hasOwnProperty('success') && result.success) {
                    result.tracks.forEach(data => {
                            _table.row.add({
                                "code": data.employee.code,
                                "firstname": data.employee.firstName,
                                "lastname": data.employee.lastName,
                                "email": data.employee.email,
                                "createdAt": data.track.createdAt,
                                "action": _actions[data.track.action]
                            }).draw();
                        }
                    );
                }
            }
        });
    }

    return Asiscontrol;
}())();
