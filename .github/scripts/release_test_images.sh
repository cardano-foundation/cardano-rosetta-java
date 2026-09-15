#!/usr/bin/env bash
# Resolve published Linux/AMD64 Docker artifacts to immutable tag@digest references.

get_release_env_value() {
  local key=$1 file=${2:-.env.docker-compose} value
  value=$(awk -F= -v key="$key" '$1 == key {print substr($0, index($0, "=") + 1)}' "$file")
  [[ -n "$value" ]] || {
    echo "Missing $key in $file." >&2
    return 1
  }
  printf '%s' "$value"
}

resolve_linux_amd64_digest() {
  local image=$1 inspection media_type digest platform
  inspection=$(docker buildx imagetools inspect "$image" --format '{{json .}}')
  media_type=$(jq -r '.manifest.mediaType // ""' <<< "$inspection")

  case "$media_type" in
    application/vnd.oci.image.index.v1+json|application/vnd.docker.distribution.manifest.list.v2+json)
      digest=$(jq -er '
        [.manifest.manifests[] |
          select(.platform.os == "linux" and .platform.architecture == "amd64") |
          select(.mediaType == "application/vnd.oci.image.manifest.v1+json" or
                 .mediaType == "application/vnd.docker.distribution.manifest.v2+json") |
          select(has("artifactType") | not) |
          select(.annotations["vnd.docker.reference.type"] != "attestation-manifest")]
        | if length == 1 then .[0].digest
          else error("Expected one runnable linux/amd64 manifest, found \(length)") end
      ' <<< "$inspection")
      ;;
    application/vnd.oci.image.manifest.v1+json|application/vnd.docker.distribution.manifest.v2+json)
      digest=$(jq -er '.manifest.digest' <<< "$inspection")
      platform=$(jq -er '.image | "\(.os // "")/\(.architecture // "")"' <<< "$inspection")
      if [[ "$platform" != "linux/amd64" ]]; then
        echo "$image is a single-platform $platform image, expected linux/amd64." >&2
        return 1
      fi
      ;;
    *)
      echo "$image does not resolve to an OCI/Docker image manifest." >&2
      return 1
      ;;
  esac

  if [[ ! "$digest" =~ ^sha256:[0-9a-f]{64}$ ]]; then
    echo "Could not resolve an immutable manifest digest for $image." >&2
    return 1
  fi
  printf '%s' "$digest"
}

resolve_published_image() {
  local repository=$1 tag=$2 output_name=$3 tagged_image digest
  if [[ ! "$tag" =~ ^[A-Za-z0-9_][A-Za-z0-9_.-]{0,127}$ ]]; then
    echo "Invalid Docker tag for $repository: $tag" >&2
    return 1
  fi
  tagged_image="${repository}:${tag}"
  digest=$(resolve_linux_amd64_digest "$tagged_image")
  echo "${output_name}=${tagged_image}@${digest}" >> "$GITHUB_OUTPUT"
  echo "Resolved published image: ${tagged_image}@${digest}"
}

resolve_release_images() {
  local image_tag=$1
  resolve_published_image \
    cardanofoundation/cardano-rosetta-java-api "$image_tag" api_image
  resolve_published_image \
    cardanofoundation/cardano-rosetta-java-indexer "$image_tag" indexer_image
  resolve_published_image \
    cardanofoundation/cardano-rosetta-java-cardano-node \
    "$(get_release_env_value CARDANO_NODE_VERSION)" cardano_node_image
  resolve_published_image \
    cardanofoundation/cardano-rosetta-java-postgres \
    "$(get_release_env_value PG_VERSION_TAG)" postgres_image
  resolve_published_image \
    cardanofoundation/cardano-rosetta-java-mithril \
    "$(get_release_env_value MITHRIL_VERSION)" mithril_image
}
