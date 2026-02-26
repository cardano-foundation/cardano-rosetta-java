{{/*
Expand the full name of the chart using the release name.
*/}}
{{- define "cardano-rosetta-java.fullname" -}}
{{- printf "%s" .Release.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Determine the target namespace.
*/}}
{{- define "cardano-rosetta-java.namespace" -}}
{{- .Values.global.namespace | default "cardano" }}
{{- end }}

{{/*
Determine the active hardware profile.
*/}}
{{- define "cardano-rosetta-java.profile" -}}
{{- .Values.global.profile | default "mid" }}
{{- end }}

{{/*
Common labels applied to every resource.
*/}}
{{- define "cardano-rosetta-java.commonLabels" -}}
app.kubernetes.io/name: cardano-rosetta-java
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Values.global.releaseVersion | default .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels for pod matching.
*/}}
{{- define "cardano-rosetta-java.selectorLabels" -}}
app.kubernetes.io/name: cardano-rosetta-java
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Name of the Kubernetes Secret that holds the database password.
*/}}
{{- define "cardano-rosetta-java.dbSecretName" -}}
{{- printf "%s-db-secret" .Release.Name }}
{{- end }}

{{/*
Resolve the database host. If an explicit host is provided in global.db.host
use it; otherwise fall back to the in-cluster PostgreSQL service name.
*/}}
{{- define "cardano-rosetta-java.dbHost" -}}
{{- if .Values.global.db.host -}}
{{- .Values.global.db.host -}}
{{- else -}}
{{- printf "%s-postgresql" .Release.Name -}}
{{- end }}
{{- end }}

{{/*
Service name for the cardano-node subchart.
*/}}
{{- define "cardano-rosetta-java.nodeServiceName" -}}
{{- printf "%s-cardano-node" .Release.Name }}
{{- end }}

{{/*
Service name for the yaci-indexer subchart.
*/}}
{{- define "cardano-rosetta-java.yaciServiceName" -}}
{{- printf "%s-yaci-indexer" .Release.Name }}
{{- end }}

{{/*
ConfigMap name for node configuration files.
*/}}
{{- define "cardano-rosetta-java.nodeConfigMapName" -}}
{{- printf "%s-node-config" .Release.Name }}
{{- end }}

{{/*
Look up resource requests/limits for a given component under the active profile.
Usage:
  {{- include "cardano-rosetta-java.profileResources" (dict "root" . "component" "api") | nindent 10 }}
*/}}
{{- define "cardano-rosetta-java.profileResources" -}}
{{- $profile := (include "cardano-rosetta-java.profile" .root) -}}
{{- $resources := index .root.Values.global.profiles $profile "resources" .component -}}
{{- toYaml $resources -}}
{{- end }}

{{/*
Look up database tuning parameters for the active profile.
Usage:
  {{- $dbSettings := include "cardano-rosetta-java.dbProfileSettings" . | fromYaml }}
*/}}
{{- define "cardano-rosetta-java.dbProfileSettings" -}}
{{- $profile := (include "cardano-rosetta-java.profile" .) -}}
{{- $db := index .Values.global.profiles $profile "db" -}}
{{- toYaml $db -}}
{{- end }}
