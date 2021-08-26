#!/usr/bin/env bash

# shellcheck disable=SC2223
: ${HOST=localhost}
# shellcheck disable=SC2223
: ${PORT=8443}
# shellcheck disable=SC2223
: ${PLAYER_ID_REVS_RECS=2}
# shellcheck disable=SC2223
: ${PLAYER_ID_NOT_FOUND=13}

function assertCurl() {
  local expectedHttpCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  # shellcheck disable=SC2155
  local result=$(eval "$curlCmd")
  local httpCode="${result:(-3)}"
  RESPONSE='' && ((${#result} > 3)) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedHttpCode" ]; then
    if [ "$httpCode" = "200" ]; then
      echo "Test OK (HTTP Code: $httpCode)"
    else
      echo "Test OK (HTTP Code: $httpCode, $RESPONSE)"
    fi
    return 0
  else
    echo "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
    echo "- Failing command: $curlCmd"
    echo "- Response Body: $RESPONSE"
    return 1
  fi
}

function assertEqual() {
  local expected=$1
  local actual=$2

  if [ "$actual" = "$expected" ]; then
    echo "Test OK (actual value: $actual)"
    return 0
  else
    echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
    return 1
  fi
}

function testUrl() {
  # shellcheck disable=SC2124
  url=$@
  if $url -ks -f -o /dev/null; then
    return 0
  else
    return 1
  fi
}

function waitForService() {
  # shellcheck disable=SC2124
  url=$@
  echo -n "Wait for: $url... "
  n=0
  until testUrl "$url"; do
    n=$((n + 1))
    if [[ $n == 100 ]]; then
      echo " Give up"
      exit 1
    else
      sleep 3
      echo -n ", retry #$n "
    fi
  done
  echo "DONE, continues..."
}

set -e

# shellcheck disable=SC2046
# shellcheck disable=SC2006
echo "Start Tests:" $(date)

echo "HOST=${HOST}"
echo "PORT=${PORT}"

# shellcheck disable=SC2199
if [[ $@ == *"start"* ]]; then
  echo "Restarting the test environment..."
  echo "$ docker-compose down --remove-orphans"
  docker-compose down --remove-orphans
  echo "$ docker-compose up -d"
  docker-compose up -d
fi

waitForService curl -k https://$HOST:$PORT/actuator/health

ACCESS_TOKEN=$(curl -k https://writer:secret@$HOST:$PORT/oauth/token -d grant_type=password -d username=magnus -d password=password -s | jq .access_token -r)
AUTH="-H \"Authorization: Bearer $ACCESS_TOKEN\""

assertCurl 404 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_NOT_FOUND $AUTH -s"

assertCurl 422 "curl -k https://$HOST:$PORT/player-composite/-1 $AUTH -s"
assertEqual "\"Invalid playerId: -1\"" "$(echo "$RESPONSE" | jq .message)"

assertCurl 400 "curl -k https://$HOST:$PORT/player-composite/invalidPlayerId $AUTH -s"
assertEqual "\"Type mismatch.\"" "$(echo "$RESPONSE" | jq .message)"

assertCurl 401 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_REVS_RECS -s"

READER_ACCESS_TOKEN=$(curl -k https://reader:secret@$HOST:$PORT/oauth/token -d grant_type=password -d username=magnus -d password=password -s | jq .access_token -r)
READER_AUTH="-H \"Authorization: Bearer $READER_ACCESS_TOKEN\""

assertCurl 200 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_REVS_RECS $READER_AUTH -s"
assertCurl 403 "curl -k https://$HOST:$PORT/player-composite/$PLAYER_ID_REVS_RECS $READER_AUTH -X DELETE -s"

# shellcheck disable=SC2046
# shellcheck disable=SC2006
echo "End, all tests OK:" $(date)

# shellcheck disable=SC2199
if [[ $@ == *"stop"* ]]; then
  echo "Stopping the test environment..."
  echo "$ docker-compose down --remove-orphans"
  docker-compose down --remove-orphans
fi
