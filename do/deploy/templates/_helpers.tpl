{{- define "spexity.fullname" -}}
{{- if .Chart.Name -}}
{{- .Chart.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
spexity
{{- end -}}
{{- end -}}
