<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Ygor - Alpha</title>
	</head>
	<body>
		<div class="row">
			<div class="col-xs-10 col-xs-offset-1">
			</div>

			<div class="col-xs-10 col-xs-offset-1">
				<br />

				<h3>
					<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
					<g:message code="index.development" />
				</h3>
			</div>

			<div class="col-xs-10 col-xs-offset-1">
				<br />
				<p class="lead"></p>

				<p><h2><g:message code="about.title" /></h2></p>
				<p><g:message code="about.content.1" /></p>
				<p><g:message code="about.content.2" /></p>
				<p><g:message code="about.content.3" /></p>
				<dl>
				<br/>
				<div class="panel panel-default">
						<div class="panel-heading">
							<h4 class="panel-title">
								<a data-toggle="collapse" href="#versions" style="text-decoration:none"><g:message code="index.versionhistory" /></a>
							</h4>
						</div>
						<div id="versions" class="panel-collapse collapse">
							<div class="panel-body">
								<dl>
									<br /><dt><g:message code="recent.changes.key" /></dt><br />

									<dd>- <g:message code="recent.changes" /></dd>
									<br /><dt>0.93</dt><br />

									<dd>- <g:message code="version.093.0" /></dd>
									<br /><dt>0.92</dt><br />

									<dd>- <g:message code="version.092.0" /></dd>
									<br /><dt>0.91</dt><br />

									<dd>- <g:message code="version.091.0" /></dd>
									<br /><dt>0.90</dt><br />

									<dd>- <g:message code="version.090.0" /></dd>
									<br /><dt>0.89</dt><br />

									<dd>- <g:message code="version.089.0" /></dd>
									<br /><dt>0.88</dt><br />

									<dd>- <g:message code="version.088.0" /></dd>
									<br /><dt>0.87</dt><br />

									<dd>- <g:message code="version.087.0" /></dd>
									<br /><dt>0.86</dt><br />

									<dd>- <g:message code="version.086.0" /></dd>
									<br /><dt>0.85</dt><br />

									<dd>- <g:message code="version.085.0" /></dd>
									<br /><dt>0.84</dt><br />

									<dd>- <g:message code="version.084.0" /></dd>
									<br /><dt>0.83</dt><br />

									<dd>- <g:message code="version.083.0" /></dd>
									<br /><dt>0.82</dt><br />

									<dd>- <g:message code="version.082.0" /></dd>
									<br /><dt>0.81 </dt><br />

									<dd>- <g:message code="version.081.0" /></dd>
									<br /><dt>0.80 </dt><br />

									<dd>- <g:message code="version.080.0" /></dd>
									<br /><dt>0.79 </dt><br />

									<dd>- <g:message code="version.079.0" /></dd>
									<br /><dt>0.78 </dt><br />

									<dd>- <g:message code="version.078.1" /></dd>
									<br /><dt>0.77 </dt><br />

									<dd>- <g:message code="version.077.1" /></dd>
									<br /><dt>0.76 </dt><br />

									<dd>- <g:message code="version.076.1" /></dd>
									<br /><dt>0.75 </dt><br />

									<dd>- <g:message code="version.075.1" /></dd>
									<br /><dt>0.74 </dt><br />

									<dd>- <g:message code="version.074.1" /></dd>
									<br /><dt>0.73 </dt><br />

									<dd>- <g:message code="version.073.1" /></dd>
									<br /><dt>0.72 </dt><br />

									<dd>- <g:message code="version.072.1" /></dd>
									<br /><dt>0.71 </dt><br />

									<dd>- <g:message code="version.071.1" /></dd>
									<br /><dt>0.70 </dt><br />

									<dd>- <g:message code="version.070.1" /></dd>
									<br /><dt>0.60 </dt><br />

									<dd>- <g:message code="version.060.1" /></dd>
									<br /><dt>0.59 </dt><br />

									<dd>- <g:message code="version.059.1" /></dd>
									<br /><dt>0.58 </dt><br />

									<dd>- <g:message code="version.058.1" /></dd>
									<br /><dt>0.57 </dt><br />

									<dd>- <g:message code="version.057.1" /></dd>
									<br /><dt>0.56 </dt><br />

									<dd>- <g:message code="version.056.1" /></dd>
									<br /><dt>0.55 </dt><br />

									<dd>- <g:message code="version.055.1" /></dd>
									<br /><dt>0.54 </dt><br />

									<dd>- <g:message code="version.054.1" /></dd>
									<br /><dt>0.53 </dt><br />

									<dd>- <g:message code="version.053.1" /></dd>
									<br /><dt>0.52 </dt><br />

									<dd>- Bugfix: Mapping Platform after the Selection of the Platform </dd>
									<br /><dt>0.51 </dt><br />

									<dd>- Bugfix: Json Export mit UTF-8 & Plattform URL Mapping & EBooks mit Plattfrom Mapping </dd>

									<br /><dt>0.50 </dt><br />

									<dd>- Bugfix: Tipp -> AccessEndDate </dd>
									<br /><dt>0.49 </dt><br />

									<dd>- Feature: Unterscheidung von Journal- und EBook-Paketen (Database -> disabled) und deren Informationsanreicherung</dd>

									<br /><dt>0.48 </dt><br />

									<dd>- Bugfix: Tipp -> AccessStartDate & AccessEndDate </dd>

									<br /><dt>0.47 (Release)</dt><br />

									<dd>- Füge Button "Eingabe korrigieren" hinzu und behalte für diesen Schritt die vorherigen Einstellungen</dd>

									<br /><dt>0.46</dt><br />

									<dd>- Erlaube verschiedene Schreibweisen in der KBART-Spalte 'zdb_id'</dd>
									<dd>- Erlaube 'coverage_notes' statt 'notes' im KBART-File (Rückwärts-Kompatibilität zu KBART Phase I)</dd>
									<dd>- Zeige Basisadresse der angeschlossenen GOKb-Instanz im Bereich 'Über'</dd>

									<br /><dt>0.45</dt><br />

									<dd>- Optimiere nominal platform Behandlung</dd>
									<dd>- Passe DNB-Request an Restrukturierung der DNB-API an</dd>

									<br /><dt>0.44</dt><br />

									<dd>- Ersetze Setzen eines exklusiven primären Identifiers durch fixe Priorisierung: 1. ZDB-ID, 2. eISSN, 3. pISSN</dd>

									<br /><dt>0.43</dt><br />

									<dd>- Korrektur: Entferne "@" aus Titelfeldern bei allen verwendeten Konnektoren</dd>

									<br /><dt>0.42</dt><br />

									<dd>- Korrektur: Verarbeitung von Coverage num_-Feldern</dd>

									<br /><dt>0.41</dt><br />

									<dd>- Hole ZDB-Daten von services.dnb.de</dd>

									<br /><dt>0.40</dt><br />

									<dd>- Hole Plattform und Provider aus Elasticsearch Index</dd>

									<br /><dt>0.39</dt><br />

									<dd>- Korrektur: Verarbeitung von access_date-Feldern</dd>

									<br /><dt>0.38</dt><br />

									<dd>- Entferne "@" aus Pica-Titelfeldern</dd>
									<dd>- Korrigiere History Events mit leerem Titel</dd>

									<br /><dt>0.37</dt><br />

									<dd>- Erhöhe Session-Dauer (auf 16 Stunden)</dd>

									<br /><dt>0.36</dt><br />

									<dd>- Korrektur: formatiere Coverage-Felder als String</dd>

									<br /><dt>0.35</dt><br />

									<dd>- Mache access_start_date und access_end_date optional</dd>
									<dd>- Korrektur: verstecke GOKb-Passwort bei der Eingabe</dd>
									<dd>- Korrektur: repariere Senden von Paketen an die GOKb</dd>

									<br /><dt>0.34</dt><br />

									<dd>- Verarbeite access_start_date und access_end_date </dd>

									<br /><dt>0.33</dt><br />

									<dd>- Verwende GOKb-Elasticsearch-Index zur Befüllung </dd>

									<br /><dt>0.32</dt><br />

									<dd>- Korrektur: repariere Credentials "Senden" Button</dd>

									<br /><dt>0.31</dt><br />

									<dd>- Ignoriere BOM in Windows-generierten Eingabedateien</dd>

									<br /><dt>0.30</dt><br />

									<dd>- Eingabe von Credentials beim Senden von prozessierten Daten zur GOKb.</dd>
									<dd>- Zeige Plattform-URL in Plattform-Auswahlliste.</dd>
									<dd>- Setze Plattformnamen (statt -URL) im Exportfeld "nominalPlatform".</dd>

									<br /><dt>0.29</dt><br />

									<dd>- Nutzerfreundlichere Fehlermeldung bei irregulären CSV-Headern.</dd>

									<br /><dt>0.28</dt><br />

									<dd>- Korrektur: TIPP-Url muss nicht Plattform-URL matchen</dd>

									<br /><dt>0.27</dt><br />

									<dd>- Korrektur: Verarbeitung von SRU-Anfragen ohne konkreten Treffer</dd>
									<dd>- Korrektur: JSON-Struktur für History Events</dd>
									<dd>- Verbesserte Statistik</dd>

									<br /><dt>0.26</dt><br />

									<dd>- Auswahl für <em>nominalProvider</em> hinzugefügt</dd>

									<br /><dt>0.25</dt><br />

									<dd>- Korrektur: SRU-Anfragen liefern multiple Treffer</dd>
									<dd>- KBART-Mapping: Normierung der Platform-URL</dd>
									<dd>- KBART-Mapping: <em>@</em>-Zeichen in Titeln entfernen</dd>

									<br /><dt>0.24</dt><br />

														<dd>- KBART-Mapping: Fehlerbehebung</dd>
														<dd>- KBART-Mapping: Feld <em>cover_depth</em> hinzugefügt</dd>
														<dd>- KBART-Mapping: Feld <em>notes</em> umbenannt</dd>
									<dd>- Quellcodeüberarbeitung</dd>

									<br /><dt>0.23</dt><br />

									<dd>- Kleinere Fehlerkorrekturen und Verbesserungen</dd>

									<br /><dt>0.22</dt><br />

									<dd>- Stacktrace bei Fehlern anzeigen</dd>

									<br /><dt>0.21</dt><br />

									<dd>- KBART-Mapping: Feld <em>coverage_notes</em> umbenannt</dd>
									<dd>- TIPP-Plattform wird <em>immer</em> vom PackageHeader übernommen</dd>
									<dd>- Subdomains in TIPP-Url werden beim Matching berücksichtigt</dd>
									<dd>- Defekte Tests repariert</dd>
									<dd>- Versionshistorie angelegt</dd>

									<br /><dt>0.20</dt><br />

								</dl>
							</div>
						</div>
					</div>
				</div>

			</div>
		</div>
	</body>
</html>
