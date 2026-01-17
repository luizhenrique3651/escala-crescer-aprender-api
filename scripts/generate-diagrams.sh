#!/usr/bin/env bash
# Gera PNG a partir dos SVGs presentes em docs/
# Tenta rsvg-convert, inkscape e convert (ImageMagick) nesta ordem.
set -euo pipefail
SVG_DIR="docs"
OUT_DIR="$SVG_DIR/out"
mkdir -p "$OUT_DIR"

convert_svg() {
  local svg="$1"
  local out="$2"
  if command -v rsvg-convert >/dev/null 2>&1; then
    rsvg-convert -o "$out" "$svg" || return 1
  elif command -v inkscape >/dev/null 2>&1; then
    inkscape "$svg" --export-type=png --export-filename="$out" || return 1
  elif command -v convert >/dev/null 2>&1; then
    convert "$svg" "$out" || return 1
  else
    echo "Nenhuma ferramenta de conversão encontrada. Instale librsvg (rsvg-convert), inkscape ou ImageMagick." >&2
    return 2
  fi
}

for svg in "$SVG_DIR"/*.svg; do
  [ -e "$svg" ] || continue
  base=$(basename "$svg" .svg)
  out="$OUT_DIR/${base}.png"
  echo "Convertendo $svg -> $out"
  convert_svg "$svg" "$out"
done

echo "Conversão concluída. Arquivos gerados em $OUT_DIR"

