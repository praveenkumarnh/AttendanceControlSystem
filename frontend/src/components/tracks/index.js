import jQuery from 'jquery';
import TrackService from "../../services/tracks";

import 'datatables.net-bs4';
import 'datatables.net-responsive-bs4';
import 'datatables.net-scroller-bs4';
import 'datatables.net-select-bs4';

import view from "html-loader!./index.html";
import "./index.css";

const actions = {
    "entrance": "Entró",
    "exit": "Salió"
};

let TableHandler;

class Tracks {
    static render() {
        jQuery("data-table").append(view);

        TableHandler = jQuery('#track-list').DataTable({
            "order": [
                [4, "desc"]
            ],
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
            columns: [{
                    data: 'code',
                    width: "50px"
                },
                {
                    data: 'firstname'
                },
                {
                    data: 'lastname'
                },
                {
                    data: 'email'
                },
                {
                    data: 'createdAt'
                },
                {
                    data: 'action',
                    width: "30px"
                }
            ]
        });

        Tracks.fecthAll();
    }

    static fecthAll() {
        TrackService.fetchAll().done(function (tracks) {
            TableHandler.clear().draw();

            tracks.forEach(data => {
                TableHandler.row.add({
                    "code": data.employee.code,
                    "firstname": data.employee.firstName,
                    "lastname": data.employee.lastName,
                    "email": data.employee.email,
                    "createdAt": data.track.createdAt,
                    "action": actions[data.track.action]
                }).draw();
            });
        });
    }
}

export {
    TableHandler,
    Tracks
};