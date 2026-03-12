{{- define "yaci-indexer.fullname" -}}
{{- printf "%s-yaci-indexer" .Release.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "yaci-indexer.saName" -}}
{{- printf "%s-sa" .Release.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "yaci-indexer.commonLabels" -}}
app.kubernetes.io/name: yaci-indexer
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Values.global.releaseVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "yaci-indexer.selectorLabels" -}}
app: {{ include "yaci-indexer.fullname" . }}
component: yaci-indexer
{{- end }}

{{- define "yaci-indexer.profile" -}}
{{- .Values.global.profile | default "mid" }}
{{- end }}
