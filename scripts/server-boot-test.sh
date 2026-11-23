#!/usr/bin/env bash
# Boots a real Forge 1.20.1 dedicated server with one built Cottagecore Living jar in its mods folder, places every
# decoration with /setblock from a datapack, lets the world run for 20 seconds and stops. Fails if the server crashes,
# the mod does not load, the datapack function does not load (an unknown block id makes it fail to parse), or the log
# has an error that names the mod.
#   usage: scripts/server-boot-test.sh <path to the mod jar>
set -u
JAR="$(cd "$(dirname "$1")" && pwd)/$(basename "$1")"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MC=$(grep '^minecraft_version=' "$ROOT/1.20.1/gradle.properties" | cut -d= -f2)
FORGE=$(grep '^forge_version=' "$ROOT/1.20.1/gradle.properties" | cut -d= -f2)
SERVER="$ROOT/build/boot-test-server"
rm -rf "$SERVER"; mkdir -p "$SERVER/mods"
cd "$SERVER"

curl -fsSL -o installer.jar "https://maven.minecraftforge.net/net/minecraftforge/forge/$MC-$FORGE/forge-$MC-$FORGE-installer.jar"
java -jar installer.jar --installServer > install.log 2>&1 || { tail -40 install.log; exit 1; }
cp "$JAR" mods/

echo "eula=true" > eula.txt
cat > server.properties <<PROPS
online-mode=false
function-permission-level=4
spawn-protection=0
view-distance=4
simulation-distance=4
level-type=minecraft\:flat
level-seed=20261123
initial-enabled-packs=vanilla,file/cottage_boot
PROPS
PACK="world/datapacks/cottage_boot"
mkdir -p "$PACK/data/cottage_boot/functions" "$PACK/data/minecraft/tags/functions"
echo '{"pack":{"pack_format":15,"description":"Cottagecore Living boot test"}}' > "$PACK/pack.mcmeta"
echo '{"values":["cottage_boot:start"]}' > "$PACK/data/minecraft/tags/functions/load.json"
printf 'forceload add 0 0 63 63\nschedule function cottage_boot:place 100t\n' > "$PACK/data/cottage_boot/functions/start.mcfunction"
{
  i=0
  while read -r id; do
    [ -n "$id" ] || continue
    echo "setblock $(( (i % 20) * 3 )) 10 $(( (i / 20) * 3 )) cottagecoreliving:$id"
    i=$((i + 1))
  done < "$ROOT/tools/generated_ids.txt"
  echo "say COTTAGE_BOOT_PLACED $i"
  echo 'schedule function cottage_boot:finish 400t'
} > "$PACK/data/cottage_boot/functions/place.mcfunction"
printf 'say COTTAGE_BOOT_OK\nstop\n' > "$PACK/data/cottage_boot/functions/finish.mcfunction"

LOG="$SERVER/boot.log"
bash run.sh nogui < /dev/null > "$LOG" 2>&1 &
PID=$!
result=timeout
for _ in $(seq 1 120); do
  sleep 5
  if grep -q "COTTAGE_BOOT_OK" "$LOG"; then result=ok; break; fi
  if grep -q -E "This crash report has been saved|Encountered an unexpected exception|Failed to start the minecraft server|Failed to load function cottage_boot" "$LOG"; then result=crash; break; fi
  if ! kill -0 $PID 2>/dev/null; then result=exited; break; fi
done
sleep 10
pkill -P $PID 2>/dev/null; kill $PID 2>/dev/null; wait $PID 2>/dev/null

grep -n -i -E "COTTAGE_BOOT|cottagecoreliving|Cottagecore Living|Done \(" "$LOG" | head -40
for f in crash-reports/*.txt; do [ -f "$f" ] && { echo "=== $f"; head -80 "$f"; }; done
grep -n -E "ERROR|Exception|Caused by" "$LOG" | head -60
echo "Result: $result"
[ "$result" = ok ] || exit 1
grep -q "This crash report has been saved" "$LOG" && exit 1
grep -q -E "Done \([0-9.]+s\)!" "$LOG" || { echo "The server never finished starting"; exit 1; }
grep -q "COTTAGE_BOOT_PLACED $(grep -c . "$ROOT/tools/generated_ids.txt")" "$LOG" || { echo "Not every decoration was placed"; exit 1; }
if grep -E "ERROR|Exception|Caused by" "$LOG" | grep -q -E 'com.sugarplumstudios|cottagecoreliving([^-.a-z]|$)'; then echo "The log has errors naming Cottagecore Living"; exit 1; fi
echo "Server boot test passed"
