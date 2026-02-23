{{- define "rosetta-api.fullname" -}}
{{- printf "%s-rosetta-api" .Release.Name }}
{{- end }}

{{- define "rosetta-api.commonLabels" -}}
app.kubernetes.io/name: rosetta-api
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Values.global.releaseVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "rosetta-api.selectorLabels" -}}
app: {{ printf "%s-rosetta-api" .Release.Name }}
component: rosetta-api
{{- end }}

{{- define "rosetta-api.profile" -}}
{{- .Values.global.profile | default "mid" }}
{{- end }}
