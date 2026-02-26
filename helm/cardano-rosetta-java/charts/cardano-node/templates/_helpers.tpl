{{- define "cardano-node.fullname" -}}
{{- printf "%s-cardano-node" .Release.Name }}
{{- end }}

{{- define "cardano-node.commonLabels" -}}
app.kubernetes.io/name: cardano-node
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Values.global.cardanoNodeVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "cardano-node.selectorLabels" -}}
app: {{ printf "%s-cardano-node" .Release.Name }}
component: cardano-node
{{- end }}

{{- define "cardano-node.dbSecretName" -}}
{{- printf "%s-db-secret" .Release.Name }}
{{- end }}

{{- define "cardano-node.profile" -}}
{{- .Values.global.profile | default "mid" }}
{{- end }}
