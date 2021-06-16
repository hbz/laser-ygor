<!-- _uploadFile.gsp -->
<%@ page import="ygor.Enrichment" %>

<g:if test="${enrichment == null || enrichment.status == null}">

    <!-- KBART OR ONIX FILE UPLOAD -->
    <div class="row">
        <div class="col-xs-12">
            <g:set var="filename" value="${message(code: 'uploadFile.file.nofile')}" scope="page"/>
            <g:if test="${session.lastUpdate?.file?.originalFilename != null}">
                <g:set var="filename" value="${session.lastUpdate?.file?.originalFilename}" scope="page"/>
            </g:if>
            <g:uploadForm name="uploadFile" action="uploadFile">
                <ul class="list-group content-list">
                    <li class="list-group-item">
                        <div class="input-group">
                            <span class="input-group-addon"><g:message code="uploadFile.file.label"/></span>
                            <input class="form-control" type="text" id="uploadFileLabel" name="uploadFileLabel" readonly
                                   value="${filename}" size="46"/>
                        </div>
                    </li>
                </ul>
                <ul class="list-group content-list">
                    <li class="list-group-item">
                        <label class="btn btn-link btn-file">
                            <input type="file" accept=".csv,.tsv,.ssv,.txt,.xml" name="uploadFile"
                                   style="display: none;"/><g:message code="uploadFile.button.select"/>
                        </label>
                        <input type="submit" value="${message(code: 'uploadFile.button.upload')}"
                               class="btn btn-success"/>
                        <script>
                            jQuery(document).on('change', "[name='uploadFile']", function () {
                                var input = $(this),
                                    files = input.get(0).files;
                                if (files) {
                                    var file0 = files[0],
                                        filename = file0.name,
                                        replaced = filename.replace(/\\/g, '/'),
                                        label = replaced.replace(/.*\//, '');
                                    jQuery('#uploadFileLabel')[0].setAttribute("value", label);
                                }
                            });
                        </script>
                    </li>
                </ul>
            </g:uploadForm>
        </div>
    </div><!-- .row -->
    <!-- KBART FILE UPLOAD END -->

    <!-- KBART URL-DRIVEN PROCESSING -->
    <br><br>
    <div class="row">
        <div class="col-xs-12">
            <ul class="list-group how-to-list">
                <li class="list-group-item">
                    <span class="glyphicon glyphicon-share-alt" aria-hidden="true"></span>
                    &nbsp; <g:message code="howtostep1.uploadUrl"/>
                </li>
            </ul>
            <g:set var="url" value="${message(code: 'uploadUrl.url.noUrl')}" scope="page"/>
            <g:if test="${session.lastUpdate?.url != null}">
                <g:set var="url" value="${session.lastUpdate?.url}" scope="page"/>
            </g:if>
            <g:uploadForm name="uploadUrl" action="uploadUrl">
                <ul class="list-group content-list">
                    <li class="list-group-item">
                        <div class="input-group">
                            <span class="input-group-addon"><g:message code="uploadUrl.url.label"/></span>
                            <input class="form-control" type="text" id="uploadUrlText" name="uploadUrlText"
                                   type="url" value="${url}" size="46"/>
                        </div>
                        <br>
                        <div>
                            <g:message code="uploadUrl.autoUpdate" />
                            <g:checkBox name="urlAutoUpdate" checked="false" value="${urlAutoUpdate}"/>
                        </div>
                    </li>
                </ul>
                <ul class="list-group content-list">
                    <li class="list-group-item">
                        <input type="submit" value="${message(code: 'uploadFile.button.upload')}"
                               class="btn btn-success"/>
                        <script>
                            $("#uploadUrlText").on("focus", function() {
                                $("#uploadUrlText").val("");
                            });
                            jQuery(document).on('change', "[name='uploadUrl']", function () {
                                jQuery('#uploadUrlLabel')[0].setAttribute("value", label);
                            });
                        </script>
                    </li>
                </ul>
            </g:uploadForm>
        </div>
    </div><!-- .row -->
    <!-- KBART URL-DRIVEN PROCESSING END -->

    <!-- RAW DATA UPLOAD -->
    <div class="row">
        <div class="col-xs-12">
            <br><br>
            <ul class="list-group how-to-list">
                <li class="list-group-item">
                    <span class="glyphicon glyphicon-share-alt" aria-hidden="true"></span>
                    &nbsp; <g:message code="howtostep1.uploadRaw"/>
                </li>
            </ul>
            <g:set var="filenameRaw" value="${message(code: 'uploadFile.raw.file.nofile')}" scope="page"/>
            <g:uploadForm id="uploadRawFile" action="uploadRawFile">

                <ul class="list-group content-list">
                    <li class="list-group-item">
                        <div class="input-group">
                            <span class="input-group-addon"><g:message code="uploadFile.raw.file.label"/></span>
                            <input class="form-control" type="text" id="uploadRawFileLabel" name="uploadRawFileLabel"
                                   readonly value="${filenameRaw}" size="46"/>
                        </div>
                    </li>
                </ul>

                <ul class="list-group content-list">
                    <li class="list-group-item">
                        <label class="btn btn-link btn-file">
                            <input type="file" accept=".raw.zip" name="uploadRawFile"
                                   style="display: none;"/><g:message code="uploadFile.button.select"/>
                        </label>
                        <input type="submit" value="${message(code: 'uploadFile.button.upload')}"
                               class="btn btn-success"/>
                        <script>
                            jQuery(document).on('change', "[name='uploadRawFile']", function () {
                                var input = $(this),
                                    files = input.get(0).files;
                                if (files) {
                                    var file0 = files[0],
                                        filename = file0.name,
                                        replaced = filename.replace(/\\/g, '/'),
                                        label = replaced.replace(/.*\//, '');
                                    jQuery('#uploadRawFileLabel')[0].setAttribute("value", label)
                                }
                            });
                        </script>
                    </li>
                </ul>
            </g:uploadForm>
        </div>
    </div><!-- .row -->
    <!-- RAW DATA UPLOAD END -->
</g:if>
