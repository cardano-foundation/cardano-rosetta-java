{{- define "postgresql.fullname" -}}
{{- printf "%s-postgresql" .Release.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "postgresql.headlessName" -}}
{{- printf "%s-postgresql-headless" .Release.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "postgresql.commonLabels" -}}
app.kubernetes.io/name: postgresql
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "postgresql.selectorLabels" -}}
app: {{ include "postgresql.fullname" . }}
component: postgresql
{{- end }}

{{- define "postgresql.profile" -}}
{{- .Values.global.profile | default "mid" }}
{{- end }}
