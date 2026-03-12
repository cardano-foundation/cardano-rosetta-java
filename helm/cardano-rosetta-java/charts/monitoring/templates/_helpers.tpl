{{- define "monitoring.grafanaName" -}}
{{- printf "%s-grafana" .Release.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "monitoring.pgExporterName" -}}
{{- printf "%s-pg-exporter" .Release.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "monitoring.prometheusName" -}}
{{- printf "%s-prometheus" .Release.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "monitoring.nodeExporterName" -}}
{{- printf "%s-node-exporter" .Release.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "monitoring.commonLabels" -}}
app.kubernetes.io/name: monitoring
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}
