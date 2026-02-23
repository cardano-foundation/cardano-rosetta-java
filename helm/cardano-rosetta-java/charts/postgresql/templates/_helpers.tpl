{{- define "postgresql.fullname" -}}
{{- printf "%s-postgresql" .Release.Name }}
{{- end }}

{{- define "postgresql.commonLabels" -}}
app.kubernetes.io/name: postgresql
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "postgresql.selectorLabels" -}}
app: {{ printf "%s-postgresql" .Release.Name }}
component: postgresql
{{- end }}

{{- define "postgresql.profile" -}}
{{- .Values.global.profile | default "mid" }}
{{- end }}
