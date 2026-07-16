#!/usr/bin/env bash
#
# 블루그린 무중단 배포 전환 스크립트
#
# 흐름: 현재 활성 색의 반대 색으로 새 이미지를 띄우고 → healthy 되면 Caddy를 그 색으로
#       무중단 전환(caddy reload) → 이전 색 정지. healthy가 안 되면 전환하지 않고 중단(다운타임 0).
#
# 사용: IMAGE_TAG=<이미지태그> bash deploy.sh
# 상세: docs/plans/blue-green-implementation.md
set -euo pipefail
cd "$(dirname "$0")"                          # docker/prod 로 이동

: "${IMAGE_TAG:?IMAGE_TAG(새 이미지 태그)가 필요합니다}"
UPSTREAM=./caddy/upstream.conf
STATE=./active_color

# 1) 현재 활성 색 파악 (upstream.conf가 진실). 없으면 최초 기동으로 보고 green부터 띄운다.
CUR=$(grep -oE 'app-(blue|green)' "$UPSTREAM" 2>/dev/null | head -1 | sed 's/app-//' || true)
if [ "$CUR" = "green" ]; then NEW=blue; else NEW=green; fi
echo "▶ 활성:${CUR:-none} → 배포대상:$NEW (tag=$IMAGE_TAG)"

# 2) 새 이미지 pull + 새 색만 기동 (헌 색/redis는 건드리지 않음)
IMAGE_TAG="$IMAGE_TAG" docker compose pull "app-$NEW"
IMAGE_TAG="$IMAGE_TAG" docker compose up -d --no-deps "app-$NEW"

# 3) 새 색이 healthy 될 때까지 대기 (start_period 40s + Flyway 여유, 최대 ~150s)
echo "▶ app-$NEW 헬스체크 대기..."
status=starting
for i in $(seq 1 30); do
  status=$(docker inspect -f '{{.State.Health.Status}}' "app-$NEW" 2>/dev/null || echo starting)
  echo "   [$i] $status"
  [ "$status" = healthy ] && break
  sleep 5
done
if [ "$status" != healthy ]; then
  echo "✗ app-$NEW 비정상($status) → 전환 중단(기존 색 유지, 다운타임 없음)"
  docker compose stop "app-$NEW" || true
  exit 1
fi

# 4) Caddy 업스트림 전환 + 무중단 reload
echo "reverse_proxy app-$NEW:8080" > "$UPSTREAM"
docker compose exec -T caddy caddy reload --config /etc/caddy/Caddyfile --adapter caddyfile
echo "$NEW" > "$STATE"
echo "✔ 트래픽 전환 완료 → app-$NEW"

# 5) 이전 색 정지 (메모리 회수). 롤백은 blue-green-implementation.md §9 (docker compose start)
if [ -n "$CUR" ] && [ "$CUR" != "$NEW" ]; then
  docker compose stop "app-$CUR"
  echo "▶ 이전 색 app-$CUR 정지 (롤백 필요 시 docker compose start app-$CUR 로 즉시 복구)"
fi

# 6) 활성 이미지 태그를 .env에 기록 (다음 compose 작업의 일관성용)
if grep -q '^IMAGE_TAG=' .env 2>/dev/null; then
  sed -i "s/^IMAGE_TAG=.*/IMAGE_TAG=$IMAGE_TAG/" .env
else
  echo "IMAGE_TAG=$IMAGE_TAG" >> .env
fi
echo "완료: 활성=$NEW (tag=$IMAGE_TAG)"
