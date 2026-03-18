{{- define "cardano-node.fullname" -}}
{{- printf "%s-cardano-node" .Release.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "cardano-node.headlessName" -}}
{{- printf "%s-cardano-node-headless" .Release.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "cardano-node.pvcName" -}}
{{- printf "%s-cardano-node-data" .Release.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "cardano-node.commonLabels" -}}
app.kubernetes.io/name: cardano-node
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Values.global.cardanoNodeVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "cardano-node.selectorLabels" -}}
app: {{ include "cardano-node.fullname" . }}
component: cardano-node
{{- end }}

{{- define "cardano-node.profile" -}}
{{- .Values.global.profile | default "mid" }}
{{- end }}
