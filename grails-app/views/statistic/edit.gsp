<meta name="layout" content="enrichment">

<div class="row">
    <div class="col-xs-12">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title"><g:message code="statistic.edit.record"/> - ${record.multiFields.get("publicationTitle")?.getPrioValue()}</h3>
            </div>
            <div class="statistics-data">
                <table class="statistics-details">
                    <tr>
                        <th><g:message code="statistic.edit.field"/></th>
                        <th><g:message code="statistic.edit.value"/></th>
                        <th><g:message code="statistic.edit.source"/></th>
                        <th><g:message code="statistic.edit.status"/></th>
                    </tr>
                    <g:set var="lineCounter" value="${0}" />
                    <g:each in="${record.multiFields}" var="multiField">
                        <g:if test="${multiField?.value?.getPrioValue()}">
                            <tr class="${ (lineCounter % 2) == 0 ? 'even hover' : 'odd hover'}">
                                <td class="statistics-cell-key">${multiField.key}</td>
                                <td class="statistics-cell-value" contenteditable="true">${multiField.value.getPrioValue()}</td>
                                <td class="statistics-cell-source">${multiField.value.getPrioSource()}</td>
                                <td class="statistics-cell-status"><g:message code="${multiField.value.status}"/></td>
                            </tr>
                            <g:set var="lineCounter" value="${lineCounter + 1}"/>
                        </g:if>
                    </g:each>
                </table>
            </div>
        </div>
        <!-- <ul class="list-group content-list">
            <input type="submit" value="${message(code:'statistic.edit.cancel')}" class="btn btn-warning"/>
            <input type="submit" value="${message(code:'statistic.edit.save')}" class="btn btn-success"/>
        </ul> -->
    </div>
</div>

<script>
    var tableRowIndex;
    var keys    = document.querySelectorAll('.statistics-cell-key'),
        values  = document.querySelectorAll('.statistics-cell-value'),
        rows = [].filter.call(document.querySelectorAll("tr"), function(row) {
            return row.getElementsByClassName('statistics-cell-key').length != 0;
        });
        // rows only contains data entries

    values.forEach((valueField) => {
        valueField.addEventListener('keydown', function (event) {
            let target = event.target,
                input = target.nodeName != 'INPUT' && target.nodeName != 'TEXTAREA',
                data = {};
            if (input) {
                if (event.which == 27 /* that is "escape" */) {
                    // restore unedited state
                    document.execCommand('undo');
                    target.blur();
                }
                else if (event.which == 13 /* that is "enter" */) {
                    // save && send update
                    var valuesArray = Array.prototype.slice.call(values);
                    tableRowIndex = valuesArray.indexOf(valueField);
                    const rowKey = keys.item(tableRowIndex).innerHTML;
                    const value = target.innerHTML;
                    jQuery.ajax({
                        type: 'POST',
                        url: '${grailsApplication.config.grails.app.context}/statistic/update',
                        data: {
                            key: rowKey,
                            value: value,
                            uid: '${record.uid}',
                            sthash: '${sthash}'
                        },
                        success: function(data) {
                            var json = jQuery.parseJSON(data);
                            // const row = rows.item(rowKey);
                            // window.location = '${grailsApplication.config.grails.app.context}/statistic/edit';
                        },
                        error: function (deXMLHttpRequest, textStatus, errorThrown) {
                            console.log("ERROR - Could not update statistics table, failing Ajax request.");
                            console.log(data)
                        }
                    });
                    target.blur();
                    event.preventDefault();
                }
            }
        }, true);
    });
</script>